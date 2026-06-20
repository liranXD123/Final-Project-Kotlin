// package declaration for the pollution ui feature
package com.example.finalprojectweatherapp.ui.pollution

// import statements for required android and project dependencies
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.finalprojectweatherapp.R
import com.example.finalprojectweatherapp.data.remote.models.PollutionItem
import com.example.finalprojectweatherapp.databinding.FragmentPollutionBinding
import com.example.finalprojectweatherapp.databinding.ItemPollutantRowBinding
import com.example.finalprojectweatherapp.utils.AqiLevel
import com.example.finalprojectweatherapp.utils.PollutantDisplay
import com.example.finalprojectweatherapp.utils.PollutantSeverity
import com.example.finalprojectweatherapp.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

// annotate with @androidentrypoint to enable field injection with hilt
@AndroidEntryPoint
// define pollutionfragment class extending fragment with the specified layout resource
class PollutionFragment : Fragment(R.layout.fragment_pollution) {

    // inject pollutionviewmodel using the viewmodels delegate
    private val viewModel: PollutionViewModel by viewModels()
    // declare a nullable backing property for view binding
    private var _binding: FragmentPollutionBinding? = null
    // declare a non-nullable property to access binding, throwing an exception if accessed when null
    private val binding get() = _binding!!

    // initialize scale segments list lazily to hold the view references
    private val scaleSegments by lazy {
        // create a list containing the five scale segment views
        listOf(
            binding.scaleSegment1,
            binding.scaleSegment2,
            binding.scaleSegment3,
            binding.scaleSegment4,
            binding.scaleSegment5
        )
    }

    /*
     * called immediately after oncreateview has returned.
     * initializes the view binding and fetches pollution data.
     * * parameters:
     * view - the view returned by oncreateview
     * savedinstancestate - if non-null, this fragment is being re-constructed from a previous saved state
     * * returns: none
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // call the superclass implementation of onviewcreated
        super.onViewCreated(view, savedInstanceState)
        // bind the layout view to the fragment binding class
        _binding = FragmentPollutionBinding.bind(view)

        // set up observers for the viewmodel's live data
        observeViewModel()

        // retrieve latitude from arguments, defaulting to 34.05 if not found
        val lat = arguments?.getFloat("lat", 34.05f)?.toDouble() ?: 34.05
        // retrieve longitude from arguments, defaulting to -118.24 if not found
        val lon = arguments?.getFloat("lon", -118.24f)?.toDouble() ?: -118.24
        // request pollution data from the viewmodel using the coordinates
        viewModel.getPollutionData(lat, lon)
    }

    /*
     * observes the pollution state from the viewmodel and updates the ui accordingly.
     * * parameters: none
     * returns: none
     */
    private fun observeViewModel() {
        // observe the pollutionstate livedata with the view lifecycle owner
        viewModel.pollutionState.observe(viewLifecycleOwner) { state ->
            // handle different states of the resource
            when (state) {
                // show loading ui when state is loading
                is Resource.Loading -> showLoading()
                // handle the success state
                is Resource.Success -> {
                    // safely get the first item from the list and show content
                    state.data?.list?.firstOrNull()?.let { item ->
                        // display the retrieved pollution item
                        showContent(item)
                        // show error if the list is empty or null
                    } ?: showError(getString(R.string.error_unknown))
                }
                // show error ui with the provided message or a default network error
                is Resource.Error -> showError(state.message ?: getString(R.string.error_network))
            }
        }
    }

    /*
     * updates the ui to display the loading state.
     * * parameters: none
     * returns: none
     */
    private fun showLoading() {
        // make the progress bar visible
        binding.pbPollution.isVisible = true
        // hide the main content view
        binding.contentPollution.isVisible = false
        // hide the error card view
        binding.cardPollutionError.isVisible = false
    }

    /*
     * updates the ui to display an error state with a specific message.
     * * parameters:
     * message - the error string to display to the user
     * * returns: none
     */
    private fun showError(message: String) {
        // hide the progress bar
        binding.pbPollution.isVisible = false
        // hide the main content view
        binding.contentPollution.isVisible = false
        // make the error card view visible
        binding.cardPollutionError.isVisible = true
        // set the error message text in the textview
        binding.tvPollutionError.text = message
    }

    /*
     * updates the ui to display the successfully retrieved pollution content.
     * * parameters:
     * item - the pollutionitem data object containing air quality info
     * * returns: none
     */
    private fun showContent(item: PollutionItem) {
        // hide the progress bar
        binding.pbPollution.isVisible = false
        // hide the error card view
        binding.cardPollutionError.isVisible = false
        // make the main content view visible
        binding.contentPollution.isVisible = true

        // convert the aqi index into an aqilevel enum
        val level = AqiLevel.fromIndex(item.main.aqi)
        // populate the main aqi hero section with the level
        bindAqiHero(level)
        // populate the list of individual pollutants
        bindPollutantRows(item.components)
    }

