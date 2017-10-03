package oussaki.com.rxdownloader;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.oussaki.rxfilesdownloader.DownloadStrategy;
import com.oussaki.rxfilesdownloader.FileContainer;
import com.oussaki.rxfilesdownloader.RxDownloader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    String TAG = "RxDownloader";

    String url = "https://www.nissan-cdn.net/content/dam/Nissan/nissan_middle_east/vehicles/patrol/product_code/product_version/overview/en.jpg.ximg.m_12_m.smart.jpg";
    String url2 = "https://www.nissan-cdn.net/content/dam/Nissan/nissan_middle_east/vehicles/patrol/product_code/product_version/overview/en.jpg";
    String url3 = "https://www.nissan-cdn.net/content/dam/Nissan/nissan_middle_east/vehicles/x-trail/product_code/product_version/overview/14TDI_ROGb004x.jpg";
    String aVideo = "http://techslides.com/demos/sample-videos/small.mp4";
    EditText multiline;
    ProgressBar progressBar;
    TextView txtProgress;

    /* PublishSubject emits to an observer only those items that are emitted
    * by the source Observable, subsequent to the time of the subscription.
    */
    private void streams() {
        Observable.interval(1, TimeUnit.SECONDS)
                .map(input -> {
                    throw new IOException();
                })
                .onErrorReturn(error -> "Uh oh")
                .subscribe(System.out::println);
    }

    private void example() {
        RxDownloader rxDownloader = new RxDownloader
                .Builder(context)
                .addFile("http://reactivex.io/assets/Rx_Logo_S.png")
                .build();
        // Subscribe to start downloading files
        rxDownloader.asList().subscribe((fileContainers, throwable) -> {
           // Do awesome things with your files
        });
    }

    private void Sample() {
        new RxDownloader.Builder(context)
                .addFile(url)
                .addFile("video", aVideo)
                .addFile("videoxssda", aVideo)
                .addFile("vxidaseo", aVideo)
                .addFile("file2", url2)
                .addFile("videoxssda", aVideo)
                .addFile("vxidaseo", "http://goolpanitchugos.com/j.jpg")
                .addFile("file2", url2)
                .addFile("videoxssda", aVideo)
                .addFile("vidaseo", "http://goolpanitchugo.com/j.jpg")
                .addFile("vxidaseo", aVideo)
                .addFile("file2", url2)
                .addFile(url3)
                .strategy(DownloadStrategy.MAX)
                .build()
                .doOnStart(() -> {
                    progressBar.setProgress(0);
                    multiline.setText("");
                    txtProgress.setText("About to start downloading");
                })
                .doOnProgress(progress -> {
                    progressBar.setProgress(progress);
                    multiline.append("\n Progress " + progress);
                    txtProgress.setText("Progress: " + progress + "%");
                })
                .doOnSingleError(throwable -> {
                    multiline.append("\n " + throwable.getMessage());
                })
                .doOnCompleteWithError(() -> {
                    txtProgress.setText("Download finished with error");
                })
                .doOnCompleteWithSuccess(() -> {
                    txtProgress.setText("Download finished successfully");
                })
                .asList()
                .subscribe((entries, throwable) -> {
                    Log.e(TAG, "Files count:" + entries.size());
                    // Showing the list of files
                    for (FileContainer fileContainer : entries) {
                        if (fileContainer.isSucceed()) {
                            Log.e(TAG, "File: " + fileContainer.getFile().getName()
                                    + ", status: " + fileContainer.isSucceed()
                                    + ", is there Bytes ? " + (fileContainer.getBytes() != null));
                        }
                    }

                });
    }

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        txtProgress = (TextView) findViewById(R.id.progress);
        multiline = (EditText) findViewById(R.id.multiline);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        findViewById(R.id.sync).setOnClickListener(view -> streams());
        findViewById(R.id.Async).setOnClickListener(view -> Sample());
    }

}
