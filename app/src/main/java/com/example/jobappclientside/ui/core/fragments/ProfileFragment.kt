package com.example.jobappclientside.ui.core.fragments

import android.content.Intent
import android.os.Bundle
import android.os.FileUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.jobappclientside.R
import com.example.jobappclientside.databinding.FragmentProfileBinding
import com.example.jobappclientside.databinding.FragmentSearchBinding
import com.example.jobappclientside.other.DataStoreUtil
import com.example.jobappclientside.other.DispatchersProvider
import com.example.jobappclientside.ui.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment: Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var dataStoreUtils: DataStoreUtil

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvCreateJobPost.setOnClickListener {
            navigateToCreateJobFragment()
        }

        binding.tvAccountDetails.setOnClickListener {
            navigateToAccountDetailsFragment()
        }

        binding.tvJobPostings.setOnClickListener {
            navigateToJobPostingsFragment()
        }

        binding.tvLogout.setOnClickListener {
            navigateToLoginScreen()
        }
    }

    private fun navigateToCreateJobFragment() {
        findNavController().navigate(R.id.action_profileFragment_to_createJobFragment, null, null)
    }

    private fun navigateToAccountDetailsFragment() {
        findNavController().navigate(R.id.action_profileFragment_to_detailsFragment)
    }

    private fun navigateToJobPostingsFragment() {
        findNavController().navigate(R.id.action_profileFragment_to_postedJobsFragment)
    }

    private fun navigateToLoginScreen() {
        viewLifecycleOwner.lifecycleScope.launch(DispatchersProvider().main) {
            dataStoreUtils.logUserOut()

            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}