package com.example.jobappclientside.repositories

import com.example.jobappclientside.datamodels.regular.JobPost
import com.example.jobappclientside.datamodels.requests.AccountDetailsRequest
import com.example.jobappclientside.datamodels.requests.AccountRequest
import com.example.jobappclientside.datamodels.requests.FavouriteJobRequest
import com.example.jobappclientside.datamodels.responses.BasicApiResponse
import com.example.jobappclientside.remote.HttpApi
import com.example.jobappclientside.remote.Resource
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import kotlin.Exception

class DefaultRepositoryImpl(private val httpApi: HttpApi): AbstractRepository {
    override suspend fun registerAccount(username: String, password: String): Resource<String> {
        val accountRequest = AccountRequest(username, password)

        return handleBasicApiResponse { httpApi.registerAccount(accountRequest) }
    }

    override suspend fun loginAccount(username: String, password: String): Resource<String> {
        val accountRequest = AccountRequest(username, password)

        return handleBasicApiResponse { httpApi.loginAccount(accountRequest) }
    }

    override suspend fun createJobPost(jobLogo: MultipartBody.Part?, jobPost: RequestBody): Resource<String> {
        return handleBasicApiResponse { httpApi.createJobPost(jobLogo, jobPost) }
    }

    override suspend fun deleteJobPost(jobID: String, username: String): Resource<String> {
        return handleBasicApiResponse { httpApi.deleteJobPost(jobID, username) }
    }

    override suspend fun addJobToFavourites(jobID: String, accountUsername: String): Resource<String> {
        val favouriteJobRequest = FavouriteJobRequest(jobID, accountUsername)

        return handleBasicApiResponse { httpApi.addJobToFavourites(favouriteJobRequest) }
    }

    override suspend fun deleteJobFromFavourites(jobID: String, accountUsername: String): Resource<String> {
        val favouriteJobRequest = FavouriteJobRequest(jobID, accountUsername)

        return handleBasicApiResponse { httpApi.deleteJobFromFavourites(favouriteJobRequest) }
    }

    override suspend fun getJobs(jobFilter: String, searchQuery: String, requesterUsername: String): Resource<List<JobPost>> {
       return handleGeneralResponse { httpApi.getJobs(jobFilter, searchQuery, requesterUsername) }
    }

    override suspend fun getSavedJobs(requesterUsername: String): Resource<List<JobPost>> {
        return handleGeneralResponse { httpApi.getSavedJobs(requesterUsername) }
    }

    override suspend fun getAccountDetails(accountUsername: String): Resource<AccountDetailsRequest> {
        return handleGeneralResponse { httpApi.getAccountDetails(accountUsername) }
    }

    override suspend fun updateAccountDetails(profilePic: MultipartBody.Part?, accountDetails: RequestBody): Resource<String> {
        return handleBasicApiResponse { httpApi.updateAccountDetails(profilePic, accountDetails) }
    }

    override suspend fun getPostedJobs(accountUsername: String): Resource<List<JobPost>> {
        return handleGeneralResponse { httpApi.getPostedJobs(accountUsername) }
    }

    private suspend fun <T> handleGeneralResponse(
        httpRequest: suspend () -> Response<T>
    ): Resource<T> {
        return try {
            val networkCall = httpRequest()
            if(networkCall.isSuccessful) {
                Resource.Success(networkCall.body()!!)
            } else {
                Resource.Error(networkCall.message())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error("An unknown error occurred")
        }
    }

    private suspend fun handleBasicApiResponse(
        httpRequest: suspend () -> Response<BasicApiResponse>
    ): Resource<String> {
        return try {
            val networkCall = httpRequest()
            if(networkCall.isSuccessful && networkCall.body()!!.status) {
                Resource.Success(networkCall.body()!!.message)
            } else if(networkCall.isSuccessful) {
                Resource.Error(networkCall.body()?.message ?: networkCall.message())
            } else {
                Resource.Error(networkCall.message())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error("An unknown error occurred")
        }
    }
}