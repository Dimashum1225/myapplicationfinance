package com.example.myapplicationfinance15_05

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.Date

class FinanceDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "FinanceDB"

        private const val TABLE_TRANSACTIONS = "Transactions"
        private const val TABLE_BALANCE = "Balance"

        private const val KEY_ID = "id"
        private const val KEY_AMOUNT = "amount"
        private const val KEY_CATEGORY = "category"
        private const val KEY_DATE = "date"
        private const val KEY_TYPE = "type"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTransactionsTable = ("CREATE TABLE $TABLE_TRANSACTIONS($KEY_ID INTEGER PRIMARY KEY, $KEY_AMOUNT REAL, $KEY_CATEGORY TEXT, $KEY_DATE TEXT, $KEY_TYPE TEXT)")
        db.execSQL(createTransactionsTable)

        val createBalanceTable = ("CREATE TABLE $TABLE_BALANCE($KEY_ID INTEGER PRIMARY KEY, $KEY_AMOUNT REAL)")
        db.execSQL(createBalanceTable)

        // Инициализируем таблицу баланса начальным значением
        val initialValues = ContentValues().apply {
            put(KEY_AMOUNT, 0.0)
        }
        db.insert(TABLE_BALANCE, null, initialValues)
    }
    @SuppressLint("Range")
    fun getExpenses(): List<Transaction> {
        val expenses = mutableListOf<Transaction>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_TRANSACTIONS WHERE $KEY_TYPE = 'расход'", null)
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val amount = cursor.getDouble(cursor.getColumnIndex(KEY_AMOUNT))
                val category = cursor.getString(cursor.getColumnIndex(KEY_CATEGORY))
                val date = cursor.getString(cursor.getColumnIndex(KEY_DATE))
                expenses.add(Transaction(amount, category, date))
            }
            cursor.close()
        }
        return expenses
    }

    // Метод для получения данных о доходах
    @SuppressLint("Range")
    fun getIncomes(): List<Transaction> {
        val incomes = mutableListOf<Transaction>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM TABLE_ $TABLE_TRANSACTIONS WHERE $KEY_TYPE = 'доход'", null)
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val amount = cursor.getDouble(cursor.getColumnIndex(KEY_AMOUNT))
                val category = cursor.getString(cursor.getColumnIndex(KEY_CATEGORY))
                val date = cursor.getString(cursor.getColumnIndex(KEY_DATE))
                incomes.add(Transaction(amount, category, date))
            }
            cursor.close()
        }
        return incomes
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BALANCE")
        onCreate(db)
    }

    fun addTransaction(amount: Double, category: String, date: String, type: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_AMOUNT, amount)
            put(KEY_CATEGORY, category)
            put(KEY_DATE, date)
            put(KEY_TYPE, type)
        }
        db.insert(TABLE_TRANSACTIONS, null, values)
        db.close()

        // Обновляем баланс после добавления транзакции
        updateBalance()

    }

    private fun updateBalance() {
        val db = this.writableDatabase

        // Считаем сумму всех расходов
        val expenseQuery = "SELECT SUM($KEY_AMOUNT) FROM $TABLE_TRANSACTIONS WHERE $KEY_TYPE = 'расход'"
        val expenseCursor = db.rawQuery(expenseQuery, null)
        var expenses = 0.0
        if (expenseCursor.moveToFirst()) {
            expenses = expenseCursor.getDouble(0)
        }
        expenseCursor.close()

        // Считаем сумму всех доходов
        val incomeQuery = "SELECT SUM($KEY_AMOUNT) FROM $TABLE_TRANSACTIONS WHERE $KEY_TYPE = 'доход'"
        val incomeCursor = db.rawQuery(incomeQuery, null)
        var income = 0.0
        if (incomeCursor.moveToFirst()) {
            income = incomeCursor.getDouble(0)
        }
        incomeCursor.close()

        // Вычисляем текущий баланс
        val currentBalance = income - expenses

        // Обновляем значение баланса в таблице
        val values = ContentValues().apply {
            put(KEY_AMOUNT, currentBalance)
        }
        db.update(TABLE_BALANCE, values, "$KEY_ID = ?", arrayOf("1"))

        db.close()
    }

    fun getCurrentBalance(): Double {
        val db = this.readableDatabase

        val balanceQuery = "SELECT $KEY_AMOUNT FROM $TABLE_BALANCE WHERE $KEY_ID = 1"
        val cursor = db.rawQuery(balanceQuery, null)
        var balance = 0.0
        if (cursor.moveToFirst()) {
            balance = cursor.getDouble(0)
        }
        cursor.close()
        db.close()

        return balance
    }
    fun getLastTransactions(limit: Int): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_TRANSACTIONS,
            arrayOf(KEY_AMOUNT, KEY_CATEGORY, KEY_DATE),
            null,
            null,
            null,
            null,
            "$KEY_ID DESC",
            "$limit"
        )
        with(cursor) {
            while (moveToNext()) {
                val amount = getDouble(getColumnIndexOrThrow(KEY_AMOUNT))
                val category = getString(getColumnIndexOrThrow(KEY_CATEGORY))
                val date = getString(getColumnIndexOrThrow(KEY_DATE))
                transactions.add(Transaction(amount, category, date))
            }
        }
        cursor.close()
        return transactions
    }

    fun getTransactionsSum(startDate: Date, endDate: Date, type: String): Double {
        var sum = 0.0
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM($KEY_AMOUNT) FROM $TABLE_TRANSACTIONS WHERE $KEY_DATE BETWEEN ? AND ? AND $KEY_TYPE = ?",
            arrayOf(startDate.time.toString(), endDate.time.toString(), type)
        )
        if (cursor.moveToFirst()) {
            sum = cursor.getDouble(0)
        }
        cursor.close()
        return sum
    }

}
data class Transaction(val amount: Double, val category: String, val date: String)


