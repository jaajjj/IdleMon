package com.example.idlemon

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ConnexionManager {
    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()
    private const val TAG = "ConnexionManager"

    fun estConnecte(): Boolean = auth.currentUser != null
    fun getEmail(): String? = auth.currentUser?.email

    //déconnexion
    fun deconnexion(context: Context) {
        auth.signOut()
        SaveManager.clearPendingPull()
        SaveManager.deleteLocal(context)
        SaveManager.resetToDefault(context)
        val credentialManager = CredentialManager.create(context)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) { }
        }
    }

    //inscription
    fun inscription(email: String, pass: String, context: Context, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //save cloud
                    SaveManager.cloudSyncComplete = true
                    SaveManager.sauvegarder(onSuccess, onFailure)
                } else {
                    onFailure(task.exception?.message ?: "Erreur d'inscription")
                }
            }
    }

    //connexion normale
    fun connexion(email: String, pass: String, context: Context, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    SaveManager.charger(context, onSuccess, onFailure = {
                        // Pas de save cloud : on autorise le local actuel à devenir la référence
                        SaveManager.cloudSyncComplete = true
                        SaveManager.sauvegarder(onSuccess, onFailure)
                    })
                } else {
                    onFailure(task.exception?.message ?: "Identifiants incorrects")
                }
            }
    }

    //Logique Google
    fun connexionGoogle(context: Context, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val credentialManager = CredentialManager.create(context)
        val webClientId = "250685621215-opqldocbb57comvogmj09eag78p7nncn.apps.googleusercontent.com"

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false) 
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential

                val googleIdTokenCredential = when {
                    credential is GoogleIdTokenCredential -> credential
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                        GoogleIdTokenCredential.createFrom(credential.data)
                    }
                    else -> null
                }

                if (googleIdTokenCredential != null) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                    auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                SaveManager.charger(context, 
                                    onSuccess = { onSuccess() },
                                    onFailure = {
                                        SaveManager.cloudSyncComplete = true
                                        SaveManager.sauvegarder(onSuccess, onFailure)
                                    }
                                )
                            } else {
                                onFailure("Erreur Firebase Google")
                            }
                        }
                } else {
                    onFailure("Type de credential inattendu")
                }
            } catch (e: GetCredentialException) {
                onFailure("Erreur Google [${e.type}]")
            } catch (e: Exception) {
                onFailure(e.message ?: "Action annulée")
            }
        }
    }

    //reset pour dev
    fun resetCompte(context: Context, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        SaveManager.deleteLocal(context)
        SaveManager.resetToDefault(context)
        db.collection("users").get()
            .addOnSuccessListener { result ->
                val batch = db.batch()
                for (document in result) {
                    batch.delete(document.reference)
                }
                batch.commit()
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onFailure("Erreur Batch : ${it.message}") }
            }
            .addOnFailureListener { onFailure("Erreur accès : ${it.message}") }
    }
}
