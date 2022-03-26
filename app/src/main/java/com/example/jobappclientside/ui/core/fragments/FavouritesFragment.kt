package com.example.jobappclientside.ui.core.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jobappclientside.R
import com.example.jobappclientside.databinding.FragmentFavouritesBinding
import com.example.jobappclientside.datamodels.regular.JobPost
import com.example.jobappclientside.other.DataStoreUtil
import com.example.jobappclientside.other.snackbar
import com.example.jobappclientside.ui.adapters.FavouritesAdapter
import com.example.jobappclientside.ui.core.viewmodels.FavouritesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FavouritesFragment: Fragment() {
    private var _binding: FragmentFavouritesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FavouritesViewModel by viewModels()

    private lateinit var favouritesAdapter: FavouritesAdapter
    private lateinit var curLoggedInUsername: String

    @Inject
    lateinit var dataStoreUtil: DataStoreUtil

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        getLoggedInUsername()
        _binding = FragmentFavouritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadSavedJobs()
        subscribeToObservers()
    }

    private fun getLoggedInUsername() {
        viewLifecycleOwner.lifecycleScope.launch(viewModel.dispatchersProvider.default) {
            curLoggedInUsername = dataStoreUtil.getUsername()
        }
    }

    private fun subscribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launch(viewModel.dispatchersProvider.main) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.jobRequest.collect { event ->
                    when(event) {
                        is FavouritesViewModel.FavouriteJobsEvent.FavouriteJobsLoading -> {
                            toggleProgressBar(true)
                        }
                        is FavouritesViewModel.FavouriteJobsEvent.FavouriteJobsSuccess -> {
                            toggleProgressBar(false)
                            submitJobs(event.data)
                        }
                        is FavouritesViewModel.FavouriteJobsEvent.FavouriteJobsError -> {
                            toggleProgressBar(false)
                            snackbar(event.message)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch(viewModel.dispatchersProvider.main) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.jobAction.collect { jobAction ->
                    when(jobAction) {
                        is FavouritesViewModel.FavouriteJobsAction.DeleteSuccess -> {
                            snackbar("Job deleted successfully")
                            submitModifiedJobs(jobAction.data)
                        }
                        is FavouritesViewModel.FavouriteJobsAction.DeleteError -> {
                            snackbar(jobAction.message)
                        }
                    }
                }
            }
        }
    }

    private fun submitModifiedJobs(jobList: List<JobPost>) {
        favouritesAdapter.differ.submitList(jobList)
        jobList.ifEmpty { binding.tvNoJobsAvailable.visibility = View.VISIBLE }
    }

    private fun submitJobs(jobList: List<JobPost>) {
        if(jobList.isNotEmpty()) {
            favouritesAdapter.differ.submitList(jobList)
        } else {
            binding.tvNoJobsAvailable.visibility = View.VISIBLE
        }
    }

    private fun setupRecyclerView() {
        favouritesAdapter = FavouritesAdapter()
        favouritesAdapter.setOnJobDeleteClickListener { jobPost ->
            viewModel.deleteSavedJob(
                jobPost.jobID,
                curLoggedInUsername,
                favouritesAdapter.differ.currentList
            )
        }

        favouritesAdapter.setOnJobClickListener {
            val bundle = Bundle().apply {
                putSerializable("jobPost", it)
                putSerializable("username", curLoggedInUsername)
            }

            findNavController().navigate(R.id.action_favouritesFragment_to_individualJobFragment, bundle)
        }

        binding.rvFavourites.apply {
            adapter = favouritesAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun loadSavedJobs() {
        viewModel.getSavedJobs(curLoggedInUsername)
    }

    private fun toggleProgressBar(status: Boolean) {
        when(status) {
            true -> {
                binding.progressBar.visibility = View.VISIBLE
            }
            false -> {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}