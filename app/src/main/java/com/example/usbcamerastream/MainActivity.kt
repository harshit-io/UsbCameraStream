package com.example.usbcamerastream

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import com.serenegiant.usb.USBMonitor
import com.serenegiant.usb.UVCCamera

class MainActivity : AppCompatActivity(), SurfaceHolder.Callback {

    private lateinit var surfaceView: SurfaceView
    private lateinit var usbManager: UsbManager
    private var camera: UVCCamera? = null
    private var controlBlock: USBMonitor.UsbControlBlock? = null
    private lateinit var usbMonitor: USBMonitor
    private val TAG = "USB_CAM"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        surfaceView = findViewById(R.id.surfaceView)
        surfaceView.holder.addCallback(this)
        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        usbMonitor = USBMonitor(this, object : USBMonitor.OnDeviceConnectListener {

            override fun onAttach(device: UsbDevice) {
                Log.d(TAG, "USB device attached: ${device.deviceName}")
                usbMonitor.requestPermission(device)
                Log.d(TAG, "USB permission requested")
            }

            override fun onDettach(device: UsbDevice) {
                Log.d(TAG, "USB device detached: ${device.deviceName}")
            }

            override fun onConnect(
                device: UsbDevice,
                ctrlBlock: USBMonitor.UsbControlBlock,
                createNew: Boolean
            ) {
                Log.d(TAG, "USB device connected: ${device.deviceName}")
                controlBlock = ctrlBlock
                openCamera()
            }

            override fun onDisconnect(device: UsbDevice, ctrlBlock: USBMonitor.UsbControlBlock) {
                Log.d(TAG, "USB device disconnected: ${device.deviceName}")
                closeCamera()
            }

            override fun onCancel(device: UsbDevice) {
                Log.d(TAG, "USB permission canceled: ${device.deviceName}")
            }
        })

    }

    override fun onStart() {
        super.onStart()
        usbMonitor.register()
    }

    override fun onStop() {
        usbMonitor.unregister()
        super.onStop()
    }
    private fun openCamera() {
        try {
            Log.d(TAG, "Opening UVC camera")

            camera = UVCCamera().apply {
                open(controlBlock)
                Log.d(TAG, "Camera opened successfully")

                setPreviewSize(640, 480, UVCCamera.FRAME_FORMAT_MJPEG)
                Log.d(TAG, "Preview size set: 640x480 MJPEG")

                setPreviewDisplay(surfaceView.holder.surface)
                Log.d(TAG, "Surface attached")

                startPreview()
                Log.d(TAG, "Preview started")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open camera", e)
        }
    }
    private fun closeCamera() {
        camera?.stopPreview()
        camera?.destroy()
        camera = null
    }
    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d(TAG, "Surface created")
    }
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.d(TAG, "Surface changed: $width x $height")
    }
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.d(TAG, "Surface destroyed")
    }
}