package com.oussaki.rxfilesdownloader;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.File;
import java.util.List;
import java.util.Map;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Single;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by oussama on 9/17/2017.
 */

@RunWith(RobolectricTestRunner.class)
@Config(
        sdk = Build.VERSION_CODES.LOLLIPOP,
        manifest = "build/intermediates/manifests/aapt/debug/AndroidManifest.xml")
public class RxDownloaderTest {
    RxDownloader rxDownloader;
    Context context;
    String TAG = "Tests";

    @Before
    public void setUp() throws Exception {
        context = RuntimeEnvironment.application;
        Log.i("Rx", "init rxdownloading");
        String url = "https://www.nissan-cdn.net/content/dam/Nissan/nissan_middle_east/vehicles/patrol/product_code/product_version/overview/en.jpg.ximg.m_12_m.smart.jpg";
        String url2 = "https://www.nissan-cdn.net/content/dam/Nissan/nissan_middle_east/vehicles/patrol/product_code/product_version/overview/en.jpg";
        String url3 = "https://www.nissan-cdn.net/content/dam/Nissan/nissan_middle_east/vehicles/x-trail/product_code/product_version/overview/14TDI_ROGb004x.jpg";
        rxDownloader = new RxDownloader.Builder(context)
                .setStorage(RxStorage.DATA_DIRECTORY)
                .setStrategy(Strategy.ASYNC)
                .addFile("file1", url)
                .addFile(url3)
                .addFile(url2)
                .build()
                .setListeners(new IDownloadProgress() {
                    @Override
                    public void OnStart() {

                    }

                    @Override
                    public void OnProgress(int progress) {

                    }

                    @Override
                    public void OnComplete() {

                    }

                    @Override
                    public void OnError(Throwable throwable) {

                    }
                });
    }

    @Test
    public void IsDownloadingAsyncFiles() throws Exception {
//        Single<List<Map.Entry<String, File>>> obs = rxDownloader.asObservable().toList();
//        List<Map.Entry<String, File>> res = obs.blockingGet();
//        for (int i = 0; i < res.size(); i++) {
//            Log.i("RxDownloader", "key" + res.get(i).getKey() + ",value " + res.get(i).getValue());
//        }
//        assertNotEquals(res.size(), 0);
//        assertThat(res.size()).isEqualTo(3);
    }
}
