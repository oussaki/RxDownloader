package oussaki.com.rxdownloader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.oussaki.rxfilesdownloader.IDownloadProgress;
import com.oussaki.rxfilesdownloader.RxDownloader;
import com.oussaki.rxfilesdownloader.Strategy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Cancellable;
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

    private void FooTry() {
        downloaded = 0;
        final OkHttpClient client = new OkHttpClient();
        final HashMap<String, String> files = new HashMap<>();
        files.put(url, "file1.jpg");
        files.put(url2, "file2.jpg");
        files.put(aVideo, "video.mp4");
        files.put(url, "file3.jpg");
        files.put(url3, "file4.jpg");
        files.put(url3, "file5.jpg");
        size = files.size();

        Observable.fromIterable(files.keySet())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(disposable -> {
                    // you can init here

                })
                .flatMap(fileurl -> {
                    final String filename = files.get(fileurl);
                    final File file = new File(getCacheDir() + File.separator + filename);
                    if (file.exists())
                        Log.d(TAG, "file exist");
                    final Request request = new Request.Builder().url(fileurl).build();
                    return Observable
                            .fromCallable(() -> client.newCall(request).execute())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnNext(response -> {
                                try {
                                    saveToFile(response.body().bytes(), file);
                                    Log.d("files", "Saving file " + file.getPath());
                                    downloaded++;
                                } catch (IOException io) {
                                    Log.e(TAG, "IOException" + io.getMessage());
//                                            errors++;
                                }
                            })
                            .doOnComplete(() -> {
                                int progress = Math.abs((downloaded * 100) / size);
                                progressBar.setProgress(progress);
//                                multiline.setText(multiline.getText().toString() + " /n Progress: " + progress + "file:"
//                                        + file.getPath());
//                                txtProgress.setText("Progress: " + progress);
                            }).map(response -> {
                                Log.d("maping", "Maping files " + filename + ",path" + file.getPath());
                                return new AbstractMap.SimpleEntry<>(files.get(filename), file);
                            });
                })
                .doFinally(() -> txtProgress.setText("Download finish"))
                .toList()
                .subscribe((simpleEntries, throwable) -> {
                    Toast.makeText(getApplicationContext(), "i have " +
                            simpleEntries.size() + " Files here", Toast.LENGTH_LONG).show();
                });

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


    private void doSomeWork() {
        ReplaySubject<Tuple> source = ReplaySubject.create();

        source.subscribe(getFirstObserver());

        final HashMap<String, String> files = new HashMap<>();
        files.put(url, "file1.jpg");
        files.put(url2, "file2.jpg");
        files.put(aVideo, "video.mp4");
        files.put(url, "file3.jpg");
        files.put(url3, "file4.jpg");
        files.put(url3, "file5.jpg");
        done = files.size();
        size = files.size();

        Observable
                .fromIterable(files.keySet())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .flatMap(urlx -> {
                    Observable
                            .fromCallable(() -> downloadFile(urlx))
                            .onErrorReturn(throwable -> {
                                Log.e(TAG, "throwable");
                                byte[] b = new byte[1];
                                Log.e(TAG, "b.length:" + b.length);
                                return b;
                            }).onErrorResumeNext(observer -> {
//                        observer.onNext();
                    })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnNext(response -> {
                                if (response.length == 1) {
                                    source.onError(new IOException("Can not download the file"));
                                    return;
                                }

                                done--;
                                Log.i(TAG, "do on next:" + response.length);
                                current_thread();
                                final File file = new File(getCacheDir() + File.separator + files.get(urlx));
                                int progress = 0;
                                if (size > 0)
                                    progress = Math.abs(((done * 100) / size) - 100);

                                Tuple tuple = new Tuple();
                                tuple.setBytes(response);
                                tuple.setProgress(progress);
                                tuple.setFile(file);
                                source.onNext(tuple);
                                if (done == 0) {
                                    Log.i(TAG, done + " i will throw on complete");
                                    source.onComplete();
                                }

                                Log.e(TAG, "do on next");
                            }).subscribe();
                    return   observer -> {
                    };
                })
                .subscribe(o -> {

                });

//
//        Observable
//                .fromCallable(() -> ok.newCall(new Request.Builder().url(url2).build()).execute().body())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io())
//                .doOnNext(response -> {
//                    current_thread();
//                    Log.e(TAG, "do on next:" + response.byteStream().toString());
//                    source.onNext(response.contentLength());
//                    done++;
//                    if (done == 2) {
//                        Log.i(TAG, "2 i will throw on complete");
//                        source.onComplete();
//                    }
//
//                    Log.e(TAG, "do on next");
//                }).subscribe();


//        source.onComplete();

        /*
         * it will emit 1, 2, 3, 4 for second observer also as we have used replay
         */
        //  source.subscribe(getSecondObserver());

    }

    private Observer<Tuple> getFirstObserver() {
        return new Observer<Tuple>() {

            @Override
            public void onSubscribe(Disposable d) {
                progressBar.setProgress(0);
                multiline.setText("");
                Log.d(TAG, " First onSubscribe : " + d.isDisposed());
            }

            @Override
            public void onNext(Tuple tuple) {
                try {
                    saveToFile(tuple.getBytes(), tuple.getFile()); // save file

                    Log.d(TAG, " First onNext value : " + tuple.getFile().getName());
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
                Log.d(TAG, "Download onComplete");
                multiline.append(" Download Complete");
                multiline.append("\n");
            }
        };
    }

    private Observer<Integer> getSecondObserver() {
        return new Observer<Integer>() {

            @Override
            public void onSubscribe(Disposable d) {
                multiline.append(" Second onSubscribe : isDisposed :" + d.isDisposed());
                Log.d(TAG, " Second onSubscribe : " + d.isDisposed());

            }

            @Override
            public void onNext(Integer value) {
                multiline.append(" Second onNext : value : " + value);
                multiline.append("\n");
                Log.d(TAG, " Second onNext value : " + value);
            }

            @Override
            public void onError(Throwable e) {
                multiline.append(" Second onError : " + e.getMessage());
                multiline.append("\n");
                Log.d(TAG, " Second onError : " + e.getMessage());
            }

            @Override
            public void onComplete() {
                multiline.append(" Second onComplete");
                multiline.append("\n");
                Log.d(TAG, " Second onComplete");
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
