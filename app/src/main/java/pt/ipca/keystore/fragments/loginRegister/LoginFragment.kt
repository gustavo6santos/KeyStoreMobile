package pt.ipca.keystore.fragments.loginRegister

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import pt.ipca.keystore.R
import pt.ipca.keystore.activities.ShoppingActivity
import pt.ipca.keystore.databinding.FragmentLoginBinding
import pt.ipca.keystore.util.Resource
import pt.ipca.keystore.viewmodel.LoginViewModel

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {
    private lateinit var binding: FragmentLoginBinding
    private val viewModel by viewModels<LoginViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cancelButtonLogin.setOnClickListener{
            findNavController().navigate(R.id.action_accountOptions_to_registerFragment)
        }

        binding.fypButton.setOnClickListener {
            showResetPasswordDialog()
        }


        binding.apply {
            buttonLogin.setOnClickListener {
                val email = EmailAddress.text.toString().trim()
                val password = Password.text.toString()
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    viewModel.login(email, password)
                    Log.d("LoginFragment", "Attempting to log in with email: $email")
                } else {
                    Snackbar.make(requireView(), "Email and Password cannot be empty", Snackbar.LENGTH_LONG).show()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.login.collect {
                when (it) {
                    is Resource.Loading -> {
                        if (isAdded) {  // Check if the fragment is added to its activity
                            binding.buttonLogin.startAnimation()
                        }
                    }
                    is Resource.Success -> {
                        if (isAdded) {
                            binding.buttonLogin.revertAnimation()
                            try {
                                Intent(requireActivity(), ShoppingActivity::class.java).also { intent ->
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                }
                            } catch (e: Exception) {
                                Log.e("LoginFragment", "Error starting ShoppingActivity", e)
                                Snackbar.make(requireView(), "Failed to start ShoppingActivity", Snackbar.LENGTH_LONG).show()
                            }
                        }
                    }
                    is Resource.Error -> {
                        if (isAdded) {
                            Log.e("LoginFragment", "Login error: ${it.message}")
                            Snackbar.make(requireView(), "Error: ${it.message}", Snackbar.LENGTH_LONG).show()
                            binding.buttonLogin.revertAnimation()
                        }
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun showResetPasswordDialog() {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.dialog_reset_password, null)
        val emailInput = view.findViewById<EditText>(R.id.emailEditText)
        val cancelButton = view.findViewById<Button>(R.id.cancelButton)
        val submitButton = view.findViewById<Button>(R.id.submitButton)

        val dialog = AlertDialog.Builder(context)
            .setView(view)
            .create()

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        submitButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            if (email.isNotEmpty()) {
                viewModel.sendPasswordResetEmail(email)
                dialog.dismiss()
            } else {
                Snackbar.make(requireView(), "Email cannot be empty", Snackbar.LENGTH_LONG).show()
            }
        }

        dialog.show()
    }
}
