package com.example.finalprojectweatherapp.ui.favorites

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.SearchView
import com.example.finalprojectweatherapp.R
import com.example.finalprojectweatherapp.databinding.FragmentFavoritesBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavoritesFragment : Fragment(R.layout.fragment_favorites) {

    private val viewModel: FavoritesViewModel by viewModels()
    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private lateinit var favoritesAdapter: FavoritesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFavoritesBinding.bind(view)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnAddFavorite.setOnClickListener {
            findNavController().navigate(R.id.addFavoriteFragment)
        }

        binding.btnSortAlpha.setOnClickListener {
            viewModel.setSortType(FavoritesViewModel.SortType.ALPHABETICAL)
        }
        binding.btnSortTemp.setOnClickListener {
            viewModel.setSortType(FavoritesViewModel.SortType.TEMPERATURE)
        }

        binding.searchFavorites.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filter(newText ?: "")
                return true
            }
        })
    }

    private fun setupRecyclerView() {
        favoritesAdapter = FavoritesAdapter { entity ->
            // Handle click - maybe go to details
        }
        binding.rvFavorites.apply {
            adapter = favoritesAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // Swipe to delete
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = true

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val entity = favoritesAdapter.currentList[position]
                viewModel.deleteFavorite(entity)
                Snackbar.make(requireView(), "${entity.cityName} deleted", Snackbar.LENGTH_LONG).apply {
                    setAction("Undo") {
                        viewModel.saveFavorite(entity)
                    }
                    show()
                }
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.rvFavorites)
    }

    private fun observeViewModel() {
        viewModel.favorites.observe(viewLifecycleOwner) { list ->
            android.util.Log.d("FavoritesFragment", "Favorites updated: ${list.size} items")
            favoritesAdapter.submitList(list)
            binding.tvEmptyFavorites.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}