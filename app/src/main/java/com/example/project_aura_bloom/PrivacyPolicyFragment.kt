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

        viewPolicy?.let {
            val policyView = viewPolicy.findViewById<WebView>(R.id.privacy_policy_webview)
            policyView.webViewClient = WebViewClient()
            policyView.settings.javaScriptEnabled = true
            policyView.loadUrl(privacyPolicyURL)
        }

        return viewPolicy
        }
}