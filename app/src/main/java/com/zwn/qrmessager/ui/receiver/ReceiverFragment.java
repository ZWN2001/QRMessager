package com.zwn.qrmessager.ui.receiver;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.huawei.uikit.hwbutton.widget.HwButton;
import com.zwn.qrmessager.databinding.FragmentReceiverBinding;
import com.zwn.qrmessager.ui.CommonActivity;

public class ReceiverFragment extends Fragment {


    private static final int REQUEST_CODE = 1;
    private static final int REQUEST_CODE_SCAN_MULTI = 0X011;
    FragmentReceiverBinding binding;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentReceiverBinding.inflate(inflater, container, false);
        HwButton receiverStartButton = binding.receiverStartBtn;
        receiverStartButton.setOnClickListener(this::requestPermissionAndStart);
        return binding.getRoot();
    }

    private void requestPermissionAndStart(View v){
        ActivityCompat.requestPermissions(
                requireActivity(),
                new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_CODE);
        Intent intent = new Intent(requireActivity(), CommonActivity.class);
        this.startActivityForResult(intent, REQUEST_CODE_SCAN_MULTI);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        requireActivity();
        if (resultCode != Activity.RESULT_OK || data == null) { return; }
        //Default View
        if (requestCode == REQUEST_CODE_SCAN_MULTI) {
//            HmsScan obj = data.getParcelableExtra(ScanUtil.RESULT);
//            if (obj != null) {
//                String result = obj.getOriginalValue();
//            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}