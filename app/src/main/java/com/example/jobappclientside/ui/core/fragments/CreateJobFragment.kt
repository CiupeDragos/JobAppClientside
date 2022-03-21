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
import com.example.jobappclientside.R
import com.example.jobappclientside.databinding.FragmentCreateJobBinding
import com.example.jobappclientside.other.DataStoreUtil
import com.example.jobappclientside.other.snackbar
import com.example.jobappclientside.ui.core.viewmodels.CreateJobViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CreateJobFragment: Fragment() {

    private var _binding: FragmentCreateJobBinding? = null
    private val binding get() = _binding!!

    private lateinit var loadContract: ActivityResultLauncher<String>

    private val viewModel: CreateJobViewModel by viewModels()
    private var curLogoUri: Uri? = null

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
        toggleBottomNav(false)

        binding.btnPickImage.setOnClickListener {
            pickImage()
        }

        binding.btnCreateJobPost.setOnClickListener {

            viewModel.createJobPost(
                curLoggedInUsername,
                binding.etJobTitle.text.toString(),
                binding.etJobCompany.text.toString(),
                binding.etJobLocation.text.toString(),
                getJobType(),
                getJobRemote(),
                10,
                binding.etJobDescription.text.toString(),
                binding.etJobRequirements.text.toString(),
                binding.etJobBenefits.text.toString(),
                curLogoUri
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
                            snackbar(jobEvent.message)
                            //navigate to profile fragment again
                        }
                        is CreateJobViewModel.CreateJobEvent.CreateJobError -> {
                            snackbar(jobEvent.message)
                        }
                        is CreateJobViewModel.CreateJobEvent.CreateJobLoading -> {
                            //toggle progress bar
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

    private fun getJobRemote(): Boolean? {
        val selectBtnIDJobRemote = binding.radioGrpRemote.checkedRadioButtonId

        if(selectBtnIDJobRemote != - 1) {
            when(binding.root.findViewById<RadioButton>(selectBtnIDJobRemote).text.toString()) {
                "Yes" -> {
                    return true
                }
                "No" -> {
                    return false
                }
            }
        }
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        toggleBottomNav(true)
    }
}