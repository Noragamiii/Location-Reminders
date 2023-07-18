package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.GeofencingConstants
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

// Reference https://developers.google.com/maps/documentation/android-sdk/location?hl=vi#kotlin
class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    // Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private val fragTAG = SelectLocationFragment::class.java.simpleName
    private lateinit var map: GoogleMap
    private var locationSelected = false

    // Create variable temp
    private var tempLatitude: Double = 0.0
    private var tempLongitude: Double = 0.0
    private var tempSelectedLocation: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        val layoutId = R.layout.fragment_select_location
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        // setup map
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.saveLocationButton.setOnClickListener {
            if (locationSelected) {
                locationSelected = false
                onLocationSelected()
            }
            else {
                Toast.makeText(context,"Please select a location.", Toast.LENGTH_SHORT).show()
            }
        }
        return binding.root
    }

    private fun onLocationSelected() {
        setDataItem()
        _viewModel.navigationCommand.value = NavigationCommand.Back
    }

    private fun setDataItem() {
        _viewModel.reminderSelectedLocationStr.value = tempSelectedLocation
        _viewModel.latitude.value = tempLatitude
        _viewModel.longitude.value = tempLongitude
    }

    private fun isPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        // Reference https://developer.android.com/training/permissions/requesting?hl=vi
        when {
            isPermissionGranted() -> {
                map.isMyLocationEnabled = true
                checkDeviceLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(android.R.string.ok) {
                        requestPermissions(
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            REQUEST_LOCATION_PERMISSION
                        )
                    }.show()
            }
            else -> {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION
                )
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            tempSelectedLocation = poi.name
            tempLongitude = poi.latLng.longitude
            tempLatitude = poi.latLng.latitude
            map?.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    poi.latLng,
                    DEFAULT_ZOOM
                )
            )
            poiMarker?.showInfoWindow()
        }
        locationSelected = true
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            val mapMarker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
            )
            tempSelectedLocation = getString(R.string.title_activity_maps)
            tempLongitude = latLng.longitude
            tempLatitude = latLng.latitude
            map?.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    latLng,
                    DEFAULT_ZOOM
                )
            )
            mapMarker?.showInfoWindow()
        }
        locationSelected = true
    }

    private fun setMapStyle(map: GoogleMap?) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style)
            )
            if (success != true) {
                Log.e(fragTAG,"Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }


    private fun checkDeviceLocation(resolve:Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    startIntentSenderForResult(exception.resolution.intentSender,
                        REQUEST_TURN_DEVICE_LOCATION_ON, null, 0, 0, 0, null)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocation()
                }.show()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableMyLocation()
        setPoiClick(map)
        setMapLongClick(map)
        setMapStyle(map)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        // Check if location permissions are granted and if so enable the
        // location data layer.
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            } else {
                // In case they previously chose "Deny & don't ask again",
                // Reference https://github.com/google-developer-training/advanced-android-kotlin-geo-fences/blob/master/app/src/main/java/com/example/android/treasureHunt/HuntMainActivity.kt
                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.settings) {
                        startActivity(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    }.show()

            }
        }
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
        private const val REQUEST_TURN_DEVICE_LOCATION_ON = 2
        private const val DEFAULT_ZOOM = 15f
    }
}

private const val TAG = "SelectLocationFragment"