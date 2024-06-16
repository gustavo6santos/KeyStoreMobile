package pt.ipca.keystore.fragments.settings

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
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import pt.ipca.keystore.R
import pt.ipca.keystore.adpters.CardAdapter
import pt.ipca.keystore.data.Card
import pt.ipca.keystore.databinding.FragmentPersonalDetailsBinding
import pt.ipca.keystore.viewmodel.ProfileViewModel
import pt.ipca.keystore.util.Resource

@AndroidEntryPoint
class PersonalDetailsFragment : Fragment() {

    private var _binding: FragmentPersonalDetailsBinding? = null
    private val binding get() = _binding!!
    private val cards = mutableListOf<Card>()
    private lateinit var adapter: CardAdapter

    private val viewModel by viewModels<ProfileViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeUser()
        observeCards()
        observeAddCardResult()
        observeRemoveCardResult()
        observeUpdateNameResult()

        binding.buttonNavigateToBilling.setOnClickListener {
            findNavController().navigate(R.id.action_personalDetailsFragment_to_billingAddressFragment)
        }

        binding.buttonAddPayment.setOnClickListener {
            showAddCardDialog()
        }

        binding.buttonEditName.setOnClickListener {
            val newName = binding.editTextName.text.toString()
            if (newName.isNotEmpty()) {
                viewModel.updateUserName(newName)
            } else {
                Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = CardAdapter(cards) { card ->
            removeCard(card)
        }
        binding.recyclerViewPaymentOptions.adapter = adapter
        binding.recyclerViewPaymentOptions.layoutManager = LinearLayoutManager(context)
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
                viewModel.addCard(name, cardNumber, expiryDate, cvc)
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }



    private fun removeCard(card: Card) {
        viewModel.removeCard(card)
    }

    private fun observeUser() {
        lifecycleScope.launchWhenStarted {
            viewModel.user.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Show loading indicator if needed
                    }
                    is Resource.Success -> {
                        val user = resource.data
                        if (user != null) {
                            binding.personalDetailsEmail.text = user.email
                            binding.editTextName.setText(user.username)
                        }
                    }
                    is Resource.Error -> {
                        Toast.makeText(context, "Error: ${resource.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun observeCards() {
        lifecycleScope.launchWhenStarted {
            viewModel.cards.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Show loading indicator if needed
                    }
                    is Resource.Success -> {
                        cards.clear()
                        cards.addAll(resource.data ?: emptyList())
                        adapter.notifyDataSetChanged()
                    }
                    is Resource.Error -> {
                        Toast.makeText(context, "Error: ${resource.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun observeAddCardResult() {
        lifecycleScope.launchWhenStarted {
            viewModel.addCardResult.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Show loading indicator if needed
                    }
                    is Resource.Success -> {
                        Toast.makeText(context, "Card added successfully", Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Error -> {
                        Toast.makeText(context, "Error: ${resource.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun observeRemoveCardResult() {
        lifecycleScope.launchWhenStarted {
            viewModel.removeCardResult.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Show loading indicator if needed
                    }
                    is Resource.Success -> {
                        Toast.makeText(context, "Card removed successfully", Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Error -> {
                        Toast.makeText(context, "Error: ${resource.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun observeUpdateNameResult() {
        lifecycleScope.launchWhenStarted {
            viewModel.updateNameResult.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Show loading indicator if needed
                    }
                    is Resource.Success -> {
                        Toast.makeText(context, "Name updated successfully", Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Error -> {
                        Toast.makeText(context, "Error: ${resource.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
