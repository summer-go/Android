package com.android.samples.camera1;

import java.io.IOException;
import java.util.List;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements Handler.Callback{

    private static final int MSG_OPEN_CAMERA = 1;
    private static final int MSG_CLOSE_CAMERA = 2;
    private static final int MSG_SET_PREVIEW_SIZE = 3;
    private static final int MSG_SET_PREVIEW_SURFACE = 4;
    private static final int MSG_START_PREVIEW = 5;
    private static final int MSG_STOP_PREVIEW = 6;
    private static final int MSG_SET_PICTURE_SIZE = 7;
    private static final int MSG_TAKE_PICTURE = 8;


    private static final int REQUEST_PERMISSIONS_CODE = 1;
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final String TAG = MainActivity.class.getSimpleName();

    int numberOfCameras = Camera.getNumberOfCameras();// 获取摄像头个数
    private DeviceOrientationListener mDeviceOrientationListener;
    private HandlerThread mCameraThread;
    private Handler mCameraHandler;

    private Camera mCamera;
    private int mCameraId = -1;
    private Camera.CameraInfo mCameraInfo;

    private SurfaceHolder mPreviewSurface;
    private int mPreviewSurfaceWidth;
    private int mPreviewSurfaceHeight;

    private static final int PREVIEW_FORMAT = ImageFormat.NV21;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_main);

        mDeviceOrientationListener = new DeviceOrientationListener(this);

        startCameraThread();

        initCameraInfo();

        SurfaceView cameraPreview = findViewById(R.id.camera_preview);
        cameraPreview.getHolder().addCallback(new PreviewSurfaceCallback());

        Button switchCameraButton = findViewById(R.id.switch_camera);
        switchCameraButton.setOnClickListener(new OnSwitchCameraButtonClickListener());

        Button takePictureButton = findViewById(R.id.take_picture);
        takePictureButton.setOnClickListener(new OnTakePictureButtonClickListener());
    }

    private class OnTakePictureButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            takePicture();
            restartPreview();
        }
    }

    /**
     * 拍照。
     */
    private void takePicture() {
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            mCamera.setParameters(parameters);
            mCamera.takePicture(new ShutterCallback(), new RawCallback(), new PostviewCallback(), new JpegCallback());
        }
    }

    private class ShutterCallback implements Camera.ShutterCallback {
        @Override
        public void onShutter() {
            Log.d(TAG, "onShutter() called");
        }
    }

    private class RawCallback implements Camera.PictureCallback {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "On raw taken.");
        }
    }

    private class PostviewCallback implements Camera.PictureCallback {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "On postview taken.");
        }
    }

    private class JpegCallback implements Camera.PictureCallback {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "On jpeg taken.");
        }
    }

    private class OnSwitchCameraButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            if (mCameraHandler != null && mPreviewSurface != null) {
                int cameraId = switchCameraId();// 切换摄像头 ID
                mCameraHandler.sendEmptyMessage(MSG_STOP_PREVIEW);// 停止预览
                mCameraHandler.sendEmptyMessage(MSG_CLOSE_CAMERA);// 关闭当前的摄像头
                mCameraHandler.obtainMessage(MSG_OPEN_CAMERA, cameraId, 0).sendToTarget();// 开启新的摄像头
                mCameraHandler.obtainMessage(MSG_SET_PREVIEW_SIZE, mPreviewSurfaceWidth, mPreviewSurfaceHeight).sendToTarget();// 配置预览尺寸
                mCameraHandler.obtainMessage(MSG_SET_PICTURE_SIZE, mPreviewSurfaceWidth, mPreviewSurfaceHeight).sendToTarget();// 配置照片尺寸
                mCameraHandler.obtainMessage(MSG_SET_PREVIEW_SURFACE, mPreviewSurface).sendToTarget();// 配置预览 Surface
                mCameraHandler.sendEmptyMessage(MSG_START_PREVIEW);// 开启预览
            }
        }
    }

    private void restartPreview(){
        mCameraHandler.sendEmptyMessage(MSG_STOP_PREVIEW);// 停止预览
        mCameraHandler.sendEmptyMessage(MSG_CLOSE_CAMERA);// 关闭当前的摄像头
        mCameraHandler.obtainMessage(MSG_OPEN_CAMERA, getCameraId(), 0).sendToTarget();// 开启新的摄像头
        mCameraHandler.obtainMessage(MSG_SET_PREVIEW_SIZE, mPreviewSurfaceWidth, mPreviewSurfaceHeight).sendToTarget();// 配置预览尺寸
        mCameraHandler.obtainMessage(MSG_SET_PICTURE_SIZE, mPreviewSurfaceWidth, mPreviewSurfaceHeight).sendToTarget();// 配置照片尺寸
        mCameraHandler.obtainMessage(MSG_SET_PREVIEW_SURFACE, mPreviewSurface).sendToTarget();// 配置预览 Surface
        mCameraHandler.sendEmptyMessage(MSG_START_PREVIEW);// 开启预览
    }

    /**
     * 切换前后置时切换ID
     */
    private int switchCameraId() {
        if (mCameraId == mFrontCameraId && hasBackCamera()) {
            return mBackCameraId;
        } else if (mCameraId == mBackCameraId && hasFrontCamera()) {
            return mFrontCameraId;
        } else {
            throw new RuntimeException("No available camera id to switch.");
        }
    }

    private void startCameraThread() {
        mCameraThread = new HandlerThread("CameraThread");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper(), this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "----- onStart ----");

        if (mDeviceOrientationListener != null) {
            mDeviceOrientationListener.enable();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "----- onResume ----");

        if (!isRequiredPermissionsGranted() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_PERMISSIONS_CODE);
        } else if (mCameraHandler != null) {
            mCameraHandler.obtainMessage(MSG_OPEN_CAMERA, getCameraId(), 0).sendToTarget();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "----- onPause ----");

        closeCamera();
    }

    private int getCameraId(){
        if (hasFrontCamera()){
            return mFrontCameraId;
        } else if (hasBackCamera()) {
            return mBackCameraId;
        } else {
            throw new RuntimeException("No available camera id found.");
        }
    }

    /**
     * 判断是否有后置摄像头。
     *
     * @return true 代表有后置摄像头
     */
    private boolean hasBackCamera() {
        return mBackCameraId != -1;
    }

    /**
     * 判断是否有前置摄像头。
     *
     * @return true 代表有前置摄像头
     */
    private boolean hasFrontCamera() {
        return mFrontCameraId != -1;
    }


    /**
     * 关闭相机。
     */
    private void closeCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 判断我们需要的权限是否被授予，只要有一个没有授权，我们都会返回 false。
     *
     * @return true 权限都被授权
     */
    private boolean isRequiredPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    private Camera.CameraInfo mFrontCameraInfo = null;
    private int mFrontCameraId = -1;

    @Nullable
    private Camera.CameraInfo mBackCameraInfo = null;
    private int mBackCameraId = -1;

    /**
     * 初始化摄像头信息。
     */
    private void initCameraInfo() {
        Log.d(TAG, "---- initCameraInfo  ---- ");

        int numberOfCameras = Camera.getNumberOfCameras();// 获取摄像头个数
        for (int cameraId = 0; cameraId < numberOfCameras; cameraId++) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, cameraInfo);
            Log.d(TAG, "cameraId :" + cameraId + " orientation : " + cameraInfo.orientation + " facing " + cameraInfo.facing);

            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                // 后置摄像头信息
                mBackCameraId = cameraId;
                mBackCameraInfo = cameraInfo;
            } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
                // 前置摄像头信息
                mFrontCameraId = cameraId;
                mFrontCameraInfo = cameraInfo;
            }
        }
    }

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        switch (msg.what) {
            case MSG_OPEN_CAMERA: {
                Log.d(TAG, "----MSG_OPEN_CAMERA----");
                openCamera(msg.arg1);
                break;
            }
            case MSG_CLOSE_CAMERA: {
                Log.d(TAG, "----MSG_CLOSE_CAMERA----");
                closeCamera();
                break;
            }
            case MSG_SET_PREVIEW_SIZE: {
                Log.d(TAG, "----MSG_SET_PREVIEW_SIZE----");
                int shortSide = msg.arg1;
                int longSide = msg.arg2;
                setPreviewSize(shortSide, longSide);
                break;
            }
            case MSG_SET_PREVIEW_SURFACE: {
                Log.d(TAG, "----MSG_SET_PREVIEW_SURFACE----");
                SurfaceHolder previewSurface = (SurfaceHolder) msg.obj;
                setPreviewSurface(previewSurface);
                break;
            }
            case MSG_START_PREVIEW: {
                Log.d(TAG, "----MSG_START_PREVIEW----");
                startPreview();
                break;
            }
            case MSG_STOP_PREVIEW: {
                stopPreview();
                break;
            }
            case MSG_SET_PICTURE_SIZE: {
                int shortSide = msg.arg1;
                int longSide = msg.arg2;
                setPictureSize(shortSide,longSide);
                break;
            }
            case MSG_TAKE_PICTURE: {
                takePicture();
                break;
            }
            default:
                throw new IllegalArgumentException("Illegal message: " + msg.what);
        }
        return false;
    }

    /**
     * 根据指定的尺寸要求设置照片尺寸，我们会考虑指定尺寸的比例，并且去符合比例的最大尺寸作为照片尺寸。
     *
     * @param shortSide 短边长度
     * @param longSide  长边长度
     */
    private void setPictureSize(int shortSide, int longSide) {
        Camera camera = mCamera;
        if (camera != null && shortSide != 0 && longSide != 0) {
            float aspectRatio = (float) longSide / shortSide;
            Camera.Parameters parameters = camera.getParameters();
            List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
            for (Camera.Size pictureSize : supportedPictureSizes) {
                if ((float) pictureSize.width / pictureSize.height == aspectRatio) {
                    parameters.setPictureSize(pictureSize.width, pictureSize.height);
                    camera.setParameters(parameters);
                    Log.d(TAG, "setPictureSize() called with: width = " + pictureSize.width + "; height = " + pictureSize.height);
                    break;
                }
            }
        }
    }


    /**
     * 停止预览。
     */
    private void stopPreview() {
        Camera camera = mCamera;
        if (camera != null) {
            camera.stopPreview();
            Log.d(TAG, "stopPreview() called");
        }
    }

    private void setPreviewSize(int shortSide, int longSide) {
        if (mCamera != null && shortSide != 0 && longSide != 0){
            float aspectRatio = (float)longSide / shortSide;
            Camera.Parameters parameters = mCamera.getParameters();
            List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
            for (Camera.Size previewSize : supportedPreviewSizes) {
                if((float)previewSize.width / previewSize.height == aspectRatio  && previewSize.height <= shortSide && previewSize.width <= longSide) {
//                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
                    String str = parameters.getFocusMode();
                    parameters.setPreviewSize(previewSize.width, previewSize.height);
                    Log.d(TAG, "setPreviewSize() called with: with = " + previewSize.width + "  height = " + previewSize.height);
                    List<String> aa = parameters.getSupportedFocusModes();
                    Log.d(TAG, "str : " + str);
                    if(isPreviewFormatSupported(parameters, PREVIEW_FORMAT)){
                        parameters.setPreviewFormat(PREVIEW_FORMAT);
                        int frameWidth = previewSize.width;
                        int frameHeight = previewSize.height;
                        int previewFormat = parameters.getPreviewFormat();
                        PixelFormat pixelFormat = new PixelFormat();
                        PixelFormat.getPixelFormatInfo(previewFormat, pixelFormat);
                        int bufferSize = (frameWidth * frameHeight * pixelFormat.bitsPerPixel) / 8;
                        mCamera.addCallbackBuffer(new byte[bufferSize]);
                        mCamera.addCallbackBuffer(new byte[bufferSize]);
                        mCamera.addCallbackBuffer(new byte[bufferSize]);
                        Log.d(TAG, "Add three callback buffers with size: " + bufferSize);
                    }

                    mCamera.setParameters(parameters);
                }
            }
        }
    }

    private boolean isPreviewFormatSupported(Camera.Parameters parameters, int format) {
        List<Integer> supportedPreviewFormats = parameters.getSupportedPreviewFormats();
        return supportedPreviewFormats != null && supportedPreviewFormats.contains(format);
    }

    private void setPreviewSurface(SurfaceHolder previewSurface) {
        if (mCamera != null && previewSurface != null) {
            try {
                mCamera.setPreviewDisplay(previewSurface);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startPreview() {
        if (mCamera != null && mPreviewSurface != null) {
            mCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    // 使用完buffer之后回收复用
                    camera.addCallbackBuffer(data);
                }
            });
            mCamera.startPreview();
        }
    }


    private void openCamera(int cameraId) {
        if(mCamera != null){
            throw new RuntimeException("You must close previous camera before open a new one");
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            mCamera = Camera.open(cameraId);
            mCameraId = cameraId;
            mCameraInfo = cameraId == mFrontCameraId ? mFrontCameraInfo : mBackCameraInfo;
            Log.d(TAG, "Camera[" + cameraId + "] has been opened");
//            mCamera.setDisplayOrientation(getCameraDisplayOrientation(mCameraInfo));
        }
    }

    private int getCameraDisplayOrientation(Camera.CameraInfo cameraInfo) {
        Log.d(TAG, "----- getCameraDisplayOrientation ----");

        int roration = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (roration) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;

            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;

        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
            result = (cameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (cameraInfo.orientation - degrees + 360) %360;
        }
        Log.d(TAG, "degrees: " + degrees + "  orientation: " + cameraInfo.orientation + " result =  " + result);

        return result;
    }

    private class DeviceOrientationListener extends OrientationEventListener {

        public DeviceOrientationListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {

        }
    }

    private class PreviewSurfaceCallback implements SurfaceHolder.Callback{

        @Override
        public void surfaceCreated(SurfaceHolder holder) {

        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.d(TAG,"surfaceChanged " + "format : " + format + " width : " + width + " height : " + height);
            mPreviewSurface = holder;
            mPreviewSurfaceWidth = width;
            mPreviewSurfaceHeight = height;
            if(mCameraHandler != null){
                mCameraHandler.obtainMessage(MSG_SET_PREVIEW_SIZE, width, height).sendToTarget();
                mCameraHandler.obtainMessage(MSG_SET_PICTURE_SIZE).sendToTarget();
                mCameraHandler.obtainMessage(MSG_SET_PREVIEW_SURFACE, holder).sendToTarget();
                mCameraHandler.sendEmptyMessage(MSG_START_PREVIEW);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.d(TAG,"surfaceDestroyed ");

            mPreviewSurface = null;
            mPreviewSurfaceWidth = 0;
            mPreviewSurfaceHeight = 0;
        }
    }
}