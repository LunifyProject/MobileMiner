// Copyright (c) 2025 Lunify
//
// Please see the included LICENSE file for more information.

package xyz.lunify.androidminer.network;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Json {

    private static final String LOG_TAG = "MiningSvc";

    private Json() {
    }

    public static String fetch(String url) {

        StringBuilder data = new StringBuilder();
        try {
            URL urlFetch = new URL(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) urlFetch.openConnection();
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                data.append(line);
            }

        } catch (Exception e) {
            Log.i(LOG_TAG, e.toString());
        }

        return data.toString();
    }
}
