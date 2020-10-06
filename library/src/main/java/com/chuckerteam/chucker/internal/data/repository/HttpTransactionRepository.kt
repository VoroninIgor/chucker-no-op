package com.chuckerteam.chucker.internal.data.repository

import androidx.lifecycle.LiveData
import com.chuckerteam.chucker.internal.data.entity.Transaction
import com.chuckerteam.chucker.internal.data.entity.TransactionTuple

/**
 * Repository Interface representing all the operations that are needed to let Chucker work
 * with [Transaction] and [TransactionTuple]. Please use [HttpTransactionDatabaseRepository] that
 * uses Room and SqLite to run those operations.
 */
internal interface HttpTransactionRepository {

    suspend fun insertTransaction(transaction: Transaction)

    fun updateTransaction(transaction: Transaction): Int

    suspend fun deleteOldTransactions(threshold: Long)

    suspend fun deleteAllTransactions()

    fun getSortedTransactionTuples(): LiveData<List<TransactionTuple>>

    fun getFilteredTransactionTuples(code: String, path: String): LiveData<List<TransactionTuple>>

    fun getTransaction(transactionId: Long): LiveData<Transaction?>

    suspend fun getAllTransactions(): List<Transaction>
}
