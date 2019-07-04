package com.example.ganeshhegde.youcanforget.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

class NetworkUtils {
    companion object {
        fun hasConnectivity(context:Context): Boolean {
            val connectivityManager:ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            return if(connectivityManager is ConnectivityManager)
            {
                val info:NetworkInfo? = connectivityManager.activeNetworkInfo
                info?.isConnected ?:false
            }else
            {
                false
            }









          /*  if(info != null && info.isConnected)
            {
                return false
            }else
            {
                return false
            }

           *//* if(!)
            {
                return false
            }

            return true*//*
//            return(info != null && info.isConnected)*/
        }
    }
}