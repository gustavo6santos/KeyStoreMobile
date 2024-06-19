package pt.ipca.keystore.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pt.ipca.keystore.data.CartProduct
import pt.ipca.keystore.databinding.BillingProductsRvItemBinding
import pt.ipca.keystore.databinding.OrderProductsRvitemBinding
import pt.ipca.keystore.helper.getProductPrice

class BillingProductsAdapter : RecyclerView.Adapter<BillingProductsAdapter.BillingProductsViewHolder>() {

    inner class BillingProductsViewHolder(val binding: OrderProductsRvitemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(billingProduct: CartProduct) {
            binding.apply {
                Glide.with(itemView).load(billingProduct.product.images[0]).into(imageCartProduct)
                tvProductCartName.text = billingProduct.product.title
                tvBillingProductQuantity.text = billingProduct.quantity.toString()
                tvGameKeyOrder.text = billingProduct.product.gameKey
                val priceProduct = billingProduct.product.price.getProductPrice(billingProduct.product.price)
                tvProductCartPrice.text = "${String.format("%.2f", priceProduct)} €"
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<CartProduct>() {
        override fun areItemsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean {
            return oldItem.product == newItem.product
        }

        override fun areContentsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillingProductsViewHolder {
        return BillingProductsViewHolder(
            OrderProductsRvitemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: BillingProductsViewHolder, position: Int) {
        val billingProduct = differ.currentList[position]
        holder.bind(billingProduct)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}
