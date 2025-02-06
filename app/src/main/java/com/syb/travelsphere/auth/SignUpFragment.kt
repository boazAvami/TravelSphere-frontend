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
import com.syb.travelsphere.databinding.FragmentSignUpBinding

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private lateinit var authManager: AuthManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authManager = AuthManager()

        // Sign Up Button Click
        binding.signUpButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(requireContext(), "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            signUp(email, password)
        }

//        binding.signUpButton.setOnClickListener {
//            val email = binding.emailEditText.text.toString().trim()
//            val password = binding.passwordEditText.text.toString().trim()
//
//            if (email.isNotEmpty() && password.isNotEmpty()) {
//                signUp(email, password)
//            } else {
//                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
//            }
//        }

        // Navigate to Sign In
        binding.signInLink.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun signUp(email: String, password: String) {
        authManager.signUpUser(email, password) { user ->
            if (user != null) {
                Toast.makeText(requireContext(), "Sign up successful!", Toast.LENGTH_SHORT).show()
                navigateToMainActivity()
            } else {
                Toast.makeText(requireContext(), "Sign up failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(requireActivity(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
