package com.syb.travelsphere.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.syb.travelsphere.MainActivity
import com.syb.travelsphere.R
import com.syb.travelsphere.databinding.FragmentSignInBinding

class SignInFragment : Fragment() {

    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!
    private lateinit var authManager: AuthManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authManager = AuthManager()

        // Sign In Button Click
        binding.signInButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                signIn(email, password)
            } else {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Navigate to Sign Up
        binding.signUpLink.setOnClickListener {
            findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
        }
//        binding.signUpLink.setOnClickListener {
//            val action = SignInFragmentDirections
//                .actionSignInFragmentToSignUpFragment(email = binding.emailEditText.text.toString().trim())
//            findNavController().navigate(action)
//        }

    }

    private fun signIn(email: String, password: String) {
        authManager.signInUser(email, password) { user ->
            if (user != null) {
                Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()
                navigateToMainActivity()
            } else {
                Toast.makeText(requireContext(), "Authentication failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to navigate to MainActivity after login
    private fun navigateToMainActivity() {
        val intent = Intent(requireActivity(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish() // Close AuthActivity so user can't go back
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
