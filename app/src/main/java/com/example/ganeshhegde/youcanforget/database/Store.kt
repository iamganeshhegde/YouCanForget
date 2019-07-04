package com.example.ganeshhegde.youcanforget.database

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "store")
data class Store(@ColumnInfo(name = "name") var name: String = "",
                 @ColumnInfo(name = "mobileNumber") var mobileNumber: String = "",
                 @ColumnInfo(name = "email") var email: String = "",
                 @ColumnInfo(name = "latitude") var latitude: String = "",
                 @ColumnInfo(name = "longitude") var longitude: String = "",
                 @ColumnInfo(name = "address") var address: String = "",
                 @ColumnInfo(name = "pinCode") var pinCode: String = "",
                 @ColumnInfo(name = "imageUrl") var imageUrl: String = "",
                 @ColumnInfo(name = "date") var date: String = ""):Serializable{

    @ColumnInfo(name = "storeId")
    @PrimaryKey(autoGenerate = true)
    var storeId = 0

}