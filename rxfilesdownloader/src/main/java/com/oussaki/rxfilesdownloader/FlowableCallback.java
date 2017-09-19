package com.oussaki.rxfilesdownloader;

import android.util.Log;

import java.io.IOException;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.AsyncSubject;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by oussama abdallah , AKA oussaki on 9/13/2017 , 1:07 PM.
 */

public class FlowableCallback implements Callback {
    private final AsyncSubject<Response> subject = AsyncSubject.create();
    String TAG = "FlowableCallback";
    public Observable<Response> getFlowable() {
        return subject.toFlowable(BackpressureStrategy.BUFFER).subscribeOn(Schedulers.io()).toObservable();
    }

    @Override
    public void onFailure(Call call, IOException e) {
        Log.e(TAG, "Flowable Callback Failure:" + e.getMessage());
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        Log.e(TAG, "onResponse status: " + response.isSuccessful());
        subject.onNext(response);
        subject.onComplete();
    }
}