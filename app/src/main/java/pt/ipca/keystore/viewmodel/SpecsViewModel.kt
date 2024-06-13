package pt.ipca.keystore.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.ipca.keystore.data.Specs
import pt.ipca.keystore.util.Resource
import javax.inject.Inject
@HiltViewModel
class SpecsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth

)
    :ViewModel() {

        private val _addNewSpecs = MutableStateFlow<Resource<Specs>>(Resource.Unspecified())
        val addNewSpecs = _addNewSpecs.asStateFlow()

        private val _error = MutableSharedFlow<String>()
        val error = _error.asSharedFlow()

        fun addSpecs(specs: Specs){
            val validateInputs = validadeInputs(specs)
            if(validateInputs) {
                viewModelScope.launch {
                    _addNewSpecs.emit(Resource.Loading())
                }
                firestore.collection("user").document(auth.uid!!).collection("specs").document()
                    .set(specs).addOnSuccessListener {
                        viewModelScope.launch { _addNewSpecs.emit(Resource.Success(specs))}

                    }.addOnFailureListener {
                        viewModelScope.launch { _addNewSpecs.emit(Resource.Error(it.message.toString())) }

                    }
            }else{
                viewModelScope.launch{
                    _error.emit("All fields are required")
                }
            }
        }

    private fun validadeInputs(specs: Specs): Boolean {
        return specs.cpuModel.trim().isNotEmpty() &&
                specs.gpuModel.trim().isNotEmpty() &&
                specs.osType.trim().isNotEmpty() &&
                specs.ram != 0

    }
}