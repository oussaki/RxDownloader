package com.oussaki.rxfilesdownloader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.ReplaySubject;
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
    int remains;
    ReplaySubject<FileContainer> subject;

    ItemsObserver itemsObserver;
    RxStorage rxStorage;
    OnStart onStart;
    OnError onError;
    OnCompleteWithSuccess onCompleteWithSuccess;
    OnCompleteWithError onCompleteWithError;
    OnProgress onProgress;
    private OkHttpClient client;
    private int STRATEGY;
    // Actions
    private File STORAGE;
    private List<FileContainer> files;
    private int downloaded = 0;

    RxDownloader(final Builder builder) {


        Log.i(TAG, "Constructor");
        this.context = builder.context;
        this.client = builder.client;
        this.STRATEGY = builder.STRATEGY;
        this.files = new ArrayList<>(builder.files.size());
        this.files.addAll(builder.files);
        this.STORAGE = builder.STORAGE;
        this.subject = ReplaySubject.create();
        this.rxStorage = builder.rxStorage;
        this.itemsObserver = new ItemsObserver(rxStorage);
        subject
                .doOnError(throwable -> {
                    Log.d(TAG, "Do on error subject");
                });

    }

    /*
    * Action to be taken when error thrown for one single file
    * */
    public RxDownloader doOnSingleError(OnError action) {
        this.onError = action;
        this.itemsObserver.onError(action);
        return this;
    }

    /*
    * initProgress : Before starting downloading
    * */
    public RxDownloader doOnStart(OnStart action) {
        this.onStart = action;
        this.itemsObserver.onStart(action);
        return this;
    }

    /**
     * doOnCompleteWithSuccess : When successfully finish downloading all the files
     */
    public RxDownloader doOnCompleteWithSuccess(OnCompleteWithSuccess action) {
        this.onCompleteWithSuccess = action;
        this.itemsObserver.onCompleteWithSuccess(action);
        return this;
    }

    /**
     * doOnCompleteWithError : When downloading ends with an error
     */
    public RxDownloader doOnCompleteWithError(OnCompleteWithError action) {
        this.onCompleteWithError = action;
        this.itemsObserver.onCompleteWithError(action);
        return this;
    }


    /**
     * doOnProgress(int progress) : On downloading files
     */
    public RxDownloader doOnProgress(OnProgress action) {
        this.onProgress = action;
        this.itemsObserver.onProgress(action);
        return this;
    }

    boolean isNull(Object obj) {
        if (obj == null)
            Log.i(TAG, "Object is null");
        else
            Log.i(TAG, "Object " + obj.getClass().toString() + " is not null");
        return obj == null;
    }

    private void current_thread() {
        Log.e(TAG, "Thread:" + Thread.currentThread().getName());
    }

    /**
     * Download a file from using an HTTP client
     *
     * @param url
     * @return byte[]
     */
    byte[] downloadFile(String url) throws IOException {
        return client.newCall(new Request.Builder().url(url).build()).execute().body().bytes();
    }

    FileContainer produceFileContainerFromBytes(final byte[] bytes, final FileContainer emptyContainer) {
        current_thread();
        Log.d(TAG, "fileContainer success" + emptyContainer.isSuccessed());
        if (emptyContainer.isSuccessed()) {
            final String filename = emptyContainer.getFilename();
            final File file = new File(context.getCacheDir() + File.separator + filename);
            int progress = 0;
            if (size > 0)
                progress = Math.abs(((remains * 100) / size) - 100);

            emptyContainer.setBytes(bytes);
            emptyContainer.setProgress(progress);
            emptyContainer.setFile(file);
        }
        Log.e(TAG, "Empty container return");
        return emptyContainer;
    }

    private void publishContainer(FileContainer fileContainer) {
        current_thread();
        if (fileContainer.isSuccessed())
            subject.onNext(fileContainer);

        Log.e(TAG, "publishContainer on next subject");
        if (remains == 0) {
            if (errors == 0)
                this.itemsObserver.CompleteWithSuccess();
            else
                this.itemsObserver.CompleteWithError();

            Log.i(TAG, remains + " i will throw on complete");
//          subject.onCompleteWithSuccess(); // it was like this
        }
    }

    private void catchDownloadError(byte[] bytes, FileContainer fileContainer) {
        if (bytes.length == 1) {
            errors++;
            fileContainer.setSuccessed(false);
        } else
            fileContainer.setSuccessed(true);

        remains--;
    }

    private void handleDownloadError(FileContainer fileContainer) throws IOException {
        if (!fileContainer.isSuccessed())
            throw new IOException("Can not download the file " + fileContainer.getFilename());
    }


    /**
     * Get an observable of one file downloader
     *
     * @param fileContainer
     * @return
     */
    private Observable<FileContainer> ObservableFileDownloader(final FileContainer fileContainer) {
        Observable<FileContainer> observable = Observable
                .fromCallable(() -> downloadFile(fileContainer.getUrl()))
                .onErrorReturn(throwable -> {
                    Log.e(TAG, "throwable");
                    byte[] b = new byte[1];
                    Log.e(TAG, "b.length:" + b.length);
                    return b;
                })
                .subscribeOn(Schedulers.io())
                .doOnNext(bytes -> catchDownloadError(bytes, fileContainer))
                .map(bytes -> produceFileContainerFromBytes(bytes, fileContainer))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(fileContainerError -> handleDownloadError(fileContainerError));

        if (STRATEGY == DownloadStrategy.ALL)
            observable = allStrategy(observable);
        else
            observable = maxStrategy(observable, fileContainer);

        return observable.doOnNext(fileContainerOnNext -> publishContainer(fileContainerOnNext))
                .filter(fileContainer1 -> {
                    return fileContainer1.isSuccessed();
                });
    }

    private Observable<FileContainer> maxStrategy(Observable<FileContainer> observable, FileContainer fileContainer) {
        Log.d(TAG, "Going to use max strategy");
        return observable
                .doOnError(throwable -> {
                    Log.d(TAG, "doOnError");
                    this.itemsObserver.onError(throwable);
                })
                .onErrorReturn(error -> {
                    Log.d(TAG, "onErrorReturn");
                    return fileContainer;
                });
    }

    private Observable<FileContainer> allStrategy(Observable<FileContainer> observable) {
        Log.d(TAG, "Going to use all strategy");
        return observable
                .doOnError(throwable -> {
                    Log.d(TAG, "doOnError");
                    this.itemsObserver.onError(throwable);
                })
                .onErrorResumeNext(throwable -> {
                    throwable.onComplete();
//                    this.subject.onCompleteWithSuccess();
                    this.itemsObserver.CompleteWithError();
                });
    }

    /**
     * Downloading the files Asynchronously
     *
     * @param observable
     * @return
     */
    private Observable<FileContainer> parallelDownloading(Observable<FileContainer> observable) {
        return observable.flatMap(fileContainer -> ObservableFileDownloader(fileContainer));
    }


    /**
     * Converts the downloaded files to be observable and consumed reactively
     *
     * @return
     */
    public Single<List<FileContainer>> asList() {

        this.subject.subscribe(this.itemsObserver);

        this.size = this.files.size();
        this.remains = this.size;
        Observable<FileContainer> observable = Observable
                .fromIterable(this.files)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());

