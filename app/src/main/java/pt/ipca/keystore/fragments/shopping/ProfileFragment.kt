package pt.ipca.keystore.fragments.shopping

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import pt.ipca.keystore.R
import pt.ipca.keystore.activities.LoginRegisterActivity
import pt.ipca.keystore.databinding.FragmentBillingBinding
import pt.ipca.keystore.databinding.FragmentOrderDetailBinding
import pt.ipca.keystore.databinding.FragmentProfileBinding
import pt.ipca.keystore.util.Resource
import pt.ipca.keystore.util.showBottomNavigationView
import pt.ipca.keystore.viewmodel.ProfileViewModel
@AndroidEntryPoint
class ProfileFragment: Fragment() {
    private lateinit var binding: FragmentProfileBinding
    val viewModel by viewModels<ProfileViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.linearAllOrders.setOnClickListener{
            findNavController().navigate(R.id.action_profileFragment_to_allOrdersFragment)
        }

        binding.specs.setOnClickListener{
            findNavController().navigate(R.id.action_profileFragment_to_specsFragment)
        }

        binding.linearLogOut.setOnClickListener{
            viewModel.logout()
            val intent = Intent(requireActivity(), LoginRegisterActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        lifecycleScope.launchWhenStarted {
            viewModel.user.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.progressbarSettings.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        binding.progressbarSettings.visibility = View.GONE
                        binding.tvUserName.text = "${it.data?.username}"
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        binding.progressbarSettings.visibility = View.GONE
                    }
                    else -> Unit
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        showBottomNavigationView()
    }
}

