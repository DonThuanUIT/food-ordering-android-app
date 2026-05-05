package com.foodorderingapp.utils;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
public class FileUtils {
    public static File getFileFromUri(Context context, Uri uri) throws Exception {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            throw new Exception("Unable to read photos from gallery!");
        }

        File tempFile = File.createTempFile("upload_img_", ".jpg", context.getCacheDir());
        tempFile.deleteOnExit();

        OutputStream outputStream = new FileOutputStream(tempFile);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        outputStream.close();
        inputStream.close();

        return tempFile;
    }
}
