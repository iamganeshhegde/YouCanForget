package com.example.ganeshhegde.youcanforget.activities

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.View
import com.example.ganeshhegde.youcanforget.R
import com.example.ganeshhegde.youcanforget.database.Store
import com.example.ganeshhegde.youcanforget.databinding.ActivityStoreDetailsBinding
import com.uber.sdk.android.core.UberSdk
import com.uber.sdk.android.rides.RideParameters
import com.uber.sdk.android.rides.RideRequestButton
import com.uber.sdk.rides.client.SessionConfiguration

class StoreDetailsActivity : BaseActivity() {

    lateinit var rootView: View
    lateinit var activityStoreDetailsBinding: ActivityStoreDetailsBinding
    lateinit var store: Store

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityStoreDetailsBinding = DataBindingUtil.setContentView(this, R.layout.activity_store_details)
        rootView = activityStoreDetailsBinding.root

        activityStoreDetailsBinding.handlers = this


        store = getIntent().getSerializableExtra("store_item") as Store

        if (store != null) {
            setUpView()
        }
        initUberDetails(activityStoreDetailsBinding.uberRequestButton)

    }

    private fun setUpView() {

        activityStoreDetailsBinding.store = store

    }

    private fun initUberDetails(rideRequestButton: RideRequestButton) {

        var config = SessionConfiguration.Builder()
                .setClientId(resources.getString(R.string.uber_client_id))
                .setClientSecret(resources.getString(R.string.uber_client_secret))
                .setRedirectUri("https://login.uber.com/oauth/v2/authorize?response_type=code&client_id=ofAT5w1iWLGpU8ZQ9rTqlV67kteD6tVI")
                .setEnvironment(SessionConfiguration.Environment.SANDBOX)
                .build()


        UberSdk.initialize(config)




        var rideParams = RideParameters.Builder()
                .setDropoffLocation(store.latitude.toDouble(),store.longitude.toDouble(),store.name,store.address)
                .build()

        rideRequestButton.setRideParameters(rideParams)

    }


    fun onClick(view: View) {
        when (view.id) {

            R.id.back ->
                onBackPressed()

        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}