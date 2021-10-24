package com.yorren.esemkainvestasi.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.yorren.esemkainvestasi.BuildConfig;
import com.yorren.esemkainvestasi.HttpHandler;
import com.yorren.esemkainvestasi.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

public class LoginActivity extends AppCompatActivity {
    private Button btnSubmit;
    private EditText edtMail, edtPassword;
    private ProgressDialog progressDialog;
    private String final_token;
    private JSONObject jsonData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtMail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnSubmit = findViewById(R.id.btnSubmit);

        //mengecek koneksi url
        new CheckConnection().execute();

        btnSubmit.setOnClickListener(v -> {
            Login();
        });
    }

    private void Login() {
        String email = edtMail.getText().toString();
        String password = edtPassword.getText().toString();
        if (email.isEmpty()) {
            edtMail.setError("Please input email");
        }
        if (password.isEmpty()) {
            edtMail.setError("Please input password");
        }
        try {
            jsonData = new JSONObject();
            jsonData.put("email", email);
            jsonData.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new PostLogin().execute();
    }

    private class PostLogin extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpHandler httpHandler = new HttpHandler();
            String jsonResponse = httpHandler.AuthLogin(BuildConfig.BASE_URL + "api/Auth", jsonData.toString());
            final_token = null;
            if (jsonResponse != null) {
                    final_token = jsonResponse;
                    Log.d("TOKEN", "access_token : " + final_token);
            } else {
                Log.e("TAG", "Could not get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Could not get json from server.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return final_token;
        }

        @Override
        protected void onPostExecute(String s) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (s != null) {
                Snackbar.make(findViewById(R.id.parent_layout), "Login berhasil " + s, Snackbar.LENGTH_SHORT).show();
                SharedPreferences token = getApplicationContext().getSharedPreferences("access-token", Context.MODE_PRIVATE);
                token.edit().putString("TOKEN", s).apply();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            } else {
                Snackbar.make(findViewById(R.id.parent_layout), "Wrong username or password", BaseTransientBottomBar.LENGTH_SHORT).show();
            }
        }
    }

    private class CheckConnection extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.setMessage("Please wait..");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                URL myUrl = new URL(BuildConfig.BASE_URL);
                URLConnection connection = myUrl.openConnection();
                connection.setConnectTimeout(6000);
                connection.connect();
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                progressDialog.dismiss();
                try {
                    HttpHandler httpHandler = new HttpHandler();
                    String jsonString = httpHandler.MakeServiceCall(BuildConfig.BASE_URL + "api/Auth/me", LoginActivity.this);
                    Log.e("TAG", "Response from url: " + jsonString);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                progressDialog.dismiss();
                Snackbar.make(findViewById(R.id.parent_layout), "Connection failed", BaseTransientBottomBar.LENGTH_SHORT).show();
            }
        }
    }
}