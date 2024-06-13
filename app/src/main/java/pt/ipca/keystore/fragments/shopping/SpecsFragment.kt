package pt.ipca.keystore.fragments.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.squareup.okhttp.Address
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import pt.ipca.keystore.R
import pt.ipca.keystore.data.Specs
import pt.ipca.keystore.databinding.FragmentSpecsBinding
import pt.ipca.keystore.util.Resource
import pt.ipca.keystore.viewmodel.SpecsViewModel

@AndroidEntryPoint
class SpecsFragment: Fragment() {

    private lateinit var binding: FragmentSpecsBinding
    val viewModel by viewModels<SpecsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launchWhenStarted {
            viewModel.addNewSpecs.collectLatest {
                when(it) {
                    is Resource.Loading -> {
                        binding.progressbarAddress.visibility = View.VISIBLE
                    }
                    is Resource.Success ->{
                        binding.progressbarAddress.visibility = View.INVISIBLE
                    }
                    is Resource.Error ->{
                        Toast.makeText(requireContext(),it.message,Toast.LENGTH_SHORT).show()

                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.error.collectLatest {
                Toast.makeText(requireContext(),it,Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSpecsBinding.inflate(inflater)
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.cpu_model_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.cpuModel.adapter = adapter
        }

        // Initialize ArrayAdapter for GPU Model Spinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.gpu_model_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.gpuModel.adapter = adapter
        }

        // Initialize ArrayAdapter for RAM Spinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.ram_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.ramModel.adapter = adapter
        }

        // Initialize ArrayAdapter for OS Type Spinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.os_type_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.osTypeModel.adapter = adapter
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            buttonSave.setOnClickListener{

                // Obter os valores selecionados pelos spinners
                val cpuModelTitle = cpuModel.selectedItem.toString()
                val gpuModelTitle = gpuModel.selectedItem.toString()
                val ramTitle = ramModel.selectedItem.toString().split("GB")[0].trim().toInt() // Converter para Int
                val osTypeTitle = osTypeModel.selectedItem.toString()

                // Criar um objeto Specs com os valores selecionados
                val specs = Specs(cpuModelTitle, gpuModelTitle, ramTitle, osTypeTitle)
                viewModel.addSpecs(specs)
        }
        }
    }

}