package com.oussaki.rxfilesdownloader;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.List;

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
    RxDownloader.Builder builder;
    Context context;
    String TAG = "Tests";

    @Before
    public void setUp() throws Exception {
        context = RuntimeEnvironment.application;
        Log.i("Rx", "init rxdownloading");
        String url = "https://www.nissan-cdn.net/content/dam/Nissan/nissan_middle_east/vehicles/patrol/product_code/product_version/overview/en.jpg.ximg.m_12_m.smart.jpg";
        String url2 = "https://www.nissan-cdn.net/content/dam/Nissan/nissan_middle_east/vehicles/patrol/product_code/product_version/overview/en.jpg";
        String url3 = "https://www.nissan-cdn.net/content/dam/Nissan/nissan_middle_east/vehicles/x-trail/product_code/product_version/overview/14TDI_ROGb004x.jpg";
        builder = new RxDownloader.Builder(context)
                .addFile("file1", url)
                .addFile(url3)
                .addFile(url2);
    }

    @Test
    public void isMaxStrategyworks() throws Exception {
        rxDownloader = builder
                .strategy(DownloadStrategy.MAX)
                .addFile("http://fakeURL.com/error-file.jpg")
                .build();

        List<FileContainer> res = rxDownloader.asList().blockingGet();
        res.forEach(fileContainer -> {
            Log.i("RxDownloader", "key" + fileContainer.getFilename() + ",value " + fileContainer.getUrl());
        });
        assertNotEquals(res.size(), 0);
        Assert.assertEquals(res.size(), 3);
    }

    @Test
    public void isAllStrategyworks() throws Exception {
        rxDownloader = builder
                .strategy(DownloadStrategy.ALL)
                .addFile("http://fakeURL.com/error-file2.jpg")
                .build();
        List<FileContainer> res = rxDownloader.asList().blockingGet();
        Assert.assertEquals(res.size(), 0);
    }
}
