package com.example.jobappclientside.ui.login.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.jobappclientside.R
import com.example.jobappclientside.databinding.FragmentRegisterBinding
import com.example.jobappclientside.other.snackbar
import com.example.jobappclientside.ui.login.viewmodels.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment: Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToUiObservers()

        binding.apply {
            btnRegister.setOnClickListener {
                val username = etRegisterUsername.text.toString()
                val password = etRegisterPassword.text.toString()
                val confirmedPassword = etRegisterPasswordRepeat.text.toString()

                viewModel.registerUser(username, password, confirmedPassword)
            }
        }
    }

    private fun subscribeToUiObservers() {
        lifecycleScope.launch(viewModel.dispatchersProvider.main) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginEvents.collect { event ->
                    when(event) {
                        is LoginViewModel.LoginEvent.LoginLoading -> {
                            toggleProgressBar(true)
                        }
                        is LoginViewModel.LoginEvent.LoginError -> {
                            snackbar(event.message)
                            toggleProgressBar(false)
                        }
                        is LoginViewModel.LoginEvent.LoginSuccess -> {
                            snackbar(event.message)
                            toggleProgressBar(false)
                            navigateToLogin()
                        }
                    }
                }
            }
        }
    }

    private fun toggleProgressBar(value: Boolean) {
        when(value) {
            true -> {
                binding.progressBar.visibility = View.VISIBLE
            }
            false -> {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun navigateToLogin() {
        findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}