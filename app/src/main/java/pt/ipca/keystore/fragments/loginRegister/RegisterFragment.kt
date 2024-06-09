package pt.ipca.keystore.fragments.loginRegister

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pt.ipca.keystore.R
import pt.ipca.keystore.data.User
import pt.ipca.keystore.databinding.FragmentRegisterBinding
import pt.ipca.keystore.util.RegisterValidation
import pt.ipca.keystore.viewmodel.RegisterViewModel
import pt.ipca.keystore.util.Resource

private val TAG = "RegisterFragment"
@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            buttonCreateAccount.setOnClickListener {
                val user = User(
                    Username.text.toString().trim(),
                    EmailAddress.text.toString().trim()

                )
                val password = Password.text.toString()

                viewModel.createAccount(user, password)
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.register.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.buttonCreateAccount.startAnimation()
                    }
                    is Resource.Success -> {
                        Log.d("test", resource.data.toString())
                        binding.buttonCreateAccount.revertAnimation()
                    }
                    is Resource.Error -> {
                        Log.e(TAG, resource.message.toString())
                        binding.buttonCreateAccount.revertAnimation()
                    }
                    else -> Unit
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.validation.collect{
                validation ->
                if(validation.EmailAdress is  RegisterValidation.Failed) {
                    withContext(Dispatchers.Main){
                        binding.EmailAddress.apply {
                            requestFocus()
                            error = validation.EmailAdress.message
                        }
                    }
                }
                if (validation.Password is RegisterValidation.Failed){
                    withContext(Dispatchers.Main){
                        binding.Password.apply {
                            requestFocus()
                            error = validation.Password.message
                        }
                    }
                }
            }
        }
    }
}
