package com.example.jobappclientside.datamodels.regular

data class JobPost(
    val jobID: String,
    val jobCreatorUsername: String,
    val jobType: String,
    val jobSalary: Int,
    val jobLocation: String,
    val jobTitle: String,
    val jobCompany: String,
    val jobDescription: String,
    val jobRequirements: String,
    val jobBenefits: String,
    val isAddedToFavourites: Boolean
)
