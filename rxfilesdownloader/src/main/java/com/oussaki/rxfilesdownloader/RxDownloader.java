package com.oussaki.rxfilesdownloader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by oussama abdallah , AKA oussaki on 9/13/2017 , 3:02 PM.
 */

public class RxDownloader {
    public static String TAG = "RxDownloader";
    Context context;
    int errors = 0;
    int size;
    private OkHttpClient client;
    private int STRATEGY;
    private File STORAGE;
    private HashMap<String, String> files;
    private IDownloadProgress iDownloadProgress;
    private int downloaded = 0;

    RxDownloader(final Builder builder) {
        Log.i(TAG, "Constructor");
        this.client = builder.client;
        this.STRATEGY = builder.STRATEGY;
        this.files = new HashMap<>(builder.files.size());
        this.files.putAll(builder.files);
        this.STORAGE = builder.STORAGE;
    }


    /**
     * Set Listeners for downloader to Handle events Like :
     * initProgress : Before starting downloading
     * OnProgress(int progress) : On downloading files
     * OnFinish : When finally finish downloading files
     *
     * @param iDownloadProgress
     * @return
     */
    public RxDownloader setListeners(IDownloadProgress iDownloadProgress) {
        this.iDownloadProgress = iDownloadProgress;
        return this;
    }

    boolean isNull(Object obj) {
        if (obj == null)
            Log.i(TAG, "Object is null");
        else
            Log.i(TAG, "Object " + obj.getClass().toString() + " is not null");
        return obj == null;
    }

    /**
     * Store a file in Local Storage
     *
     * @param is
     * @param file
     * @throws IOException
     */
    void saveToFile(InputStream is, File file) throws IOException {
        BufferedInputStream input = new BufferedInputStream(is);
        OutputStream output = new FileOutputStream(file);
        byte[] data = new byte[1024];
        long total = 0;
        int count;
        while ((count = input.read(data)) != -1) {
            total += count;
            output.write(data, 0, count);
        }
        output.flush();
        output.close();
        input.close();
    }

    private void current_thread() {
        Log.e(TAG, "Thread:" + Thread.currentThread().getName());
    }

    /**
     * Download a file
     *
     * @param filename
     * @return
     */
    private Observable<Flowable<File>> DownloadAFile(final String filename) {

        final String fileurl = files.get(filename);
        final File file = new File(STORAGE + File.separator + filename);
        if (file.exists())
            Log.d(TAG, "This file exists");
        final Request request = new Request.Builder()
                .url(fileurl)
                .build();
        return Observable
                .fromCallable(() -> client.newCall(request))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(throwable -> Log.e(TAG, "Error", throwable))
                .map(call -> {
                    final FlowableCallback callback = new FlowableCallback();
                    call.enqueue(callback);
                    return callback.getObservable()
                            .doOnNext(response -> {
                                try {
                                    final InputStream inputStream = response.body().byteStream();
                                    current_thread();
                                    saveToFile(inputStream, file);
                                    Log.d(TAG, "file saved");
                                } catch (IOException io) {
                                    Log.e(TAG, "IOException" + io.getMessage());
//                                            errors++;
                                }
                            })
                            .doOnComplete(() -> {
                                downloaded++;
                                Log.d(TAG, "dooncomplete increment downloads to:" + downloaded);
                            })
                            .map(response -> {
                                current_thread();
                                return file;
                            });
//                            .subscribe();
                })
                .doOnComplete(() -> {
                    int progress = Math.abs((downloaded * 100) / size);
                    Log.i(TAG, filename + " just downloaded, progress is :"
                            + progress + " " + (this.STRATEGY == Strategy.ASYNC ? "Async" : "Sync"));

                    if (!isNull(iDownloadProgress) && progress != 100) {
                        current_thread();
                        iDownloadProgress.OnProgress(progress);
                    }

                    if (progress == 100) {
                        current_thread();
                        iDownloadProgress.OnFinish();
                    }

                });
//                .map(flowable -> {
//                    flowable.toFuture().get();
//                    current_thread();
//                    Log.d(TAG, "Maping files " + filename + ",path" + file.getPath());
//                    return new AbstractMap.SimpleEntry<>(files.get(filename), file);
//                })
//                ;
    }

