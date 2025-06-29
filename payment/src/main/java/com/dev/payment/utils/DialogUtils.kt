package com.dev.payment.utils

import android.app.AlertDialog
import android.content.Context

fun showCommonDialog(
    context: Context,
    title: String? = null,
    message: String,
    onYes: () -> Unit,
    onNo: (() -> Unit)? = null
) {
    val builder = AlertDialog.Builder(context)
    if (!title.isNullOrEmpty()) {
        builder.setTitle(title)
    }
    builder.setMessage(message)
        .setPositiveButton("Yes") { dialog, _ ->
            dialog.dismiss()
            onYes()
        }
        .setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
            onNo?.invoke()
        }
        .setCancelable(false)
        .show()
}