package com.example.jobappclientside.ui.core.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jobappclientside.datamodels.requests.AccountDetailsRequest
import com.example.jobappclientside.other.AbstractDispatchers
import com.example.jobappclientside.repositories.AbstractRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.jobappclientside.remote.Resource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IndividualJobViewModel @Inject constructor(
    val dispatchersProvider: AbstractDispatchers,
    private val repository: AbstractRepository
): ViewModel() {

    sealed class DetailsEvent {
        data class DetailsSuccess(val data: AccountDetailsRequest): DetailsEvent()
        data class DetailsError(val message: String): DetailsEvent()
        object DetailsLoading: DetailsEvent()
    }

    sealed class DeleteEvent {
        data class DeleteJobSuccess(val data: String): DeleteEvent()
        data class DeleteJobError(val message: String): DeleteEvent()
        object DeleteJobLoading: DeleteEvent()
    }

    private val _detailsFlow = MutableStateFlow<DetailsEvent>(DetailsEvent.DetailsLoading)
    val detailsFlow: StateFlow<DetailsEvent> = _detailsFlow

    private val _eventsFlow = MutableSharedFlow<DeleteEvent>()
    val eventsFlow: SharedFlow<DeleteEvent> = _eventsFlow

    fun loadCreatorDetails(username: String) {
        viewModelScope.launch(dispatchersProvider.io) {
            when(val result = repository.getAccountDetails(username)) {
                is Resource.Success -> {
                    _detailsFlow.emit(DetailsEvent.DetailsSuccess(result.data!!))
                }
                is Resource.Error -> {
                    _detailsFlow.emit(DetailsEvent.DetailsError(result.message ?: "An unknown error occurred"))
                }
            }
        }
    }

    fun deleteJobPost(jobID: String, username: String) {
        viewModelScope.launch(dispatchersProvider.io) {
            _eventsFlow.emit(DeleteEvent.DeleteJobLoading)
            when(val result = repository.deleteJobPost(jobID, username)) {
                is Resource.Success -> {
                    _eventsFlow.emit(DeleteEvent.DeleteJobSuccess(result.data ?: "Job deleted successfully"))
                }
                is Resource.Error -> {
                    _eventsFlow.emit(DeleteEvent.DeleteJobError(result.message ?: "An unknown error occurred"))
                }
            }
        }
    }
}