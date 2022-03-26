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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JobSearchViewModel @Inject constructor(
    val dispatchersProvider: AbstractDispatchers,
    private val repository: AbstractRepository
) : ViewModel() {

    sealed class RequestEvent {
        data class JobRequestSuccess(val data: List<JobPost>) : RequestEvent()
        data class JobRequestError(val message: String) : RequestEvent()
        data class JobSaveSuccess(val data: String): RequestEvent()
        data class JobSaveError(val message: String): RequestEvent()
        object JobRequestLoading : RequestEvent()
    }

    private val _searchFiltersFlow = MutableStateFlow<List<JobFilterItem>>(listOf())
    val searchFiltersFlow: StateFlow<List<JobFilterItem>> = _searchFiltersFlow

    private val _requestFlow = MutableSharedFlow<RequestEvent>()
    val requestFlow: SharedFlow<RequestEvent> = _requestFlow

    fun emitFilters(filterList: List<JobFilterItem>) {
        viewModelScope.launch(dispatchersProvider.default) {
            _searchFiltersFlow.emit(filterList)
        }
    }

    suspend fun requestJobsWithFilters(filterList: List<JobFilterItem>, searchQuery: String, requesterUsername: String) {

        Log.d("MainActivityDebug", "Request for jobs sent")

        _requestFlow.emit(RequestEvent.JobRequestLoading)

        val jobTypeItem = filterList.indexOfFirst { it.filterName == "jobType" }
        val jobMinSalaryItem = filterList.indexOfFirst { it.filterName == "jobMinSalary" }
        val jobLocationItem = filterList.indexOfFirst { it.filterName == "jobLocation" }
        val jobRemoteItem = filterList.indexOfFirst { it.filterName == "jobRemote" }

        val jobFilter = JobFilter(
            if (jobTypeItem != -1) filterList[jobTypeItem].filterValue else null,
            if (jobMinSalaryItem != -1) filterList[jobMinSalaryItem].filterValue.toInt() else null,
            if (jobLocationItem != -1) filterList[jobLocationItem].filterValue else null,
            if(jobRemoteItem != -1) filterList[jobRemoteItem].filterValue else null
        )
        val filterToSend = Gson().toJson(jobFilter)

        when (val result = repository.getJobs(filterToSend, searchQuery, requesterUsername)) {
            is Resource.Success -> {
                _requestFlow.emit(RequestEvent.JobRequestSuccess(result.data ?: listOf()))
            }
            is Resource.Error -> {
                _requestFlow.emit(
                    RequestEvent.JobRequestError(
                        result.message ?: "An unknown error occurred"
                    )
                )
            }
        }
    }

    fun removeFilterFromList(filter: JobFilterItem, jobFilterCurList: List<JobFilterItem>) {
        viewModelScope.launch(dispatchersProvider.default) {
            val listToModify = jobFilterCurList.toMutableList()
            listToModify.remove(filter)
            _searchFiltersFlow.emit(listToModify.toList())
        }
    }

    fun addJobToFavourites(jobId: String, accountUsername: String) {
        viewModelScope.launch(dispatchersProvider.io) {
            when(val result = repository.addJobToFavourites(jobId, accountUsername)) {
                is Resource.Success -> {
                    _requestFlow.emit(RequestEvent.JobSaveSuccess(result.data ?: "Job saved successfully"))
                }
                is Resource.Error -> {
                    _requestFlow.emit(RequestEvent.JobSaveError(result.message ?: "Unknown error occurred"))
                }
            }
        }
    }

    fun removeJobFromFavourites(jobID: String, accountUsername: String) {
        viewModelScope.launch(dispatchersProvider.io) {
            when(val result = repository.deleteJobFromFavourites(jobID, accountUsername)) {
                is Resource.Success -> {
                    _requestFlow.emit(RequestEvent.JobSaveSuccess(result.data ?: "Job removed successfully"))
                }
                is Resource.Error -> {
                    _requestFlow.emit(RequestEvent.JobSaveError(result.message ?: "Unknown error occurred"))
                }
            }
        }
    }
}