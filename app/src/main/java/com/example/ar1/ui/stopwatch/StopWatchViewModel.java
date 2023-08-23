package com.example.ar1.ui.stopwatch;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class StopWatchViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public StopWatchViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is dashboard fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}