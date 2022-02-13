package com.zwn.qrmessager.ui.sender;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

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
import java.util.Arrays;

public class QrFragment extends Fragment {

    private final String filePath;
    byte[] data = new byte[512];
    ImageView image;

    public QrFragment(String path) {
        this.filePath = path;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        QrViewModel qrViewModel = new ViewModelProvider(this).get(QrViewModel.class);
        FragmentQrBinding binding = FragmentQrBinding.inflate(inflater, container, false);
        HwButton backBtn = binding.qrBackBtn;
        image = binding.imageQr;
        backBtn.setOnClickListener(this::backBtnFunction);
        qrViewModel.getImageBitmap().observe(getViewLifecycleOwner(), bitmap -> {
            image.setImageBitmap(bitmap);
        });
        try {
            generateQRCode();
        } catch (IOException | WriterException | InterruptedException e) {
            e.printStackTrace();
        }
        return binding.getRoot();
    }

    private void backBtnFunction(View v){
        requireActivity().getSupportFragmentManager().beginTransaction()
                .remove(this).commit();
    }

    private void generateQRCode() throws IOException, WriterException, InterruptedException {
        File file = new File(filePath);
        InputStream inputStream = new FileInputStream(file);
        int width = Constant.getSettings().getScreenWidth();
        if(width == 0){
            Toast.makeText(requireContext(), "无宽高设置", Toast.LENGTH_SHORT).show();
        }else {

            while (inputStream.read(data,0,512) != -1) {
                //buffer为读出来的二进制数据，长度1024，最后一段数据小于1024
                HmsBuildBitmapOption options = new HmsBuildBitmapOption.Creator().setBitmapMargin(8).
                        setBitmapColor(Color.BLACK).setBitmapBackgroundColor(Color.WHITE).create();
                Bitmap resultImage = ScanUtil.buildBitmap(Arrays.toString(data), 0, width, width, options);
                    image.setImageBitmap(resultImage);
//                    Thread.sleep(1000);//等1s
            }
        }

        inputStream.close();
    }
}