package com.example.jobappclientside.ui.core.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jobappclientside.datamodels.regular.JobPost
import com.example.jobappclientside.other.AbstractDispatchers
import com.example.jobappclientside.remote.Resource
import com.example.jobappclientside.repositories.AbstractRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavouritesViewModel @Inject constructor(
    val dispatchersProvider: AbstractDispatchers,
    private val repository: AbstractRepository
): ViewModel() {

    sealed class FavouriteJobsEvent {
        data class FavouriteJobsSuccess(val data: List<JobPost>): FavouriteJobsEvent()
        data class FavouriteJobsError(val message: String): FavouriteJobsEvent()
        object FavouriteJobsLoading: FavouriteJobsEvent()
    }

    sealed class FavouriteJobsAction {
        data class DeleteSuccess(val data: List<JobPost>): FavouriteJobsAction()
        data class DeleteError(val message: String): FavouriteJobsAction()
    }

    private val _jobRequest = MutableStateFlow<FavouriteJobsEvent>(FavouriteJobsEvent.FavouriteJobsSuccess(listOf()))
    val jobRequest: StateFlow<FavouriteJobsEvent> = _jobRequest

    private val _jobAction = MutableSharedFlow<FavouriteJobsAction>()
    val jobAction: SharedFlow<FavouriteJobsAction> = _jobAction

    fun getSavedJobs(requesterUsername: String) {
        viewModelScope.launch(dispatchersProvider.io) {
            _jobRequest.emit(FavouriteJobsEvent.FavouriteJobsLoading)
            when(val result = repository.getSavedJobs(requesterUsername)) {
                is Resource.Success -> {
                    _jobRequest.emit(FavouriteJobsEvent.FavouriteJobsSuccess(result.data ?: listOf()))
                }
                is Resource.Error -> {
                    _jobRequest.emit(FavouriteJobsEvent.FavouriteJobsError(result.message ?: "An unknown error has occurred"))
                }
            }
        }
    }

    fun deleteSavedJob(jobID: String, requesterUsername: String, curList: List<JobPost>) {
        viewModelScope.launch(dispatchersProvider.io) {
            when(val result = repository.deleteJobFromFavourites(jobID, requesterUsername)) {
                is Resource.Success -> {
                    _jobAction.emit(FavouriteJobsAction.DeleteSuccess(deleteJobFromSavedList(jobID, curList)))
                }
                is Resource.Error -> {
                    _jobAction.emit(FavouriteJobsAction.DeleteError(result.message ?: "Unknown error occurred"))
                }
            }
        }
    }

   private fun deleteJobFromSavedList(jobID: String, jobList: List<JobPost>): List<JobPost> {
        val newList = jobList.toMutableList()
        val toDeleteIndex = newList.indexOfFirst { it.jobID == jobID }
        newList.removeAt(toDeleteIndex)

        return newList
    }
}