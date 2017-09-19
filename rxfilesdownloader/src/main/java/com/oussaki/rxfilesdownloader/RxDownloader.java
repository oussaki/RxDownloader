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
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by oussama abdallah , AKA oussaki on 9/13/2017 , 3:02 PM.
 */

public class RxDownloader {
    static String TAG = "RxDownloader";
    Context context;
    OkHttpClient client;
    int STRATEGY;
    File STORAGE;
    HashMap<String, String> files;
    IDownloadProgress iDownloadProgress;
    int downloaded = 0;
    int errors = 0;
    int size;

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
     * @param iDownloadProgress
     * @return
     */
    public RxDownloader setListeners(IDownloadProgress iDownloadProgress) {
        this.iDownloadProgress = iDownloadProgress;
        return this;
    }

    @Deprecated
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

    /**
     * Download a file
     *
     * @param filename
     * @return
     */
    private Observable<Map.Entry<String, File>> DownloadAFile(final String filename) {
        if (iDownloadProgress != null)
            iDownloadProgress.initProgress();

        final File file = new File(STORAGE + File.separator + filename);
        if (file.exists()) {
            Log.d(TAG, "file exist");
        }
        Request request = new Request.Builder().url(files.get(filename)).build();
        final FlowableCallback callback = new FlowableCallback();
        client.newCall(request).enqueue(callback);
        return callback.getFlowable()
                .map(new Function<Response, Map.Entry<String, File>>() {
                    @Override
                    public Map.Entry<String, File> apply(Response response) throws Exception {
                        try {
                            final InputStream inputStream = response.body().byteStream();
                            saveToFile(inputStream, file);
                            downloaded++;
                            int progress = Math.abs((downloaded * 100) / size);
                            if (iDownloadProgress != null)
                                iDownloadProgress.OnProgress(progress);
                        } catch (IOException io) {
                            errors++;
                        }
                        Map.Entry<String, File> entry =
                                new AbstractMap.SimpleEntry<>(files.get(filename), file);

                        return entry;
                    }
                }).doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        errors++;
                        Log.i(TAG, "Error accept");
                    }
                }).doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        if (iDownloadProgress != null)
                            iDownloadProgress.OnFinish();
                    }
                });
    }

    /**
     * Downloading the files Asynchronously
     *
     * @param observable
     * @return
     */
    public Single<List<Map.Entry<String, File>>> AsyncDownloading(Observable<String> observable) {
        Log.i(TAG, "AsyncDownloading");
        return observable
                .flatMap(new Function<String, ObservableSource<Map.Entry<String, File>>>() {
                    @Override
                    public ObservableSource<Map.Entry<String, File>> apply(final String filename) throws Exception {
                        return DownloadAFile(filename);
                    }
                })
                .toList();
    }

    /**
     * Downloading the files Synchronously
     *
     * @param observable
     * @return
     */
    public Single<List<Map.Entry<String, File>>> SyncDownloading(Observable<String> observable) {
        Log.i(TAG, "SyncDownloading");
        return observable.map(new Function<String, Map.Entry<String, File>>() {
            @Override
            public Map.Entry<String, File> apply(final String filename) throws Exception {
                return DownloadAFile(filename).blockingFirst();
            }
        }).toList();
    }

    /**
     * Converts the downloaded files to be observable and consumed reactively
     *
     * @return
     */
    public Single<List<Map.Entry<String, File>>> asObservable() {
        size = files.size();
        Observable<String> observable = Observable.fromIterable(files.keySet());
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
         * @param context
         */
        public Builder(Context context) {
            this.context = context;
            STRATEGY = Strategy.DEFAULT;
            client = new OkHttpClient();
            files = new HashMap<>();
            this.STORAGE = context.getCacheDir();
            Log.i("RxDownloader", "Builder Constructor called");
        }


        public HashMap<String, String> getListOfFilesUrls() {
            return files;
        }

        /**
         * Set a custom Http Client (OkhttpClient )
         *
         * @param client
         * @return
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
         * @return
         */
        Builder setStrategy(int strategy) {
            STRATEGY = strategy;
            return this;
        }

        /**
         * Add a URL of a file to the list of downloading
         * and rename it to the given name
         *
         * @param name
         * @param url
         * @return
         */
        public Builder addFile(String name, String url) {
            String extesion = "";
            if (name.indexOf(".") < 0)
                extesion = ExtractExtension(url);
            files.put(name + extesion, url);
            return this;
        }

        /**
         * Extract the extension of file from a given URL
         *
         * @param url
         * @return
         */
        protected String ExtractExtension(String url) {
            return url.substring(url.lastIndexOf("."));
        }

        /**
         * ُExtract the Name and extension of given file URL
         *
         * @param url
         * @return
         */
        protected String ExtractNameAndExtension(String url) {
            return url.substring(url.lastIndexOf("/") + 1);
        }


        /**
         * Set the storage type to save files in
         *
         * @param url
         * @return
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
         * @return
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
         * @return
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
