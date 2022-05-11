package dk.mths.jomo.service

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.ktx.Firebase
import java.lang.Exception
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

open class FireLog {
    private val userIdKey = "uid"
    private val timestampKey = "timestamp"
    private val firebaseLogTag = "FirebaseLog"
    private val tagKey = "tag"

    private val params: HashMap<String, Any> = hashMapOf()

    fun withContext(key: String, value: Any): FireLog {
        params[key] = value
        return this
    }

    fun withTimestamp(temporal: TemporalAccessor): FireLog {
        params[timestampKey] = DateTimeFormatter.ISO_INSTANT.format(temporal)
        return this
    }

    fun sendLog(tag: String) {
        injectTrackingData().addOnSuccessListener {
            params[tagKey] = tag

            Firebase.firestore
                .collection("logs")
                .add(params)
                .addOnSuccessListener { documentReference ->
                    Log.d(firebaseLogTag, "DocumentSnapshot added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.wtf(firebaseLogTag, "Error adding document", e)
                }
        }
    }

    fun sendError(e: Exception) {
        injectTrackingData().addOnSuccessListener {
            params[tagKey] = "ERROR"
            params["error_message"] = e.localizedMessage as Any
            params["stacktrace"] = e.stackTraceToString()

            Firebase.firestore
                .collection("errors")
                .add(params)
                .addOnSuccessListener { documentReference ->
                    Log.d(firebaseLogTag, "DocumentSnapshot added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.wtf(firebaseLogTag, "Error adding document", e)
                }
        }
    }

    private fun injectTrackingData(): Task<String>{
        return FirebaseInstallations.getInstance().id.addOnSuccessListener { firebaseInstanceId ->
            params[userIdKey] = firebaseInstanceId
            if(!params.containsKey(timestampKey)){
                params[timestampKey] = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
            }
        }
    }
}