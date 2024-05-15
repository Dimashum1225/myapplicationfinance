package com.example.myapplicationfinance15_05

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(private val transactions: List<Transaction>) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewAmount: TextView = itemView.findViewById(R.id.textViewAmount)
        val textViewCategory: TextView = itemView.findViewById(R.id.textViewCategory)
        val textViewDate: TextView = itemView.findViewById(R.id.textViewDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.transaction_item, parent, false)
        return TransactionViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val currentTransaction = transactions[position]
        holder.textViewAmount.text = currentTransaction.amount.toString()
        holder.textViewCategory.text = currentTransaction.category
        holder.textViewDate.text = currentTransaction.date
    }

    override fun getItemCount() = transactions.size
}
