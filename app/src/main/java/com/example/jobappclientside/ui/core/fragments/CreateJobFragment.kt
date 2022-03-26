package com.example.jobappclientside.ui.core.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.example.jobappclientside.R
import com.example.jobappclientside.databinding.FragmentCreateJobBinding
import com.example.jobappclientside.datamodels.regular.JobPost
import com.example.jobappclientside.other.Constants.BASE_URL
import com.example.jobappclientside.other.DataStoreUtil
import com.example.jobappclientside.other.snackbar
import com.example.jobappclientside.ui.core.viewmodels.CreateJobViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class CreateJobFragment: Fragment() {

    private var _binding: FragmentCreateJobBinding? = null
    private val binding get() = _binding!!

    private lateinit var loadContract: ActivityResultLauncher<String>

    private val viewModel: CreateJobViewModel by viewModels()
    private val navArgs: CreateJobFragmentArgs by navArgs()

    private var curLogoUri: Uri? = null
    private var firstTrigger = true

    @Inject
    lateinit var dataStoreUtil: DataStoreUtil
    private lateinit var curLoggedInUsername: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerForImagePick()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateJobBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
        getLoggedInUsername()
        checkIfItsOpenedForEdit()
        toggleBottomNav(false)

        binding.btnPickImage.setOnClickListener {
            pickImage()
        }

        binding.btnCreateJobPost.setOnClickListener {

            viewModel.createJobPost(
                navArgs.jobPost?.jobID,
                curLoggedInUsername,
                binding.etJobTitle.text.toString(),
                binding.etJobCompany.text.toString(),
                binding.etJobLocation.text.toString(),
                getJobType(),
                getJobRemote(),
                getJobSalary(),
                binding.etJobDescription.text.toString(),
                binding.etJobRequirements.text.toString(),
                binding.etJobBenefits.text.toString(),
                curLogoUri,
                navArgs.jobPost?.jobImageUrl,
                navArgs.jobPost?.jobTimestamp
            )
        }
    }

    private fun registerForImagePick() {
        loadContract = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                curLogoUri = uri
                binding.imgJobLogo.setImageURI(uri)
            }
        }
    }

    private fun pickImage() {
        loadContract.launch("image/*")
    }

    private fun toggleBottomNav(state: Boolean) {
        val bottomNav: BottomNavigationView = requireActivity().findViewById(R.id.bottomNav)
        when(state) {
            false -> {
                bottomNav.visibility = View.GONE
            }
            true -> {
                bottomNav.visibility = View.VISIBLE
            }
        }
    }

    private fun subscribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launch(viewModel.dispatchersProvider.main) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.jobEventFlow.collect { jobEvent ->
                    when(jobEvent) {
                        is CreateJobViewModel.CreateJobEvent.CreateJobSuccess -> {
                            toggleProgressBar(false)
                            if(navArgs.jobPost != null ) snackbar("Job edited successfully")
                            else snackbar("Job uploaded successfully")
                            navigateBack(jobEvent.uploadedJobPost)
                        }
                        is CreateJobViewModel.CreateJobEvent.CreateJobError -> {
                            toggleProgressBar(false)
                            snackbar(jobEvent.message)
                        }
                        is CreateJobViewModel.CreateJobEvent.CreateJobLoading -> {
                            toggleProgressBar(true)
                        }
                    }
                }
            }
        }
    }

    private fun getLoggedInUsername() {
        viewLifecycleOwner.lifecycleScope.launch(viewModel.dispatchersProvider.default) {
            curLoggedInUsername = dataStoreUtil.getUsername()
        }
    }

    private fun getJobType(): String {
        val selectBtnIDJobType = binding.radioGrpType.checkedRadioButtonId
        if(selectBtnIDJobType != -1) {
            return binding.root.findViewById<RadioButton>(selectBtnIDJobType).text.toString()
        }
        return ""
    }

    private fun getJobRemote(): String {
        val selectBtnIDJobRemote = binding.radioGrpRemote.checkedRadioButtonId

        if(selectBtnIDJobRemote != - 1) {
            return binding.root.findViewById<RadioButton>(selectBtnIDJobRemote).text.toString()
        }
        return ""
    }

    private fun toggleProgressBar(state: Boolean) {
        when(state) {
            false -> {
                binding.progressBar.visibility = View.GONE
            }
            true -> {
                binding.progressBar.visibility = View.VISIBLE
            }
        }
    }

    private fun getJobSalary(): Int {
        val salaryString = binding.etJobSalary.text.toString()
        return if(salaryString.isNotEmpty()) salaryString.toInt() else -1
    }

    private fun navigateBack(modifiedJobPost: JobPost) {
        if(navArgs.jobPost != null) {
            //came to edit from job details screen
            val bundle = Bundle().apply {
                putSerializable("jobPost", modifiedJobPost)
                putSerializable("username", modifiedJobPost.jobCreatorUsername)
            }
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.individualJobFragment, true)
                .build()
            findNavController().navigate(R.id.action_createJobFragment_to_individualJobFragment, bundle, navOptions)
        } else {
            //came to create job from profile screen
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.profileFragment, true)
                .build()
            findNavController().navigate(R.id.action_createJobFragment_to_profileFragment, null, navOptions = navOptions)
        }
    }

    private fun checkIfItsOpenedForEdit() {
        navArgs.jobPost?.let { jobToEdit ->
            binding.apply {
                etJobTitle.setText(jobToEdit.jobTitle)
                etJobCompany.setText(jobToEdit.jobCompany)
                etJobLocation.setText(jobToEdit.jobLocation)
                etJobSalary.setText(jobToEdit.jobSalary.toString())
                etJobDescription.setText(jobToEdit.jobDescription)
                etJobRequirements.setText(jobToEdit.jobRequirements)
                etJobBenefits.setText(jobToEdit.jobBenefits)

                if(jobToEdit.jobImageUrl.isNotEmpty() && firstTrigger) {
                    Glide.with(this@CreateJobFragment)
                        .load(BASE_URL + jobToEdit.jobImageUrl)
                        .signature(ObjectKey(UUID.randomUUID()))
                        .into(binding.imgJobLogo)
                    firstTrigger = false
                }
                getSelectedType(jobToEdit.jobType)
                getSelectedRemote(jobToEdit.jobRemote)

                binding.btnCreateJobPost.text = "Save changes"
            }
        }
    }

    private fun getSelectedType(jobType: String) {
        when(jobType) {
            "Full-time" -> {
                binding.btnFullTime.isChecked = true
            }
            "Part-time" -> {
                binding.btnPartTime.isChecked = true
            }
        }
    }

    private fun getSelectedRemote(jobRemote: String) {
        when(jobRemote) {
            "Yes" -> {
                binding.btnYes.isChecked = true
            }
            "No" -> {
                binding.btnNo.isChecked = true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        toggleBottomNav(true)
    }
}