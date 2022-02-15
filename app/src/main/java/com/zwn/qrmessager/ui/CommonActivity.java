package com.zwn.qrmessager.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.huawei.hms.ml.scan.HmsScan;
import com.huawei.hms.ml.scan.HmsScanAnalyzer;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.zwn.qrmessager.MainActivity;
import com.zwn.qrmessager.R;
import com.zwn.qrmessager.util.CameraOperation;
import com.zwn.qrmessager.util.CommonHandler;
import com.zwn.qrmessager.util.draw.ScanResultView;

import java.io.IOException;
import java.util.Objects;

public class CommonActivity extends AppCompatActivity {

//    public static final int REQUEST_CODE_PHOTO = 0X1113;
    private static final String TAG = "CommonActivity";
    private SurfaceHolder surfaceHolder;
    private CameraOperation cameraOperation;
    private SurfaceCallBack surfaceCallBack;
    private CommonHandler handler;
    private boolean isShow;
    public static final String SCAN_RESULT = "scanResult";
    public ScanResultView scanResultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_common);

        cameraOperation = new CameraOperation();
        surfaceCallBack = new SurfaceCallBack();
        SurfaceView cameraPreview = findViewById(R.id.surfaceView);
        adjustSurface(cameraPreview);
        surfaceHolder = cameraPreview.getHolder();
        isShow = false;
        setBackOperation();
        scanResultView = findViewById(R.id.scan_result_view);
    }

    private void adjustSurface(SurfaceView cameraPreview) {
        FrameLayout.LayoutParams paramSurface = (FrameLayout.LayoutParams) cameraPreview.getLayoutParams();
        if (getSystemService(Context.WINDOW_SERVICE) != null) {
            WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            Display defaultDisplay = windowManager.getDefaultDisplay();
            Point outPoint = new Point();
            defaultDisplay.getRealSize(outPoint);
            float sceenWidth = outPoint.x;
            float sceenHeight = outPoint.y;
            float rate;
            if (sceenWidth / (float) 1080 > sceenHeight / (float) 1920) {
                rate = sceenWidth / (float) 1080;
                int targetHeight = (int) (1920 * rate);
                paramSurface.width = FrameLayout.LayoutParams.MATCH_PARENT;
                paramSurface.height = targetHeight;
                int topMargin = (int) (-(targetHeight - sceenHeight) / 2);
                if (topMargin < 0) {
                    paramSurface.topMargin = topMargin;
                }
            } else {
                rate = sceenHeight / (float) 1920;
                int targetWidth = (int) (1080 * rate);
                paramSurface.width = targetWidth;
                paramSurface.height = FrameLayout.LayoutParams.MATCH_PARENT;
                int leftMargin = (int) (-(targetWidth - sceenWidth) / 2);
                if (leftMargin < 0) {
                    paramSurface.leftMargin = leftMargin;
                }
            }
        }
    }

    private void setBackOperation() {
        ImageView backBtn = findViewById(R.id.back_img);
        backBtn.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            CommonActivity.this.finish();
        });
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        CommonActivity.this.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isShow) {
            initCamera();
        } else {
            surfaceHolder.addCallback(surfaceCallBack);
        }
    }

    @Override
    protected void onPause() {
        if (handler != null) {
            handler.quit();
            handler = null;
        }
        cameraOperation.close();
        if (!isShow) {
            surfaceHolder.removeCallback(surfaceCallBack);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initCamera() {
        try {
            cameraOperation.open(surfaceHolder);
            if (handler == null) {
                handler = new CommonHandler(CommonActivity.this, cameraOperation);
            }
        } catch (IOException e) {
            Log.w(TAG, e);
        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode != RESULT_OK || data == null || requestCode != REQUEST_CODE_PHOTO) {
//            return;
//        }
//        try {
//            System.out.println('1');
//            decodeMultiAsyn(MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData()));
//        } catch (Exception e) {
//            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
//        }
//    }
//
//    private void decodeMultiAsyn(Bitmap bitmap) {
//        MLFrame image = MLFrame.fromBitmap(bitmap);
//        HmsScanAnalyzer analyzer = new HmsScanAnalyzer.Creator(this).setHmsScanTypes(HmsScan.ALL_SCAN_TYPE).create();
//        analyzer.analyzInAsyn(image).addOnSuccessListener(hmsScans -> {
//            if (hmsScans != null && hmsScans.size() > 0 && hmsScans.get(0) != null && !TextUtils.isEmpty(hmsScans.get(0).getOriginalValue())) {
//                HmsScan[] infos = new HmsScan[hmsScans.size()];
//                Intent intent = new Intent();
//                intent.putExtra(SCAN_RESULT, hmsScans.toArray(infos));
//                setResult(RESULT_OK, intent);
//                CommonActivity.this.finish();
//            }
//        }).addOnFailureListener(e -> Log.w(TAG, e));
//    }

    class SurfaceCallBack implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (!isShow) {
                isShow = true;
                initCamera();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            isShow = false;
        }
    }
}