package com.example.jobappclientside.ui.core.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavAction
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.signature.ObjectKey
import com.example.jobappclientside.R
import com.example.jobappclientside.databinding.FragmentIndividualJobBinding
import com.example.jobappclientside.datamodels.requests.AccountDetailsRequest
import com.example.jobappclientside.other.Constants.BASE_URL
import com.example.jobappclientside.other.snackbar
import com.example.jobappclientside.ui.core.viewmodels.IndividualJobViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

@AndroidEntryPoint
class IndividualJobFragment: Fragment() {

    private var _binding: FragmentIndividualJobBinding? = null
    private val binding get() = _binding!!

    private val navArgs: IndividualJobFragmentArgs by navArgs()
    private val viewModel: IndividualJobViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentIndividualJobBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toggleBottomNav(false)
        loadDetailsFromJobPost()
        subscribeToObservers()

        binding.btnDeleteJob.setOnClickListener {
            viewModel.deleteJobPost(navArgs.jobPost.jobID, navArgs.username)
        }

        binding.btnEditJob.setOnClickListener {
            editJob()
        }
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

    private fun loadDetailsFromJobPost() {
        val curJobPost = navArgs.jobPost
        binding.apply {
            tvJobTitleTopBar.text = curJobPost.jobTitle
            tvJobTitleValue.text = curJobPost.jobTitle
            tvJobCompanyValue.text = curJobPost.jobCompany
            tvJobLocationValue.text = curJobPost.jobLocation
            tvJobTypeValue.text = curJobPost.jobType
            tvJobRemoteValue.text = curJobPost.jobRemote
            tvJobDescriptionValue.text = curJobPost.jobDescription
            tvJobRequirementsValue.text = curJobPost.jobRequirements
            tvJobBenefitsValue.text = curJobPost.jobBenefits

            //parsing the price
            val numberFormat = NumberFormat.getCurrencyInstance()
            numberFormat.maximumFractionDigits = 0
            val convertedFormat = numberFormat.format(curJobPost.jobSalary)
            tvJobSalaryValue.text = convertedFormat

            if(navArgs.username != curJobPost.jobCreatorUsername) {
                btnDeleteJob.visibility = View.GONE
                btnEditJob.visibility = View.GONE
            }

            loadAccountDetails()
        }
    }

    private fun loadAccountDetails() {
        viewModel.loadCreatorDetails(navArgs.jobPost.jobCreatorUsername)
    }

    private fun parseAccountDetails(accountDetailsRequest: AccountDetailsRequest) {
        binding.apply {
            tvCreatorPhoneValue.text = accountDetailsRequest.phoneNumber.ifEmpty { "Not specified" }
            tvCreatorEmailValue.text = accountDetailsRequest.email.ifEmpty { "Not specified" }
            tvJobCreator.text = accountDetailsRequest.realName
        }

        Glide.with(this)
            .load(BASE_URL + accountDetailsRequest.profilePicUrl)
            .signature(ObjectKey(UUID.randomUUID()))
            .into(binding.imgProfileIcon)
    }

    private fun subscribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launch(viewModel.dispatchersProvider.main) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.detailsFlow.collect { event ->
                    when(event) {
                        is IndividualJobViewModel.DetailsEvent.DetailsLoading -> {
                            toggleProgressBar(true)
                        }
                        is IndividualJobViewModel.DetailsEvent.DetailsSuccess -> {
                            toggleProgressBar(false)
                            parseAccountDetails(event.data)
                        }
                        is IndividualJobViewModel.DetailsEvent.DetailsError -> {
                            snackbar(event.message)
                            toggleProgressBar(false)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch(viewModel.dispatchersProvider.main) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eventsFlow.collect { deleteEvent ->
                    when(deleteEvent) {
                        is IndividualJobViewModel.DeleteEvent.DeleteJobSuccess -> {
                            toggleProgressBar(false)
                            snackbar(deleteEvent.data)
                            navigateBack()
                        }
                        is IndividualJobViewModel.DeleteEvent.DeleteJobError -> {
                            toggleProgressBar(false)
                            snackbar(deleteEvent.message)
                        }
                        is IndividualJobViewModel.DeleteEvent.DeleteJobLoading -> {
                            toggleProgressBar(true)
                        }
                    }
                }
            }
        }
    }

    private fun navigateBack() {
        val prevFragment = findNavController().previousBackStackEntry!!.destination.id
        findNavController().popBackStack(prevFragment, true)
    }

    private fun editJob() {
        val bundle = Bundle().apply {
            putSerializable("jobPost", navArgs.jobPost)
        }
        findNavController().navigate(R.id.action_individualJobFragment_to_createJobFragment, bundle)
    }


    override fun onStop() {
        super.onStop()
        toggleBottomNav(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}