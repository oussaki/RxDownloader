package oussaki.com.rxdownloader;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.oussaki.rxfilesdownloader.DownloadStrategy;
import com.oussaki.rxfilesdownloader.FileContainer;
import com.oussaki.rxfilesdownloader.RxDownloader;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import io.reactivex.observers.TestObserver;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    String url = "https://www.nissan-cdn.net/content/dam/Nissan/nissan_middle_east/vehicles/patrol/product_code/product_version/overview/en.jpg.ximg.m_12_m.smart.jpg";
    String url2 = "https://www.nissan-cdn.net/content/dam/Nissan/nissan_middle_east/vehicles/patrol/product_code/product_version/overview/en.jpg";
    String url3 = "https://www.nissan-cdn.net/content/dam/Nissan/nissan_middle_east/vehicles/x-trail/product_code/product_version/overview/14TDI_ROGb004x.jpg";
    Context appContext;
    RxDownloader rxDownloader;
    RxDownloader.Builder builder;

    @Rule
    public final TrampolineSchedulerRule schedulerRule = new TrampolineSchedulerRule();

    @Before
    public void setup() throws Exception {
        appContext = InstrumentationRegistry.getTargetContext();
        builder = new RxDownloader.Builder(appContext)
                .addFile("file1", url)
                .addFile(url3)
                .addFile(url2);
    }

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        assertEquals("oussaki.com.rxdownloader", appContext.getPackageName());
    }

}
