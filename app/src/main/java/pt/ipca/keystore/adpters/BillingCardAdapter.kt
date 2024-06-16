package pt.ipca.keystore.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pt.ipca.keystore.R
import pt.ipca.keystore.data.Card

class BillingCardAdapter(
    private val cards: MutableList<Card>,
    private val onSelectCard: (Card) -> Unit
) : RecyclerView.Adapter<BillingCardAdapter.CardViewHolder>() {

    private var selectedPosition: Int = -1

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.cardName)
        private val detailsTextView: TextView = itemView.findViewById(R.id.cardNumber)
        private val selectButton: Button = itemView.findViewById(R.id.buttonSelect)

        fun bind(card: Card, isSelected: Boolean) {
            nameTextView.text = card.name
            detailsTextView.text = "**** **** **** ${card.cardNumber.takeLast(4)}"
            itemView.isSelected = isSelected

            selectButton.setOnClickListener {
                onSelectCard(card)
                val previousSelectedPosition = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(previousSelectedPosition)
                notifyItemChanged(selectedPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.billing_card_item, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(cards[position], position == selectedPosition)
    }

    override fun getItemCount() = cards.size
}
