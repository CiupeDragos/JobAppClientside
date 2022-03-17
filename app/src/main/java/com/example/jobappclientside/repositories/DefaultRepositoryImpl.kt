package com.example.jobappclientside.repositories

import android.util.Log
import com.example.jobappclientside.datamodels.regular.JobFilter
import com.example.jobappclientside.datamodels.regular.JobPost
import com.example.jobappclientside.datamodels.requests.AccountRequest
import com.example.jobappclientside.datamodels.requests.FavouriteJobRequest
import com.example.jobappclientside.datamodels.responses.BasicApiResponse
import com.example.jobappclientside.remote.HttpApi
import com.example.jobappclientside.remote.Resource
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

    override suspend fun createJobPost(jobPost: JobPost): Resource<String> {
        return handleBasicApiResponse { httpApi.createJobPost(jobPost) }
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

    override suspend fun getJobs(jobFilter: String): Resource<List<JobPost>> {
        try {
            val networkCall = httpApi.getJobs(jobFilter)
            if(networkCall.isSuccessful) {
                return Resource.Success(networkCall.body() ?: listOf())
            } else {
               return Resource.Error(networkCall.message())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return Resource.Error("Repository error")
        }
    }


    private suspend fun handleBasicApiResponse(
        httpRequest: suspend () -> Response<BasicApiResponse>
    ): Resource<String> {
        val result = try {
            val networkCall = httpRequest()
            if(networkCall.isSuccessful && networkCall.body()!!.status) {
                Resource.Success(networkCall.body()!!.message)
            } else if(networkCall.isSuccessful) {
                Resource.Error(networkCall.body()?.message ?: networkCall.message())
            } else {
                Resource.Error(networkCall.message())
            }
        } catch (e: Exception) {
            Resource.Error("An unknown error occurred")
        }
        return result
    }
}