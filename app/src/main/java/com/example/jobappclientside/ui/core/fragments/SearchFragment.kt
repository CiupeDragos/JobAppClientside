package com.example.jobappclientside.ui.core.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jobappclientside.R
import com.example.jobappclientside.databinding.FragmentSearchBinding
import com.example.jobappclientside.datamodels.regular.JobPost
import com.example.jobappclientside.other.DataStoreUtil
import com.example.jobappclientside.other.snackbar
import com.example.jobappclientside.ui.adapters.JobFiltersAdapter
import com.example.jobappclientside.ui.adapters.JobPostsAdapter
import com.example.jobappclientside.ui.core.viewmodels.JobSearchViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment: Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: JobSearchViewModel by activityViewModels()


    private var firstTimeOpen = true

    private lateinit var jobFiltersAdapter: JobFiltersAdapter
    private lateinit var jobPostsAdapter: JobPostsAdapter
    private lateinit var curLoggedInUsername: String

    @Inject
    lateinit var dataStoreUtil: DataStoreUtil
    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        firstTimeOpen = true
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getLoggedInUsername()
        setupRecyclerViews()
        subscribeToObservers()
        setSearchTextListener()

        binding.imgApplyFilters.setOnClickListener {
            navigateToFiltersFragment()
        }
    }

    private fun subscribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launch(viewModel.dispatchersProvider.main) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchFiltersFlow.collect { jobFilterList ->
                    Log.d("MainActivityDebug", "Search flow observed")
                    jobFiltersAdapter.differ.submitList(jobFilterList)
                    viewModel.requestJobsWithFilters(
                        jobFilterList,
                        binding.etSearchJobs.text?.toString() ?: "",
                        curLoggedInUsername
                    )
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch(viewModel.dispatchersProvider.main) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.requestFlow.collect { requestEvent ->
                    when(requestEvent) {
                        is JobSearchViewModel.RequestEvent.JobRequestSuccess -> {
                            submitJobs(requestEvent.data)
                            toggleProgressBar(false)
                            Log.d("MainActivityDebug", "JobResult flow observed for success")
                        }
                        is JobSearchViewModel.RequestEvent.JobRequestError -> {
                            Log.d("MainActivityDebug", requestEvent.message)
                            snackbar(requestEvent.message)
                            toggleProgressBar(false)
                        }
                        is JobSearchViewModel.RequestEvent.JobRequestLoading -> {
                            toggleProgressBar(true)
                            Log.d("MainActivityDebug", "JobResult flow observed for loading")
                        }
                        is JobSearchViewModel.RequestEvent.JobSaveSuccess -> {
                            snackbar(requestEvent.data)
                        }
                        is JobSearchViewModel.RequestEvent.JobSaveError -> {
                            snackbar(requestEvent.message)
                        }
                    }
                }
            }
        }
    }

    private fun submitJobs(jobList: List<JobPost>) {
        if(jobList.isNotEmpty()) {
            jobPostsAdapter.differ.submitList(jobList)
            binding.tvNoJobsAvailable.visibility = View.GONE
        } else {
            binding.tvNoJobsAvailable.visibility = View.VISIBLE
        }
    }

    private fun setupRecyclerViews() {
        setupFilersRecyclerView()
        setupJobsRecyclerView()
    }

    private fun toggleProgressBar(state: Boolean) {
        when(state) {
            true -> {
                binding.progressBar.visibility = View.VISIBLE
            }
            false -> {
                binding.progressBar.visibility = View.GONE
            }
        }
    }


    private fun setSearchTextListener() {
        binding.etSearchJobs.addTextChangedListener {
            if(!firstTimeOpen) {
                val searchText = it?.toString() ?: ""
                searchJob?.cancel()
                binding.tvNoJobsAvailable.visibility = View.GONE
                searchJob = viewLifecycleOwner.lifecycleScope.launch(viewModel.dispatchersProvider.io) {
                    delay(500L)
                    viewModel.requestJobsWithFilters(
                        jobFiltersAdapter.differ.currentList,
                        searchText,
                        curLoggedInUsername
                    )
                }
            } else {
                firstTimeOpen = false
            }
        }
    }

    private fun getLoggedInUsername() {
        viewLifecycleOwner.lifecycleScope.launch(viewModel.dispatchersProvider.default) {
            curLoggedInUsername = dataStoreUtil.getUsername()
        }
    }

    private fun navigateToFiltersFragment() {
        findNavController().navigate(R.id.action_searchFragment_to_filtersFragment, null, null)
    }

    private fun setOnFilterClick() {
        jobFiltersAdapter.setOnFilterClick { jobFilterItem ->
            viewModel.removeFilterFromList(jobFilterItem, jobFiltersAdapter.differ.currentList)
        }
    }

    private fun setupFilersRecyclerView() {
        jobFiltersAdapter = JobFiltersAdapter(viewModel)
        setOnFilterClick()

        binding.rvJobFilters.apply {
            adapter = jobFiltersAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        }
    }

    private fun setOnJobSaveClick() {
        jobPostsAdapter.setOnJobSaveClickListener { jobPost ->

            val curList = jobPostsAdapter.differ.currentList
            val curJobIndex = curList.indexOfFirst { it.jobID == jobPost.jobID }
            val newList = curList.toMutableList()

            when(newList[curJobIndex].isAddedToFavourites) {
                false -> {
                    newList[curJobIndex].isAddedToFavourites = true
                    viewModel.addJobToFavourites(jobPost.jobID, curLoggedInUsername)
                }
                true -> {
                    newList[curJobIndex].isAddedToFavourites = false
                    viewModel.removeJobFromFavourites(jobPost.jobID, curLoggedInUsername)
                }
            }

            jobPostsAdapter.differ.submitList(newList)
            //Async list differ not updating list ? :(((
            jobPostsAdapter.notifyDataSetChanged()
        }
    }

    private fun setOnJobClick() {
        jobPostsAdapter.setOnJobClickListener {
            val bundle = Bundle().apply {
                putSerializable("jobPost", it)
                putSerializable("username", curLoggedInUsername)
            }
            findNavController().navigate(R.id.action_searchFragment_to_individualJobFragment, bundle)
        }
    }

    private fun setupJobsRecyclerView() {
        jobPostsAdapter = JobPostsAdapter("")
        setOnJobSaveClick()
        setOnJobClick()

        binding.rvSearchJobs.apply {
            adapter = jobPostsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}