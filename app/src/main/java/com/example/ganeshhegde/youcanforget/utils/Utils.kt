package com.example.ganeshhegde.youcanforget.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.content.ContextCompat
import com.example.ganeshhegde.youcanforget.database.Store
import java.io.Console
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat

class Utils {

    companion object {
        fun createVcfFile(context: Context, store:Store) {

            var file = File(context.getExternalFilesDir(null),"store_"+SimpleDateFormat("yyyyMMdd_HHmmss"))

            var fw = FileWriter(file)

            fw.write("BEGIN:VCARD\r\n")
            fw.write("VERSION:3.0\r\n")
            fw.write("N:"+store.name+"\r\n")
            fw.write("TITLE:"+store.name+"\r\n")
            fw.write("TEL;TYPE=WORK,VOICE:"+store.mobileNumber+"\r\n")
            fw.write("ADR;TYPE=WORK:;;"+store.address+"\r\n")
            fw.write("END:VCARD"+"\r\n")
            fw.close()

            var intent = Intent()
            intent.action = android.content.Intent.ACTION_SEND
            intent.type = "text/x-vcard"
            intent.putExtra(Intent.EXTRA_STREAM,Uri.parse(Uri.fromFile(file).toString()))
//            intent.setDataAndType(Uri.fromFile(file),"text/v-card")
            context.startActivity(Intent.createChooser(intent,"Select an option to share"))

        }
    }

}