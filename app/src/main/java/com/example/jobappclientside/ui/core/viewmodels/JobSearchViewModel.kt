package com.example.jobappclientside.ui.core.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jobappclientside.datamodels.regular.JobFilter
import com.example.jobappclientside.datamodels.regular.JobFilterItem
import com.example.jobappclientside.datamodels.regular.JobPost
import com.example.jobappclientside.other.AbstractDispatchers
import com.example.jobappclientside.remote.Resource
import com.example.jobappclientside.repositories.AbstractRepository
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JobSearchViewModel @Inject constructor(
    val dispatchersProvider: AbstractDispatchers,
    private val repository: AbstractRepository
): ViewModel() {


    sealed class JobEvent {
        data class JobRequestSuccess(val data: List<JobPost>): JobEvent()
        data class JobRequestError(val message: String): JobEvent()
    }

    private val _searchFiltersFlow = MutableStateFlow(initializeFilters())
    val searchFiltersFlow: StateFlow<List<JobFilterItem>> = _searchFiltersFlow

    private val _jobRequestFlow = MutableStateFlow<JobEvent>(JobEvent.JobRequestSuccess(listOf()))
    val jobRequestFlow: StateFlow<JobEvent> = _jobRequestFlow

    fun emitFilters(filterList: List<JobFilterItem>) {
        viewModelScope.launch(dispatchersProvider.default) {
            _searchFiltersFlow.emit(filterList)
        }
    }

    fun requestJobsWithFilters(filterList: List<JobFilterItem>) {
        viewModelScope.launch(dispatchersProvider.io) {
            val jobTypeItem = filterList.indexOfFirst { it.filterName == "jobType" }

            val jobMinSalaryItem = filterList.indexOfFirst { it.filterName == "jobMinSalary" }

            val jobLocationItem = filterList.indexOfFirst { it.filterName == "jobLocation" }

            val jobFilter = JobFilter(
                filterList[jobTypeItem].filterValue,
                filterList[jobMinSalaryItem].filterValue?.toInt(),
                filterList[jobLocationItem].filterValue
            )
            val filterToSend = Gson().toJson(jobFilter)
            Log.d("Main Activity", "$jobFilter")

            when(val result = repository.getJobs(filterToSend)) {
                is Resource.Success -> {
                    _jobRequestFlow.emit(JobEvent.JobRequestSuccess(result.data ?: listOf()))
                }
                is Resource.Error -> {
                    _jobRequestFlow.emit(JobEvent.JobRequestError(result.message ?: "An unknown error occurred"))
                }
            }
        }
    }

    fun removeFilterFromList(filter: JobFilterItem, jobFilterCurList: List<JobFilterItem>) {
        viewModelScope.launch(dispatchersProvider.default) {
            val listToModify = jobFilterCurList.toMutableList()
            val indexToModify = jobFilterCurList.indexOfFirst {
                it.filterName == filter.filterName
            }
            listToModify[indexToModify].filterValue = null
            _searchFiltersFlow.emit(listToModify.toList())
        }
    }

    private fun initializeFilters(): List<JobFilterItem> {
        return listOf(
            JobFilterItem("jobType", null),
            JobFilterItem("jobMinSalary", null),
            JobFilterItem("jobLocation", null)
            )
    }
}