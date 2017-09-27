package oussaki.com.rxdownloader;

import android.os.Build;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowLog;

import static org.junit.Assert.assertEquals;

/**
 * Created by oussama on 9/20/2017.
 */

@RunWith(RobolectricTestRunner.class)
@Config(
        constants = BuildConfig.class,
        sdk = Build.VERSION_CODES.LOLLIPOP
//        manifest = "build/intermediates/manifests/full/debug/AndroidManifest.xml"
)
public class UiTest {
    Main main;
    ShadowActivity shadowActivity;

    @Before
    public void setup() throws Exception {
        ShadowLog.stream = System.out;
        main = Robolectric.setupActivity(Main.class);
    }

    @Test
    public void LoginClickPerformed() throws Exception {
        shadowActivity = Shadows.shadowOf(main);
        shadowActivity.findViewById(R.id.Async).performClick();

//        assertEquals("Download finish successfully",
//                ((TextView) shadowActivity.findViewById(R.id.progress)).getText().toString()
//        );
    }

}
