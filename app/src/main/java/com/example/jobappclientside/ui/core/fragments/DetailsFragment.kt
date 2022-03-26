package com.example.jobappclientside.ui.core.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.example.jobappclientside.R
import com.example.jobappclientside.databinding.FragmentDetailsBinding
import com.example.jobappclientside.datamodels.requests.AccountDetailsRequest
import com.example.jobappclientside.other.Constants.BASE_URL
import com.example.jobappclientside.other.DataStoreUtil
import com.example.jobappclientside.other.snackbar
import com.example.jobappclientside.ui.core.viewmodels.DetailsViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class DetailsFragment: Fragment() {

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetailsViewModel by viewModels()

    private lateinit var curUsername: String
    private var curUri: Uri? = null

    private var firstFire = true

    private lateinit var loadContract: ActivityResultLauncher<String>

    @Inject
    lateinit var dataStoreUtil: DataStoreUtil



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerForActivityResult()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toggleBottomNav(false)
        loadAccountDetails()
        subscribeToObservers()

        binding.btnApply.setOnClickListener {
            applyChanges(
                binding.etName.text.toString(),
                binding.etPhoneNumber.text.toString(),
                binding.etEmail.text.toString()
            )
        }

        binding.btnPickImage.setOnClickListener {
            pickImage()
        }

    }

    private fun subscribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launch(viewModel.dispatchersProvider.main) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.detailsFlow.collect { loadEvent ->
                    when(loadEvent) {
                        is DetailsViewModel.LoadEvent.LoadSuccess -> {
                            toggleProgressBar(false)
                            parseDetails(loadEvent.data)
                        }
                        is DetailsViewModel.LoadEvent.LoadError -> {
                            toggleProgressBar(false)
                            snackbar(loadEvent.message)
                        }
                        is DetailsViewModel.LoadEvent.LoadLoading -> {
                            toggleProgressBar(true)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch(viewModel.dispatchersProvider.main) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eventFlow.collect { event ->
                    when(event) {
                        is DetailsViewModel.ApplyEvent.ApplySuccess -> {
                            toggleProgressBar(false)
                            snackbar(event.data)
                            navigateToProfile()
                        }
                        is DetailsViewModel.ApplyEvent.ApplyError -> {
                            toggleProgressBar(false)
                            snackbar(event.message)
                        }
                        is DetailsViewModel.ApplyEvent.ApplyLoading -> {
                            toggleProgressBar(true)
                        }
                    }
                }
            }
        }
    }

    private fun loadAccountDetails() {
        viewLifecycleOwner.lifecycleScope.launch(viewModel.dispatchersProvider.default) {
            curUsername = dataStoreUtil.getUsername()
            viewModel.loadAccountDetails(curUsername)
        }
    }

    private fun applyChanges(
        realName: String,
        phoneNumber: String,
        email: String
    ) {
        viewModel.applyChanges(
            curUri,
            curUsername,
            realName,
            phoneNumber,
            email
        )
    }

    private fun registerForActivityResult() {
        loadContract = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                curUri = uri
                binding.imgProfileIcon.setImageURI(uri)
            }
        }
    }

    private fun pickImage() {
        loadContract.launch("image/*")
    }

    private fun parseDetails(details: AccountDetailsRequest) {
        binding.etName.setText(details.realName)
        binding.etPhoneNumber.setText(details.phoneNumber)
        binding.etEmail.setText(details.email)

        if(details.profilePicUrl.isNotEmpty() && firstFire) {
            Log.d("MainActivityDebug", "Glide fired")
            Glide.with(this)
                .load(BASE_URL + details.profilePicUrl)
                .signature(ObjectKey(UUID.randomUUID()))
                .into(binding.imgProfileIcon)
            firstFire = false
        }
    }

    private fun navigateToProfile() {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.profileFragment, true)
            .build()

        toggleBottomNav(true)
        findNavController().navigate(R.id.action_detailsFragment_to_profileFragment, null, navOptions)
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

    private fun toggleProgressBar(state: Boolean){
        when(state) {
            false -> {
                binding.progressBar.visibility = View.GONE
            }
            true -> {
                binding.progressBar.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        toggleBottomNav(true)
    }
}