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
import com.example.jobappclientside.databinding.FragmentPostedJobsBinding
import com.example.jobappclientside.datamodels.regular.JobPost
import com.example.jobappclientside.other.DataStoreUtil
import com.example.jobappclientside.other.snackbar
import com.example.jobappclientside.ui.adapters.JobPostsAdapter
import com.example.jobappclientside.ui.core.viewmodels.PostedJobsViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PostedJobsFragment: Fragment() {

    private var _binding: FragmentPostedJobsBinding? = null
    private val binding get() = _binding!!

    private lateinit var postedJobsAdapter: JobPostsAdapter
    private val viewModel: PostedJobsViewModel by viewModels()

    @Inject
    lateinit var dataStoreUtil: DataStoreUtil

    private lateinit var curLoggedInUsername: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPostedJobsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toggleBottomNav(false)
        setupRecyclerView()
        getPostedJobs()
        subscribeToObservers()
    }

    private fun toggleBottomNav(state: Boolean) {
        val bottomNav: BottomNavigationView = requireActivity().findViewById(R.id.bottomNav)
        when(state) {
            false -> {
                bottomNav.visibility = View.GONE
            }
            true -> {
                bottomNav.visibility = View.VISIBLE
            }
        }
    }

    private fun subscribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launch(viewModel.dispatchersProvider.main) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.postedJobsFlow.collect { postedEvent ->
                    when(postedEvent) {
                        is PostedJobsViewModel.PostedJobsEvent.PostedJobsLoading -> {
                            toggleProgressBar(true)
                        }
                        is PostedJobsViewModel.PostedJobsEvent.PostedJobsSuccess -> {
                            submitJobs(postedEvent.data)
                            toggleProgressBar(false)
                        }
                        is PostedJobsViewModel.PostedJobsEvent.PostedJobsError -> {
                            snackbar("An unknown error occurred")
                            toggleProgressBar(false)
                        }
                    }
                }
            }
        }
    }

    private fun toggleProgressBar(state: Boolean) {
        when(state) {
            false -> {
                binding.progressBar.visibility = View.GONE
            }
            true -> {
                binding.progressBar.visibility = View.VISIBLE
            }
        }
    }

    private fun setupRecyclerView() {
        postedJobsAdapter = JobPostsAdapter("PostedJobs")
        postedJobsAdapter.setOnJobClickListener {
            val bundle = Bundle().apply {
                putSerializable("jobPost", it)
                putSerializable("username", curLoggedInUsername)
            }

            findNavController().navigate(R.id.action_postedJobsFragment_to_individualJobFragment, bundle)
        }

        binding.rvPostedJobs.apply {
            adapter = postedJobsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun getPostedJobs() {
        viewLifecycleOwner.lifecycleScope.launch(viewModel.dispatchersProvider.default) {
            curLoggedInUsername = dataStoreUtil.getUsername()
            viewModel.getPostedJobs(curLoggedInUsername)
        }
    }

    private fun submitJobs(jobList: List<JobPost>) {
        if(jobList.isNotEmpty()) {
            postedJobsAdapter.differ.submitList(jobList)
        } else {
            binding.tvNoJobsAvailable.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        toggleBottomNav(true)
    }
}