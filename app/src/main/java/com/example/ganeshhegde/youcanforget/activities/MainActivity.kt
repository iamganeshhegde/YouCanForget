package com.example.ganeshhegde.youcanforget.activities

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.View
import com.example.ganeshhegde.youcanforget.R
import com.example.ganeshhegde.youcanforget.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {

    lateinit var activityMainBinding: ActivityMainBinding
    lateinit var rootView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityMainBinding = DataBindingUtil.setContentView(this@MainActivity, R.layout.activity_main)
        activityMainBinding.handlers = this@MainActivity

        rootView = activityMainBinding.root


    }

    fun onClick(view:View)
    {
        when(view.id)
        {
            R.id.saveDetailsCV ->{
                startActivity(Intent(this@MainActivity, SaveDetailsActivity::class.java))
            }
            R.id.displayListCV ->{
                startActivity(Intent(this@MainActivity, DisplaySavedListActivity::class.java))

            }
        }
    }

}