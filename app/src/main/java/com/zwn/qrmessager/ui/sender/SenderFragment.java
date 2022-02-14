package com.zwn.qrmessager.ui.sender;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import androidx.lifecycle.ViewModelProvider;

import com.huawei.uikit.hwbutton.widget.HwButton;

import com.zwn.qrmessager.R;
import com.zwn.qrmessager.databinding.FragmentSenderBinding;
import com.zwn.qrmessager.util.FileUtil;

import java.io.File;

public class SenderFragment extends Fragment {

    private FragmentSenderBinding binding;
    TextView textFileDetails;
    File file;

    public static final int CHOOSE_FILE_CODE = 0;
    public static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 1;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SenderViewModel senderViewModel = new ViewModelProvider(this).get(SenderViewModel.class);
        binding = FragmentSenderBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        HwButton chooseFileBtn = binding.btnChooseFile;
        HwButton startTransBtn = binding.btnConfirmSend;
        chooseFileBtn.setOnClickListener(this::getFile);
        startTransBtn.setOnClickListener(this::startTrans);
        textFileDetails = binding.textFileDetails;
        senderViewModel.getText().observe(getViewLifecycleOwner(), s -> textFileDetails.setText(s));
        return root;
    }

    private void getFile(View v){
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED) {
//            if (!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
//                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_READ_EXTERNAL_STORAGE);
//            }
        } else {
            chooseFile();
        }
    }

    private void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*").addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "Choose File"), CHOOSE_FILE_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(requireContext(), "亲，木有文件管理器啊-_-!!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Activity.RESULT_OK == resultCode) {
            if (requestCode == CHOOSE_FILE_CODE) {
                assert data != null;
                Uri uri = data.getData();
                String filepath = FileUtil.getFilePathByUri(requireContext(), uri);
                assert filepath != null;
                file = new File(filepath);
                StringBuilder t = new StringBuilder();
                t.append("文件名：").append(file.getName()).append("\n")
                        .append("文件大小约：").append(file.length()).append(" Byte").append("\n")
                        .append("文件路径：").append(filepath);
                textFileDetails.setText( t );
            }
        }
    }

    private void startTrans(View v){
        if (file==null){
            Toast.makeText(requireContext(), "请先选择文件", Toast.LENGTH_SHORT).show();
        }else {
            if(file.length()>2100000){//2MB的近似值
                Toast.makeText(requireContext(),"文件太大啦！",Toast.LENGTH_SHORT).show();
            }else {
                QrFragment qrFragment = new QrFragment(file.getPath());
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.app_bar_main, qrFragment).commit();
            }
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}