    /**
     * Downloading the files Asynchronously
     *
     * @param observable
     * @return
     */
    private Observable<Flowable<File>> AsyncDownloading(Observable<String> observable) {
        return observable.flatMap(fileurl -> DownloadAFile(fileurl));
    }

    /**
     * Downloading the files Synchronously
     *
     * @param observable
     * @return
     */
    public Observable<Observable<Flowable<File>>> SyncDownloading(Observable<String> observable) {
        return observable.map(fileurl -> DownloadAFile(fileurl));
    }

    /**
     * Converts the downloaded files to be observable and consumed reactively
     *
     * @return
     */
    public Observable<Map.Entry<String, File>> asObservable() {
        size = files.size();
        Observable observable = Observable.fromIterable(files.keySet())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(disposable -> {
                    // you can init here
                    if (!isNull(iDownloadProgress))
                        iDownloadProgress.initProgress();
                }).doFinally(() -> {
//                    if (!isNull(iDownloadProgress))
//                        iDownloadProgress.OnFinish();
                });

        if (STRATEGY == Strategy.ASYNC)
            return AsyncDownloading(observable);
        else
            return SyncDownloading(observable);
    }

    public static final class Builder {
        Context context;
        OkHttpClient client;
        int STRATEGY;
        File STORAGE;
        /**
         * HashMap of files to be downloaded
         */
        HashMap<String, String> files;

        /**
         * @param context The context
         */
        public Builder(Context context) {
            this.context = context;
            STRATEGY = Strategy.DEFAULT;
            client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .build();
            files = new HashMap<>();
            this.STORAGE = context.getCacheDir();
            Log.i("RxDownloader", "Builder Constructor called");
        }


        /**
         * Set a custom Http Client (OkhttpClient )
         *
         * @param client and Okhttp instance
         * @return Builder
         */
        Builder setClient(@NonNull OkHttpClient client) {
            if (client != null)
                this.client = client;
            return this;
        }

        /**
         * Set strategy for downloading files
         * (asynchronous , synchronous)
         *
         * @param strategy
         * @return Builder
         */
        public Builder setStrategy(int strategy) {
            STRATEGY = strategy;
            return this;
        }

        /**
         * Add a URL of a file to the list of downloading
         * and rename it to the given name
         *
         * @param newName
         * @param url
         * @return Builder
         */
        public Builder addFile(String newName, String url) {
            String extesion = "";
            if (newName.indexOf(".") < 0)
                extesion = ExtractExtension(url);
            files.put(newName + extesion, url);
            return this;
        }

        /**
         * Extract the extension of file from a given URL
         *
         * @param url
         * @return Builder
         */
        protected String ExtractExtension(String url) {
            return url.substring(url.lastIndexOf("."));
        }

        /**
         * ُExtract the Name and extension of given file URL
         *
         * @param url
         * @return Builder
         */
        protected String ExtractNameAndExtension(String url) {
            return url.substring(url.lastIndexOf("/") + 1);
        }


        /**
         * Set the storage type to save files in
         *
         * @param url
         * @return Builder
         */
        public Builder addFile(String url) {
            String name = ExtractNameAndExtension(url);
            files.put(name, url);
            return this;
        }

        /**
         * Set the storage type to save files in
         *
         * @param STORAGE
         * @return Builder
         */
        public Builder setStorage(int STORAGE) {
            if (STORAGE == RxStorage.DATA_DIRECTORY)
                this.STORAGE = context.getCacheDir();
            else if (STORAGE == RxStorage.EXTERNAL_CACHE_DIR)
                this.STORAGE = context.getExternalCacheDir();
            return this;
        }

        /**
         * Add Bulk of files to the HashMap
         *
         * @param files
         * @return Builder
         */
        public Builder addFiles(HashMap<String, String> files) {
            this.files.putAll(files);
            return this;
        }

        public RxDownloader build() {
            return new RxDownloader(this);
        }
    }


}
