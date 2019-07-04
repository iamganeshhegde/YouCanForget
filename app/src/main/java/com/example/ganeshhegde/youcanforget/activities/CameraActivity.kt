package com.example.ganeshhegde.youcanforget.activities

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.*
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.example.ganeshhegde.youcanforget.R
import com.example.ganeshhegde.youcanforget.databinding.ActivityCameraBinding
import kotlinx.android.synthetic.main.activity_camera.view.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CameraActivity : BaseActivity() {

    lateinit var activityCameraBinding: ActivityCameraBinding
    lateinit var rootView: View

    private var takePictureButton: Button? = null
    private var textureView: TextureView? = null

    private var cameraId: String? = null
    protected var cameraDevice: CameraDevice? = null
    protected lateinit var cameraCaptureSessions: CameraCaptureSession
    protected var captureRequest: CaptureRequest? = null
    protected lateinit var captureRequestBuilder: CaptureRequest.Builder
    private var imageDimension: Size? = null
    private var imageReader: ImageReader? = null
    private val file: File? = null
    private val mFlashSupported: Boolean = false
    private var mBackgroundHandler: Handler? = null
    private var mBackgroundThread: HandlerThread? = null
    var count = 0;
     var outputArray:ArrayList<Any> = ArrayList<Any>()
    var limit = 3


    var GANESHMETHOD = "GaneshMethod";

    internal var textureListener: TextureView.SurfaceTextureListener = object : TextureView.SurfaceTextureListener {
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            //open your camera here
            openCamera()
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            // Transform you image captured size according to the surface width and height
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            return false
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }
    private val stateCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            //This is called when the camera is open
            Log.e(TAG, "onOpened")
            cameraDevice = camera
            createCameraPreview()
        }

        override fun onDisconnected(camera: CameraDevice) {
            cameraDevice!!.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            cameraDevice!!.close()
            cameraDevice = null
        }
    }
    internal val captureCallbackListener: CameraCaptureSession.CaptureCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
            super.onCaptureCompleted(session, request, result)
            Toast.makeText(this@CameraActivity, "Saved:" + file!!, Toast.LENGTH_SHORT).show()
            createCameraPreview()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        activityCameraBinding = DataBindingUtil.setContentView(this, R.layout.activity_camera)
        activityCameraBinding.handlers = this
        rootView = activityCameraBinding.root


        textureView = rootView.picture_view
//        findViewById(R.id.picture_view) as TextureView
        assert(textureView != null)
        textureView!!.surfaceTextureListener = textureListener
        takePictureButton = findViewById(R.id.take_picture) as Button
        assert(takePictureButton != null)
        takePictureButton!!.setOnClickListener {
            takePicture()
//            createInputArray()

        }
    }

   /* private fun createInputArray() {
        var inputArray = ArrayList<Any>()
        inputArray.add(1)
        inputArray.add(2)
        inputArray.add(3)

        var a = ArrayList<Any>()
        a.add(4)
        a.add(5)
        a.add(6)

        var b = ArrayList<Any>()
        b.add(7)
        b.add(8)
        b.add(9)

        var c = ArrayList<Any>()
        c.add(10)
        c.add(11)
        c.add(12)

        var d = ArrayList<Any>()
        d.add(13)
        d.add(14)
        d.add(15)

        var e = ArrayList<Int>()
        e.add(16)
        e.add(17)
        e.add(18)

        d.add(e)
        c.add(d)
        b.add(c)
        a.add(b)
        inputArray.add(a)

        Log.i(GANESHMETHOD,inputArray.toString())

        callRecursive(inputArray)

    }*/

    /*private fun callRecursive(inputArray: ArrayList<Any>) {
        count++

        for(i in 0.. inputArray.size-2)
        {
            outputArray.add(inputArray.get(i))
        }

            if(count > limit)
            {
                outputArray.add(inputArray.get(3))
            }else
            {
//                outputArray.add(inputArray.get(i))
                callRecursive(inputArray.get(3) as ArrayList<Any>)
            }

     *//*   if(count <=limit)
        {
            callRecursive(inputArray.get(3) as ArrayList<Any>)
        }*//*


        Log.i(GANESHMETHOD+"output",outputArray.toString())

    }*/

    protected fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("Camera Background")
        mBackgroundThread!!.start()
        mBackgroundHandler = Handler(mBackgroundThread!!.looper)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    protected fun stopBackgroundThread() {
        mBackgroundThread!!.quitSafely()
        try {
            mBackgroundThread!!.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    protected fun takePicture() {
        if (null == cameraDevice) {
            Log.e(TAG, "cameraDevice is null")
            return
        }
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val characteristics = manager.getCameraCharacteristics(cameraDevice!!.id)
            var jpegSizes: Array<Size>? = null
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!.getOutputSizes(ImageFormat.JPEG)
            }
            var width = 640
            var height = 480
            if (jpegSizes != null && 0 < jpegSizes.size) {
                width = jpegSizes[0].width
                height = jpegSizes[0].height
            }
            val reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1)
            val outputSurfaces = ArrayList<Surface>(2)
            outputSurfaces.add(reader.surface)
            outputSurfaces.add(Surface(textureView!!.surfaceTexture))
            val captureBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(reader.surface)
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
            // Orientation
            val rotation = windowManager.defaultDisplay.rotation
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation))
//            val file = File(Environment.getExternalStorageDirectory().toString() + "/"+Environment.DIRECTORY_PICTURES+ "/pic.jpg")
//            val file = File(Environment.getExternalStorageDirectory().toString() + "/" + Environment.DIRECTORY_PICTURES + getFileName())

            val path = Environment.getExternalStorageDirectory().absolutePath +"/YouCanForget"
            val directory = File(path)

            if(!directory.exists())
            {
                directory.mkdirs()
            }

            val file = File(directory, getFileName()+".jpg")


            val readerListener = object : ImageReader.OnImageAvailableListener {
                override fun onImageAvailable(reader: ImageReader) {
                    var image: Image? = null
                    try {
                        image = reader.acquireLatestImage()
                        val buffer = image!!.planes[0].buffer
                        val bytes = ByteArray(buffer.capacity())
                        buffer.get(bytes)
                        save(bytes)
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        image?.close()
                    }
                }

                @Throws(IOException::class)
                private fun save(bytes: ByteArray) {
                    var output: OutputStream? = null
                    try {
                        output = FileOutputStream(file)
                        output!!.write(bytes)
                    } finally {
                        output?.close()
                    }
                }
            }
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler)
            val captureListener = object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
                    super.onCaptureCompleted(session, request, result)
                    Toast.makeText(this@CameraActivity, "Saved:$file", Toast.LENGTH_SHORT).show()

                    var intent = Intent()
                    intent.putExtra("IMAGE_PATH", file.toString())
                    setResult(1000, intent)

                    finish()
