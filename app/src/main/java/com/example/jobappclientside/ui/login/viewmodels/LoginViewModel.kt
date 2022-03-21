package com.example.jobappclientside.ui.login.viewmodels

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jobappclientside.other.AbstractDispatchers
import com.example.jobappclientside.other.Constants.MAX_USERNAME_LENGTH
import com.example.jobappclientside.other.Constants.MIN_PASSWORD_LENGTH
import com.example.jobappclientside.other.Constants.MIN_USERNAME_LENGTH
import com.example.jobappclientside.other.DataStoreUtil
import com.example.jobappclientside.remote.Resource
import com.example.jobappclientside.repositories.AbstractRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    val dispatchersProvider: AbstractDispatchers,
    private val repository: AbstractRepository
): ViewModel() {

    sealed class LoginEvent {
        data class LoginError(val message: String): LoginEvent()
        data class LoginSuccess(val message: String): LoginEvent()
        object LoginLoading: LoginEvent()
    }

    private val _loginEvents = MutableSharedFlow<LoginEvent>()
    val loginEvents: SharedFlow<LoginEvent> = _loginEvents


    fun logUser(username: String, password: String) {
        viewModelScope.launch(dispatchersProvider.io) {
            if(!checkCredentials(username, password)) {
                _loginEvents.emit(LoginEvent.LoginError("Username or password can't be empty"))
                return@launch
            }
            _loginEvents.emit(LoginEvent.LoginLoading)
            when(val result = repository.loginAccount(username, password)) {
                is Resource.Error -> {
                    _loginEvents.emit(LoginEvent.LoginError(result.message ?: "An unknown error occurred"))
                }
                is Resource.Success -> {
                    _loginEvents.emit(LoginEvent.LoginSuccess(result.data ?: "Successfully logged in"))
                }
            }
        }

    }

    fun registerUser(username: String, password: String, confirmedPassword: String) {
        viewModelScope.launch(dispatchersProvider.io) {
            if((!checkCredentials(username, password)) || confirmedPassword.trim().isEmpty()) {
                _loginEvents.emit(LoginEvent.LoginError("No field can be left empty"))
                return@launch
            }
            if(username.length < MIN_USERNAME_LENGTH || username.length > MAX_USERNAME_LENGTH) {
                _loginEvents.emit(LoginEvent.LoginError("Username length must be between $MIN_USERNAME_LENGTH and $MAX_USERNAME_LENGTH"))
                return@launch
            }
            if(password.length < MIN_PASSWORD_LENGTH) {
                _loginEvents.emit(LoginEvent.LoginError("Password length must be at least $MIN_PASSWORD_LENGTH"))
                return@launch
            }
            if(password != confirmedPassword) {
                _loginEvents.emit(LoginEvent.LoginError("The passwords do not match"))
                return@launch
            }
            _loginEvents.emit(LoginEvent.LoginLoading)

            when(val result = repository.registerAccount(username, password)) {
                is Resource.Error -> {
                    _loginEvents.emit(LoginEvent.LoginError(result.message ?: "An unknown error occurred"))
                }
                is Resource.Success -> {
                    _loginEvents.emit(LoginEvent.LoginSuccess(result.data ?: "Account registered successfully"))
                }
            }
        }
    }

    private fun checkCredentials(username: String, password: String): Boolean {
        if(username.trim().isEmpty() || password.trim().isEmpty()) {
            return false
        }
        return true
    }
}