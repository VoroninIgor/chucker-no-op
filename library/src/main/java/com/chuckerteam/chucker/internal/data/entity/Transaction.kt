@file:Suppress("TooManyFunctions")

package com.chuckerteam.chucker.internal.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.chuckerteam.chucker.internal.support.FormatUtils
import com.chuckerteam.chucker.internal.support.FormattedUrl
import com.chuckerteam.chucker.internal.support.JsonConverter
import com.google.gson.reflect.TypeToken
import okhttp3.Headers
import okhttp3.HttpUrl
import java.util.Date

/**
 * Represent a full HTTP transaction (with Request and Response). Instances of this classes
 * should be populated as soon as the library receives data from OkHttp.
 */
@Suppress("LongParameterList")
@Entity(tableName = "transactions")
internal class Transaction(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id")
    var id: Long = 0,
    @ColumnInfo(name = "requestDate") var requestDate: Long?,
    @ColumnInfo(name = "responseDate") var responseDate: Long?,
    @ColumnInfo(name = "tookMs") var tookMs: Long?,
    @ColumnInfo(name = "method") var method: String?,
    @ColumnInfo(name = "url") var url: String?,
    @ColumnInfo(name = "port") var port: Int?,
    @ColumnInfo(name = "requestContentType") var requestContentType: String?,
    @ColumnInfo(name = "requestHeaders") var requestHeaders: String?,
    @ColumnInfo(name = "requestBody") var requestBody: String?,
    @ColumnInfo(name = "responseCode") var responseCode: String?,
    @ColumnInfo(name = "responseMessage") var responseMessage: String?,
    @ColumnInfo(name = "error") var error: String?,
    @ColumnInfo(name = "responseContentType") var responseContentType: String?,
    @ColumnInfo(name = "responseHeaders") var responseHeaders: String?,
    @ColumnInfo(name = "responseBody") var responseBody: String?
) {

    constructor() : this(
        requestDate = null,
        responseDate = null,
        tookMs = null,
        method = null,
        url = null,
        port = null,
        requestContentType = null,
        requestHeaders = null,
        requestBody = null,
        responseCode = null,
        responseMessage = null,
        error = null,
        responseContentType = null,
        responseHeaders = null,
        responseBody = null
    )

    enum class Status {
        Requested,
        Complete,
        Failed
    }

    val status: Status
        get() = when {
            error != null -> Status.Failed
            responseCode == null -> Status.Requested
            else -> Status.Complete
        }

    val urlFormatted: String?
        get() = "${url?:""}:${port?:""}"

    val requestDateString: String?
        get() = requestDate?.let { Date(it).toString() }

    val responseDateString: String?
        get() = responseDate?.let { Date(it).toString() }

    val durationString: String?
        get() = tookMs?.let { "$it ms" }

    val responseSummaryText: String?
        get() {
            return when (status) {
                Status.Failed -> error
                Status.Requested -> null
                else -> "$responseCode ${responseMessage ?: ""}"
            }
        }

    val notificationText: String
        get() {
            return when (status) {
                Status.Failed -> "! ! !  ${method ?: ""} "
                Status.Requested -> ". . .  ${method ?: ""} "
                else -> "$responseCode ${method ?: ""}"
            }
        }

    fun setRequestHeaders(headers: List<Header>) {
        requestHeaders = JsonConverter.instance.toJson(headers)
    }

    fun getParsedRequestHeaders(): List<Header>? {
        return JsonConverter.instance.fromJson<List<Header>>(
            requestHeaders,
            object : TypeToken<List<Header>>() {
            }.type
        )
    }

    fun getParsedResponseHeaders(): List<Header>? {
        return JsonConverter.instance.fromJson<List<Header>>(
            responseHeaders,
            object : TypeToken<List<Header>>() {
            }.type
        )
    }

    fun getRequestHeadersString(withMarkup: Boolean): String {
        return FormatUtils.formatHeaders(getParsedRequestHeaders(), withMarkup)
    }

    fun getResponseHeadersString(withMarkup: Boolean): String {
        return FormatUtils.formatHeaders(getParsedResponseHeaders(), withMarkup)
    }

    private fun toHttpHeaderList(headers: Headers): List<Header> {
        val httpHeaders = ArrayList<Header>()
        for (i in 0 until headers.size()) {
            httpHeaders.add(Header(headers.name(i), headers.value(i)))
        }
        return httpHeaders
    }

    private fun formatBody(body: String, contentType: String?): String {
        return when {
            contentType.isNullOrBlank() -> body
            contentType.contains("json", ignoreCase = true) -> FormatUtils.formatJson(body)
            contentType.contains("xml", ignoreCase = true) -> FormatUtils.formatXml(body)
            contentType.contains("x-www-form-urlencoded", ignoreCase = true) ->
                FormatUtils.formatUrlEncodedForm(body)
            else -> body
        }
    }

    fun getFormattedRequestBody(): String {
        return requestBody?.let { formatBody(it, requestContentType) } ?: ""
    }

    fun getFormattedResponseBody(): String {
        return responseBody ?: ""
    }

    fun getFormattedUrl(encode: Boolean): String {
        val httpUrl = url?.let(HttpUrl::get) ?: return ""
        return FormattedUrl.fromHttpUrl(httpUrl, encode).url
    }

    fun getFormattedPath(encode: Boolean): String {
        val httpUrl = url?.let(HttpUrl::get) ?: return ""
        return FormattedUrl.fromHttpUrl(httpUrl, encode).pathWithQuery
    }

    // Not relying on 'equals' because comparison be long due to request and response sizes
    // and it would be unwise to do this every time 'equals' is called.
    @Suppress("ComplexMethod")
    fun hasTheSameContent(other: Transaction?): Boolean {
        if (this === other) return true
        if (other == null) return false

        return (id == other.id) &&
            (requestDate == other.requestDate) &&
            (responseDate == other.responseDate) &&
            (tookMs == other.tookMs) &&
            (method == other.method) &&
            (url == other.url) &&
            (requestContentType == other.requestContentType) &&
            (requestHeaders == other.requestHeaders) &&
            (requestBody == other.requestBody) &&
            (responseCode == other.responseCode) &&
            (responseMessage == other.responseMessage) &&
            (error == other.error) &&
            (responseContentType == other.responseContentType) &&
            (responseHeaders == other.responseHeaders) &&
            (responseBody == other.responseBody)
    }
}
