package com.example.jobappclientside.repositories

import com.example.jobappclientside.datamodels.regular.JobPost
import com.example.jobappclientside.datamodels.requests.AccountDetailsRequest
import com.example.jobappclientside.remote.Resource
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface AbstractRepository {

    suspend fun registerAccount(username: String, password: String): Resource<String>

    suspend fun loginAccount(username: String, password: String): Resource<String>

    suspend fun createJobPost(jobLogo: MultipartBody.Part?, jobPost: RequestBody): Resource<String>

    suspend fun deleteJobPost(jobID: String, username: String): Resource<String>

    suspend fun addJobToFavourites(jobID: String, accountUsername: String): Resource<String>

    suspend fun deleteJobFromFavourites(jobID: String, accountUsername: String): Resource<String>

    suspend fun getJobs(jobFilter: String, searchQuery: String, requesterUsername: String): Resource<List<JobPost>>

    suspend fun getSavedJobs(requesterUsername: String): Resource<List<JobPost>>

    suspend fun getAccountDetails(accountUsername: String): Resource<AccountDetailsRequest>

    suspend fun updateAccountDetails(profilePic: MultipartBody.Part?, accountDetails: RequestBody): Resource<String>

    suspend fun getPostedJobs(accountUsername: String): Resource<List<JobPost>>
}