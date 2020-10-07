package com.chuckerteam.chucker.api

import android.content.Context
import com.chuckerteam.chucker.internal.data.entity.Header
import com.chuckerteam.chucker.internal.data.entity.Transaction
import com.google.gson.Gson
import com.google.protobuf.MessageOrBuilder
import com.google.protobuf.TextFormat
import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.ForwardingClientCall
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.Status

public class GrpcChuckerInterceptor(
    private val address: String = "",
    private val port: Int = 0,
    private val context: Context,
    private val collector: ChuckerCollector = ChuckerCollector(context),
) : ClientInterceptor {

    override fun <M, R> interceptCall(
        methodDescriptor: MethodDescriptor<M, R>,
        callOptions: CallOptions,
        next: Channel
    ): ClientCall<M, R> {
        val transaction = Transaction()
        transaction.apply {
            this.url = address
            this.port = this@GrpcChuckerInterceptor.port
        }

        return object : BackendForwardingClientCall<M, R>(methodDescriptor, next.newCall(methodDescriptor, callOptions)) {

            override fun start(responseListener: Listener<R>, headers: Metadata) {
                transaction.apply {
                    requestHeaders = Gson().toJson(getHeadersList(headers))
                    method = methodName
                    requestDate = System.currentTimeMillis()
                }
                super.start(BackendListener(responseListener, transaction), headers)
            }

            override fun sendMessage(message: M) {
                transaction.apply {
                    requestBody = message.toString()
                }
                super.sendMessage(message)
                collector.onRequestSent(transaction)
            }
        }
    }

    private fun getHeadersList(headers: Metadata): List<Header> {
        return headers.keys().map { headerName ->
            Header(
                headerName,
                headers[Metadata.Key.of(headerName, Metadata.ASCII_STRING_MARSHALLER)] ?: ""
            )
        }
    }

    private inner class BackendListener<R>(
        var responseListener: ClientCall.Listener<R>,
        var transaction: Transaction
    ) :
        ClientCall.Listener<R>() {

        override fun onMessage(message: R) {
            transaction.apply {
                responseBody = TextFormat.printer().escapingNonAscii(false).printToString(
                    message as MessageOrBuilder
                )
            }

            responseListener.onMessage(message)
        }

        override fun onHeaders(headers: Metadata) {
            transaction.apply {
                responseHeaders = Gson().toJson(getHeadersList(headers))
            }
            responseListener.onHeaders(headers)
        }

        override fun onClose(status: Status, metadata: Metadata) {
            transaction.apply {
                responseCode = status.code.name
                error = status.cause?.toString()

                responseDate = System.currentTimeMillis()
                tookMs = (responseDate ?: 0L) - (requestDate ?: 0L)
            }
            responseListener.onClose(status, metadata)
            collector.onResponseReceived(transaction)
        }

        override fun onReady() {
            responseListener.onReady()
        }
    }

    private open class BackendForwardingClientCall<M, R> protected constructor(
        method: MethodDescriptor<M, R>,
        delegate: ClientCall<M, R>?
    ) : ForwardingClientCall.SimpleForwardingClientCall<M, R>(delegate) {
        var methodName: String = method.fullMethodName
    }
}
