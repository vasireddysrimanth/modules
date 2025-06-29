package com.dev.payment

import com.dev.payment.payment.Product

interface OnPaymentConfirmedListener {
    fun onPaymentConfirmed(products: List<Product>)
}