package com.chuckerteam.chucker.internal.support

import android.content.Context
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.data.entity.Transaction
import okio.Buffer
import okio.Source

internal class TransactionDetailsSharable(
    private val transaction: Transaction,
    private val encodeUrls: Boolean,
) : Sharable {
    override fun toSharableContent(context: Context): Source = Buffer().apply {
        writeUtf8("${context.getString(R.string.chucker_url)}: ${transaction.getFormattedUrl(encodeUrls)}\n")
        writeUtf8("${context.getString(R.string.chucker_method)}: ${transaction.method}\n")
        writeUtf8("${context.getString(R.string.chucker_status)}: ${transaction.status}\n")
        writeUtf8("${context.getString(R.string.chucker_response)}: ${transaction.responseSummaryText}\n")
        writeUtf8("\n")
        writeUtf8("${context.getString(R.string.chucker_request_time)}: ${transaction.requestDateString}\n")
        writeUtf8("${context.getString(R.string.chucker_response_time)}: ${transaction.responseDateString}\n")
        writeUtf8("${context.getString(R.string.chucker_duration)}: ${transaction.durationString}\n")
        writeUtf8("\n")
        writeUtf8("---------- ${context.getString(R.string.chucker_request)} ----------\n\n")

        var headers = FormatUtils.formatHeaders(transaction.getParsedRequestHeaders(), false)

        if (headers.isNotBlank()) {
            writeUtf8(headers)
            writeUtf8("\n")
        }

        writeUtf8(
            if (transaction.requestBody.isNullOrBlank()) {
                context.getString(R.string.chucker_body_empty)
            } else {
                transaction.getFormattedRequestBody()
            }
        )

        writeUtf8("\n\n")
        writeUtf8("---------- ${context.getString(R.string.chucker_response)} ----------\n\n")

        headers = FormatUtils.formatHeaders(transaction.getParsedResponseHeaders(), false)

        if (headers.isNotBlank()) {
            writeUtf8(headers)
            writeUtf8("\n")
        }

        writeUtf8(
            if (transaction.responseBody.isNullOrBlank()) {
                context.getString(R.string.chucker_body_empty)
            } else {
                transaction.getFormattedResponseBody()
            }
        )
    }
}
