package com.example.jobappclientside.remote

import com.example.jobappclientside.datamodels.regular.JobPost
import com.example.jobappclientside.datamodels.requests.AccountDetailsRequest
import com.example.jobappclientside.datamodels.requests.AccountRequest
import com.example.jobappclientside.datamodels.requests.FavouriteJobRequest
import com.example.jobappclientside.datamodels.responses.BasicApiResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
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

    @Multipart
    @POST("/createJobPost")
    suspend fun createJobPost(
        @Part jobLogo: MultipartBody.Part?,
        @Part("jobPost") jobPost: RequestBody
    ): Response<BasicApiResponse>

    @POST("/deleteJobPost")
    suspend fun deleteJobPost(
        @Query("jobID") jobID: String,
        @Query("username") username: String
    ): Response<BasicApiResponse>

    @POST("/addJobToFavourites")
    suspend fun addJobToFavourites(
        @Body favouriteJobRequest: FavouriteJobRequest
    ): Response<BasicApiResponse>

    @POST("/deleteJobFromFavourites")
    suspend fun deleteJobFromFavourites(
        @Body favouriteJobRequest: FavouriteJobRequest
    ): Response<BasicApiResponse>

    @GET("/getJobPosts")
    suspend fun getJobs(
        @Query("jobFilter") jobFilter: String,
        @Query("searchQuery") searchQuery: String,
        @Query("requesterUsername") requesterUsername: String
    ): Response<List<JobPost>>

    @GET("/getSavedJobs")
    suspend fun getSavedJobs(
        @Query("accountUsername") accountUsername: String
    ): Response<List<JobPost>>

    @GET("/getAccountDetails")
    suspend fun getAccountDetails(
        @Query("username") accountUsername: String
    ): Response<AccountDetailsRequest>

    @Multipart
    @POST("/updateAccountDetails")
    suspend fun updateAccountDetails(
        @Part profilePic: MultipartBody.Part?,
        @Part("accountDetails") accountDetails: RequestBody
    ): Response<BasicApiResponse>

    @GET("/getPostedJobs")
    suspend fun getPostedJobs(
        @Query("accountUsername") accountUsername: String
    ): Response<List<JobPost>>
}
