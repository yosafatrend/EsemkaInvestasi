package com.yorren.esemkainvestasi;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class HttpHandler {
    private static final String TAG = HttpHandler.class.getSimpleName();
    public int res_code = 0;

    public HttpHandler() {

    }

    public String AuthLogin(String requestUrl, String jsonData) {
        Log.e("HTTP Request URL ", requestUrl);
        URL url;
        String response = "";
        try {
            url = new URL(requestUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(6000);
            conn.setConnectTimeout(6000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Accept", "text/plain");
            conn.setDoOutput(true);

            Log.d("JSON", jsonData);

            OutputStream os = conn.getOutputStream();

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(jsonData);
            writer.flush();
            writer.close();
            os.close();

            int responseCode = conn.getResponseCode();
            Log.e("HTTP Response Code", Integer.toString(responseCode));
            res_code = responseCode;
            InputStream inputStream = new BufferedInputStream(conn.getInputStream());
            response = ConvertStreamToString(inputStream);
            Log.e("Response Login ", response);
        } catch (ProtocolException e) {
            Log.e(TAG, "ProtocolException :" + e.getMessage());
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException :" + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException :" + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception :" + e.getMessage());
        }
        return response;
    }

    public String MakeServiceCall(String urlReq, Context context) {
        String result = null;
        SharedPreferences token = context.getSharedPreferences("access-token", Context.MODE_PRIVATE);
        String final_token = token.getString("TOKEN", null);
        Log.e("HOMEHTTP", urlReq);

        try {
            URL mUrl = new URL(urlReq);
            HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + final_token);
            int responseCode = conn.getResponseCode();
            if (responseCode == 401) {
                result = "401";
                token.edit().putString("TOKEN", null).apply();
            }
            InputStream inputStream = new BufferedInputStream(conn.getInputStream());
            result = ConvertStreamToString(inputStream);

        } catch (ProtocolException e) {
            Log.e(TAG, "MalformedURLException :" + e.getMessage());
        } catch (MalformedURLException e) {
            Log.e(TAG, "ProtocolException :" + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException :" + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception :" + e.getMessage());
        }
        return result;
    }

    public String PerformPostCall(String requestUrl, Context context){
        URL url;
        String response;
        SharedPreferences token = context.getSharedPreferences("access-token", Context.MODE_PRIVATE);
        String final_token = token.getString("TOKEN", null);
        Log.e("HOMEHTTP", requestUrl);
        try {
            url = new URL(requestUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(3000);
            conn.setConnectTimeout(6000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("charset", "utf=8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + final_token);
            conn.setDoOutput(true);

//            OutputStream os = conn.getOutputStream();
//            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
//
//            writer.write(jsonData);
//            writer.flush();
//            writer.close();
//            os.close();
            int responseCode = conn.getResponseCode();
            Log.e("HTTP Response Code", Integer.toString(responseCode));
            res_code = responseCode;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Integer.toString(res_code);
    }

    private String ConvertStreamToString(InputStream inputStream) {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();

        String i;
        try {
            while ((i = bufferedReader.readLine()) != null) {
                sb.append(i).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
