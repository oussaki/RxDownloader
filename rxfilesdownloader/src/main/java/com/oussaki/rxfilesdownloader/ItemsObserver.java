package com.oussaki.rxfilesdownloader;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by oussama abdallah , AKA oussaki on 10/1/2017 , 10:53 AM.
 */

public class ItemsObserver implements Observer<FileContainer> {
    List<FileContainer> filesContainer;
    String TAG = "ItemsObserver";

    IDownloadProgress iDownloadProgress;
    RxStorage rxStorage;

    public ItemsObserver(RxStorage rxStorage) {
        this.rxStorage = rxStorage;
    }

    public void setiDownloadProgress(IDownloadProgress iDownloadProgress) {
        this.iDownloadProgress = iDownloadProgress;
    }

    @Override
    public void onSubscribe(Disposable d) {
        filesContainer = new ArrayList<>();
        // UI interaction and initialization
        if (iDownloadProgress != null) {
            iDownloadProgress.OnStart();
        }
        Log.d(TAG, " First onSubscribe : " + d.isDisposed());
    }

    @Override
    public void onNext(FileContainer fileContainer) {
        Log.e(TAG,"Im inside on next");
        try {
            if (rxStorage != null) {
                rxStorage.saveToFile(fileContainer.getBytes(), fileContainer.getFile()); // save file
                Log.d(TAG, " First onNext value : " + fileContainer.getFile().getName());
                filesContainer.add(fileContainer);
                if (iDownloadProgress != null)
                    Log.e(TAG,"Idownload is not null");
                else if(iDownloadProgress == null)
                    Log.e(TAG,"iDownloadProgress is NULL");

                if (iDownloadProgress != null)
                    iDownloadProgress.OnProgress(fileContainer.getProgress());
            } else {
                onError(new IllegalStateException("RxStorage is not initialized"));
            }
        } catch (IOException e) {
            onError(new IllegalStateException("Can't not save file:" + fileContainer.getFile().getName()));
        }
    }

    @Override
    public void onError(Throwable e) {
        Log.d(TAG, " First onError : " + e.getMessage());
        if (iDownloadProgress != null)
            iDownloadProgress.OnError(e);
    }

    @Override
    public void onComplete() {
        Log.d(TAG, "Download Complete");
        if (iDownloadProgress != null)
            iDownloadProgress.OnComplete();
    }
}
