package com.example.jobappclientside.ui.login.fragments
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.jobappclientside.R
import com.example.jobappclientside.databinding.FragmentLoginBinding
import com.example.jobappclientside.other.DataStoreUtil
import com.example.jobappclientside.other.snackbar
import com.example.jobappclientside.ui.core.MainActivity
import com.example.jobappclientside.ui.login.viewmodels.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment: Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    @Inject
    lateinit var dataStoreUtil: DataStoreUtil

    private lateinit var curUsername: String
    private lateinit var curPassword: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        redirectLogin()
        subscribeToUiObservers()

        binding.apply {
            btnLoginButton.setOnClickListener {
                curUsername = etLoginUsername.text.toString()
                curPassword = etLoginPassword.text.toString()
                viewModel.logUser(curUsername, curPassword)
            }

            tvCreateAccount.setOnClickListener {
                navigateToRegistration()
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
                            saveCredentials()
                            navigateToMainActivity()
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

    private fun navigateToRegistration() {
        findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
    }

    private fun saveCredentials() {
        lifecycleScope.launch(viewModel.dispatchersProvider.default) {
            dataStoreUtil.storeCredentials(curUsername, curPassword)
        }
    }

    private fun redirectLogin() {
        lifecycleScope.launch(viewModel.dispatchersProvider.default) {
            val shouldRedirect = dataStoreUtil.isUserLoggedIn()
            if(shouldRedirect) {
                navigateToMainActivity()
            }
        }
    }

    private fun navigateToMainActivity() {
       /* val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.loginFragment, true)
            .build()
        findNavController().navigate(R.id.action_loginFragment_to_mainActivity, args = null, navOptions = navOptions)
        */
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}