package oussaki.com.rxdownloader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.oussaki.rxfilesdownloader.IDownloadProgress;
import com.oussaki.rxfilesdownloader.RxDownloader;

import java.io.File;
import java.util.List;
import java.util.Map;

import io.reactivex.functions.BiConsumer;

public class Main extends AppCompatActivity {
    TextView txtProgress;
// comment
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtProgress = (TextView) findViewById(R.id.progress);
        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String url = "https://www.nissan-cdn.net/content/dam/Nissan/nissan_middle_east/vehicles/patrol/product_code/product_version/overview/en.jpg.ximg.m_12_m.smart.jpg";
                String url2 = "https://www.nissan-cdn.net/content/dam/Nissan/nissan_middle_east/vehicles/patrol/product_code/product_version/overview/en.jpg";
                String url3 = "https://www.nissan-cdn.net/content/dam/Nissan/nissan_middle_east/vehicles/x-trail/product_code/product_version/overview/14TDI_ROGb004x.jpg";

                new RxDownloader.Builder(getApplicationContext())
                        .addFile(url)
                        .addFile("file2", url2)
                        .addFile(url3)
                        .build()
                        .setListeners(new IDownloadProgress() {
                            @Override
                            public void initProgress() {
                                txtProgress.setText("Progress: 0%");
                            }

                            @Override
                            public void OnProgress(int i) {
                                Log.d("OnProgress","Called"+i);
                                txtProgress.setText("Progress: " + i + "%");
                            }

                            @Override
                            public void OnFinish() {
                                txtProgress.setText("Download finish successfully");
                            }
                        })
                        .asObservable()
                        .subscribe(new BiConsumer<List<Map.Entry<String, File>>, Throwable>() {
                            @Override
                            public void accept(List<Map.Entry<String, File>> entries, Throwable throwable) throws Exception {
                                // do something with data;
                                Log.d("ss","ssssssss");
                            }
                        });

            }
        });

    }
}
