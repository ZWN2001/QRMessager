package com.zwn.qrmessager.ui.receiver;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScan;
import com.huawei.uikit.hwbutton.widget.HwButton;
import com.zwn.qrmessager.databinding.FragmentReceiverBinding;
import com.zwn.qrmessager.ui.CommonActivity;
import com.zwn.qrmessager.ui.sender.SenderViewModel;

import java.util.Arrays;

public class ReceiverFragment extends Fragment {


    private static final int REQUEST_CODE = 1;
    private static final int REQUEST_CODE_SCAN_MULTI = 0X011;
    TextView receiverInfoText;
    FragmentReceiverBinding binding;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ReceiverViewModel receiverViewModel =  new ViewModelProvider(this).get(ReceiverViewModel.class);
        binding = FragmentReceiverBinding.inflate(inflater, container, false);
        HwButton receiverStartButton = binding.receiverStartBtn;
        receiverInfoText = binding.receiverInfoText;
        receiverStartButton.setOnClickListener(this::requestPermissionAndStart);
        receiverViewModel.getText().observe(getViewLifecycleOwner(), receiverInfoText::setText);
        return binding.getRoot();
    }

    private void requestPermissionAndStart(View v){
        ActivityCompat.requestPermissions(
                requireActivity(),
                new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_CODE);
        Intent intent = new Intent(requireActivity(), CommonActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SCAN_MULTI);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        requireActivity();
        if (resultCode != Activity.RESULT_OK || data == null) { return; }
        if (requestCode == REQUEST_CODE_SCAN_MULTI) {
            Parcelable[] obj = data.getParcelableArrayExtra(CommonActivity.SCAN_RESULT);
            if (obj != null && obj.length > 0) {
                System.out.println(Arrays.toString(obj));
                String result = obj[0].toString();
                if (result.equals("0")){
                    Toast.makeText(requireActivity(), "文件保存失败", Toast.LENGTH_SHORT).show();
                }else {
                    receiverInfoText.setText(result);
                    Toast.makeText(requireActivity(), "文件保存成功", Toast.LENGTH_SHORT).show();
                }
//                HmsScan scan ;
//                for (Parcelable parcelable : obj) {
//                    scan = (HmsScan) parcelable;
//                    System.out.println(scan.getOriginalValue());
//                    Log.e("TAG", "onActivityResult: "+ scan.getOriginalValue());
//                }
//                Toast.makeText(requireActivity(), obj.length, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}