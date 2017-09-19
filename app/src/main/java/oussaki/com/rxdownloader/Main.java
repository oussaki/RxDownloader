package oussaki.com.rxdownloader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.oussaki.rxfilesdownloader.RxDownloader;

import java.io.File;
import java.util.List;
import java.util.Map;

import io.reactivex.functions.BiConsumer;

public class Main extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        RxDownloader rxDownloader = new RxDownloader.Builder(getApplicationContext())
//                .addFile("")
//                .build().asObservable().subscribe(new BiConsumer<List<Map.Entry<String, File>>, Throwable>() {
//                    @Override
//                    public void accept(List<Map.Entry<String, File>> entries, Throwable throwable) throws Exception {
//
//                    }
//                });
    }
}
