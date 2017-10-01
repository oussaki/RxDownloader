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

    RxStorage rxStorage;
    OnStart onStart;
    OnError onError;
    OnComplete onComplete;
    OnProgress onProgress;

    public ItemsObserver(RxStorage rxStorage) {
        this.rxStorage = rxStorage;
    }

    public void onStart(OnStart onStart) {
        this.onStart = onStart;
    }

    public void onError(OnError onError) {
        this.onError = onError;
    }

    public void onComplete(OnComplete onComplete) {
        this.onComplete = onComplete;
    }

    public void onProgress(OnProgress onProgress) {
        this.onProgress = onProgress;
    }


    @Override
    public void onSubscribe(Disposable d) {
        filesContainer = new ArrayList<>();
        // UI interaction and initialization
        if (onStart != null)
            onStart.run();

        Log.d(TAG, " First onSubscribe : " + d.isDisposed());
    }

    @Override
    public void onNext(FileContainer fileContainer) {
        Log.e(TAG, "Im inside on next");
        if (fileContainer.isSuccessed()) {
            try {
                if (rxStorage != null) {
                    rxStorage.saveToFile(fileContainer.getBytes(), fileContainer.getFile()); // save file
                    Log.d(TAG, " First onNext value : " + fileContainer.getFile().getName());
                    filesContainer.add(fileContainer);

                    if (onProgress != null)
                        onProgress.run(fileContainer.getProgress());
                } else {
                    onError(new IllegalStateException("RxStorage is not initialized"));
                }
            } catch (IOException e) {
                onError(new IllegalStateException("Can't not save file:" + fileContainer.getFile().getName()));
            }
        } else
            onError(new IllegalStateException("Can't not download the file:"));
    }

    @Override
    public void onError(Throwable e) {
        Log.d(TAG, " First onError : ");
        if (e == null)
            if (onError != null)
                onError.run(e);
    }

    @Override
    public void onComplete() {
        Log.d(TAG, "Download Complete");
//        if (filesContainer.size() > 0)
            if (onComplete != null)
                onComplete.run();
    }
}
