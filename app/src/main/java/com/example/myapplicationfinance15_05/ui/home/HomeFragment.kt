package com.example.myapplicationfinance15_05.ui.home


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicationfinance15_05.FinanceDBHelper
import com.example.myapplicationfinance15_05.TransactionAdapter
import com.example.myapplicationfinance15_05.R
import com.example.myapplicationfinance15_05.Transaction
import com.example.myapplicationfinance15_05.databinding.FragmentHomeBinding

import java.text.DateFormat
import java.util.Calendar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.button.setOnClickListener {
            showAddTransactionDialog(requireContext(), "расход")
        }

        binding.button2.setOnClickListener {
            showAddTransactionDialog(requireContext(), "доход")
        }

        displayCurrentBalance(requireContext())

        val recyclerView: RecyclerView = binding.recyclerViewTransactions
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager

        val itemDecoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        ContextCompat.getDrawable(requireContext(), R.drawable.divider)?.let {
            itemDecoration.setDrawable(
                it
            )
        }
        recyclerView.addItemDecoration(itemDecoration)

        val adapter = TransactionAdapter(getLastTransactions(requireContext()))
        recyclerView.adapter = adapter

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getLastTransactions(context: Context): List<Transaction> {
        val dbHelper = FinanceDBHelper(context)
        return dbHelper.getLastTransactions(10)
    }

    private fun displayCurrentBalance(context: Context) {
        val dbHelper = FinanceDBHelper(context)
        val currentBalance = dbHelper.getCurrentBalance()
        binding.balanceTextView.text = "Текущий баланс: $currentBalance"
    }

    private fun showAddTransactionDialog(context: Context, type: String) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_add_transaction)

        val editTextAmount: EditText = dialog.findViewById(R.id.editTextAmount)
        val spinner: Spinner = dialog.findViewById(R.id.spinner)
        val items = context.resources.getStringArray(if (type == "расход") R.array.expense_transaction_items else R.array.income_transaction_items)
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, items)
        spinner.adapter = adapter

        val editTextDate: EditText = dialog.findViewById(R.id.editTextDate)
        val calendar = Calendar.getInstance()
        val currentDate = DateFormat.getDateInstance().format(calendar.time)
        editTextDate.setText(currentDate)

        val buttonAddTransaction: Button = dialog.findViewById(R.id.buttonAddTransaction)
        buttonAddTransaction.setOnClickListener {
            val amount = editTextAmount.text.toString().toDouble()
            val category = spinner.selectedItem.toString()
            val date = editTextDate.text.toString()

            val dbHelper = FinanceDBHelper(context)
            dbHelper.addTransaction(amount, category, date, type)

            dialog.dismiss()
            displayCurrentBalance(context)
        }

        dialog.show()
    }
}
