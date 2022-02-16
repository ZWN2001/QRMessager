package com.zwn.qrmessager.ui.sender;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.hmsscankit.WriterException;
import com.huawei.hms.ml.scan.HmsBuildBitmapOption;
import com.huawei.uikit.hwbutton.widget.HwButton;
import com.zwn.qrmessager.R;
import com.zwn.qrmessager.constant.Constant;
import com.zwn.qrmessager.databinding.FragmentQrBinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;

public class QrFragment extends Fragment {
    FragmentQrBinding binding;
    private final String filePath;
    byte[] data = new byte[128];
    ImageView image;
    Thread qrThread;

    public QrFragment(String path) {
        this.filePath = path;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        QrViewModel qrViewModel = new ViewModelProvider(this).get(QrViewModel.class);
        binding = FragmentQrBinding.inflate(inflater, container, false);
        HwButton backBtn = binding.qrBackBtn;
        image = binding.imageQr;
        backBtn.setOnClickListener(this::backBtnFunction);
        qrViewModel.getImageBitmap().observe(getViewLifecycleOwner(), bitmap -> image.setImageBitmap(bitmap));
        try {
            generateQRCode();
        } catch (IOException | WriterException | InterruptedException e) {
            e.printStackTrace();
        }
        return binding.getRoot();
    }

    private void backBtnFunction(View v){
        if(qrThread != null && qrThread.isAlive()){
            qrThread.interrupt();
        }
        requireActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void generateQRCode() throws IOException, WriterException, InterruptedException {
        File file = new File(filePath);

        int width = Constant.getSettings().getScreenWidth();
        if(width == 0){
            Toast.makeText(requireContext(), "无宽高设置", Toast.LENGTH_SHORT).show();
        }else {
            qrThread =  new Thread(() -> {
                InputStream inputStream;
                HmsBuildBitmapOption options = new HmsBuildBitmapOption.Creator().setBitmapMargin(8).
                        setBitmapColor(Color.BLACK).setBitmapBackgroundColor(Color.WHITE).create();
                //起始码
                Bitmap bitmap;
                //序列码
                int i = 0;
                while (!Thread.currentThread().isInterrupted()) {
                            try {
                                String startInfo = "start:" + file.getName();
                                bitmap = ScanUtil.buildBitmap(startInfo, 0, width, width, options);
                                Bitmap finalResultImage = bitmap;
                                requireActivity().runOnUiThread(() -> image.setImageBitmap(finalResultImage));
                                Thread.sleep(500);//等
                            } catch (WriterException | InterruptedException | IllegalStateException e) {
                                e.printStackTrace();
                            }

                            try {
                                inputStream = new FileInputStream(file);
                                String encodedByte;
                                int lastLength = 0;//最后一个可能不满128
                                int readLength = inputStream.read(data, 0, 128);
                                while (readLength != -1) {
                                    if(readLength<128){ lastLength = readLength; }
                                    encodedByte = Base64.getEncoder().encodeToString(data);
                                    Bitmap resultImage = ScanUtil.buildBitmap(i + ":" + encodedByte, 0, width, width, options);
                                    i++;
                                    requireActivity().runOnUiThread(() -> image.setImageBitmap(resultImage));
                                    Thread.sleep(120);//等
                                    readLength = inputStream.read(data, 0, 128);
                                }
                                bitmap = ScanUtil.buildBitmap("over:" + i +":" + lastLength, 0, width, width, options);
                                Bitmap finalBitmap = bitmap;
                                requireActivity().runOnUiThread(() -> image.setImageBitmap(finalBitmap));
                                Thread.sleep(500);//等
                                inputStream.close();
                                i = 0;
                            } catch (IOException | WriterException | InterruptedException | IllegalStateException e) {
                                e.printStackTrace();
                            }

                    }
            });
            qrThread.start();
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}