//                    createCameraPreview()

                }
            }
            cameraDevice!!.createCaptureSession(outputSurfaces, object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler)
                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                    }

                }

                override fun onConfigureFailed(session: CameraCaptureSession) {}
            }, mBackgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    protected fun createCameraPreview() {
        try {
            val texture = textureView!!.surfaceTexture!!
            texture.setDefaultBufferSize(imageDimension!!.width, imageDimension!!.height)
            val surface = Surface(texture)
            captureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder.addTarget(surface)
            cameraDevice!!.createCaptureSession(Arrays.asList(surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession
                    updatePreview()
                }

                override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                    Toast.makeText(this@CameraActivity, "Configuration change", Toast.LENGTH_SHORT).show()
                }
            }, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun openCamera() {
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        Log.e(TAG, "is camera open")
        try {
            cameraId = manager.cameraIdList[0]
            val characteristics = manager.getCameraCharacteristics(cameraId!!)
            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
            imageDimension = map.getOutputSizes(SurfaceTexture::class.java)[0]
            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this@CameraActivity, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CAMERA_PERMISSION)
                return
            }
            manager.openCamera(cameraId!!, stateCallback, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

        Log.e(TAG, "openCamera X")
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    protected fun updatePreview() {
        if (null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return")
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun closeCamera() {
        if (null != cameraDevice) {
            cameraDevice!!.close()
            cameraDevice = null
        }
        if (null != imageReader) {
            imageReader!!.close()
            imageReader = null
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(this@CameraActivity, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onResume() {
        super.onResume()
        Log.e(TAG, "onResume")
        startBackgroundThread()
        if (textureView!!.isAvailable) {
            openCamera()
        } else {
            textureView!!.surfaceTextureListener = textureListener
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onPause() {
        Log.e(TAG, "onPause")
        //closeCamera();
        stopBackgroundThread()
        super.onPause()
    }

    companion object {
        private val TAG = "AndroidCameraApi"
        private val ORIENTATIONS = SparseIntArray()

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }

        private val REQUEST_CAMERA_PERMISSION = 200
    }

    private fun getFileName(): String {

        var timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        return "IMG_" + timeStamp
    }

    fun mainTest(array: Array<Serializable>, limit: Int)
    {



        for(i in  0..limit)
        {
            for (j in 0..array.size)
            {
                if(j.equals(Int))
                {

                }
                else
                {
                    saveResultArray(j)
                }
            }
        }
    }

    private fun saveResultArray(j: Int) {

        var b:ArrayList<Int> = ArrayList()

        b.add(j)
        Log.i(GANESHMETHOD,b.toString())
    }

}


/*

package com.example.ganeshhegde.youcanforget.activities

import android.annotation.SuppressLint
import android.content.Context
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.*
import android.support.annotation.RequiresApi
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import android.view.View
import com.example.ganeshhegde.youcanforget.R
import com.example.ganeshhegde.youcanforget.databinding.ActivityCameraBinding
import junit.framework.Assert
import kotlinx.android.synthetic.main.activity_camera.*
import kotlinx.android.synthetic.main.activity_camera.view.*
import kotlinx.android.synthetic.main.activity_save_details.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.Exception
import java.lang.reflect.Array
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CameraActivity : BaseActivity() {

    lateinit var rootView: View
    lateinit var activityCameraBinding: ActivityCameraBinding
    lateinit var cameraManager: CameraManager
    var cameraDevice: CameraDevice? = null
    var backgroundHandler: Handler? = null
    var backgroundHandlerThread: HandlerThread? = null
    val TAG = CameraActivity::class.java.name
    lateinit var textureView: TextureView
    var ORIENTATIONS = SparseIntArray()
    lateinit var imageDimension: Size
    lateinit var captureRequestBuilder: CaptureRequest.Builder
    lateinit var cameraCaptureSession: CameraCaptureSession
    lateinit var cameraId: String
    lateinit var file:File

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        activityCameraBinding = DataBindingUtil.setContentView(this, R.layout.activity_camera)
//        setContentView(R.layout.activity_camera)

        activityCameraBinding.handlers = this
        rootView = activityCameraBinding.root

        ORIENTATIONS.append(Surface.ROTATION_0, 90)
        ORIENTATIONS.append(Surface.ROTATION_90, 0)
        ORIENTATIONS.append(Surface.ROTATION_180, 270)
        ORIENTATIONS.append(Surface.ROTATION_270, 180)


        textureView = rootView.picture_view

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        var cameraFacing = CameraCharacteristics.LENS_FACING_BACK


    }

    var surfaceTextureListener: TextureView.SurfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {


        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
            return false
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            openCamera()

        }

    }

    var stateCallBack: CameraDevice.StateCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice?) {

            cameraDevice = camera
            createCameraPreview()
        }

        override fun onDisconnected(camera: CameraDevice?) {
            cameraDevice?.close()
        }

        override fun onError(camera: CameraDevice?, error: Int) {

            cameraDevice?.close()
            cameraDevice = null
        }

    }

    var captureCallbackListener: CameraCaptureSession.CaptureCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : CameraCaptureSession.CaptureCallback() {

        override fun onCaptureCompleted(session: CameraCaptureSession?, request: CaptureRequest?, result: TotalCaptureResult?) {
            super.onCaptureCompleted(session, request, result)

//            showToast(getFileName())
            createCameraPreview()
        }

    }

    fun startBackgroundThread() {

        backgroundHandlerThread = HandlerThread("Camera Background")
        backgroundHandlerThread!!.start()
        backgroundHandler = Handler(backgroundHandlerThread!!.looper)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun stopBackgroundThread() {
        backgroundHandlerThread?.quitSafely()


        backgroundHandlerThread?.join()
        backgroundHandlerThread = null
        backgroundHandler = null

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun createCameraPreview() {

        try {

            var texture: SurfaceTexture = textureView.surfaceTexture

            assert(texture != null)

            texture.setDefaultBufferSize(imageDimension.width, imageDimension.height)

            var surface = Surface(texture)
            captureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)

            captureRequestBuilder.addTarget(surface)

            cameraDevice!!.createCaptureSession(Arrays.asList(surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigureFailed(session: CameraCaptureSession?) {
                    showToast("Camera config failed")
                }

                override fun onConfigured(session: CameraCaptureSession?) {
                    if (cameraDevice == null) {
                        return
                    }

                    cameraCaptureSession = session!!

                    updatePreview()
                }

            }, null)


        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun updatePreview() {


        if (cameraDevice == null) {
            showLog(TAG, "Update Preview Error")
            return
        }

        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)


        try {
            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun closeCamera() {
        if (cameraDevice != null) {
            cameraDevice!!.close()
            cameraDevice = null
        }


    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun openCamera() {

        try {
            cameraId = cameraManager.cameraIdList[0]
            var characteristics = cameraManager.getCameraCharacteristics(cameraId)
            var map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

            assert(map != null)

            imageDimension = map.getOutputSizes(SurfaceTexture::class.java)[0]

            cameraManager.openCamera(cameraId, stateCallBack, null)

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onResume() {
        super.onResume()

        startBackgroundThread()

        if (textureView.isAvailable) {
            openCamera()
        } else {
            textureView.surfaceTextureListener = surfaceTextureListener
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onPause() {
        super.onPause()

        stopBackgroundThread()
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun onClick(view: View) {
        when (view.id) {
            R.id.take_picture -> {
                takePicture()
//                saveImageToExternalDirectory()
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun takePicture() {

        if (cameraDevice == null) {
            showLog(TAG, "Camera Device Null")
            return
        }

        try {


            var characteristics: CameraCharacteristics = cameraManager.getCameraCharacteristics(cameraDevice!!.id)

            var jpegSizes: kotlin.Array<Size>? = null

            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG)

            }

            var width = 640
            var height = 480

            if (jpegSizes != null && jpegSizes.size > 0) {
                width = jpegSizes[0].width
                height = jpegSizes[0].height
            }

            var reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1)

            var outputSurfaces = ArrayList<Surface>(2)

            outputSurfaces.add(reader.surface)
            outputSurfaces.add(Surface(textureView.surfaceTexture))

            var captureBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)

            captureBuilder.addTarget(reader.surface)
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)


            //orientation
            var rotation = windowManager.defaultDisplay.rotation

            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation))

//            var file = File(saveImageToExternalDirectoryPath())
            file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),getFileName())
//            var file = File(Environment.getExternalStorageDirectory().toString()+"/pic.jpg")




            reader.setOnImageAvailableListener(object : ImageReader.OnImageAvailableListener {
                override fun onImageAvailable(reader: ImageReader?) {

                    var image = reader!!.acquireLatestImage()


                    try {
                        if (reader != null) {
                            image = reader.acquireLatestImage()
                        }

                        var buffer:ByteBuffer = image!!.planes[0].buffer
//                            var bytes: ByteArray = ByteArray(buffer.capacity())
                        var bytes: ByteArray = ByteArray(buffer.capacity())


                        buffer.get(bytes)

                        save(bytes)


                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        if (image != null) {
                            image.close()
                        }
                    }
                }

            },backgroundHandler)







          */
/*  var readerListener = ImageReader.OnImageAvailableListener {
                object : ImageReader.OnImageAvailableListener {
                    override fun onImageAvailable(reader: ImageReader?) {

                        var image: Image? = null

                        try {
                            if (reader != null) {
                                image = reader.acquireLatestImage()
                            }

                            var buffer:ByteBuffer = image!!.planes[0].buffer
//                            var bytes: ByteArray = ByteArray(buffer.capacity())
                            var bytes: ByteArray = ByteArray(buffer.capacity())


                            buffer.get(bytes)

                            save(bytes)


                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            if (image != null) {
                                image.close()
                            }
                        }

                    }

                    @Throws(IOException::class)
                    private fun save(bytes: ByteArray) {

                        var outputStream: OutputStream? = null

                        try {
                            outputStream = FileOutputStream(file).apply {
                                write(bytes)
                            }


                            textureView.bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream)

                        } finally {
                            if (outputStream != null) {
                                outputStream.close()
                            }
                        }

                    }

                }
            }


            reader.setOnImageAvailableListener(readerListener, backgroundHandler)

*//*

            var captureListener: CameraCaptureSession.CaptureCallback = object : CameraCaptureSession.CaptureCallback() {

                override fun onCaptureCompleted(session: CameraCaptureSession?, request: CaptureRequest?, result: TotalCaptureResult?) {
                    super.onCaptureCompleted(session, request, result)

                    showToast("saved :" + file)
                    createCameraPreview()

                }
            }


            cameraDevice!!.createCaptureSession(outputSurfaces, object : CameraCaptureSession.StateCallback() {
                override fun onConfigureFailed(session: CameraCaptureSession?) {
                }

                override fun onConfigured(session: CameraCaptureSession?) {

                    if (session != null) {
                        session.capture(captureBuilder.build(), captureListener, backgroundHandler)
                    }

                }

            }, backgroundHandler)


        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    private fun saveImageToExternalDirectoryPath(): String {

        var directoryPath = getDirectoryPath()
        var fileName = getFileName()

        var imageFullPath = directoryPath + "/" + fileName + ".jpg"

        return imageFullPath

    }

    private fun getDirectoryPath(): String {

        var mediaStorageDest: File
        var newName = "You Can Forget"

        mediaStorageDest = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), newName)

        if (!mediaStorageDest.exists()) {
            if (!mediaStorageDest.mkdirs()) {
                showToast("Failed to create directory")
            } else {

            }

        }
        return mediaStorageDest.path

    }

    private fun getFileName(): String {

        var timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        return "IMG_" + timeStamp

    }



    private fun save(bytes: ByteArray) {

        var outputStream: OutputStream? = null

        try {
            outputStream = FileOutputStream(file).apply {
                write(bytes)
            }


            textureView.bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream)

        } finally {
            if (outputStream != null) {
                outputStream.close()
            }
        }



    }

}

    */
