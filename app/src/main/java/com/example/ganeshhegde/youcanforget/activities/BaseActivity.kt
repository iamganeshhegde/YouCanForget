package com.example.ganeshhegde.youcanforget.activities

import android.arch.persistence.room.Room
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.ganeshhegde.youcanforget.database.StoreDatabase

open class BaseActivity: AppCompatActivity() {

//    lateinit var context: Context
    lateinit var instance: BaseActivity
    lateinit var storeDatabase: StoreDatabase

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

//        context = BaseActivity@ this


    }

    fun showToast(message: String?) {

        Toast.makeText(this,message, Toast.LENGTH_SHORT).show()

    }
    fun showLog(tag: String?, message: String) {

        Log.i(tag,message)

    }

    fun hideKeyBoard() {
        /*try {

            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

        }catch (Exception e)
        {
            e.printStackTrace();
        }*/


        if (currentFocus != null && currentFocus.windowToken != null) {
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        } else {
            try {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }

    fun showKeyBoard(view: View)
    {
        var inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(view,InputMethodManager.SHOW_IMPLICIT)
    }

}