package com.chuckerteam.chucker.internal.ui.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.databinding.ChuckerFragmentTransactionOverviewBinding
import com.chuckerteam.chucker.internal.data.entity.Transaction
import com.chuckerteam.chucker.internal.support.combineLatest

internal class TransactionOverviewFragment : Fragment() {

    private val viewModel: TransactionViewModel by activityViewModels { TransactionViewModelFactory() }

    private lateinit var overviewBinding: ChuckerFragmentTransactionOverviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        overviewBinding = ChuckerFragmentTransactionOverviewBinding.inflate(inflater, container, false)
        return overviewBinding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.findItem(R.id.save_body).isVisible = false
        viewModel.doesUrlRequireEncoding.observe(
            viewLifecycleOwner,
            Observer { menu.findItem(R.id.encode_url).isVisible = it }
        )

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.transaction
            .combineLatest(viewModel.encodeUrl)
            .observe(
                viewLifecycleOwner,
                Observer { (transaction, encodeUrl) ->
                    populateUI(transaction, encodeUrl)
                }
            )
    }

    private fun populateUI(transaction: Transaction?, encodeUrl: Boolean) {
        with(overviewBinding) {
            method.text = transaction?.method
            status.text = transaction?.status.toString()
            response.text = transaction?.responseSummaryText
            requestTime.text = transaction?.requestDateString
            responseTime.text = transaction?.responseDateString
            duration.text = transaction?.durationString
            url.text = transaction?.urlFormatted

            protocol.isVisible = false
            requestSize.isVisible = false
            responseSize.isVisible = false
            totalSize.isVisible = false
            sslValue.isVisible = false

            requestSizeLabel.isVisible = false
            responseSizeLabel.isVisible = false
            totalSizeLabel.isVisible = false
            ssl.isVisible = false
            protocolLabel.isVisible = false
        }
    }
}
