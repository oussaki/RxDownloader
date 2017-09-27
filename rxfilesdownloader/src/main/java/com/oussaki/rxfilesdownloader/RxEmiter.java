package com.oussaki.rxfilesdownloader;

import io.reactivex.FlowableEmitter;
import io.reactivex.ObservableEmitter;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Cancellable;

/**
 * Created by oussama on 9/25/2017.
 */

public class RxEmiter implements FlowableEmitter {


    @Override
    public void setDisposable(Disposable s) {

    }

    @Override
    public void setCancellable(Cancellable c) {

    }

    @Override
    public long requested() {
        return 0;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public FlowableEmitter serialize() {
        return null;
    }

    @Override
    public void onNext(Object value) {

    }

    @Override
    public void onError(Throwable error) {

    }

    @Override
    public void onComplete() {

    }
}
