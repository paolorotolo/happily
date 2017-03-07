package com.github.paolorotolo.happily;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetHappyQuote extends AsyncTask<String, String, String> {

    Context context;
    AsyncResponse delegate = null;

    public GetHappyQuote(AsyncResponse delegate) {
        this.delegate = delegate;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... aurl) {
        String messageArray = null;
        try {
            URL url = new URL("http://happyapi.co/api/daily/");
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            messageArray = parseJson(convertStreamToString(httpURLConnection.getInputStream())).toString();
            Log.e("happy", "API request returning " + messageArray);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return messageArray;
    }

    private String parseJson(String s) throws JSONException {
        JSONObject json = new JSONObject(s);
        return json.getString("quote");
    }

    protected void onProgressUpdate(String... progress) {
    }

    @Override
    protected void onPostExecute(String messageArray) {
        delegate.processFinish(messageArray);
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public interface AsyncResponse {
        void processFinish(String output);
    }
}

