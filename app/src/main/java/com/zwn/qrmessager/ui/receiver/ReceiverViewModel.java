package com.zwn.qrmessager.ui.receiver;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ReceiverViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ReceiverViewModel() {
        mText = new MutableLiveData<>();
    }

    public LiveData<String> getText() {
        return mText;
    }
}