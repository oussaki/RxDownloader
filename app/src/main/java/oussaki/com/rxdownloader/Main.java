package oussaki.com.rxdownloader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.oussaki.rxfilesdownloader.IDownloadProgress;
import com.oussaki.rxfilesdownloader.RxDownloader;
import com.oussaki.rxfilesdownloader.Strategy;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.ReplaySubject;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Main extends AppCompatActivity {
    TextView txtProgress;

// Main

    String url = "https://www.nissan-cdn.net/content/dam/Nissan/nissan_middle_east/vehicles/patrol/product_code/product_version/overview/en.jpg.ximg.m_12_m.smart.jpg";
    String url2 = "https://www.nissan-cdn.net/content/dam/Nissan/nissan_middle_east/vehicles/patrol/product_code/product_version/overview/en.jpg";
    String url3 = "https://www.nissan-cdn.net/content/dam/Nissan/nissan_middle_east/vehicles/x-trail/product_code/product_version/overview/14TDI_ROGb004x.jpg";
    String aVideo = "http://techslides.com/demos/sample-videos/small.mp4";
    String TAG = "RxDownloader";
    int downloaded = 0;
    int size;
    EditText multiline;
    ProgressBar progressBar;
    /* PublishSubject emits to an observer only those items that are emitted
    * by the source Observable, subsequent to the time of the subscription.
    */
    OkHttpClient ok = new OkHttpClient();
    int done = 0;

    private void Sample(int strategy) {
        new RxDownloader.Builder(getApplicationContext())
                .setStrategy(strategy)
                .addFile(url)
                .addFile("video", aVideo)
                .addFile("videoxssda", aVideo)
                .addFile("vidaseo", aVideo)
                .addFile("vxidaseo", aVideo)
                .addFile("file2", url2)
                .addFile(url3)
                .build()
                .setListeners(new IDownloadProgress() {
                    @Override
                    public void initProgress() {
                        Log.d("aa", "init Progress called");
                        progressBar.setProgress(0);
                        txtProgress.setText("About to start downloading...");
                    }

                    @Override
                    public void OnProgress(int progress) {
                        Log.d("OnProgress", "Called" + progress);
                        progressBar.setProgress(progress);
//                        multiline.append("\n Progress " + i);
//                        txtProgress.setText("Progress: " + i + "%");
                    }

                    @Override
                    public void OnFinish() {
                        txtProgress.setText("Download finish successfully");
                        Log.e("ddd", "Finish");

                    }
                })
                .asObservable()
                .toList()
                .subscribe((entries, throwable) -> {
                    Log.e(TAG, "entries" + entries.size());
                });
    }

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

    void saveToFile(byte[] is, File file) throws IOException {
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(file.getName(), getApplicationContext().MODE_PRIVATE);
            outputStream.write(is);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void setProgressText(String s) {
        txtProgress.setText(s);
    }

    private void current_thread() {
        Log.e(TAG, "Thread:" + Thread.currentThread().getName());
    }

    private void something() {

        ObservableEmitter emitter = new ObservableEmitter() {
            int downloaded = 0;

            @Override
            public void setDisposable(Disposable d) {

            }

            @Override
            public void setCancellable(Cancellable c) {

            }

            @Override
            public boolean isDisposed() {
                return false;
            }

            @Override
            public ObservableEmitter serialize() {
                return null;
            }

            @Override
            public void onNext(Object o) {
                current_thread();
                String url = (String) o;
                Log.i(TAG, "do on next:" + url);
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.d(TAG, "Response" + response.isSuccessful());
                        downloaded++;
                        if (downloaded == 5)
                            onComplete();
                    }
                });
            }

            @Override
            public void onError(Throwable error) {

            }

            @Override
            public void onComplete() {
                Log.i(TAG, "onComplete");
                current_thread();

//                txtProgress.setText("Completed");
            }
        };


        Observable
                .create(e -> {

                    emitter.onNext(url);
                    emitter.onNext(url2);
                    emitter.onNext(url);
                    emitter.onNext(url3);
                    emitter.onNext(url);


                }).doOnNext(o -> {

        })

                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnError(throwable -> {
                    Log.e(TAG, "on error:" + throwable.getMessage());
                })
                .subscribe();
    }

    byte[] downloadFile(String url) throws IOException {
        return ok.newCall(new Request.Builder().url(url).build()).execute().body().bytes();
    }


    void funcfu(Function<Integer, Function> x) {
        try {
            x.apply(55);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void doSomeWork() {

        ReplaySubject<Tuple> subject= ReplaySubject.create();
        Observer<Tuple> tupleObserver = getFirstObserver();
        subject.subscribe(tupleObserver);

        List<Tuple> tuples = new ArrayList<>();
        tuples.add(new Tuple(aVideo, "video.mp4"));
        tuples.add(new Tuple(url, "file3.jpg"));
        tuples.add(new Tuple(url3, "file4.jpg"));
        tuples.add(new Tuple(url2, "file2.jpg"));
        tuples.add(new Tuple(url, "filey.jpg"));
        tuples.add(new Tuple(url, "filex.jpg"));


        size = tuples.size();
        done = size;

        Observable
                .fromIterable(tuples)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .flatMap(tuple -> {
                            return Observable
                                    .fromCallable(() -> downloadFile(tuple.getUrl()))
                                    .onErrorReturn(throwable -> {
                                        Log.e(TAG, "throwable");
                                        byte[] b = new byte[1];
                                        Log.e(TAG, "b.length:" + b.length);
                                        return b;
                                    })
                                    .subscribeOn(Schedulers.io())
                                    .doOnNext(response -> {
                                        current_thread();
                                        if (response.length == 1) {
                                            subject.onError(new IOException("Can not download the file"));
                                            return;
                                        }
                                        done--;
                                        Log.i(TAG, "do on next:" + response.length);
                                    })
                                    .map(bytes -> {
                                        current_thread();
                                        final String filename = tuple.getFilename();
                                        final File file = new File(getCacheDir() + File.separator + filename);
                                        int progress = 0;
                                        if (size > 0)
                                            progress = Math.abs(((done * 100) / size) - 100);

                                        tuple.setBytes(bytes);
                                        tuple.setProgress(progress);
                                        tuple.setFile(file);
                                        return tuple;
                                    })
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .doOnNext(tupleOnNext -> {
                                        current_thread();
                                        subject.onNext(tupleOnNext);
                                        if (done == 0) {
                                            Log.i(TAG, done + " i will throw on complete");
                                            subject.onComplete();
                                        }
                                    });
                        }
                )
                .toList()
                .subscribe((LesTuples, throwable) -> {
                    current_thread();
                    Log.d(TAG, "Subscribe Recieved" + LesTuples.get(0).getFilename());
                });

    }


    private Observer<Tuple> getFirstObserver() {
        return new Observer<Tuple>() {
            List<Tuple> tuples;

            @Override
            public void onSubscribe(Disposable d) {
                tuples = new ArrayList<>();
                progressBar.setProgress(0);
                multiline.setText("");
                Log.d(TAG, " First onSubscribe : " + d.isDisposed());
            }

            @Override
            public void onNext(Tuple tuple) {
                try {
                    saveToFile(tuple.getBytes(), tuple.getFile()); // save file
                    Log.d(TAG, " First onNext value : " + tuple.getFile().getName());
                    tuples.add(tuple);
                    progressBar.setProgress(tuple.getProgress());
                    multiline.append(" Progress is: " + tuple.getProgress());
                    multiline.append(" File downloaded: " + tuple.getFile().getName());
                    multiline.append("\n");

                } catch (IOException e) {

                    onError(new IllegalStateException("Can't not save file:" + tuple.getFile().getName()));
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, " First onError : " + e.getMessage());
                multiline.append("Error when downloading");
                multiline.append("\n");
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "Download Complete");
                multiline.append(tuples.size() + " files downloaded successfully ");
                multiline.append("\n");
            }
        };
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtProgress = (TextView) findViewById(R.id.progress);
        multiline = (EditText) findViewById(R.id.multiline);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        findViewById(R.id.sync).setOnClickListener(view -> Sample(Strategy.SYNC));
        findViewById(R.id.Async).setOnClickListener(view -> doSomeWork());
    }

    class Tuple {
        File file;
        byte[] bytes;
        int progress;

        String filename;
        String url;

        Tuple(String url, String filename) {
            this.url = url;
            this.filename = filename;
        }

        Tuple() {
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public int getProgress() {
            return progress;
        }

        public void setProgress(int progress) {
            this.progress = progress;
        }

        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }
    }
}
