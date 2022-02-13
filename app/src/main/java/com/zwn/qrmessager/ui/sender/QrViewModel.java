package com.zwn.qrmessager.ui.sender;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.graphics.Bitmap;

public class QrViewModel extends ViewModel {

    private final MutableLiveData<Bitmap> image_bitmap;

    public QrViewModel() {
        image_bitmap = new MutableLiveData<>();
    }

    public LiveData<Bitmap> getImageBitmap() {
        return image_bitmap;
    }
}
