package com.example.ganeshhegde.youcanforget.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.view.*
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import com.example.ganeshhegde.youcanforget.R
import com.example.ganeshhegde.youcanforget.StoreClickListener
import com.example.ganeshhegde.youcanforget.activities.StoreDetailsActivity
import com.example.ganeshhegde.youcanforget.utils.AppConstants
import com.example.ganeshhegde.youcanforget.database.Store
import com.example.ganeshhegde.youcanforget.databinding.ItemDisplaySavedListBinding
import com.squareup.picasso.Picasso
import com.uber.sdk.android.core.UberSdk
import com.uber.sdk.android.rides.RideParameters
import com.uber.sdk.android.rides.RideRequestButton
import com.uber.sdk.rides.client.SessionConfiguration
import kotlinx.android.synthetic.main.item_display_saved_list.view.*
import java.io.File

class DisplaySavedListAdapter(context: Activity, storeList: List<Store>,storeClickListener:StoreClickListener): RecyclerView.Adapter<DisplaySavedListAdapter.MyViewHolder>() {

    lateinit var itemDisplaySavedListBinding:ItemDisplaySavedListBinding
    lateinit var storeList:List<Store>
    lateinit var context:Context
    var height:Int = 0
    lateinit var storeClickListener:StoreClickListener

    init {
        this.context = context
        this.storeList = storeList
        this.storeClickListener = storeClickListener

//        initUber()

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        /*itemDisplaySavedListBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_display_saved_list,parent,false)
        return MyViewHolder(itemDisplaySavedListBinding)*/

        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_display_saved_list,parent,false)

//        view.layoutParams.height = getViewHeight(height)/4

