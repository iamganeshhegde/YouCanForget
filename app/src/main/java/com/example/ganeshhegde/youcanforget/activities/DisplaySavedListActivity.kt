package com.example.ganeshhegde.youcanforget.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.ActionMode
import android.view.MotionEvent
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.Toast
import com.example.ganeshhegde.youcanforget.R
import com.example.ganeshhegde.youcanforget.StoreClickListener
import com.example.ganeshhegde.youcanforget.YCFApplication
import com.example.ganeshhegde.youcanforget.adapter.DisplaySavedListAdapter
import com.example.ganeshhegde.youcanforget.database.Store
import com.example.ganeshhegde.youcanforget.database.StoreDao
import com.example.ganeshhegde.youcanforget.databinding.ActivityDisplaySavedListBinding
import com.google.api.client.http.HttpContent
import com.itextpdf.text.Font
import com.itextpdf.text.Image
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_display_saved_list.*
import org.apache.http.protocol.HttpContext
import retrofit2.http.Url
import java.io.File
import java.io.FileOutputStream
import java.net.HttpCookie
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class DisplaySavedListActivity : BaseActivity(), StoreClickListener {


    lateinit var rootView: View
    lateinit var activityDisplaySavedListBinding: ActivityDisplaySavedListBinding
    lateinit var storeDao: StoreDao
    lateinit var storeList: MutableList<Store>
    lateinit var newStoreList: ArrayList<Store>
    var mobileNumber: String = ""
    var position: Int = 0
    lateinit var store: Store
    var SHARE_REQUEST_CODE = 106
    var CALL_REQUEST_CODE = 105
    var PDF_REQUEST_CODE = 104
    lateinit var adapter: DisplaySavedListAdapter
    lateinit var filteredArrayList: ArrayList<Store>
    var TAG = DisplaySavedListActivity::class.java.name

    /*ompanion object {

        var height:Int = 0


        fun getToolBarHeight(): Int {



            var viewTreeObserver:ViewTreeObserver = activityDisplaySavedListBinding.toolBar.viewTreeObserver

            if(viewTreeObserver!=null)
            {
                viewTreeObserver.addOnGlobalLayoutListener {

                    height = activityDisplaySavedListBinding.toolBar.height

                }

            }

            return height
        }
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        activityDisplaySavedListBinding = DataBindingUtil.setContentView(this, R.layout.activity_display_saved_list)

        activityDisplaySavedListBinding.handlers = this

        rootView = activityDisplaySavedListBinding.root


        storeDao = YCFApplication.get().getDatabase().storeDao()

        var storeListObject: Observable<List<Store>> = Observable.create(
                { emitter ->
                    storeList = storeDao.getAllStore() as MutableList<Store>

                    emitter.onNext(storeList);
                })

        storeListObject.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { storeData ->

                    setRecyclerViewData(storeData)

                    /*for (i in 0..storeData.size-1)
                {
                    Toast.makeText(this@DisplaySavedListActivity,storeData.get(i).name, Toast.LENGTH_SHORT).show()
                    Log.wtf("STOREDATA",storeData.get(i).name);
                }*/
                }



        searchFromList()



        initSearchViews()

    }


    /*var DRAWABLE_LEFT = 0
    var DRAWABLE_RIGHT = 2
    var result = false

    var compoundDrawables = activityDisplaySavedListBinding.editTextSearch.compoundDrawables

    var rightDrawable = compoundDrawables[DRAWABLE_RIGHT]
    var leftDrawable = compoundDrawables[DRAWABLE_LEFT]

    if(event.action == MotionEvent.ACTION_UP)
    {
        if(rightDrawable !=null)
        {
            if(event.rawX >= activityDisplaySavedListBinding.editTextSearch.right - rightDrawable.bounds.width())
            {
                activityDisplaySavedListBinding.editTextSearch.text.clear()
                true
            }
        }

        if(leftDrawable != null)
        {
            activityDisplaySavedListBinding.editTextSearch.visibility = View.GONE
            activityDisplaySavedListBinding.searchLineView.visibility = View.GONE
            true
        }
    }*/

    private fun initSearchViews() {

        activityDisplaySavedListBinding.editTextSearch.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {

                var DRAWABLE_LEFT = 0
                var DRAWABLE_RIGHT = 2

                var compoundDrawables = activityDisplaySavedListBinding.editTextSearch.compoundDrawables

                var rightDrawable = compoundDrawables[DRAWABLE_RIGHT]
                var leftDrawable = compoundDrawables[DRAWABLE_LEFT]

                if (event != null) {
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        if (rightDrawable != null) {
                            if (event.rawX >= activityDisplaySavedListBinding.editTextSearch.right - rightDrawable.bounds.width() + activityDisplaySavedListBinding.editTextSearch.paddingRight) {
                                activityDisplaySavedListBinding.editTextSearch.text.clear()
                                return true
                            }
                        }

                        if (leftDrawable != null) {
                            if (event.rawX <= leftDrawable.bounds.width() + activityDisplaySavedListBinding.editTextSearch.paddingRight) {
                                adapter.refreshRecyclerView(newStoreList)
                                activityDisplaySavedListBinding.editTextSearch.text.clear()
                                this@DisplaySavedListActivity.hideKeyBoard()
                                activityDisplaySavedListBinding.editTextSearch.visibility = View.GONE
                                activityDisplaySavedListBinding.searchLineView.visibility = View.GONE
                                return true
                            }

                        }
                    }
                }

                return false
            }

        })

    }


    private fun setRecyclerViewData(storeData: List<Store>?) {

        newStoreList = storeData as ArrayList<Store>
        activityDisplaySavedListBinding.RVDisplaySavedList.layoutManager = LinearLayoutManager(this@DisplaySavedListActivity)

        activityDisplaySavedListBinding.RVDisplaySavedList.itemAnimator = DefaultItemAnimator()

//        activityDisplaySavedListBinding.RVDisplaySavedList.addItemDecoration(DividerItemDecoration(this@DisplaySavedListActivity, LinearLayoutManager.VERTICAL))
        adapter = DisplaySavedListAdapter(this@DisplaySavedListActivity, storeData!!, this)
        activityDisplaySavedListBinding.RVDisplaySavedList.adapter = adapter


//        showToast(storeData.get(0).name+storeData.get(0).address+storeData.get(0).pinCode)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    fun onClick(view: View) {
        when (view.id) {
            R.id.back -> onBackPressed()

            R.id.downloadPdf -> {


                if (isSharePermissionsAvailable()) {
                    downloadPdfOfArrayList(newStoreList)
                } else {
                    requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), PDF_REQUEST_CODE)
                }


            }

            R.id.search_list -> {

//                activityDisplaySavedListBinding.editTextSearch.animate().

                        activityDisplaySavedListBinding.searchLineView.visibility = View.VISIBLE
                activityDisplaySavedListBinding.editTextSearch.visibility = View.VISIBLE
                activityDisplaySavedListBinding.editTextSearch.requestFocus()

                this.showKeyBoard(activityDisplaySavedListBinding.editTextSearch)


            }
