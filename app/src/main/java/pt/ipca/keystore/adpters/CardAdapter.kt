package pt.ipca.keystore.adpters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pt.ipca.keystore.R
import pt.ipca.keystore.data.Card

class CardAdapter(private val cards: MutableList<Card>, private val onRemoveClick: (Card) -> Unit) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    class CardViewHolder(itemView: View, private val onRemoveClick: (Card) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.cardName)
        private val detailsTextView: TextView = itemView.findViewById(R.id.cardNumber)
        private val removeButton: Button = itemView.findViewById(R.id.buttonRemoveCard)

        fun bind(card: Card) {
            nameTextView.text = card.name
            detailsTextView.text = "**** **** **** ${card.cardNumber.takeLast(4)}"
            removeButton.setOnClickListener {
                onRemoveClick(card)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_item, parent, false)
        return CardViewHolder(view, onRemoveClick)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(cards[position])
    }

    override fun getItemCount() = cards.size
}
