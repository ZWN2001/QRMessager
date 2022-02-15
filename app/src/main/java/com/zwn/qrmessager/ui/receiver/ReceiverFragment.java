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

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScan;
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
        if (requestCode == REQUEST_CODE_SCAN_MULTI) {
            Parcelable[] obj = data.getParcelableArrayExtra(CommonActivity.SCAN_RESULT);
            if (obj != null && obj.length > 0) {
                if (obj.length == 1) {
//                    if (obj[0] != null && !TextUtils.isEmpty(((HmsScan) obj[0]).getOriginalValue())) {
//                        Intent intent = new Intent(this, DisPlayActivity.class);
//                        intent.putExtra(RESULT, obj[0]);
//                        startActivity(intent);
//                    }
                } else {
//                    Intent intent = new Intent(this, DisPlayMulActivity.class);
//                    intent.putExtra(RESULT, obj);
//                    startActivity(intent);
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}