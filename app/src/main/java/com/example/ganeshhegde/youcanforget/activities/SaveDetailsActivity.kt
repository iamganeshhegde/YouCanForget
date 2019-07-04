package com.example.ganeshhegde.youcanforget.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.location.*
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.example.ganeshhegde.youcanforget.R
import com.example.ganeshhegde.youcanforget.YCFApplication
import com.example.ganeshhegde.youcanforget.database.Store
import com.example.ganeshhegde.youcanforget.database.StoreDao
import com.example.ganeshhegde.youcanforget.databinding.ActivitySaveDetailsBinding
import com.example.ganeshhegde.youcanforget.utils.NetworkUtils
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.lang.Exception
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList


class SaveDetailsActivity : BaseActivity() {
    val TAG = SaveDetailsActivity::class.java.name
    lateinit var activitySaveDetailsBinding: ActivitySaveDetailsBinding
    lateinit var rootView: View
    lateinit var storeDao: StoreDao
    lateinit var geocoder: Geocoder
    var latitude: Double = 0.0
    var longitude: Double = 0.0

    var address: String = ""
    var postalCode = ""

    val permission = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
    val cameraPermissions = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)

    lateinit var remainingPermissions: ArrayList<String>
    val REQUEST_CODE = 101
    val UPDATE_INTERVAL: Long = 10 * 1000
    val FASTEST_INTERVAL: Long = 2 * 1000
    val REQUEST_CHECK_SETTINGS_GPS = 102

    val CAMERA_REQUEST_CODE = 200

    val CAMERA_OPEN_REQUEST_CODE = 201

    lateinit var storeList: List<Store>


    lateinit var mFusedLocationClient: FusedLocationProviderClient

    lateinit var mSettingsClient: SettingsClient

    lateinit var mLocationRequest: LocationRequest

    lateinit var mLocationSettingRequest: LocationSettingsRequest

    lateinit var mLocationCallbacks: LocationCallback

    var isGetDetailsClicked = false
    var mLocation: Location? = null

    var imagePath: String = ""


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activitySaveDetailsBinding = DataBindingUtil.setContentView(this, R.layout.activity_save_details)

        activitySaveDetailsBinding.handlers = this
        rootView = activitySaveDetailsBinding.root

        storeDao = YCFApplication.get().getDatabase().storeDao()
        geocoder = Geocoder(this@SaveDetailsActivity, Locale.getDefault())


        init() // initialize location prerequisites
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun init() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mSettingsClient = LocationServices.getSettingsClient(this)

        setLocationPermissions()

        mLocationCallbacks = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)

                locationResult ?: return

//                showToast(locationResult.lastLocation.latitude.toString() + " " + locationResult.lastLocation.longitude.toString() + "from locationcallback")

                /* for (locationResults in locationResult.locations)
                 {*/

//                showToast("location callback called getLastKnownLocation")
//                getLastKnownLocation()
//                    saveLocationDetails(locationResult)
//                }

                saveLocationDetails(locationResult.lastLocation)


                stopLocationUpdates()

            }
        }

//        setLocationPermissions()

//        createLocationRequest()
    }


    fun getLastKnownLocation() {
        if (mFusedLocationClient != null) {
            if (!isPermissionsAvailable()) {
                return
            }
            /* mFusedLocationClient.lastLocation.addOnSuccessListener {
                 location ->

                 if(location != null)
                 {
                     latitude = location.latitude
                     longitude = location.longitude


                     showToast(latitude.toString()+" "+longitude.toString())
                 }


             }*/


            /*var locationManager:LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            var isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

            var locationListener:LocationListener = object : LocationListener {
                override fun onLocationChanged(location: Location?) {

                    if(location!=null)
                    {
                        saveLocationDetails(location)
                    }

                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

                }

                override fun onProviderEnabled(provider: String?) {

                    if(isGpsEnabled)
                    {
                        showToast("Loading from provider enabled")
                    }

                }

                override fun onProviderDisabled(provider: String?) {


                }
            }

            if(!isPermissionsAvailable())
            {
                return
            }

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100000, 4f, locationListener)
*/



            mFusedLocationClient.lastLocation.addOnCompleteListener(this)
            { task ->

                if (task.isSuccessful && task.result != null) {
                    latitude = task.result.latitude
                    longitude = task.result.longitude

//                    saveLocationDetails(task.result)

//                    showToast(latitude.toString()+" "+longitude.toString())
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()

        isGetDetailsClicked = false
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {

        mFusedLocationClient.removeLocationUpdates(mLocationCallbacks)
    }


    fun startLocationUpdates() {
        if (isPermissionsAvailable()) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallbacks, Looper.myLooper())

        }
    }

    fun createLocationRequest() {
        mLocationRequest = LocationRequest().apply {
            interval = UPDATE_INTERVAL
            fastestInterval = FASTEST_INTERVAL
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }


        var builder = LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest)

        mLocationSettingRequest = builder.build()

        var task = mSettingsClient.checkLocationSettings(mLocationSettingRequest)

        task.addOnSuccessListener { locationSettingsResponse ->
            //            showToast("task called startLocationUpDates")

            startLocationUpdates()
//            getLastKnownLocation()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                exception.startResolutionForResult(this, REQUEST_CHECK_SETTINGS_GPS)
            }
        }

    }


