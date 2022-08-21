package com.example.woman_project

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver: BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent.hasError()) {
            Log.e("GeofenceErr", GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode))
            return
        }else{
            Log.e("GeofenceErr", "NoErr")
        }
        val geofenceTransaction = geofencingEvent.geofenceTransition

        if (geofenceTransaction == Geofence.GEOFENCE_TRANSITION_ENTER
        ) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences
            val transitionMsg = when (geofenceTransaction) {
                Geofence.GEOFENCE_TRANSITION_ENTER -> "Enter"
                else -> "-"
            }
            triggeringGeofences.forEach {
                Toast.makeText(context, "${it.requestId} - $transitionMsg", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, "Unknown", Toast.LENGTH_LONG).show()
        }
    }

}