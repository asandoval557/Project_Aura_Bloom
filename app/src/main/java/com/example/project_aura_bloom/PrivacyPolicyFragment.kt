package com.example.project_aura_bloom

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore


/**
// * A simple [Fragment] subclass.
// * Use the [PrivacyPolicyFragment.newInstance] factory method to
// * create an instance of this fragment.
 */
class  PrivacyPolicyFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var firestore: FirebaseFirestore
    private val legalDocsCollection = "LegalDocuments"
    private val policyDocument = "PrivacyPolicy"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_privacy_policy, container, false)

        firestore = FirebaseFirestore.getInstance()

        val termsTextView = view.findViewById<TextView>(R.id.privacy_policy_textview)

        firestore.collection(legalDocsCollection)
            .document(policyDocument)
            .get()
            .addOnSuccessListener {document ->
                if (document != null && document.exists()){
                    val termsText = document.getString("privacyContent")
                    if (!termsText.isNullOrEmpty()) {
                        termsTextView.text = termsText
                    }
                } else {
                    termsTextView.text = "Privacy Policy is not available."
                }
            }
            .addOnFailureListener { exception ->
                Log.d("TermsFragment", "Error getting documents.", exception)
                termsTextView.text = "Error getting documents."
            }
        return view
    }
}