        return MyViewHolder(view)

    }

    private fun getViewHeight(toolBarHeight: Int): Int {



        var windowManager:WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

//        var defaultDisplay  = windowManager.defaultDisplay


//        var size = Point()

//        defaultDisplay.getSize(size)

//        return size.y


        var displayMetrics:DisplayMetrics = DisplayMetrics()

        var height = windowManager.defaultDisplay.getMetrics(displayMetrics)


        return displayMetrics.heightPixels

//        var toolBar = DisplaySavedListActivity.getToolBarHeight()
//        return (displayMetrics.heightPixels-toolBar)



        /*var windowManager:WindowManager = YCFApplication.instance.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        var display = windowManager.defaultDisplay

        var point = Point()

        var height:Int

        if(Build.VERSION.SDK_INT>19)
        {
            display.getRealSize(point)
        }else
        {
            display.getSize(point)
        }


        if(point.y > point.x)
        {
            height = point.y
        }else
        {
            height = point.x
        }


        return height*/

//        var heightPixels = Resources.getSystem().displayMetrics.heightPixels


//        return heightPixels

       /* var windowManager:WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        var defaultDisplay = windowManager.defaultDisplay

        var size = Point()

        defaultDisplay.getSize(size)

        return size.y*/
    }

    override fun getItemCount(): Int {
        return storeList.size
//        return 10
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.storeTitle.text  = storeList.get(position).name
        holder.storeMobileNumber.text = storeList.get(position).mobileNumber
        if(storeList.get(position).imageUrl == null || storeList.get(position).imageUrl == "" )
        {
//            Picasso.with(context).load(android.R.drawable.ic_menu_gallery).error(android.R.drawable.ic_menu_gallery)
            Picasso.with(context).load(R.drawable.nodata).error(R.drawable.nodata)
                    .placeholder(android.R.drawable.ic_menu_gallery).into(holder.storeImage)

        }else
        {
            /*Picasso.with(context).load(storeList.get(position).imageUrl).error(android.R.drawable.ic_menu_gallery)
                    .placeholder(android.R.drawable.ic_menu_gallery).into(holder.storeImage)*/

            Picasso.with(context).load(File(storeList.get(position).imageUrl)).error(R.drawable.nodata)
                    .placeholder(R.drawable.nodata).into(holder.storeImage)

        }

        holder.menuOptionItem.setOnClickListener {
            var popUp = PopupMenu(context,holder.menuOptionItem)

            popUp.inflate(R.menu.menu_item)

            popUp.setOnMenuItemClickListener {

                item: MenuItem? ->
                when(item!!.itemId)
                {
                    R.id.direction -> {


                        openMaps(storeList.get(position).latitude,storeList.get(position).longitude,storeList.get(position).name)

//                        var url = "http://maps.google.com/maps?saddr=" +"&daddr=" + storeList.get(position).latitude + "," + storeList.get(position).longitude
                       /* var url = "http://maps.google.com/maps?saddr="+""+"" +"&daddr=" + storeList.get(position).latitude + "," + storeList.get(position).longitude

                        var intent  = Intent(android.content.Intent.ACTION_VIEW,
                                Uri.parse(url))

                        context.startActivity(intent)

                        Toast.makeText(context,"direction",Toast.LENGTH_SHORT).show()*/
                        true
                    }

                    R.id.call ->

                    {

                        storeClickListener.onItemClicked(item.itemId,position,storeList.get(position))

                        true
                    }


                    R.id.share ->
                    {
                        storeClickListener.onItemClicked(item.itemId,position,storeList.get(position))
                        true
                    }

                    R.id.delete ->
                    {
                        storeClickListener.onItemClicked(item.itemId,position,storeList.get(position))
                        true
                    }

                    else -> {
                        false
                    }
                }

            }

            popUp.show()

            }

        holder.itemConstraintDisplay.setOnClickListener {
            var intent = Intent(context,StoreDetailsActivity::class.java)
            intent.putExtra("store_item",storeList.get(position))
            context.startActivity(intent)
        }
//        customizeUber(holder.uberRequestButton,position)

       /* holder.uberRequestButton.setOnClickListener {
            customizeUber( holder.uberRequestButton,position)

        }*/


    }

    private fun customizeUber(rideRequestButton: RideRequestButton, position: Int) {

        var rideParams = RideParameters.Builder()
                .setDropoffLocation(storeList.get(position).latitude.toDouble(),storeList.get(position).longitude.toDouble(),storeList.get(position).name,storeList.get(position).address)
                .build()

        rideRequestButton.setRideParameters(rideParams)

    }

    private fun initUber() {

        var config = SessionConfiguration.Builder()
                .setClientId(context.resources.getString(R.string.uber_client_id))
                .setClientSecret(context.resources.getString(R.string.uber_client_secret))
                .setRedirectUri("https://login.uber.com/oauth/v2/authorize?response_type=code&client_id=ofAT5w1iWLGpU8ZQ9rTqlV67kteD6tVI")
                .setEnvironment(SessionConfiguration.Environment.SANDBOX)
                .build()


        UberSdk.initialize(config)


       /* var rideParameters = RideParameters.Builder()
                .*/

    }

    private fun openMaps(latitude: String, longitude: String, name: String) {

        var intent = Intent(Intent.ACTION_VIEW,Uri.parse(AppConstants.HTTP_MAPS_GOOGLE_COM_MAPS_Q_LOC+latitude+","+longitude));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        var mapsPackageName = AppConstants.COM_GOOGLE_ANDROID_APPS_MAPS
        intent.setClassName(mapsPackageName,AppConstants.COM_GOOGLE_ANDROID_MAPS_MAPS_ACTIVITY)
        intent.`package` = mapsPackageName

        intent.putExtra(Intent.EXTRA_SUBJECT,name)

        context.startActivity(intent)



    }

    fun refreshRecyclerView( storeList: MutableList<Store>)
    {
        this.storeList = storeList
        this.notifyDataSetChanged()
//        notifyItemRemoved(position)
    }


    inner class MyViewHolder(itemView:View) : RecyclerView.ViewHolder(itemView) {

        lateinit var storeImage:ImageView
        lateinit var storeTitle:TextView
        lateinit var storeMobileNumber:TextView
        lateinit var menuOptionItem:TextView
//        lateinit var uberRequestButton :RideRequestButton
        var itemConstraintDisplay:ConstraintLayout

        init {
            this.storeImage = itemView.storeImage
            this.storeTitle = itemView.storeTitle
            this.storeMobileNumber = itemView.storeMobileNumber
            this.menuOptionItem = itemView.menuOptionItem
//            uberRequestButton = itemView.uberRequestButton
            itemConstraintDisplay = itemView.item_constraint_display
        }



    }

   /* class MyViewHolder(binding:ItemDisplaySavedListBinding) : RecyclerView.ViewHolder(binding.root) {
        lateinit var binding:ItemDisplaySavedListBinding

        init {
            this.binding = binding
        }

    }*/
}