package com.example.jobappclientside.ui.core.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.jobappclientside.databinding.FragmentSearchBinding
import com.example.jobappclientside.ui.adapters.JobFiltersAdapter
import com.example.jobappclientside.ui.core.viewmodels.JobSearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment: Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: JobSearchViewModel by activityViewModels()
    private lateinit var jobFiltersAdapter: JobFiltersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        subscribeToObservers()
        initFilters()
    }

    private fun subscribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launch(viewModel.dispatchersProvider.default) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchFiltersFlow.collect { jobFilterList ->
                    Log.d("MainActivity", "Observed for $jobFilterList")
                    viewModel.requestJobsWithFilters(jobFilterList)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch(viewModel.dispatchersProvider.default) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.jobRequestFlow.collect { jobEvent ->
                    when(jobEvent) {
                        is JobSearchViewModel.JobEvent.JobRequestSuccess -> {
                            val jobPosts = jobEvent.data //The actual job posts retrieved from the backend
                            Log.d("MainActivity", "$jobPosts")
                        }
                        is JobSearchViewModel.JobEvent.JobRequestError -> {
                            Log.d("MainActivity", jobEvent.message)
                        }
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        jobFiltersAdapter = JobFiltersAdapter(viewModel)
        jobFiltersAdapter.setOnFilterClick { jobFilterItem ->
            viewModel.removeFilterFromList(jobFilterItem, jobFiltersAdapter.differ.currentList)
        }
        binding.rvJobFilters.apply {
            adapter = jobFiltersAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }
    }

    private fun initFilters() {
        viewModel.initFilters()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}