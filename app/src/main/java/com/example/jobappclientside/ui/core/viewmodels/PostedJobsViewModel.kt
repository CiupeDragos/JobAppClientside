package com.example.jobappclientside.ui.core.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jobappclientside.remote.Resource
import com.example.jobappclientside.datamodels.regular.JobPost
import com.example.jobappclientside.other.AbstractDispatchers
import com.example.jobappclientside.repositories.AbstractRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostedJobsViewModel @Inject constructor(
    val dispatchersProvider: AbstractDispatchers,
    private val repository: AbstractRepository
): ViewModel() {

    sealed class PostedJobsEvent {
        data class PostedJobsSuccess(val data: List<JobPost>): PostedJobsEvent()
        data class PostedJobsError(val message: String): PostedJobsEvent()
        object PostedJobsLoading: PostedJobsEvent()
    }

    private val _postedJobsFlow = MutableStateFlow<PostedJobsEvent>(PostedJobsEvent.PostedJobsLoading)
    val postedJobsFlow: StateFlow<PostedJobsEvent> = _postedJobsFlow

    fun getPostedJobs(accountUsername: String) {
        viewModelScope.launch(dispatchersProvider.io) {
            when(val result = repository.getPostedJobs(accountUsername)) {
                is Resource.Success -> {
                    _postedJobsFlow.emit(PostedJobsEvent.PostedJobsSuccess(result.data ?: listOf()))
                }
                is Resource.Error -> {
                    _postedJobsFlow.emit(PostedJobsEvent.PostedJobsError(result.message ?: "An unknown error occurred"))
                }
            }
        }
    }
}