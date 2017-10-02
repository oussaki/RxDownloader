package com.oussaki.rxfilesdownloader;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertNotEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    RxDownloader rxDownloader;
    Context context;
    String TAG = "Tests";

    @Before
    public void setup() {
        context = InstrumentationRegistry.getTargetContext();
        Log.i("Rx", "init rxdownloading");
        String url = "https://www.nissan-cdn.net/content/dam/Nissan/nissan_middle_east/vehicles/patrol/product_code/product_version/overview/en.jpg.ximg.m_12_m.smart.jpg";
        String url2 = "https://www.nissan-cdn.net/content/dam/Nissan/nissan_middle_east/vehicles/patrol/product_code/product_version/overview/en.jpg";
        String url3 = "https://www.nissan-cdn.net/content/dam/Nissan/nissan_middle_east/vehicles/x-trail/product_code/product_version/overview/14TDI_ROGb004x.jpg";
        rxDownloader = new RxDownloader.Builder(context)
                .strategy(DownloadStrategy.MAX)
                .addFile("file1", url)
                .addFile(url3)
                .addFile(url2)
                .build();
    }


    @Test
    public void RxFilesDownloaderIsNotNull() throws Exception {
        Assert.assertNotNull(rxDownloader);
    }

    @Test
    public void IsDownloadingAsyncFiles() throws Exception {
        List<FileContainer> res = rxDownloader.asList().blockingGet();
        res.forEach(fileContainer -> {
            Log.i("RxDownloader", "key" + fileContainer.getFilename() + ",value " + fileContainer.getUrl());
        });
        assertNotEquals(res.size(), 0);
    }


}
