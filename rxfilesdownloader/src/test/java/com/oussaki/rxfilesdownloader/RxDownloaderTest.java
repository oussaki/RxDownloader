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

import io.reactivex.Flowable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by oussama on 9/17/2017.
 */

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.LOLLIPOP,
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
                    public void initProgress() {
                        Log.i(TAG, "init Progress ");
                    }

                    @Override
                    public void OnProgress(int progress) {
                        Log.i(TAG, "OnProgress " + progress);
                    }

                    @Override
                    public void OnFinish() {
                        Log.i(TAG, "OnFinish");
                    }
                });
    }

    @Test
    public void clickingLogin_shouldStartLoginActivity() {
        int x = 1;
        assertThat(x).isEqualTo(1);
    }

    @Test
    public void IsDownloadingAsyncFiles() throws Exception {
        Flowable<List<Map.Entry<String, File>>> obs = rxDownloader.asObservable().toFlowable();
        List<Map.Entry<String, File>> res = obs.blockingFirst();
        for (int i = 0; i < res.size(); i++) {
            Log.i("RxDownloader", "key" + res.get(i).getKey() + ",value " + res.get(i).getValue());
        }
        assertNotEquals(res.size(), 0);
    }
}
