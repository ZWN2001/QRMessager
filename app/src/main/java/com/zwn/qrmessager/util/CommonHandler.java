/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zwn.qrmessager.util;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScan;
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions;
import com.zwn.qrmessager.ui.CommonActivity;

import java.io.ByteArrayOutputStream;

public final class CommonHandler extends Handler {

    private static final String TAG = "MainHandler";
    private static final double DEFAULT_ZOOM = 1.0;
    private final CameraOperation cameraOperation;
    private final HandlerThread decodeThread;
    private final Handler decodeHandle;
    private final Activity activity;

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
                    HmsScan[] result = decodeSyn(msg.arg1, msg.arg2, (byte[]) msg.obj, activity);
                    if (result == null || result.length == 0) {
                        restart(DEFAULT_ZOOM);
                    } else if (TextUtils.isEmpty(result[0].getOriginalValue()) && result[0].getZoomValue() != 1.0) {
                        restart(result[0].getZoomValue());
                    } else if (!TextUtils.isEmpty(result[0].getOriginalValue())) {
                        Message message = new Message();
                        message.what = msg.what;
                        message.obj = result;
                        CommonHandler.this.sendMessage(message);
                        restart(DEFAULT_ZOOM);
                    } else{
                         restart(DEFAULT_ZOOM);
                    }
            }
        };
        cameraOperation.startPreview();
        restart(DEFAULT_ZOOM);
    }

    /**
     * Call the MultiProcessor API in synchronous mode.
     */
    private HmsScan[] decodeSyn(int width, int height, byte[] data, final Activity activity) {
        Bitmap bitmap = convertToBitmap(width, height, data);
            HmsScanAnalyzerOptions options = new HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.ALL_SCAN_TYPE).setPhotoMode(false).create();
            return ScanUtil.decodeWithBitmap(activity, bitmap, options);
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

    @Override
    public void handleMessage(Message message) {
        Log.e(TAG, String.valueOf(message.what));
        removeMessages(1);
        if (message.what == 0) {
            CommonActivity commonActivity1 = (CommonActivity) activity;
            commonActivity1.scanResultView.clear();
            Intent intent = new Intent();
            intent.putExtra(CommonActivity.SCAN_RESULT, (HmsScan[]) message.obj);
            activity.setResult(RESULT_OK, intent);
            activity.finish();
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
            Log.w(TAG, e);
        }
    }

    public void restart(double zoomValue) {
        cameraOperation.callbackFrame(decodeHandle, zoomValue);
    }
}
