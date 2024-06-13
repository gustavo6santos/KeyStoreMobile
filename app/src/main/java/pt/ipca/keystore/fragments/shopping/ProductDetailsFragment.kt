package pt.ipca.keystore.fragments.shopping

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import pt.ipca.keystore.R
import pt.ipca.keystore.activities.ShoppingActivity
import pt.ipca.keystore.adpters.SpecsAdapter
import pt.ipca.keystore.adpters.ViewPager2Images
import pt.ipca.keystore.data.CartProduct
import pt.ipca.keystore.data.Specs
//import pt.ipca.keystore.data.CartProduct
import pt.ipca.keystore.databinding.FragmentProductDetailsBinding
import pt.ipca.keystore.util.Resource
import pt.ipca.keystore.util.hideBottomNavigationView
import pt.ipca.keystore.viewmodel.DetailsViewModel


@AndroidEntryPoint
class ProductDetailsFragment : Fragment() {
    private val args by navArgs<ProductDetailsFragmentArgs>()
    private lateinit var binding: FragmentProductDetailsBinding
    private val viewPagerAdapter by lazy { ViewPager2Images() }
    private val viewModel by viewModels<DetailsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        hideBottomNavigationView()
        binding = FragmentProductDetailsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val product = args.product

        binding.buttonCheckCompatibility.setOnClickListener {
            val userSpecs = getUserSpecs()
            viewModel.checkCompatibility(userSpecs, product)
        }

        binding.buttonAddToCart.setOnClickListener {
            viewModel.addUpdateProductInCart(CartProduct(product, 1))
        }

        lifecycleScope.launchWhenStarted {
            viewModel.addToCart.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.buttonAddToCart.startAnimation()
                    }
                    is Resource.Success -> {
                        binding.buttonAddToCart.revertAnimation()
                        Toast.makeText(requireContext(), "Product was added", Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Error -> {
                        binding.buttonAddToCart.stopAnimation()
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.checkCompatibility.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        // Mostra um loader ou algo do tipo
                    }
                    is Resource.Success -> {
                        Toast.makeText(requireContext(), it.data, Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

        binding.apply {
            Glide.with(this@ProductDetailsFragment)
                .load(product.images.firstOrNull())
                .into(imageProductDetails)
            tvProductName.text = product.title
            tvProductPrice.text = "$ ${product.price}"
            tvProductDescription.text = product.description
            tvCpuModel.text = product.cpuModel
            tvGpuModel.text = product.gpuModel
            tvRam.text = product.ram?.toString()
            tvOsType.text = product.osType
        }
    }

    private fun getUserSpecs(): Specs {
        return Specs(
            cpuModel = "Intel Core i5-9600K",
            gpuModel = "NVIDIA GTX 1060 6GB",
            ram = 16,
            osType = "Windows 10"
        )
    }
}
