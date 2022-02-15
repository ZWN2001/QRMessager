package com.zwn.qrmessager.util;

import android.app.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;

import com.huawei.hms.ml.scan.HmsScan;
import com.huawei.hms.ml.scan.HmsScanAnalyzer;
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.zwn.qrmessager.ui.CommonActivity;
import com.zwn.qrmessager.util.draw.ScanResultView;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public final class CommonHandler extends Handler {

    private static final double DEFAULT_ZOOM = 1.0;
    private final CameraOperation cameraOperation;
    private final HandlerThread decodeThread;
    private final Handler decodeHandle;
    private final Activity activity;
    private List<String> result;

    public CommonHandler(final Activity activity, CameraOperation cameraOperation) {
        this.cameraOperation = cameraOperation;
        this.activity = activity;
        decodeThread = new HandlerThread("DecodeThread");
        decodeThread.start();
        decodeHandle = new Handler(decodeThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                if (msg == null) {
                    return;
                }
                decodeAsyn(msg.arg1, msg.arg2, (byte[]) msg.obj);
            }
        };
        cameraOperation.startPreview();
        restart(DEFAULT_ZOOM);
    }

    /**
     * Convert camera data into bitmap data.
     */
    private Bitmap convertToBitmap(int width, int height, byte[] data) {
        YuvImage yuv = new YuvImage(data, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        yuv.compressToJpeg(new Rect(0, 0, width, height), 100, stream);
        return BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.toByteArray().length);
    }

    /**
     * Call the MultiProcessor API in asynchronous mode.
     */
    private void decodeAsyn(int width, int height, byte[] data) {
        final Bitmap bitmap = convertToBitmap(width, height, data);
        MLFrame image = MLFrame.fromBitmap(bitmap);
        HmsScanAnalyzerOptions options = new HmsScanAnalyzerOptions.Creator().setHmsScanTypes(com.huawei.hms.ml.scan.HmsScanBase.ALL_SCAN_TYPE).create();
        HmsScanAnalyzer analyzer = new HmsScanAnalyzer(options);
        analyzer.analyzInAsyn(image).addOnSuccessListener(hmsScans -> {
            if (hmsScans != null && hmsScans.size() > 0 && hmsScans.get(0) != null && !TextUtils.isEmpty(hmsScans.get(0).getOriginalValue())) {
                HmsScan[] infos = new HmsScan[hmsScans.size()];
                Message message = new Message();
                message.obj = hmsScans.toArray(infos);
                CommonHandler.this.sendMessage(message);
            }
            restart(DEFAULT_ZOOM);
            bitmap.recycle();
        }).addOnFailureListener(e -> {
            restart(DEFAULT_ZOOM);
            bitmap.recycle();
        });
    }

    @Override
    public void handleMessage(Message message) {
        removeMessages(1);
        if (message.what == 0) {
            CommonActivity commonActivity1 = (CommonActivity) activity;
            commonActivity1.scanResultView.clear();
            Intent intent = new Intent();
            intent.putExtra(CommonActivity.SCAN_RESULT, (HmsScan[]) message.obj);
            activity.setResult(RESULT_OK, intent);
            //Show the scanning result on the screen.
                CommonActivity commonActivity = (CommonActivity) activity;
                HmsScan[] arr = (HmsScan[]) message.obj;
                for (int i = 0; i < arr.length; i++) {
                    if (i == 0) {
                        commonActivity.scanResultView.add(new ScanResultView.HmsScanGraphic(commonActivity.scanResultView, arr[i], Color.YELLOW));
                    } else if (i == 1) {
                        commonActivity.scanResultView.add(new ScanResultView.HmsScanGraphic(commonActivity.scanResultView, arr[i], Color.BLUE));
                    } else if (i == 2){
                        commonActivity.scanResultView.add(new ScanResultView.HmsScanGraphic(commonActivity.scanResultView, arr[i], Color.RED));
                    } else if (i == 3){
                        commonActivity.scanResultView.add(new ScanResultView.HmsScanGraphic(commonActivity.scanResultView, arr[i], Color.GREEN));
                    } else {
                        commonActivity.scanResultView.add(new ScanResultView.HmsScanGraphic(commonActivity.scanResultView, arr[i]));
                    }
                }
                commonActivity.scanResultView.setCameraInfo(1080, 1920);
                commonActivity.scanResultView.invalidate();
                sendEmptyMessageDelayed(1,500);
        }else if(message.what == 1){
            CommonActivity commonActivity1 = (CommonActivity) activity;
            commonActivity1.scanResultView.clear();
        }
    }

    public void quit() {
        try {
            cameraOperation.stopPreview();
            decodeHandle.getLooper().quit();
            decodeThread.join(500);
        } catch (InterruptedException e) {
//            Log.w(TAG, e);
        }
    }

    public void restart(double zoomValue) {
        cameraOperation.callbackFrame(decodeHandle, zoomValue);
    }
}
