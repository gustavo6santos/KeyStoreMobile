package pt.ipca.keystore.adpters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.AsyncListUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import pt.ipca.keystore.data.Product

import pt.ipca.keystore.databinding.SpecsRvItemBinding

class SpecsAdapter: RecyclerView.Adapter<SpecsAdapter.SpecsViewHolder>() {
    inner class SpecsViewHolder(private val binding: SpecsRvItemBinding):
        ViewHolder(binding.root){
            fun bind(product: Product){
                binding.apply {
                    tvCpuModel.text = product.cpuModel
                    tvGpuModel.text = product.gpuModel
                    tvRam.text = product.ram.toString()
                    tvOsType.text = product.osType
                }

            }
        }

    private val diffCallback = object : DiffUtil.ItemCallback<Int>(){
        override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this,diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpecsViewHolder {
        return SpecsViewHolder(
            SpecsRvItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun onBindViewHolder(holder: SpecsViewHolder, position: Int) {
        val specs = differ.currentList[position]
        holder.bind(Product())
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }
}