package com.example.project_aura_bloom

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient



/**
 * A simple [Fragment] subclass.
 * Use the [TermsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TermsFragment : Fragment() {

    private val termsandconditionsPolicyURL = "https://1drv.ms/b/s!Ai8u-cbMfbjBg8po7yjoY387emAp7Q?e=C2opUU" //load from MoonWolves Sharepoint


//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
//        }
//    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
       val viewTermsConditions = inflater.inflate(R.layout.fragment_terms, container, false)

        viewTermsConditions?.let {
            val termsconditionsView = viewTermsConditions.findViewById<WebView>(R.id.terms_conditions_webview)
            termsconditionsView.webViewClient = WebViewClient()
            termsconditionsView.settings.javaScriptEnabled = true
            termsconditionsView.loadUrl(termsandconditionsPolicyURL)
        }

        return viewTermsConditions
    }

//    companion object {
//        /**
//         * Use this factory method to create a new instance of
//         * this fragment using the provided parameters.
//         *
//         * @param param1 Parameter 1.
//         * @param param2 Parameter 2.
//         * @return A new instance of fragment termsFragment.
//         */
//        // TODO: Rename and change types and number of parameters
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            TermsFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
//    }
}