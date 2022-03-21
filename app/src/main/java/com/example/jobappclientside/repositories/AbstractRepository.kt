package com.example.jobappclientside.repositories

import com.example.jobappclientside.datamodels.regular.JobFilter
import com.example.jobappclientside.datamodels.regular.JobPost
import com.example.jobappclientside.datamodels.requests.AccountRequest
import com.example.jobappclientside.datamodels.requests.FavouriteJobRequest
import com.example.jobappclientside.remote.Resource
import okhttp3.MultipartBody
import okhttp3.MultipartReader
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
}