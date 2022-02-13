package com.zwn.qrmessager.ui.sender;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SenderViewModel extends ViewModel {

    private final MutableLiveData<String> fileDetail;

    public SenderViewModel() {
        fileDetail = new MutableLiveData<>();
    }

    public LiveData<String> getText() {
        return fileDetail;
    }
}