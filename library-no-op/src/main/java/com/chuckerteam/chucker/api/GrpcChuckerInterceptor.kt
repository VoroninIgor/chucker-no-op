package com.chuckerteam.chucker.api

import android.content.Context
import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.ForwardingClientCall
import io.grpc.MethodDescriptor

public class GrpcChuckerInterceptor(
    private val address: String = "",
    private val port: Int = 0,
    private val context: Context,
    private val collector: ChuckerCollector = ChuckerCollector(context),
) : ClientInterceptor {
    override fun <ReqT : Any?, RespT : Any?> interceptCall(method: MethodDescriptor<ReqT, RespT>?, callOptions: CallOptions?, next: Channel): ClientCall<ReqT, RespT> {
        return object :
            ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
        }
    }
}
