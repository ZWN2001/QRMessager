package com.zwn.qrmessager.util;

import android.app.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.huawei.hms.ml.scan.HmsScan;
import com.huawei.hms.ml.scan.HmsScanAnalyzer;
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.zwn.qrmessager.constant.Constant;
import com.zwn.qrmessager.ui.CommonActivity;
import com.zwn.qrmessager.util.draw.ScanResultView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

import androidx.annotation.RequiresApi;

public final class CommonHandler extends Handler {

    private static final double DEFAULT_ZOOM = 1.0;
    private final CameraOperation cameraOperation;
    private final HandlerThread decodeThread;
    private final Handler decodeHandle;
    private final Activity activity;
    private final HashMap<String, String> received = new HashMap<>();
    private int numAll = 0;//总个数
    private int lastNum = 0;//不满的数组的长度
    private String fileName = "";//不满的数组的长度

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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void handleMessage(Message message) {
        removeMessages(1);
        if (message.what == 0) {
            CommonActivity commonActivity1 = (CommonActivity) activity;
            commonActivity1.scanResultView.clear();
                CommonActivity commonActivity = (CommonActivity) activity;
                HmsScan[] arr = (HmsScan[]) message.obj;
                String[] array;
            for (HmsScan hmsScan : arr) {
                array = hmsScan.getOriginalValue().split(":");
                if (!fileName.equals("") && numAll != 0 && received.size() == numAll) {
                    transToFileAndFinish();
                } else {
                    if (array[0].equals("start")) {
                        fileName = array[1];
                    } else if (array[0].equals("over")) {
                        numAll = Integer.parseInt(array[1]);
                        lastNum = Integer.parseInt(array[2]);
                    } else {
                        String[] finalArray = array;
                        received.computeIfAbsent(array[0], k -> finalArray[1]);
                    }
                }
                  commonActivity.scanResultView.add(new ScanResultView.HmsScanGraphic(commonActivity.scanResultView, arr[0], Color.YELLOW));
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
        } catch (InterruptedException e) { e.printStackTrace();}
    }

    public void restart(double zoomValue) {
        cameraOperation.callbackFrame(decodeHandle, zoomValue);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void transToFileAndFinish(){
        int i;
        byte[] all = new byte[128* (numAll - 1) + lastNum];
        for (i = 0; i < numAll - 1; i++){
            byte[] decoded = Base64.getDecoder().decode(received.get(i+""));
            System.arraycopy(decoded, 0, all, 0, 128);
        }
        byte[] decoded = Base64.getDecoder().decode(received.get(numAll - 1 + ""));
        System.arraycopy(decoded, 0, all, 0, 128);

        String filepath = Constant.getSettings().getFilepath();
        if(filepath.isEmpty()){
            Intent intent = new Intent();
            intent.putExtra(CommonActivity.SCAN_RESULT, 0);
            activity.setResult(RESULT_OK, intent);
        }else {
            String pathName = filepath + fileName;
            int j = createFileAndWrite(pathName,all);
            Intent intent = new Intent();
            if(j == 0){
                intent.putExtra(CommonActivity.SCAN_RESULT, filepath + fileName);
            }else {
                intent.putExtra(CommonActivity.SCAN_RESULT, 0);
            }
            activity.setResult(RESULT_OK, intent);
        }
        activity.finish();
    }
    private int createFileAndWrite(String filePath,byte[] res){
        //传入路径 + 文件名
        File mFile = new File(filePath);
        if (mFile.exists()){
            boolean  b = mFile.delete();
            if(!b){
                return -1;
            }
        }
        try {
           boolean b =  mFile.createNewFile();
           if(b){
               FileOutputStream outStream = new FileOutputStream(mFile);
               outStream.write(res);
               outStream.close();
               Log.e("TAG", "createFileAndWrite: "+mFile.length() );
               return 0;
           }
         return -1;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
