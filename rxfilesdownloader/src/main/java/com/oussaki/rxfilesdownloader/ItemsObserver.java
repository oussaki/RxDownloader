package com.oussaki.rxfilesdownloader;

import android.util.Log;

import com.koushikdutta.async.future.Cancellable;

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
    OnCompleteWithSuccess onCompleteWithSuccess;
    OnCompleteWithError onCompleteWithError;
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

    public void onCompleteWithSuccess(OnCompleteWithSuccess onCompleteWithSuccess) {
        this.onCompleteWithSuccess = onCompleteWithSuccess;
    }

    public void onCompleteWithError(OnCompleteWithError onCompleteWithError) {
        this.onCompleteWithError = onCompleteWithError;
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

        Log.d(TAG, "ItemsObserver onSubscribe");
    }

    @Override
    public void onNext(FileContainer fileContainer) {
        Log.e(TAG, "Im inside on next");
        if (fileContainer.isSucceed()) {
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
        Log.d(TAG, " ItemObserver onError : " + e.getMessage());
        if (e != null)
            if (onError != null)
                onError.run(e);
    }

    public void CompleteWithError() {
        // TO-DO
        if (onCompleteWithError != null)
            onCompleteWithError.run();
        Log.d(TAG, "Download end-up with error");
        onComplete();
    }

    public void CompleteWithSuccess() {
        // TO-DO
        if (onCompleteWithSuccess != null)
            onCompleteWithSuccess.run();
        Log.d(TAG, "Download Complete with success");
        onComplete();
    }

    @Override
    public void onComplete() {

    }
}
