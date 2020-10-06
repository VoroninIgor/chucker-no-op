package com.chuckerteam.chucker.internal.ui.transaction

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.databinding.ChuckerListItemTransactionBinding
import com.chuckerteam.chucker.internal.data.entity.Transaction
import com.chuckerteam.chucker.internal.data.entity.TransactionTuple
import java.text.DateFormat

internal class TransactionAdapter internal constructor(
    context: Context,
    private val listener: TransactionClickListListener?
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {
    private var transactions: List<TransactionTuple> = arrayListOf()

    private val colorDefault: Int = ContextCompat.getColor(context, R.color.chucker_status_default)
    private val colorRequested: Int = ContextCompat.getColor(context, R.color.chucker_status_requested)
    private val colorError: Int = ContextCompat.getColor(context, R.color.chucker_status_error)
    private val color500: Int = ContextCompat.getColor(context, R.color.chucker_status_500)
    private val color400: Int = ContextCompat.getColor(context, R.color.chucker_status_400)
    private val color300: Int = ContextCompat.getColor(context, R.color.chucker_status_300)

    override fun getItemCount(): Int = transactions.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val viewBinding = ChuckerListItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(viewBinding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) =
        holder.bind(transactions[position])

    fun setData(transactions: List<TransactionTuple>) {
        this.transactions = transactions
        notifyDataSetChanged()
    }

    inner class TransactionViewHolder(
        private val itemBinding: ChuckerListItemTransactionBinding
    ) : RecyclerView.ViewHolder(itemBinding.root), View.OnClickListener {

        private var transactionId: Long? = null

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            transactionId?.let {
                listener?.onTransactionClick(it, adapterPosition)
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(transaction: TransactionTuple) {
            transactionId = transaction.id

            itemBinding.apply {
                path.text = "${transaction.method} ${transaction.getFormattedPath(encode = false)}"
                host.text = transaction.host
                timeStart.text = DateFormat.getTimeInstance().format(transaction.requestDate)

                if (transaction.status === Transaction.Status.Complete) {
                    code.text = if (transaction.responseCode.toString() == "OK") transaction.responseCode.toString() else "ERROR"
                    duration.text = transaction.durationString
                } else {
                    code.text = ""
                    duration.text = ""
                }
                if (transaction.status === Transaction.Status.Failed) {
                    code.text = "!!!"
                }
            }

            setStatusColor(transaction)
        }

        private fun setStatusColor(transaction: TransactionTuple) {
            val color: Int = when {
                (transaction.status === Transaction.Status.Failed) -> colorError
                (transaction.status === Transaction.Status.Requested) -> colorRequested
                (transaction.responseCode == null) -> colorDefault
                (transaction.responseCode != "OK") -> color500
                else -> colorDefault
            }
            itemBinding.code.setTextColor(color)
            itemBinding.path.setTextColor(color)
        }
    }

    interface TransactionClickListListener {
        fun onTransactionClick(transactionId: Long, position: Int)
    }
}
