package com.example.ganeshhegde.youcanforget

import com.example.ganeshhegde.youcanforget.database.Store

interface StoreClickListener {

    fun onItemClicked(itemId:Int,position: Int,store:Store)
}