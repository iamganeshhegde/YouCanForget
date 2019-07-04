package com.example.ganeshhegde.youcanforget.database

import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE

@Dao
interface StoreDao {

    @Query("SELECT *FROM STORE")
    fun getAllStore():List<Store>


    @Insert(onConflict = REPLACE)
    fun insertStore(store:Store)

    @Update(onConflict = REPLACE)
    fun updateStore(store: Store)

    @Delete()
    fun deleteStore(store: Store)
}