//        if (STRATEGY == DownloadStrategy.PARALLEL)
        observable = parallelDownloading(observable);
//        else
//            observable = sequentialDownloading(observable).toList()

        return observable.toList();
    }


    /*
    * Builder Class
    * */
    public static final class Builder {
        Context context;
        OkHttpClient client;
        int STRATEGY;
        File STORAGE;
        RxStorage rxStorage;
        /**
         * HashMap of files to be downloaded
         */
        List<FileContainer> files;

        /**
         * @param context The context
         */
        public Builder(Context context) {
            this.context = context;
            STRATEGY = DownloadStrategy.DEFAULT;
            client = new OkHttpClient.Builder()
                    .connectTimeout(500, TimeUnit.MILLISECONDS)
                    .build();
            files = new ArrayList<>();
            this.STORAGE = context.getCacheDir();
            this.rxStorage = new RxStorage(context);
            Log.i("RxDownloader", "Builder Constructor called");
        }


        /**
         * Set a custom Http Client (OkHttpClient )
         *
         * @param client and OkHttp instance
         * @return Builder
         */
        Builder client(@NonNull OkHttpClient client) {
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
        public Builder strategy(int strategy) {
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
            files.add(new FileContainer(url, newName + extesion));
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
            files.add(new FileContainer(url, name));
            return this;
        }

        /**
         * Set the storage type to save files in
         *
         * @param STORAGE
         * @return Builder
         */
        public Builder storage(int STORAGE) {
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
        public Builder addFiles(List<FileContainer> files) {
            this.files.addAll(files);
            return this;
        }

        public RxDownloader build() {
            return new RxDownloader(this);
        }
    }


}
