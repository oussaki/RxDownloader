package com.oussaki.rxfilesdownloader;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by oussama on 9/16/2017.
 */

public class RxStorage {
    static int DATA_DIRECTORY = 0; //
    static int EXTERNAL_CACHE_DIR = 1;
    Context context;

    RxStorage(Context context) {
        this.context = context;
    }

    /**
     * Save an InputStream into a file
     *
     * @param is
     * @param file
     * @throws IOException
     */
    void saveToFile(InputStream is, File file) throws IOException {
        BufferedInputStream input = new BufferedInputStream(is);
        OutputStream output = new FileOutputStream(file);
        byte[] data = new byte[1024];
        long total = 0;
        int count;
        while ((count = input.read(data)) != -1) {
            total += count;
            output.write(data, 0, count);
        }
        output.flush();
        output.close();
        input.close();
    }


    void saveToFile(byte[] is, File file) throws IOException {
        FileOutputStream outputStream;
        try {
            outputStream = context.openFileOutput(file.getName(), context.MODE_PRIVATE);
            outputStream.write(is);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
