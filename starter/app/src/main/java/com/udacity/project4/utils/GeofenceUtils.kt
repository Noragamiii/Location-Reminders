package com.udacity.project4.utils

import java.util.concurrent.TimeUnit

//Reference https://github.com/udacity/android-kotlin-geo-fences/blob/master/app/src/main/java/com/example/android/treasureHunt/GeofenceUtils.kt#L48
internal object GeofencingConstants {

    val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = TimeUnit.HOURS.toMillis(1)
    const val GEOFENCE_RADIUS_IN_METERS = 100f
    const val EXTRA_GEOFENCE_INDEX = "GEOFENCE_INDEX"
}