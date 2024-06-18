package pt.ipca.keystore.fragments.shopping

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import pt.ipca.keystore.R
import pt.ipca.keystore.adapters.BillingProductsAdapter
import pt.ipca.keystore.adapters.BillingCardAdapter
import pt.ipca.keystore.data.Card
import pt.ipca.keystore.data.CartProduct
import pt.ipca.keystore.data.Order
import pt.ipca.keystore.databinding.FragmentBillingBinding
import pt.ipca.keystore.util.HorizontalItemDecoration
import pt.ipca.keystore.util.Resource
import pt.ipca.keystore.viewmodel.BillingViewModel
import pt.ipca.keystore.viewmodel.OrderViewModel

@AndroidEntryPoint
class BillingFragment : Fragment() {
    private lateinit var binding: FragmentBillingBinding
    private val billingProductsAdapter by lazy { BillingProductsAdapter() }
    private val billingViewModel by viewModels<BillingViewModel>()
    private val args by navArgs<BillingFragmentArgs>()
    private var products = emptyList<CartProduct>()
    private var totalPrice = 0f
    private val orderViewModel by viewModels<OrderViewModel>()
    private val cards = mutableListOf<Card>()
    private lateinit var cardAdapter: BillingCardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        products = args.products.toList()
        totalPrice = args.totalPrice
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBillingBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBillingProductsRv()
        setupCardRecyclerView()

        binding.buttonAddPayment.setOnClickListener {
            showAddCardDialog()
        }

        lifecycleScope.launchWhenStarted {
            billingProductsAdapter.differ.submitList(products)

            binding.tvTotalPrice.text = "$totalPrice €"

            binding.buttonPlaceOrder.setOnClickListener {
                showOrderConfirmationDialog()
            }
        }

        lifecycleScope.launchWhenStarted {
            orderViewModel.order.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.buttonPlaceOrder.startAnimation()
                    }

                    is Resource.Success -> {
                        binding.buttonPlaceOrder.revertAnimation()
                        findNavController().navigate(R.id.homeFragment)
                        Snackbar.make(requireView(), "Your order was placed", Snackbar.LENGTH_LONG).show()
                    }

                    is Resource.Error -> {
                        binding.buttonPlaceOrder.revertAnimation()
                        Toast.makeText(requireContext(), "Error ${it.message}", Toast.LENGTH_SHORT)
                            .show()
                    }

                    else -> Unit
                }
            }
        }

        billingProductsAdapter.differ.submitList(products)

        // Fetch the payment options from the view model
        billingViewModel.getCards()
        observeCards()
    }

    private fun setupBillingProductsRv() {
        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = billingProductsAdapter
            addItemDecoration(HorizontalItemDecoration())
        }
    }

    private fun setupCardRecyclerView() {
        cardAdapter = BillingCardAdapter(cards) { card ->
            // Handle card selection
        }
        binding.recyclerViewPaymentOptions.apply {
            adapter = cardAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun showAddCardDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_card, null)
        val editTextName = dialogView.findViewById<EditText>(R.id.editTextName)
        val editTextCardNumber = dialogView.findViewById<EditText>(R.id.editTextCardNumber)
        val editTextExpiryDate = dialogView.findViewById<EditText>(R.id.editTextExpiryDate)
        val editTextCvc = dialogView.findViewById<EditText>(R.id.editTextCvc)
        val buttonCancel = dialogView.findViewById<Button>(R.id.buttonCancel)
        val buttonAdd = dialogView.findViewById<Button>(R.id.buttonAdd)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setTitle(null) // Remove default title
            .create()

        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        buttonAdd.setOnClickListener {
            val name = editTextName.text.toString()
            val cardNumber = editTextCardNumber.text.toString()
            val expiryDate = editTextExpiryDate.text.toString()
            val cvc = editTextCvc.text.toString()

            if (name.isNotEmpty() && cardNumber.isNotEmpty() && expiryDate.isNotEmpty() && cvc.isNotEmpty()) {
                billingViewModel.addCard(name, cardNumber, expiryDate, cvc)
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun observeCards() {
        lifecycleScope.launchWhenStarted {
            billingViewModel.cards.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {

                    }
                    is Resource.Success -> {
                        cards.clear()
                        cards.addAll(resource.data ?: emptyList())
                        cardAdapter.notifyDataSetChanged()
                    }
                    is Resource.Error -> {
                        Toast.makeText(context, "Error: ${resource.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun showOrderConfirmationDialog() {
        val alertDialog = AlertDialog.Builder(requireContext()).apply {
            setTitle("Order Items")
            setMessage("Do you want to finish the order?")
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            setPositiveButton("Yes") { dialog, _ ->
                val order = Order(
                    totalPrice,
                    products
                )
                orderViewModel.placeOrder(order)
                dialog.dismiss()
            }
        }
        alertDialog.create()
        alertDialog.show()
    }
}
