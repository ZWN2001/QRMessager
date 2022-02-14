package com.zwn.qrmessager.ui.receiver;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScan;
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions;
import com.zwn.qrmessager.databinding.FragmentScaningBinding;

public class ScaningFragment extends Fragment {

    private static final int REQUEST_CODE = 1;
    private static final int REQUEST_CODE_SCAN_ONE = 0X01;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentScaningBinding binding = FragmentScaningBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    private void requestPermission(View v){
        ActivityCompat.requestPermissions(
                requireActivity(),
                new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        ScanUtil.startScan(requireActivity(), REQUEST_CODE_SCAN_ONE, new HmsScanAnalyzerOptions.Creator().create());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        requireActivity();
        if (resultCode != Activity.RESULT_OK || data == null) { return; }
        //Default View
        if (requestCode == REQUEST_CODE_SCAN_ONE) {
            HmsScan obj = data.getParcelableExtra(ScanUtil.RESULT);
            if (obj != null) {
                String result = obj.getOriginalValue();
            }
        }
    }

}