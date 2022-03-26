package com.example.jobappclientside.ui.core.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jobappclientside.datamodels.requests.AccountDetailsRequest
import com.example.jobappclientside.other.AbstractDispatchers
import com.example.jobappclientside.remote.Resource
import com.example.jobappclientside.repositories.AbstractRepository
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    val dispatchersProvider: AbstractDispatchers,
    private val repository: AbstractRepository,
    private val context: Context
): ViewModel(){

    sealed class LoadEvent {
        data class LoadSuccess(val data: AccountDetailsRequest): LoadEvent()
        data class LoadError(val message: String): LoadEvent()
        object LoadLoading: LoadEvent()
    }

    sealed class ApplyEvent {
        data class ApplySuccess(val data: String): ApplyEvent()
        data class ApplyError(val message: String): ApplyEvent()
        object ApplyLoading: ApplyEvent()
    }

    private val _detailsFlow = MutableStateFlow<LoadEvent>(LoadEvent.LoadLoading)
    val detailsFlow: StateFlow<LoadEvent> = _detailsFlow

    private val _eventFlow = MutableSharedFlow<ApplyEvent>()
    val eventFlow: SharedFlow<ApplyEvent> = _eventFlow

    fun loadAccountDetails(accountUsername: String) {
        viewModelScope.launch(dispatchersProvider.io) {

            _detailsFlow.emit(LoadEvent.LoadLoading)
            when(val result = repository.getAccountDetails(accountUsername)) {
                is Resource.Success -> {
                    _detailsFlow.emit(LoadEvent.LoadSuccess(result.data!!))
                }
                is Resource.Error -> {
                    _detailsFlow.emit(LoadEvent.LoadError(result.message ?: "An unknown error occurred"))
                }
            }
        }
    }

    fun applyChanges(
        profilePicUri: Uri?,
        username: String,
        realName: String,
        phoneNumber: String,
        email: String
    ) {
        viewModelScope.launch(dispatchersProvider.io) {

            _eventFlow.emit(ApplyEvent.ApplyLoading)
            val accountDetailsRequest = AccountDetailsRequest(
                "",
                username,
                realName,
                phoneNumber,
                email
            )
            var profileImageFile: MultipartBody.Part? = null
            profilePicUri?.let { uri ->
                val stream = context.contentResolver.openInputStream(uri) ?: return@launch
                val profilePicRequest = stream.readBytes().toRequestBody("image/jpeg".toMediaTypeOrNull())

                profileImageFile = MultipartBody.Part.createFormData(
                    "profilePic",
                    "${username}.jpeg",
                    profilePicRequest
                )
            }
            val accountDetailsJson = Gson().toJson(accountDetailsRequest)
            val accountDetailsBody = accountDetailsJson.toRequestBody("multipart/form-data".toMediaTypeOrNull())

            when(val result = repository.updateAccountDetails(profileImageFile, accountDetailsBody)) {
                is Resource.Success -> {
                    _eventFlow.emit(ApplyEvent.ApplySuccess(result.data ?: "Changes saved successfully"))
                }
                is Resource.Error -> {
                    _eventFlow.emit(ApplyEvent.ApplyError(result.message ?: "An unknown error occurred in vm"))
                }
            }
        }
    }

}