package com.example.project_aura_bloom

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore




/**
 * A simple [Fragment] subclass.
 * Use the [TermsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TermsFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private val legalDocsCollection = "LegalDocuments"
    private val termsDocument = "TermsAndConditions"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_terms, container, false)

        firestore = FirebaseFirestore.getInstance()

        val termsTextView = view.findViewById<TextView>(R.id.terms_conditions_textview)

        firestore.collection(legalDocsCollection)
            .document(termsDocument)
            .get()
            .addOnSuccessListener {document ->
                if (document != null && document.exists()){
                    val termsText = document.getString("termsContent")
                    if (!termsText.isNullOrEmpty()) {
                        termsTextView.text = termsText
                    }
                } else {
                    termsTextView.text = "Terms and Conditions are not available."
                }
            }
            .addOnFailureListener { exception ->
                Log.d("TermsFragment", "Error getting documents.", exception)
                termsTextView.text = "Error getting documents."
            }
        return view
    }

}