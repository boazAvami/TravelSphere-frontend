package com.syb.travelsphere.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.syb.travelsphere.MainActivity
import com.syb.travelsphere.R
import com.syb.travelsphere.databinding.FragmentSignInBinding
import com.syb.travelsphere.utils.InputValidator

class SignInFragment : Fragment() {

    private var binding: FragmentSignInBinding? = null
    private lateinit var authManager: AuthManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignInBinding.inflate(inflater, container, false)

        // Get email input from signUp page
        val email =  arguments?.let {
            SignInFragmentArgs.fromBundle(it).email
        }
        binding?.emailEditText?.setText(email ?: "")

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authManager = AuthManager()

        binding?.signInButton?.setOnClickListener {
            val email = binding?.emailEditText?.text.toString().trim()
            val password = binding?.passwordEditText?.text.toString().trim()

            val isValid = validateInputs()
            // Proceed with Sign-In if Input is Valid
            if (isValid) {
                signIn(email, password)
            }
        }

        // Navigate to Sign Up
        binding?.signUpLink?.setOnClickListener {
            val email = binding?.emailEditText?.text.toString().trim()
            val action = SignInFragmentDirections.actionSignInFragmentToSignUpFragment().apply {
                this.email = email
            }
            findNavController().navigate(action)
        }
    }

    private fun validateInputs(): Boolean {
        val email = binding?.emailEditText?.text.toString().trim()
        val password = binding?.passwordEditText?.text.toString().trim()

        var isValid = true

        if (!InputValidator.validateEmail(email, binding?.emailInputLayout)) {
            isValid = false
        }
        if (!InputValidator.validatePassword(password, binding?.passwordInputLayout)) {
            isValid = false
        }

        return isValid
    }


    private fun signIn(email: String, password: String) {
        binding?.progressBar?.visibility = View.VISIBLE
        authManager.signInUser(email, password) { user ->
            if (user != null) {
                Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()
                navigateToMainActivity()
            } else {
                Toast.makeText(requireContext(), "Authentication failed. Please check your credentials.", Toast.LENGTH_SHORT).show()
            }

            binding?.progressBar?.visibility = View.GONE
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(requireActivity(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish() // Close AuthActivity
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
