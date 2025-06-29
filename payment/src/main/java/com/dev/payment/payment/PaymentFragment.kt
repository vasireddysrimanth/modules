package com.module.payment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.dev.payment.OnPaymentConfirmedListener
import com.dev.payment.R
import com.dev.payment.databinding.FragmentPaymentBinding
import com.dev.payment.payment.Product
import com.dev.payment.utils.showCommonDialog

class PaymentFragment : Fragment() {

   private var _binding: FragmentPaymentBinding? = null
   private val binding get() = _binding!!

   private var products: List<Product> = emptyList()

   // Callback listener to notify host (CheckOutFragment) after confirmation
   private var listener: OnPaymentConfirmedListener? = null

   companion object {
      private const val ARG_PRODUCTS = "products"

      fun newInstance(products: List<Product>): PaymentFragment {
         return PaymentFragment().apply {
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
   }

   override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
   ): View {
      _binding = FragmentPaymentBinding.inflate(inflater, container, false)
      return binding.root
   }

   override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
      super.onViewCreated(view, savedInstanceState)

      binding.btnConfirm.visibility = View.GONE

      binding.paymentRadioGroup.setOnCheckedChangeListener { _: RadioGroup, checkedId: Int ->
         when (checkedId) {
            R.id.radio_cash -> {
               showPopup("Pay with Cash") {
                  binding.btnConfirm.visibility = View.VISIBLE
                  binding.btnConfirm.text = "Place Order"
                  binding.btnConfirm.setOnClickListener {
                     listener?.onPaymentConfirmed(products)
                  }
               }
            }

            R.id.radio_amazon_upi -> {
               showPopup("Credit/Debit Card - Feature not implemented") {
                  binding.btnConfirm.visibility = View.GONE
               }
            }

            R.id.radio_other_upi -> {
               showPopup("Continue to UPI App") {
                  launchUPIApp()
               }
            }
         }
      }
   }

   private fun showPopup(message: String, onOk: () -> Unit) {
      showCommonDialog(
         context = requireContext(),
         title = "Payment Method Selected",
         message = message,
         onYes = {
            onOk()
         },
         onNo = {
            binding.paymentRadioGroup.clearCheck()
            binding.btnConfirm.visibility = View.GONE
         }
      )
   }

   private fun launchUPIApp() {
      val uri = Uri.parse("upi://pay?pa=example@upi&pn=SStore&mc=0000&tid=txn123&tr=ref123&tn=YourOrder&am=1&cu=INR")
      val intent = Intent(Intent.ACTION_VIEW, uri)
      if (intent.resolveActivity(requireContext().packageManager) != null) {
         startActivity(intent)
      } else {
         Toast.makeText(requireContext(), "No UPI app found", Toast.LENGTH_SHORT).show()
      }
   }

   override fun onDestroyView() {
      super.onDestroyView()
      _binding = null
   }


}
