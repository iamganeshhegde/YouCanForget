package com.example.ganeshhegde.youcanforget.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

@Database(entities = arrayOf(Store::class),version = 1)
abstract class StoreDatabase: RoomDatabase() {

    abstract fun storeDao():StoreDao
}