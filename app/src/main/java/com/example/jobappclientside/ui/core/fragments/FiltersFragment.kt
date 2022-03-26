package com.example.jobappclientside.ui.core.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.jobappclientside.R
import com.example.jobappclientside.databinding.FragmentFiltersBinding
import com.example.jobappclientside.datamodels.regular.JobFilterItem
import com.example.jobappclientside.ui.core.viewmodels.JobSearchViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FiltersFragment: Fragment() {

    private var _binding: FragmentFiltersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: JobSearchViewModel by activityViewModels()

    private var filterList: MutableList<JobFilterItem> = mutableListOf()
    private var filtersApplied = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        toggleBottomNav(false)
        _binding = FragmentFiltersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()

        binding.btnApply.setOnClickListener {
            applyFilters()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        toggleBottomNav(true)
    }

    private fun applyFilters() {
        val selectedTypeBtn = binding.radioGrpType.checkedRadioButtonId
        val selectedRemoteBtn = binding.radioGrpRemote.checkedRadioButtonId

        if(selectedTypeBtn != -1) {
            filterList.add(
                JobFilterItem(
                    "jobType",
                    binding.root.findViewById<RadioButton>(selectedTypeBtn).text.toString()
                )
            )
        }
        if(selectedRemoteBtn != -1) {
            filterList.add(
                JobFilterItem(
                    "jobRemote",
                    binding.root.findViewById<RadioButton>(selectedRemoteBtn).text.toString()
                )
            )
        }
        if(binding.etJobLocation.text.toString().isNotEmpty()) {
            filterList.add(
                JobFilterItem(
                    "jobLocation",
                    binding.etJobLocation.text.toString()
                )
            )
        }
        if(binding.etJobMinSalary.text.toString().isNotEmpty()) {
            filterList.add(
                JobFilterItem(
                    "jobMinSalary",
                    binding.etJobMinSalary.text.toString()
                )
            )
        }
        filtersApplied = true
        navigateToSearch()
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
                viewModel.searchFiltersFlow.collect { filterList ->
                    parseFilterList(filterList)
                }
            }
        }
    }

    private fun parseFilterList(list: List<JobFilterItem>) {
        list.forEach { filter ->
            when(filter.filterName) {
                "jobType" -> {
                    if(filter.filterValue == "Full-time") {
                        binding.btnFullTime.isChecked = true
                    } else {
                        binding.btnPartTime.isChecked = true
                    }
                }
                "jobRemote" -> {
                    if(filter.filterValue == "Yes") {
                        binding.btnYes.isChecked = true
                    } else {
                        binding.btnNo.isChecked = true
                    }
                }
                "jobLocation" -> {
                    binding.etJobLocation.setText(filter.filterValue)
                }
                "jobMinSalary" -> {
                    binding.etJobMinSalary.setText(filter.filterValue)
                }
            }
        }
    }


    private fun navigateToSearch() {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.searchFragment, true)
            .build()
        toggleBottomNav(true)
        findNavController().navigate(R.id.action_filtersFragment_to_searchFragment, null, navOptions)
    }

    override fun onStop() {
        super.onStop()
        if(filtersApplied) {
            viewModel.emitFilters(filterList)
        }
    }
}