//            R.id.searchImageView ->
//            {
            /*  if(activityDisplaySavedListBinding.editTextSearch.visibility == View.VISIBLE)
              {
                  activityDisplaySavedListBinding.editTextSearch.visibility = View.GONE
                  activityDisplaySavedListBinding.searchImageView.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(this,android.R.color.white),android.graphics.PorterDuff.Mode.MULTIPLY)
                  hideKeyBoard()

//                    activityDisplaySavedListBinding.editTextSearch.background = resources.getDrawable(R.color.colorPrimaryDark,null)

              }else
              {
                  activityDisplaySavedListBinding.editTextSearch.visibility = View.VISIBLE

                  activityDisplaySavedListBinding.searchImageView.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(this,android.R.color.holo_orange_dark),android.graphics.PorterDuff.Mode.MULTIPLY)

//                    activityDisplaySavedListBinding.editTextSearch.background = resources.getDrawable(R.color.colorPrimary,null)


              }*/
//            }

            /* R.id.editTextSearch ->
             {
                 searchFromList()
             }*/

        }
    }

    private fun downloadPdfOfArrayList(newStoreList: ArrayList<Store>) {

        if (!newStoreList.isEmpty()) {


            var doc = com.itextpdf.text.Document()

            try {
                var path = Environment.getExternalStorageDirectory().absolutePath + "/YouCanForget"

                var dir = File(path)

                if (!dir.exists()) {
                    dir.mkdirs()
                }

//            var file = File(dir,getFileName())
                var file = File(dir, "StoreList.pdf")
                var foutputStream = FileOutputStream(file)

                PdfWriter.getInstance(doc, foutputStream)

                doc.open()


                for (i in 0..newStoreList.size - 1) {
                    var p = Paragraph((i + 1).toString() + "\n" + newStoreList.get(i).name + "\n" + newStoreList.get(i).mobileNumber + "\n" + newStoreList.get(i).address + "\n")
                    var font = Font(Font(Font.FontFamily.COURIER))

                    p.alignment = Paragraph.ALIGN_LEFT
                    p.font = font
                    doc.add(p)


                    /*
                    Adding images to pdf- tried

                      var uri = FileProvider.getUriForFile(this, applicationContext.packageName + ".my.package.name.provider", File(newStoreList.get(i).imageUrl))

                      var url = URL(uri.toString())

  //                var image = Image(Uri.fromFile(File(newStoreList.get(i).imageUrl)))

                      var image = File(newStoreList.get(position).imageUrl)

                      var bitmapFactoryOptions = BitmapFactory.Options()

                      var bitmap = BitmapFactory.decodeFile(image.absolutePath, bitmapFactoryOptions)
                      bitmap = Bitmap.createScaledBitmap(bitmap, 50, 50, true)


                      var imageDoc = Image.getInstance(newStoreList.get(position).imageUrl)

  //                var photoLocation = HttpContext.Current.Server.MapPath(newStoreList.get(position).imageUrl)


  //                p.extraParagraphSpace = resources.getDimension(R.dimen.dp_5)
  //                doc.setMargins(resources.getDimension(R.dimen.dp_5),resources.getDimension(R.dimen.dp_5),resources.getDimension(R.dimen.dp_5),resources.getDimension(R.dimen.dp_5))
  //                doc.add(imageDoc)*/
                }

                Toast.makeText(this, "PDF has been saved to " + path, Toast.LENGTH_SHORT).show()

                viewPdf("StoreList.pdf", "YouCanForget")


            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                doc.close()
            }
        } else {
            Toast.makeText(this, "Save Store Details to download PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun viewPdf(fileName: String, parent: String) {

        var name = Environment.getExternalStorageDirectory().absolutePath.toString() + "/" + parent + "/" + fileName
        var pdfFile = File(name)

//        var path = Uri.fromFile(pdfFile)
        var path = FileProvider.getUriForFile(this, applicationContext.packageName + ".my.package.name.provider", pdfFile)

        var intent = Intent(Intent.ACTION_VIEW)

        intent.setDataAndType(path, "application/pdf")
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

        try {
            startActivity(intent)

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Log.i(TAG, e.toString())
        }

    }

    private fun getFileName(): String? {

        var name = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        return name + ".pdf"
    }

    private fun searchFromList() {

        filteredArrayList = ArrayList()

        activityDisplaySavedListBinding.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {


            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                var text = activityDisplaySavedListBinding.editTextSearch.text.toString().trim()

                filteredArrayList.clear()
                for (i in 0..newStoreList.size - 1) {
                    if (storeList.get(i).name.toLowerCase().contains(s.toString().toLowerCase()) || storeList.get(i).address.toLowerCase().toString().contains(s.toString().toLowerCase())) {
                        filteredArrayList.add(storeList.get(i))
                    }

//                    notifyAdapter()
                    adapter.refreshRecyclerView(filteredArrayList)
                }
            }
        })
        /*{
            object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {

                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    var text = activityDisplaySavedListBinding.editTextSearch.text.toString.trim()
                    if(filteredArrayList != null)
                    {
                        filteredArrayList.clear()
                    }
                    filteredArrayList.clear()
                    for (i in 0..storeList.size-1)
                    {
                        if(storeList.get(i).name.toLowerCase().contains(s.toString().toLowerCase()) || storeList.get(i).address.toLowerCase().toString().contains(s.toString().toLowerCase()))
                        {
                            filteredArrayList.add(storeList.get(i))
                        }

                        adapter.refreshRecyclerView(filteredArrayList)
                    }
                }
            }
        }*/


    }

    override fun onBackPressed() {
        super.onBackPressed()

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            CALL_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        startPhoneCall(position, store.mobileNumber)
                    }
                }

            }
            SHARE_REQUEST_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    shareStoreDetails()
                }
            }

            PDF_REQUEST_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    downloadPdfOfArrayList(newStoreList)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onItemClicked(itemId: Int, position: Int, store: Store) {


        this.position = position
        this.store = store


        when (itemId) {
            R.id.call -> {
                if (isCallPermissionsAvailable()) {
                    startPhoneCall(position, store.mobileNumber)

                } else {
                    requestPermissions(arrayOf(android.Manifest.permission.CALL_PHONE), CALL_REQUEST_CODE)
                }

            }

            R.id.share -> {
                if (isSharePermissionsAvailable()) {
                    shareStoreDetails()
                } else {
                    requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), SHARE_REQUEST_CODE)
                }
            }

            R.id.delete -> {

                deleteFromDatabase(store, position)

            }
        }

    }

    private fun deleteFromDatabase(store: Store, position: Int) {
//        storeDao.deleteStore(store)


        Completable.fromAction {
            storeDao.deleteStore(store)

        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        object : CompletableObserver {
                            override fun onComplete() {

//                                notifyAdapter(position)
                                notifyAdapter(store)

                            }

                            override fun onSubscribe(d: Disposable) {
                            }

                            override fun onError(e: Throwable) {

                            }

                        }

                )
    }

    //    private fun notifyAdapter(position: Int) {
    private fun notifyAdapter(store: Store) {

        storeList.remove(store)
//        storeList.removeAt(position)
        adapter.refreshRecyclerView(storeList)
    }

    private fun shareStoreDetails() {

//        Utils.createVcfFile(this,store)

//        var uriPath: Uri = Uri.parse("android.resource://" + packageName + "/android:drawable/" + "ic_menu_gallery")
//        var uriPath = Uri.parse(store.imageUrl)
        var shareIntent = Intent()

        var bitmapPath: String
        if (store.imageUrl != null && (store.imageUrl) != "") {
            bitmapPath = MediaStore.Images.Media.insertImage(contentResolver, store.imageUrl, "ShareImage", "")
        } else {
//            bitmapPath = "android.resource://" + R::class.java.`package`.name + "/"+ContextCompat.getDrawable(this,R.drawable.store_idea_brainstorm).toString()
            bitmapPath = MediaStore.Images.Media.insertImage(contentResolver, BitmapFactory.decodeResource(resources, R.drawable.store_idea_brainstorm), "ShareImage", "")
        }

        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Store Details")
        shareIntent.putExtra(Intent.EXTRA_TEXT, store.name + "\n" + store.mobileNumber + "\n" + store.address + "\n " + "(" + store.latitude + ", " + store.longitude + ")" + "\n")
//        shareIntent.putExtra(Intent.EXTRA_STREAM, uriPath)
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(bitmapPath))
        shareIntent.type = "image*//*"

        startActivity(Intent.createChooser(shareIntent, "Select an option to share"))
    }

    private fun isSharePermissionsAvailable(): Boolean {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun isCallPermissionsAvailable(): Boolean {

        return (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED)
    }

    fun startPhoneCall(position: Int, mobileNumber: String) {
        var intent = Intent(Intent.ACTION_DIAL)
        intent.data = (Uri.parse("tel:" + mobileNumber))
        startActivity(intent)
    }


}