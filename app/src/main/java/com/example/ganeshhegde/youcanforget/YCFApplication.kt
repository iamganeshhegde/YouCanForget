package com.example.ganeshhegde.youcanforget

import android.app.Application
import android.arch.persistence.room.Room
import android.support.multidex.MultiDexApplication
import com.example.ganeshhegde.youcanforget.database.StoreDatabase

class YCFApplication : MultiDexApplication() {


    companion object {
        lateinit var instance: YCFApplication
        lateinit var storeDatabase: StoreDatabase

        fun get(): YCFApplication {
            return instance
        }

    }

    override fun onCreate() {
        super.onCreate()

        instance = this@YCFApplication

        storeDatabase = Room.databaseBuilder(applicationContext, StoreDatabase::class.java,"store.db")
                .fallbackToDestructiveMigration()
                .build()
    }


    fun getDatabase(): StoreDatabase
    {
        return storeDatabase
    }
}