//    locationResult: LocationResult

    private fun saveLocationDetails(result: Location) {


        try {

            if (NetworkUtils.hasConnectivity(this)) {
                //        latitude = locationResult.lastLocation.latitude
                latitude = result.latitude
//        longitude = locationResult.lastLocation.longitude
                longitude = result.longitude

                var addressList: List<Address> = geocoder.getFromLocation(latitude, longitude, 1)

                if (addressList != null) {
                    var addressStringBuilder = StringBuilder()

//        addressStringBuilder.append(addressList.get(0).getAddressLine(0)).append(addressList.get(0).adminArea).append(addressList.get(0).countryName)
                    addressStringBuilder.append(addressList.get(0).getAddressLine(0))

                    address = addressStringBuilder.toString()
                    postalCode = addressList.get(0).postalCode ?: ""


                    var store = Store(activitySaveDetailsBinding.name.text.toString(), activitySaveDetailsBinding.mobileNumber.text.toString(), "", latitude.toString(), longitude.toString(), address, postalCode, imagePath, Calendar.getInstance().time.toString())
//        showToast(latitude.toString() + " from save location details " + longitude.toString())


                    if (isGetDetailsClicked) {
                        saveInDatabase(store)
                    }

                } else {
                    showToast("cannot get address details")
                }

            } else {
                showToast("Please turn on Internet to get Address Details")
            }


        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun saveInDatabase(store: Store) {


        var storeObject: Observable<Unit> = io.reactivex.Observable.create(
                { emitter ->
                    emitter.onNext(storeDao.insertStore(store))
                }
        )
        /* var storeObject: Observable<Unit> = Observable.create(
                 { emitter ->
                     emitter.onNext(storeDao.insertStore(store))
                 }
         )*/

        storeObject.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { storeDetails ->
                            if (storeDetails != null)
                                Toast.makeText(this@SaveDetailsActivity, "Saved Store Details", Toast.LENGTH_SHORT).show()

                            navigateToNextScreen()
                        },
                        { error ->
                            Toast.makeText(this@SaveDetailsActivity, "Error in Adding " + error, Toast.LENGTH_SHORT).show()
                        }
                )
    }

    private fun navigateToNextScreen() {

        startActivity(Intent(this, DisplaySavedListActivity::class.java))
        finish()
    }


    @RequiresApi(Build.VERSION_CODES.M)
    fun onClick(view: View) {
        when (view.id) {
            R.id.getDetailsCV -> {

                isGetDetailsClicked = true
                saveDetails()
            }

            R.id.takePhoto -> {
                getCameraAndStoragePermissions()
            }
            R.id.back -> {
                hideKeyBoard()
                onBackPressed()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun getCameraAndStoragePermissions() {
        if (!isCameraPermissionsAvailable()) {

            requestMultiplePermissions()
        } else {
            startCameraActivity()
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestMultiplePermissions() {

        remainingPermissions = ArrayList()
        cameraPermissions.forEach { it ->
            if (checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED) {
                remainingPermissions.add(it)
            }
        }


        var array = arrayOfNulls<String>(remainingPermissions.size)
        requestPermissions(remainingPermissions.toArray(array), CAMERA_REQUEST_CODE)

    }

    private fun isCameraPermissionsAvailable(): Boolean {

        return (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setLocationPermissions() {

        if (!isPermissionsAvailable()) {
            requestPermissions(permission, REQUEST_CODE)
        } else {

            createLocationRequest()
        }
    }


    private fun isPermissionsAvailable(): Boolean {
        return (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        //permission granted for location
                        createLocationRequest()

                    } else {
                        if (shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                            showToast("Enable from requestPermission")
                        }
                    }
                }
            }


            CAMERA_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    grantResults.forEach { it ->
                        if (it != PackageManager.PERMISSION_GRANTED) {
                            showToast("Not given permission for " + it)

                            return
                        }
                    }

                    startCameraActivity()

//                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                } else {
                    showToast(" Give permission  - from requestPermission")
                }
            }
        }

    }

    private fun startCameraActivity() {

        var intent = Intent(this, CameraActivity::class.java)
        startActivityForResult(intent, CAMERA_OPEN_REQUEST_CODE)
//        startActivity(Intent(this,CameraActivity::class.java))

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CHECK_SETTINGS_GPS) {
            if (resultCode == Activity.RESULT_OK) {
//                showToast("settings Called   getLastKnownLocation")
//                showToast("settings Called   satartLocationUpdates")

                startLocationUpdates()
//                getLastKnownLocation()

            } else if (resultCode == Activity.RESULT_CANCELED) {


            }
        }

        if (requestCode == CAMERA_OPEN_REQUEST_CODE) {
            if (resultCode == 1000) {

                imagePath = data?.let { it.getStringExtra("IMAGE_PATH") } ?: ""
                /*if (data != null) {
                    imagePath = data.getStringExtra("IMAGE_PATH")
                }*/
//                imagePath = intent.extras.getString("IMAGE_PATH")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun saveDetails() {

        if (isEditTextValidated(activitySaveDetailsBinding.name, "Name") && isEditTextValidated(activitySaveDetailsBinding.mobileNumber, "Mobile Number")) {
            setLocationPermissions()

//            saveWithAllTheDetails()
        }

    }

    private fun isEditTextValidated(value: EditText, suffix: String): Boolean {

        if (value.text.trim() == null || value.text.length == 0) {
            Toast.makeText(this@SaveDetailsActivity, "Please Enter Valid " + suffix, Toast.LENGTH_SHORT).show()
            return false
        } else {
            return true
        }
    }


    fun isGpsEnabled(): Boolean {
        var locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }


}


//2 nd method

/*

private fun turnOnGps() {

    mLocationRequest = LocationRequest().setInterval(UPDATE_INTERVAL)
            .setFastestInterval(FASTEST_INTERVAL)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

    var builder:LocationSettingsRequest.Builder = LocationSettingsRequest.Builder()
            .addLocationRequest(mLocationRequest)


    mLocationSettingRequest = builder.build()


//        getCurrentLocationDetails()


    mSettingsClient.checkLocationSettings(mLocationSettingRequest)
            .addOnSuccessListener(this, object : OnSuccessListener<LocationSettingsResponse> {
                override fun onSuccess(p0: LocationSettingsResponse?) {

                    if(isPermissionsAvailable())
                    {

//                            getCurrentLocationDetails()
                        showToast("Updating from settingClient")
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallbacks,Looper.myLooper())

//                            getCurrentLocationDetails()
                    }else
                    {
//                            Activity\Compat.requestPermissions(this@SaveDetailsActivity,permission,REQUEST_CODE)
                    }

                }

            }).addOnFailureListener(this, object : OnFailureListener {
                override fun onFailure(exception: java.lang.Exception) {

                    var statusCode: Int = (exception as ApiException).statusCode

                    when(statusCode)
                    {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                        {
                            (exception as ResolvableApiException).startResolutionForResult(this@SaveDetailsActivity,REQUEST_CHECK_SETTINGS_GPS)
                        }

                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE ->
                        {
                            showToast("Please Open Settings to change Permissions")
                        }
                    }


                }

            })

    getCurrentLocationDetails()



}


private fun getCurrentLocationDetails() {


    showToast("getCurrentLocationDetails called")
    mLocationCallbacks = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)

            if(locationResult!=null)
            {
                saveLocationDetails(locationResult)
            }else{
                showToast("lat long not available")

            }
        }
    }

}*/


// 1st method


/*
//class SaveDetailsActivity : BaseActivity(), GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, com.google.android.gms.location.LocationListener {
class SaveDetailsActivity : BaseActivity() {

    val TAG = SaveDetailsActivity::class.java.name

    lateinit var activitySaveDetailsBinding: ActivitySaveDetailsBinding
    lateinit var rootView: View
    lateinit var storeDao: StoreDao

    lateinit var googleApiClient: GoogleApiClient
    final var REQUEST_CODE = 101
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    lateinit var geocoder: Geocoder
    var address: String = ""
    var postalCode: String = ""


    var mLocationManager: LocationManager? = null
    lateinit var locationManager:LocationManager


//    var UPDATE_INTERVAL:Long = 100 * 1000
    var UPDATE_INTERVAL:Long = 10 * 1000
//    var FASTEST_INTERVAL:Long = 20* 1000
    var FASTEST_INTERVAL:Long = 2* 1000

    var REQUEST_CHECK_SETTINGS_GPS = 101


    lateinit var storeList: List<Store>


    lateinit var mFusedLocationClient:FusedLocationProviderClient

    lateinit var mSettingsClient:SettingsClient

    lateinit var mLocationRequest: LocationRequest

    lateinit var mLocationSettingRequest:LocationSettingsRequest

    lateinit var mLocationCallbacks:LocationCallback

    var mLocation: Location? = null


    @RequiresApi(Build.VERSION_CODES.M)
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            activitySaveDetailsBinding = DataBindingUtil.setContentView(this, R.layout.activity_save_details)

            activitySaveDetailsBinding.handlers = this
            rootView = activitySaveDetailsBinding.root

            storeDao = YCFApplication.get().getDatabase().storeDao()
            geocoder = Geocoder(this@SaveDetailsActivity, Locale.getDefault())


            init() // initialize location prerequisites


//            mLocationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager


        getPermissionsOrSave()









//            checkLocation()




        }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getPermissionsOrSave() {

        if (!isLocationPermissionsAvailable()) {
            var permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)
            requestPermissions(permissions, REQUEST_CODE)
        } else {
            starLocationUpDates()

        }
    }

    private fun init()
    {

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mSettingsClient = LocationServices.getSettingsClient(this)

        mLocationCallbacks = object: LocationCallback()
        {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)

                if (locationResult != null) {
                    mLocation = locationResult.lastLocation


//                    upDateLocationUI()

                    saveDetails()
                }
            }

        }

        mLocationRequest = LocationRequest()
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)



        var builder:LocationSettingsRequest.Builder = LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest)


        mLocationSettingRequest = builder.build()


    }

   */
/* private fun upDateLocationUI() {

        if(mLocation !=null)
        {
            showToast(mLocation!!.latitude.toString() +" "+ mLocation!!.longitude.toString())


            getAddressFromLatLong(mLocation!!)
        }

    }

    private fun getAddressFromLatLong(mLocation: Location) {

        var listOfAddress:List<Address>

        listOfAddress = geocoder.getFromLocation(mLocation.latitude,mLocation.longitude,1)

        var address:String = listOfAddress.get(0).getAddressLine(1) + listOfAddress.get(0).adminArea + listOfAddress.get(0).countryName

        var postalCode= listOfAddress.get(0).postalCode



    }
*//*

    fun starLocationUpDates()
    {
        mSettingsClient.checkLocationSettings(mLocationSettingRequest)
                .addOnSuccessListener(this, object : OnSuccessListener<LocationSettingsResponse> {
                    @SuppressLint("MissingPermission")
                    override fun onSuccess(locationSettingsResponse: LocationSettingsResponse?) {


                        showLog(TAG,"UPDATING LOCATION")

                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallbacks, Looper.myLooper())


                        var location = locationManager.
//                        upDateLocationUI()

                        saveDetails()
                    }

                }).addOnFailureListener(this, object : OnFailureListener {
                    override fun onFailure(e: Exception) {

                        var statusCode = (e as ApiException).statusCode

                        when(statusCode)
                        {
                            LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                            {
                                showLog(TAG,"Setting not satisfied")
                                var rae:ResolvableApiException = e as ResolvableApiException
                                rae.startResolutionForResult(this@SaveDetailsActivity,REQUEST_CHECK_SETTINGS_GPS)

                            }

                            LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE ->
                            {
                                showToast("We cannot change settings here, Please go to settings")
                            }

                        }

//                        upDateLocationUI()
                        saveDetails()
                    }
                })
    }


  */
/*  fun isGooglePlayServicesAvailable(): Boolean {
        var googleApiAvailability:GoogleApiAvailability = GoogleApiAvailability.getInstance()

        if(googleApiAvailability.isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS)
        {
            var dialg = googleApiAvailability.getErrorDialog(this,googleApiAvailability.isGooglePlayServicesAvailable(this),0)
            if(dialg != null)
            {
                dialg.show()
            }
            return false
        }
        return true
    }*//*


  */
/*  private fun checkLocation() :Boolean{

        if(isProviderLocationEnabled()) {

            showToast("Enable Location From check Location")
        }

        return isProviderLocationEnabled()
    }

    private fun isProviderLocationEnabled(): Boolean {

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    }*//*


    private fun isLocationPermissionsAvailable(): Boolean {


            var result: Boolean = ContextCompat.checkSelfPermission(this@SaveDetailsActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this@SaveDetailsActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

            return result
        }


        */
/*private fun setUpGpConnection() {

            googleApiClient = GoogleApiClient.Builder(this@SaveDetailsActivity)
                    .addOnConnectionFailedListener(this)
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .build()

            googleApiClient.connect()
        }
*//*


    @RequiresApi(Build.VERSION_CODES.M)
    fun onClick(view: View) {
        when (view.id) {
            R.id.getDetailsCV ->
                if(isLocationPermissionsAvailable())
                {
                    saveDetails()
                }else
                {
                    getPermissionsOrSave()
                }

            R.id.back ->
                onBackPressed()

            R.id.getDetails ->
                getDetails()
        }
    }

    private fun getDetails() {

        var storeList: Observable<List<Store>> = Observable.create(
                { emitter ->
                    storeList = storeDao.getAllStore()

                    emitter.onNext(storeList);
                })

        storeList.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { storeData ->
                    for (i in 0..storeData.size - 1) {
                        Toast.makeText(this@SaveDetailsActivity, storeData.get(i).name, Toast.LENGTH_SHORT).show()
                        Log.wtf("STOREDATA", storeData.get(i).name);
                    }
                }
    }

    private fun saveDetails() {

        if (isEditTextValidated(activitySaveDetailsBinding.name, "Name") && isEditTextValidated(activitySaveDetailsBinding.mobileNumber, "Mobile Number")) {
            saveWithAllTheDetails()
        }

    }

    private fun saveWithAllTheDetails() {

        if(mLocation !=null) {
            showToast(mLocation!!.latitude.toString() + " " + mLocation!!.longitude.toString())

            var listOfAddress:List<Address>

            listOfAddress = geocoder.getFromLocation(mLocation!!.latitude, mLocation!!.longitude,1)

            var address:String = listOfAddress.get(0).getAddressLine(0) + listOfAddress.get(0).adminArea + listOfAddress.get(0).countryName

            var postalCode= listOfAddress.get(0).postalCode


            var store: Store = Store(activitySaveDetailsBinding.name.text.trim().toString(), activitySaveDetailsBinding.mobileNumber.text.trim().toString(), "", mLocation!!.latitude.toString(), mLocation!!.longitude.toString(), address, postalCode, "", Calendar.getInstance().time.toString())



            addDetailsThroughAsyncTask(store)
        }


    }

    private fun addDetailsThroughAsyncTask(store: Store) {


//        var storeObject = Observable.just(storeDao.insertStore(store))


        var storeObject: Observable<Unit> = Observable.create(
                { emitter ->
                    emitter.onNext(storeDao.insertStore(store))
                }
        )

        storeObject.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { storeDetails ->
                            if (storeDetails != null)
                                Toast.makeText(this@SaveDetailsActivity, "Saved Store Details", Toast.LENGTH_SHORT).show()
                        },
                        { error ->
                            Toast.makeText(this@SaveDetailsActivity, "Error in Adding " + error, Toast.LENGTH_SHORT).show()
                        }
                )
    }

    private fun isEditTextValidated(value: EditText, suffix: String): Boolean {

        if (value.text.trim() == null || value.text.length == 0) {
            Toast.makeText(this@SaveDetailsActivity, "Please Enter Valid " + suffix, Toast.LENGTH_SHORT).show()
            return false
        } else {
            return true
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
       super.onActivityResult(requestCode, resultCode, data)

       if(requestCode == REQUEST_CHECK_SETTINGS_GPS)
       {
           if(resultCode == Activity.RESULT_OK)
           {

               if(isLocationPermissionsAvailable())
               {
//                   upDateLocationUI()
                   saveDetails()
               }
               showToast("onresume called from onactivityresult")
           }
           if(resultCode == Activity.RESULT_CANCELED)
           {
               Toast.makeText(this,"Enable from onActivityResult",Toast.LENGTH_SHORT ).show()
           }
       }


   }


    override fun onResume() {
        super.onResume()

//        starLocationUpDates()
//        upDateLocationUI()

        saveDetails()
    }


  @RequiresApi(Build.VERSION_CODES.M)
   override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
       super.onRequestPermissionsResult(requestCode, permissions, grantResults)

       when(requestCode)
       {
           REQUEST_CODE ->
           {
               if(grantResults.size >0)
               {
                   if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                   {

                       starLocationUpDates()

                   }else
                   {
                       if(shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION ))
                       {
                           Toast.makeText(this,"Enable from RequestPermission",Toast.LENGTH_SHORT ).show()

                       }
                   }
               }
           }
       }
   }


    */
/*private fun getCurrentLocation() {

        if (googleApiClient != null) {
            if (isLocationPermissionsAvailable()) {
                var locationRequest: LocationRequest = LocationRequest.create()

                locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                locationRequest.fastestInterval = 10*1000
                locationRequest.interval = 10*60*1000


                var builder:LocationSettingsRequest.Builder = LocationSettingsRequest.Builder()
                        .addLocationRequest(locationRequest)


                builder.setAlwaysShow(true)




                var result: PendingResult<LocationSettingsResult>? =  LocationServices.SettingsApi.checkLocationSettings(googleApiClient,builder.build())

                if (result != null) {
                    result.setResultCallback {
                        object : ResultCallback<LocationSettingsResult> {
                            override fun onResult(locationSettingsResult: LocationSettingsResult) {

                                var status = locationSettingsResult.status

                                when(status.statusCode)
                                {
                                    LocationSettingsStatusCodes.SUCCESS -> getLatLong()

                                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                                    {
                                        status.startResolutionForResult(this@SaveDetailsActivity,REQUEST_CHECK_SETTINGS_GPS)

                                    }

                                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE ->
                                    {}
                                }
                            }
                        }
                    }
                }


            }
        }

    }

    private fun getLatLong() {

        LocationListener {
            object : com.google.android.gms.location.LocationListener {
                override fun onLocationChanged(location: Location?) {

                    storeLatLong(location)

                }

            } }

        if(!isLocationPermissionsAvailable())
        {
            return
        }

    }

    private fun storeLatLong(location: Location?) {

        if (location != null) {
            latitude = location.latitude
            longitude = location.longitude
        }

        var listOfAddress:List<Address>
        listOfAddress = geocoder.getFromLocation(latitude,longitude,1)

        address = listOfAddress.get(0).getAddressLine(0)+ listOfAddress.get(0).locality + listOfAddress.get(0).adminArea +
                listOfAddress.get(0).countryName
        postalCode = listOfAddress.get(0).postalCode




    }*//*



*/
/*

    override fun onLocationChanged(p0: Location?) {

        if (p0 != null) {
            showToast(p0.latitude.toString()+" "+p0.longitude.toString())
        }

    }

    @SuppressLint("MissingPermission")
    override fun onConnected(p0: Bundle?) {

        if(!isLocationPermissionsAvailable()) {
            return
        }

        startLocationUpdates()

        var fusedLocationProviderClient:FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationProviderClient.lastLocation
                .addOnSuccessListener(this, OnSuccessListener<Location> {

                    location ->
                    if(location!=null) {
                        mLocation = location

                        latitude = location.latitude
                        longitude = location.longitude

                        showToast(latitude.toString() +" " + longitude.toString())
                    }

                })

    }



    override fun onConnectionSuspended(p0: Int) {
        showLog(TAG, p0.toString())
        googleApiClient.connect()
    }


    override fun onConnectionFailed(p0: ConnectionResult) {
        showToast(p0.errorMessage)
    }
*//*



    */
/* @SuppressLint("MissingPermission")
   private fun startLocationUpdates() {
       mLocationRequest = LocationRequest.create()
               .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
               .setFastestInterval(FASTEST_INTERVAL)
               .setInterval(UPDATE_INTERVAL)


       if(!isLocationPermissionsAvailable()) {

           return
       }

       LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,mLocationRequest,this)

   }*//*




}


*/