package com.oussaki.rxfilesdownloader;

/**
 * Created by oussama abdallah , AKA oussaki on 9/14/2017 , 10:36 AM.
 */

public interface IDownloadProgress {
    void OnStart();

    void OnProgress(int progress);

    void OnComplete();

    void OnError(Throwable throwable);

}
