package com.example.project_aura_bloom

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient



/**
// * A simple [Fragment] subclass.
// * Use the [PrivacyPolicyFragment.newInstance] factory method to
// * create an instance of this fragment.
 */
class PrivacyPolicyFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private val privacyPolicyURL = "https://1drv.ms/u/s!Ai8u-cbMfbjBg8prIDI5o70i7Vx89Q?e=LJvwSL" //load from MoonWolves Sharepoint



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewPolicy = inflater.inflate(R.layout.fragment_privacy_policy, container, false)

        view?.let {
            val policyView = viewPolicy.findViewById<WebView>(R.id.privacy_policy_webview)
            policyView.webViewClient = WebViewClient()
            policyView.settings.javaScriptEnabled = true
            policyView.loadUrl(privacyPolicyURL)
        }

        return viewPolicy
        }


//    companion object {
//        /**
//         * Use this factory method to create a new instance of
//         * this fragment using the provided parameters.
//         *
//         * @param param1 Parameter 1.
//         * @param param2 Parameter 2.
//         * @return A new instance of fragment PrivacyPolicyFragment.
//         */
        // TODO: Rename and change types and number of parameters
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            PrivacyPolicyFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
//    }
}