package com.chuckerteam.chucker.internal.data.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.chuckerteam.chucker.internal.data.entity.Transaction
import com.chuckerteam.chucker.internal.data.entity.TransactionTuple

@Dao
internal interface HttpTransactionDao {

    @Query(
        "SELECT id, requestDate, tookMs, method, responseCode, error FROM " +
            "transactions ORDER BY requestDate DESC"
    )
    fun getSortedTuples(): LiveData<List<TransactionTuple>>

    @Query(
        "SELECT id, requestDate, tookMs, method, responseCode, error FROM " +
            "transactions WHERE responseCode LIKE :codeQuery LIKE :pathQuery " +
            "ORDER BY requestDate DESC"
    )
    fun getFilteredTuples(codeQuery: String, pathQuery: String): LiveData<List<TransactionTuple>>

    @Insert
    suspend fun insert(transaction: Transaction): Long?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(transaction: Transaction): Int

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getById(id: Long): LiveData<Transaction?>

    @Query("DELETE FROM transactions WHERE requestDate <= :threshold")
    suspend fun deleteBefore(threshold: Long)

    @Query("SELECT * FROM transactions")
    suspend fun getAll(): List<Transaction>
}
