package com.dev.payment.payment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.dev.payment.OnPaymentConfirmedListener
import com.dev.payment.R
import com.dev.payment.databinding.FragmentPaymentNewBinding
import com.dev.payment.utils.DialogUtils

class PaymentNewFragment : Fragment() {

    private var _binding: FragmentPaymentNewBinding? = null
    private val binding get() = _binding!!

    private var products: List<Product> = emptyList()
    private var listener: OnPaymentConfirmedListener? = null

    // Payment selection state
    private var selectedPaymentMethod: PaymentMethod? = null

    enum class PaymentMethod {
        CASH, CARD, UPI
    }

    companion object {
        private const val ARG_PRODUCTS = "products"
        private const val TAG = "PaymentNewFragment"

        fun newInstance(products: List<Product>): PaymentNewFragment {
            return PaymentNewFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_PRODUCTS, ArrayList(products))
                }
            }
        }
    }

    fun setOnPaymentConfirmedListener(listener: OnPaymentConfirmedListener) {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            products = it.getParcelableArrayList(ARG_PRODUCTS) ?: emptyList()
        }
        Log.d(TAG, "Products loaded: ${products.size}")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentNewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPaymentOptions()
        setupConfirmButton()
    }

    private fun setupPaymentOptions() {
        // Cash Payment Option
        binding.cardCash.setOnClickListener {
            selectPaymentMethod(PaymentMethod.CASH)
            handleCashPayment()
        }

        // Card Payment Option
        binding.cardCard.setOnClickListener {
            selectPaymentMethod(PaymentMethod.CARD)
            handleCardPayment()
        }

        // UPI Payment Option
        binding.cardUpi.setOnClickListener {
            selectPaymentMethod(PaymentMethod.UPI)
            handleUpiPayment()
        }
    }

    private fun selectPaymentMethod(method: PaymentMethod) {
        selectedPaymentMethod = method
        updateSelectionUI(method)
        Log.d(TAG, "Payment method selected: $method")
    }

    private fun updateSelectionUI(selectedMethod: PaymentMethod) {
        // Reset all indicators
        resetAllIndicators()

        // Update selected indicator and info
        when (selectedMethod) {
            PaymentMethod.CASH -> {
                updateIndicator(binding.indicatorCash, true)
                showSelectedInfo("Cash on Delivery selected")
            }
            PaymentMethod.CARD -> {
                updateIndicator(binding.indicatorCard, true)
                showSelectedInfo("Credit/Debit Card selected")
            }
            PaymentMethod.UPI -> {
                updateIndicator(binding.indicatorUpi, true)
                showSelectedInfo("UPI Payment selected")
            }
        }
    }

    private fun resetAllIndicators() {
        updateIndicator(binding.indicatorCash, false)
        updateIndicator(binding.indicatorCard, false)
        updateIndicator(binding.indicatorUpi, false)
    }

    private fun updateIndicator(indicator: View, isSelected: Boolean) {
        val context = requireContext()
        if (isSelected) {
            indicator.background = ContextCompat.getDrawable(context, R.drawable.selected_indicator)
        } else {
            indicator.background = ContextCompat.getDrawable(context, R.drawable.ubselected_indicator)
        }
    }

    private fun showSelectedInfo(message: String) {
        binding.textSelectedPayment.text = message
        binding.cardSelectedInfo.visibility = View.VISIBLE
    }

    private fun hideSelectedInfo() {
        binding.cardSelectedInfo.visibility = View.GONE
        selectedPaymentMethod = null
        resetAllIndicators()
    }

    private fun handleCashPayment() {
        showPaymentDialog(
            title = "Cash on Delivery",
            message = "You will pay ₹${getTotalAmount()} when your order arrives.\n\nThis option includes cash handling charges and you'll get up to ₹50 cashback on your next order.",
            positiveButtonText = "Confirm",
            onConfirm = {
                showConfirmButton("Place Order (Cash)")
            },
            onCancel = {
                hideSelectedInfo()
            }
        )
    }

    private fun handleCardPayment() {
        showPaymentDialog(
            title = "Card Payment",
            message = "Credit/Debit Card payment feature is currently under development.\n\nWould you like to choose a different payment method?",
            positiveButtonText = "Choose Other",
            negativeButtonText = "Cancel",
            onConfirm = {
                hideSelectedInfo()
                // Could scroll to other options or show them highlighted
            },
            onCancel = {
                hideSelectedInfo()
            }
        )
    }

    private fun handleUpiPayment() {
        showPaymentDialog(
            title = "UPI Payment",
            message = "You will be redirected to your UPI app to complete the payment of ₹${getTotalAmount()}.\n\nSupported apps: Google Pay, PhonePe, Paytm, and more.",
            positiveButtonText = "Continue",
            onConfirm = {
                launchUPIApp()
                showConfirmButton("Verify Payment")
            },
            onCancel = {
                hideSelectedInfo()
            }
        )
    }

    private fun showPaymentDialog(
        title: String,
        message: String,
        positiveButtonText: String = "Yes",
        negativeButtonText: String = "No",
        onConfirm: () -> Unit,
        onCancel: () -> Unit
    ) {
        try {
            // Try custom dialog first
            DialogUtils.showCommonDialog(
                context = requireContext(),
                title = title,
                message = message,
                onYes = onConfirm,
                onNo = onCancel
            )
        } catch (e: Exception) {
            Log.w(TAG, "Custom dialog failed, using standard AlertDialog: ${e.message}")

            // Fallback to standard AlertDialog
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButtonText) { _, _ -> onConfirm() }
                .setNegativeButton(negativeButtonText) { _, _ -> onCancel() }
                .setCancelable(false)
                .show()
        }
    }

    private fun showConfirmButton(buttonText: String) {
        binding.btnConfirm.apply {
            text = buttonText
            visibility = View.VISIBLE
            // Add some animation
            alpha = 0f
            animate()
                .alpha(1f)
                .setDuration(300)
                .start()
        }
    }

    private fun setupConfirmButton() {
        binding.btnConfirm.setOnClickListener {
            when (selectedPaymentMethod) {
                PaymentMethod.CASH -> {
                    Log.d(TAG, "Processing cash payment")
                    listener?.onPaymentConfirmed(products)
                    showSuccessMessage("Order placed successfully! Pay cash on delivery.")
                }
                PaymentMethod.UPI -> {
                    Log.d(TAG, "Verifying UPI payment")
                    // In real app, you'd verify the payment status here
                    listener?.onPaymentConfirmed(products)
                    showSuccessMessage("Payment verified! Your order has been placed.")
                }
                PaymentMethod.CARD -> {
                    // This shouldn't happen as card payment shows different dialog
                    Log.w(TAG, "Card payment confirmation attempted")
                }
                null -> {
                    Log.w(TAG, "No payment method selected")
                    Toast.makeText(requireContext(), "Please select a payment method", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun launchUPIApp() {
        val totalAmount = getTotalAmount()
        val uri = Uri.parse("upi://pay?pa=merchant@upi&pn=SStore&mc=0000&tid=txn${System.currentTimeMillis()}&tr=ref${System.currentTimeMillis()}&tn=Order Payment&am=$totalAmount&cu=INR")
        val intent = Intent(Intent.ACTION_VIEW, uri)

        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
            Log.d(TAG, "UPI app launched for amount: ₹$totalAmount")
        } else {
            Log.w(TAG, "No UPI app found")
            Toast.makeText(requireContext(), "No UPI app found on your device", Toast.LENGTH_LONG).show()
            hideSelectedInfo()
        }
    }

    private fun getTotalAmount(): String {
        // Calculate total from products - you might want to format this better
        val total = products.sumOf { it.price * it.quantity }
        return String.format("%.2f", total)
    }

    private fun showSuccessMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}