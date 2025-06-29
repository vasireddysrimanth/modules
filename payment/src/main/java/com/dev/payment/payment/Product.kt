package com.dev.payment.payment

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val id: Int,
    val title: String,
    val brand: String,
    val category: String,
    val color: String,
    val description: String,
    val discount: Int,
    val image: String,
    val model: String,
    val price: Double,
    var quantity: Int = 1
): Parcelable