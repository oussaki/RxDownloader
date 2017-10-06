package com.oussaki.rxfilesdownloader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Patterns;

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
    private Context context;
    private int errors = 0;
    private int size;
    private int remains;
    private ReplaySubject<FileContainer> subject;
    private ItemsObserver itemsObserver;
    private RxStorage rxStorage;
    /* Actions */
    private OnStart onStart;
    private OnError onError;
    private OnCompleteWithSuccess onCompleteWithSuccess;
    private OnCompleteWithError onCompleteWithError;
    private OnProgress onProgress;
    private OkHttpClient client;
    private int STRATEGY;
    /**
     * Order can be parallel or sequential
     */
    private int ORDER;
    private File STORAGE;
    private List<FileContainer> files;
    private int downloaded = 0;
    private boolean canceled = false;

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
        this.ORDER = builder.ORDER;
    }

    /**
     * Action to be taken when error thrown for one single file
     *
     * @param action
     * @return RxDownloader
     */
    public RxDownloader doOnEachSingleError(OnError action) {
        this.onError = action;
        this.itemsObserver.onError(action);
        return this;
    }

    /**
     * doOnStart : Action to be taken before start downloading
     *
     * @param action
     * @return RxDownloader
     */
    public RxDownloader doOnStart(OnStart action) {
        this.onStart = action;
        this.itemsObserver.onStart(action);
        return this;
    }

    /**
     * doOnCompleteWithSuccess : Action to be taken when successfully finish downloading all the files
     *
     * @param action
     * @return RxDownloader
     */
    public RxDownloader doOnCompleteWithSuccess(OnCompleteWithSuccess action) {
        this.onCompleteWithSuccess = action;
        this.itemsObserver.onCompleteWithSuccess(action);
        return this;
    }

    /**
     * doOnCompleteWithError : Action to be taken when downloading ends with an error
     *
     * @param action
     * @return RxDownloader
     */
    public RxDownloader doOnCompleteWithError(OnCompleteWithError action) {
        this.onCompleteWithError = action;
        this.itemsObserver.onCompleteWithError(action);
        return this;
    }


    /**
     * doOnProgress(int progress) : On downloading files
     *
     * @param action
     * @return RxDownloader
     */
    public RxDownloader doOnProgress(OnProgress action) {
        this.onProgress = action;
        this.itemsObserver.onProgress(action);
        return this;
    }

    /**
     * Check if Object is null ot not
     *
     * @param obj
     * @return boolean
     */
    boolean isNull(Object obj) {
        if (obj == null)
            Log.i(TAG, "Object is null");
        else
            Log.i(TAG, "Object " + obj.getClass().toString() + " is not null");
        return obj == null;
    }

    /*
    * Print the current thread name.
    * */
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

    /**
     * @param bytes
     * @param emptyContainer
     * @return FileContainer
     */
    FileContainer produceFileContainerFromBytes(final byte[] bytes, final FileContainer emptyContainer) {
        current_thread();
        Log.d(TAG, "fileContainer success" + emptyContainer.isSucceed());
        if (canceled && emptyContainer.isSucceed()) {
            /*
            * Canceled file want be considered as downloaded files ( Ignored )
            * */
            Log.d(TAG, "emptyContainer.setCanceled");
            emptyContainer.setCanceled(true); // to help filtration in ALL Strategy
        } else if (emptyContainer.isSucceed() && !canceled) {
            final String filename = emptyContainer.getFilename();


            final File file = new File(STORAGE + File.separator + filename);
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

    /**
     * @param fileContainer
     */
    private void publishContainer(FileContainer fileContainer) {
        current_thread();
        if (fileContainer.isSucceed())
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

    /**
     * @param bytes
     */
    private void catchCanceling(byte[] bytes) {
        // cancel only if the strategy is ALL strategy
        Log.d(TAG, "catchCanceling  " + (bytes.length == 1 && STRATEGY == DownloadStrategy.ALL));
        if (bytes.length == 1 && STRATEGY == DownloadStrategy.ALL)
            canceled = true;
    }

    /**
     * @param bytes
     * @param fileContainer
     */
    private void catchDownloadError(byte[] bytes, FileContainer fileContainer) {
        if (bytes.length == 1) {
            errors++;
            fileContainer.setSucceed(false);

        } else {
            downloaded++; // this variable is only for testing
            fileContainer.setSucceed(true);
        }

        remains--;
    }

    /**
     * @param fileContainer
     * @throws IOException
     */
    private void handleDownloadError(FileContainer fileContainer) throws IOException {
        if (!fileContainer.isSucceed())
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
                .doOnNext(bytes -> catchCanceling(bytes))
                .map(bytes -> produceFileContainerFromBytes(bytes, fileContainer))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(fileContainerError -> handleDownloadError(fileContainerError));

        if (STRATEGY == DownloadStrategy.ALL)
            observable = allStrategy(observable);
        else
            observable = maxStrategy(observable, fileContainer);

        return observable.doOnNext(fileContainerOnNext -> publishContainer(fileContainerOnNext))
                .filter(fileContainer1 -> fileContainer1.isSucceed() && !fileContainer1.isCanceled())
                .filter(fileContainer1 -> {
                    Log.d(TAG, "Filter is canceled ? " + fileContainer1.isCanceled());
                    return true;
                });
    }

    /**
     * Max Stratey handling errors
     *
     * @param observable
     * @param fileContainer
     * @return
     */
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

    /**
     * @param observable
     * @return
     */
    private Observable<FileContainer> allStrategy(Observable<FileContainer> observable) {
        Log.d(TAG, "Going to use all strategy");
        return observable
                .doOnError(throwable -> {
                    Log.d(TAG, "doOnError");
                    this.itemsObserver.onError(throwable);
                })
                .onErrorResumeNext(throwable -> {
                    Log.d(TAG, "onErrorResumeNext all strategy");
                    throwable.onComplete();
//                    this.subject.onCompleteWithSuccess();
                    subject.onComplete();
                    this.itemsObserver.CompleteWithError();
                });
    }

    /**
     * Downloading files sequentially using concatMap
     *
     * @param observable
     * @return
     */
    private Observable<FileContainer> sequentialDownloading(Observable<FileContainer> observable) {
        return observable.concatMap(fileContainer -> ObservableFileDownloader(fileContainer));
    }

    /**
     * Downloading the files in Parallel using FlatMap
     *
     * @param observable
     * @return
     */
    private Observable<FileContainer> parallelDownloading(Observable<FileContainer> observable) {
        return observable.flatMap(fileContainer -> ObservableFileDownloader(fileContainer));
    }


    /**
     * Converts the downloaded files to be observable and consumed Reactively
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

        if (ORDER == DownloadStrategy.FLAG_PARALLEL)
            observable = parallelDownloading(observable);
        else if (ORDER == DownloadStrategy.FLAG_SEQUENTIAL)
            observable = sequentialDownloading(observable);

        return observable.toList();
    }


    /**
     * Builder Class
     */
    public static final class Builder {
        Context context;
        OkHttpClient client;
        int STRATEGY;
        int ORDER;
        File STORAGE;
        RxStorage rxStorage;
        /**
         * List of files to be downloaded
         */
        List<FileContainer> files;

        /**
         * @param context
         */
        public Builder(Context context) {
            this.context = context;
            STRATEGY = DownloadStrategy.DEFAULT;
            ORDER = DownloadStrategy.FLAG_PARALLEL; // default value
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
        public Builder client(@NonNull OkHttpClient client) {
            if (client != null)
                this.client = client;
            return this;
        }


        /**
         * Set the order of downloading files
         * it could be parallel or sequential
         *
         * @param order
         * @return Builder
         */
        public Builder Order(int order) {
            ORDER = order;
            return this;
        }

        /**
         * Set strategy for downloading files
         * (MAX or ALL)
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
            if (isUrl(url)) {
                String extesion = "";
                if (newName.indexOf(".") < 0)
                    extesion = ExtractExtension(url);
                files.add(new FileContainer(url, newName + extesion));
            }
            return this;
        }

        private boolean isUrl(String url) {
            return Patterns.WEB_URL.matcher(url.toLowerCase()).matches();
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
         * ُExtract the Name and extension of a given file URL
         *
         * @param url
         * @return Builder
         */
        protected String ExtractNameAndExtension(String url) {
            return url.substring(url.lastIndexOf("/") + 1);
        }


        /**
         * Add file to downloading list
         *
         * @param url
         * @return Builder
         */
        public Builder addFile(@NonNull String url) {
            if (isUrl(url)) {
                String name = ExtractNameAndExtension(url);
                files.add(new FileContainer(url, name));
            }
            return this;
        }

        /**
         * Set the storage type to save files in
         *
         * @param storagePath
         * @return Builder
         */
        public Builder storage(@NonNull File storagePath) {
            if (storagePath != null)
                this.STORAGE = storagePath;
            return this;
        }

        /**
         * Add Bulk of files to the List of files
         *
         * @param urls
         * @return Builder
         */
        public Builder addFiles(@NonNull List<String> urls) {
            if (urls != null)
                for (String url : urls) {
                    if (isUrl(url))
                        addFile(url);
                }
            return this;
        }

        public RxDownloader build() {
            return new RxDownloader(this);
        }
    }


}
