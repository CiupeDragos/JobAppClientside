package com.example.jobappclientside.ui.core.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jobappclientside.datamodels.regular.JobPost
import com.example.jobappclientside.other.AbstractDispatchers
import com.example.jobappclientside.other.Constants.MIN_DESC_LENGTH
import com.example.jobappclientside.other.Constants.MIN_TITLE_LENGTH
import com.example.jobappclientside.remote.Resource
import com.example.jobappclientside.repositories.AbstractRepository
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CreateJobViewModel @Inject constructor(
    val dispatchersProvider: AbstractDispatchers,
    private val repository: AbstractRepository,
    private val context: Context
): ViewModel() {

    sealed class CreateJobEvent {
        data class CreateJobSuccess(val message: String): CreateJobEvent()
        data class CreateJobError(val message: String): CreateJobEvent()
        object CreateJobLoading: CreateJobEvent()
    }

    private val _jobEventFlow = MutableSharedFlow<CreateJobEvent>()
    val jobEventFlow: SharedFlow<CreateJobEvent> = _jobEventFlow

    fun createJobPost(
        jobCreator: String,
        jobTitle: String,
        jobCompany: String,
        jobLocation: String,
        jobType: String,
        jobRemote: Boolean?,
        jobSalary: Int,
        jobDesc: String,
        jobReq: String,
        jobBenefits: String,
        jobLogoUri: Uri?
    ) {

        viewModelScope.launch(dispatchersProvider.io){

            if(
                jobTitle.isEmpty() || jobCompany.isEmpty() || jobLocation.isEmpty() || jobType.isEmpty()
                || jobRemote == null || jobSalary == -1 || jobDesc.isEmpty() || jobReq.isEmpty()
                || jobBenefits.isEmpty()
            ) {
                    _jobEventFlow.emit(CreateJobEvent.CreateJobError("No field can be left empty"))
                return@launch
            }

            if(jobTitle.length < MIN_TITLE_LENGTH) {
                _jobEventFlow.emit(
                    CreateJobEvent.CreateJobError("The title needs to be at least $MIN_TITLE_LENGTH characters long")
                )
                return@launch
            }

            if(jobDesc.length < MIN_DESC_LENGTH) {
                _jobEventFlow.emit(
                    CreateJobEvent.CreateJobError("The description needs to be at least $MIN_DESC_LENGTH characters long")
                )
                return@launch
            }

            if(jobReq.length < MIN_DESC_LENGTH) {
                _jobEventFlow.emit(
                    CreateJobEvent.CreateJobError("The requirements need to be at least $MIN_DESC_LENGTH characters long")
                )
                return@launch
            }

            if(jobBenefits.length < MIN_DESC_LENGTH) {
                _jobEventFlow.emit(
                    CreateJobEvent.CreateJobError("The benefits need to be at least $MIN_DESC_LENGTH characters long")
                )
                return@launch
            }

            _jobEventFlow.emit(CreateJobEvent.CreateJobLoading)

            val jobPostToSend = JobPost(
                UUID.randomUUID().toString(),
                jobCreator,
                jobType,
                jobRemote,
                jobSalary,
                jobLocation,
                jobTitle,
                jobCompany,
                jobDesc,
                jobReq,
                jobBenefits,
                0,
                "",
                false
            )

            var jobLogoFile: MultipartBody.Part? = null
            jobLogoUri?.let { jobUri ->
                val stream = context.contentResolver.openInputStream(jobUri) ?: return@let
                val request = stream.readBytes().toRequestBody("image/jpeg".toMediaTypeOrNull())

                jobLogoFile = MultipartBody.Part.createFormData(
                    "jobLogo",
                    "${jobPostToSend.jobID}.jpeg",
                    request
                )
            }
            val jobPostJson = Gson().toJson(jobPostToSend)
            val jobPostRequest = jobPostJson.toRequestBody("multipart/form-data".toMediaTypeOrNull())

            when(val result = repository.createJobPost(jobLogoFile, jobPostRequest)) {
                is Resource.Success -> {
                    _jobEventFlow.emit(
                        CreateJobEvent.CreateJobSuccess(result.data ?: "Job added successfully")
                    )
                }
                is Resource.Error -> {
                    _jobEventFlow.emit(
                        CreateJobEvent.CreateJobError(result.message ?: "An unknown error occurred")
                    )
                }
            }

        }
    }
}