    /*
     * populates the hero section of the ui with overarching air quality index data.
     * * parameters:
     * level - the aqilevel object representing the current air quality tier
     * * returns: none
     */
    private fun bindAqiHero(level: AqiLevel) {
        // get the context associated with the fragment
        val context = requireContext()
        // resolve the foreground color for the current aqi level
        val color = ContextCompat.getColor(context, level.colorRes)
        // resolve the background color for the current aqi level
        val backgroundColor = ContextCompat.getColor(context, level.backgroundRes)

        // set the background color of the hero card
        binding.cardAqiHero.setCardBackgroundColor(backgroundColor)
        // set the appropriate icon image for the aqi level
        binding.ivAqiIcon.setImageResource(level.iconRes)
        // set the aqi label text
        binding.tvAqiLevelLabel.text = getString(level.labelRes)
        // apply the foreground color to the label text
        binding.tvAqiLevelLabel.setTextColor(color)
        // format and display the numeric aqi index
        binding.tvAqiIndex.text = getString(R.string.aqi_index_format, level.index)
        // display a descriptive explanation of the aqi level
        binding.tvAqiDescription.text = getString(level.descriptionRes)
        // display health advice based on the current aqi
        binding.tvHealthAdvice.text = getString(level.adviceRes)

        // update the visual scale indicator to match the active level
        updateAqiScale(level)
    }

    /*
     * updates the segmented aqi scale bar to highlight the current level.
     * * parameters:
     * activelevel - the currently active aqilevel to highlight on the scale
     * * returns: none
     */
    private fun updateAqiScale(activeLevel: AqiLevel) {
        // retrieve all possible aqi levels
        val allLevels = AqiLevel.entries
        // iterate through each segment view in the scale
        scaleSegments.forEachIndexed { index, segment ->
            // get the corresponding level for this segment index
            val level = allLevels[index]
            // determine if this segment represents a level up to the active one
            val isActive = level.index <= activeLevel.index
            // select the appropriate color resource based on active state
            val colorRes = if (isActive) level.colorRes else R.color.aqi_scale_inactive
            // create a gradient drawable for the segment background
            val drawable = GradientDrawable().apply {
                // set rounded corners relative to screen density
                cornerRadius = 12f * resources.displayMetrics.density
                // set the solid fill color of the drawable
                setColor(ContextCompat.getColor(requireContext(), colorRes))
            }
            // apply the generated drawable as the segment's background
            segment.background = drawable
            // set the segment opacity based on whether it is the active level, a previous level, or inactive
            segment.alpha = if (level == activeLevel) 1f else if (isActive) 0.85f else 0.4f
        }
    }

    /*
     * dynamically generates and adds views for each tracked pollutant component.
     * * parameters:
     * components - a map of pollutant chemical symbols to their concentration values
     * * returns: none
     */
    private fun bindPollutantRows(components: Map<String, Double>) {
        // clear any existing pollutant row views from the container
        binding.containerPollutants.removeAllViews()
        // obtain a layout inflater instance
        val inflater = LayoutInflater.from(requireContext())

        // iterate through the list of predefined pollutants we want to display
        PollutantDisplay.trackedPollutants.forEach { pollutant ->
            // attempt to get the value for the pollutant, skipping if not present
            val value = components[pollutant.key] ?: return@forEach
            // inflate a new row view binding for the pollutant
            val rowBinding = ItemPollutantRowBinding.inflate(inflater, binding.containerPollutants, false)
            // bind the specific pollutant data to the row view
            bindPollutantRow(rowBinding, pollutant.nameRes, value, pollutant.displayMax)
            // add the configured row view to the parent container
            binding.containerPollutants.addView(rowBinding.root)
        }

        // check if no pollutants were added to the container
        if (binding.containerPollutants.childCount == 0) {
            // create a generic textview to show an empty state message
            val emptyView = TextView(requireContext()).apply {
                // set the empty state text
                text = getString(R.string.pollution_no_components)
                // add vertical padding to the empty view
                setPadding(0, 16, 0, 16)
            }
            // add the empty state view to the container
            binding.containerPollutants.addView(emptyView)
        }
    }

    /*
     * populates an individual pollutant row view with its specific data and styling.
     * * parameters:
     * rowbinding - the view binding object for the specific row being populated
     * nameres - the string resource id for the pollutant's name
     * value - the concentration value of the pollutant
     * displaymax - the maximum expected value for this pollutant to calculate progress
     * * returns: none
     */
    private fun bindPollutantRow(
        rowBinding: ItemPollutantRowBinding,
        nameRes: Int,
        value: Double,
        displayMax: Double
    ) {
        // calculate the severity level of the pollutant value
        val severity = PollutantDisplay.severity(value, displayMax)
        // calculate the percentage progress for the progress bar
        val progress = PollutantDisplay.progressPercent(value, displayMax)
        // determine the color resource based on the computed severity
        val colorRes = when (severity) {
            // use low severity color
            PollutantSeverity.LOW -> R.color.pollutant_low
            // use medium severity color
            PollutantSeverity.MEDIUM -> R.color.pollutant_medium
            // use high severity color
            PollutantSeverity.HIGH -> R.color.pollutant_high
        }
        // resolve the actual color value from the resource id
        val color = ContextCompat.getColor(requireContext(), colorRes)

        // set the pollutant name in the textview
        rowBinding.tvPollutantName.setText(nameRes)
        // format and set the actual pollutant value text
        rowBinding.tvPollutantValue.text = getString(R.string.pollutant_value_format, value)
        // set the text label indicating severity level
        rowBinding.tvPollutantLevel.text = getString(severity.labelRes)
        // apply the severity color to the level text
        rowBinding.tvPollutantLevel.setTextColor(color)
        // update the progress bar to reflect the concentration percentage
        rowBinding.progressPollutant.progress = progress
        // set the color of the progress bar indicator
        rowBinding.progressPollutant.setIndicatorColor(color)
    }

    /*
     * called when the view previously created by oncreateview has been detached.
     * cleans up the view binding to prevent memory leaks.
     * * parameters: none
     * returns: none
     */
    override fun onDestroyView() {
        // call the superclass implementation
        super.onDestroyView()
        // nullify the binding reference to avoid memory leaks
        _binding = null
    }
}