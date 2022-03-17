package com.example.jobappclientside.remote

import com.example.jobappclientside.datamodels.regular.JobFilter
import com.example.jobappclientside.datamodels.regular.JobPost
import com.example.jobappclientside.datamodels.requests.AccountRequest
import com.example.jobappclientside.datamodels.requests.FavouriteJobRequest
import com.example.jobappclientside.datamodels.responses.BasicApiResponse
import retrofit2.Response
import retrofit2.http.*

interface HttpApi {

    @POST("/registerAccount")
    suspend fun registerAccount(
        @Body accountRequest: AccountRequest
    ): Response<BasicApiResponse>

    @POST("/loginAccount")
    suspend fun loginAccount(
        @Body accountRequest: AccountRequest
    ): Response<BasicApiResponse>

    @POST("/createJobPost")
    suspend fun createJobPost(
        @Body jobPost: JobPost
    ): Response<BasicApiResponse>

    @DELETE("/deleteJobPost")
    suspend fun deleteJobPost(
        @Query("jobID") jobID: String,
        @Query("username") username: String
    ): Response<BasicApiResponse>

    @POST("/addJobToFavourites")
    suspend fun addJobToFavourites(
        @Body favouriteJobRequest: FavouriteJobRequest
    ): Response<BasicApiResponse>

    @DELETE("/deleteJobFromFavourites")
    suspend fun deleteJobFromFavourites(
        @Body favouriteJobRequest: FavouriteJobRequest
    ): Response<BasicApiResponse>

    @GET("/getJobPosts")
    suspend fun getJobs(
        @Body jobFilter: JobFilter
    ): Response<List<JobPost>>
}