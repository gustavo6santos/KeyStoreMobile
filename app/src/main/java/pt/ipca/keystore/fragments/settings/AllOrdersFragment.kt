package pt.ipca.keystore.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import pt.ipca.keystore.adpters.AllOrdersAdapter
import pt.ipca.keystore.databinding.FragmentOrdersBinding
import pt.ipca.keystore.util.Resource
import pt.ipca.keystore.viewmodel.AllOrdersViewModel

@AndroidEntryPoint
class AllOrdersFragment: Fragment() {

    private lateinit var binding: FragmentOrdersBinding
    val viewModel by viewModels<AllOrdersViewModel>()
    val ordersAdapter by lazy { AllOrdersAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrdersBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupOrdersViewRv()

        lifecycleScope.launchWhenStarted {
            viewModel.allOrders.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.progressbarAllOrders.visibility = View.VISIBLE

                    }
                    is Resource.Success -> {
                        binding.progressbarAllOrders.visibility = View.GONE
                        ordersAdapter.differ.submitList(it.data)
                        if(it.data.isNullOrEmpty()){
                            binding.tvEmptyOrders.visibility = View.INVISIBLE
                        }

                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        binding.progressbarAllOrders.visibility = View.GONE
                    }
                    else -> Unit
                }

            }
        }
        ordersAdapter.onClick = {
            val action = AllOrdersFragmentDirections.actionAllOrdersFragmentToOrderDetailFragment(it)
            findNavController().navigate(action)
        }
    }

    private fun setupOrdersViewRv() {
        binding.rvAllOrders.apply {
            adapter = ordersAdapter
            layoutManager = LinearLayoutManager(requireContext(),RecyclerView.VERTICAL, false)
        }
    }


}