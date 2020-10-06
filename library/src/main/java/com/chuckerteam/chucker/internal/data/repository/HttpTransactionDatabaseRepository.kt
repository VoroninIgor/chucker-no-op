package com.chuckerteam.chucker.internal.data.repository

import androidx.lifecycle.LiveData
import com.chuckerteam.chucker.internal.data.entity.Transaction
import com.chuckerteam.chucker.internal.data.entity.TransactionTuple
import com.chuckerteam.chucker.internal.data.room.ChuckerDatabase
import com.chuckerteam.chucker.internal.support.distinctUntilChanged

internal class HttpTransactionDatabaseRepository(private val database: ChuckerDatabase) : HttpTransactionRepository {

    private val transactionDao get() = database.transactionDao()

    override fun getFilteredTransactionTuples(code: String, path: String): LiveData<List<TransactionTuple>> {
        val pathQuery = if (path.isNotEmpty()) "%$path%" else "%"
        return transactionDao.getFilteredTuples("$code%", pathQuery)
    }

    override fun getTransaction(transactionId: Long): LiveData<Transaction?> {
        return transactionDao.getById(transactionId)
            .distinctUntilChanged { old, new -> old?.hasTheSameContent(new) != false }
    }

    override fun getSortedTransactionTuples(): LiveData<List<TransactionTuple>> {
        return transactionDao.getSortedTuples()
    }

    override suspend fun deleteAllTransactions() {
        transactionDao.deleteAll()
    }

    override suspend fun insertTransaction(transaction: Transaction) {
        val id = transactionDao.insert(transaction)
        transaction.id = id ?: 0
    }

    override fun updateTransaction(transaction: Transaction): Int {
        return transactionDao.update(transaction)
    }

    override suspend fun deleteOldTransactions(threshold: Long) {
        transactionDao.deleteBefore(threshold)
    }

    override suspend fun getAllTransactions(): List<Transaction> = transactionDao.getAll()
}
