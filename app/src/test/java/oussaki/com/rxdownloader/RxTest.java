package oussaki.com.rxdownloader;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.TextView;

import com.oussaki.rxfilesdownloader.DownloadStrategy;
import com.oussaki.rxfilesdownloader.FileContainer;
import com.oussaki.rxfilesdownloader.RxDownloader;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowLog;

import java.util.List;

import io.reactivex.observers.TestObserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

/**
 * Created by oussama on 9/20/2017.
 */

@RunWith(RobolectricTestRunner.class)
@Config(
        constants = BuildConfig.class,
        sdk = Build.VERSION_CODES.LOLLIPOP,
        packageName = "oussaki.com.rxdownloader"
//        manifest = "build/intermediates/manifests/full/debug/AndroidManifest.xml"
)
public class RxTest {
    RxDownloader rxDownloader;
    RxDownloader.Builder builder;
    Context context;
    String TAG = "Tests";

    Main main;
    ShadowActivity shadowActivity;

    @Rule
    public final TrampolineSchedulerRule schedulerRule = new TrampolineSchedulerRule();

    @Before
    public void setup() throws Exception {
        ShadowLog.stream = System.out;
        main = Robolectric.setupActivity(Main.class);

        context = RuntimeEnvironment.application;
        Log.i(TAG, "init rxdownloading");
        String url = "https://www.nissan-cdn.net/content/dam/Nissan/nissan_middle_east/vehicles/patrol/product_code/product_version/overview/en.jpg.ximg.m_12_m.smart.jpg";
        String url2 = "https://www.nissan-cdn.net/content/dam/Nissan/nissan_middle_east/vehicles/patrol/product_code/product_version/overview/en.jpg";
        String url3 = "https://www.nissan-cdn.net/content/dam/Nissan/nissan_middle_east/vehicles/x-trail/product_code/product_version/overview/14TDI_ROGb004x.jpg";
        builder = new RxDownloader.Builder(context)
                .addFile("file1", url)
                .addFile(url3)
                .addFile(url2);
    }

    @Test
    public void isMaxStrategyWorks() throws Exception {

        rxDownloader = builder
                .strategy(DownloadStrategy.MAX)
                .addFile("http://fakeURL.com/error-file.jpg")  // this will trigger an error
                .build();
        TestObserver<List<FileContainer>> testObserver = rxDownloader.asList().test();
        testObserver.awaitTerminalEvent();
        testObserver
                .assertNoErrors()
                .assertValue(l -> l.size() == 3);
    }

    @Test
    public void isAllStrategyWorks() throws Exception {

        rxDownloader = builder
                .strategy(DownloadStrategy.ALL)
                .addFile("http://fakeURL.com/error-file2.jpg") // this will trigger an error
                .addFile("http://reactivex.io/assets/Rx_Logo_S.png")
                .build();

        TestObserver<List<FileContainer>> testObserver = rxDownloader.asList().test();
        testObserver.awaitTerminalEvent();
        testObserver
                .assertNoErrors()
                .assertValueCount(1);

    }

}
