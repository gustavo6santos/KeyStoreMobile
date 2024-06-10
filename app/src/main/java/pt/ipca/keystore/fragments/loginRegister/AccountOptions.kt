package pt.ipca.keystore.fragments.loginRegister

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import pt.ipca.keystore.R
import pt.ipca.keystore.databinding.FragmentAccountOptionsBinding

class AccountOptions : Fragment(R.layout.fragment_account_options) {
    private lateinit var binding: FragmentAccountOptionsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment using the binding object
        binding = FragmentAccountOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up click listeners for login and register buttons
        binding.loginAccountOptions.setOnClickListener {
            findNavController().navigate(R.id.action_accountOptions_to_loginFragment)
        }

        binding.registerAccountOptions.setOnClickListener {
            findNavController().navigate(R.id.action_accountOptions_to_registerFragment)
        }
    }
}
