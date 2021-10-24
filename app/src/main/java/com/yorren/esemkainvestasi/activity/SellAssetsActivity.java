package com.yorren.esemkainvestasi.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yorren.esemkainvestasi.BuildConfig;
import com.yorren.esemkainvestasi.HttpHandler;
import com.yorren.esemkainvestasi.R;

import java.text.NumberFormat;
import java.util.Locale;

public class SellAssetsActivity extends AppCompatActivity {
    private SeekBar seekBar;
    TextView tvName, tvPrice, tvType, tvNominal, tvUnit;
    Button btnConfirm;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell_assets);

        seekBar = findViewById(R.id.seekBar);
        btnConfirm = findViewById(R.id.btnConfirm);
        tvName = findViewById(R.id.tvName);
        tvPrice = findViewById(R.id.tvPrice);
        tvType = findViewById(R.id.tvType);
        tvNominal = findViewById(R.id.tvNominal);
        tvUnit = findViewById(R.id.tvUnit);

        btnConfirm.setOnClickListener(v -> {
            if (tvUnit.getText().toString().equals("0")) {
                Toast.makeText(SellAssetsActivity.this, "Sell item can't 0", Toast.LENGTH_SHORT).show();
            }
            else {
                new PostSell().execute();
            }
        });

        tvName.setText(getIntent().getStringExtra("NAME"));
        tvPrice.setText("Rp " + getIntent().getStringExtra("PRICE"));
        tvType.setText(getIntent().getStringExtra("TYPE"));
        tvUnit.setText("0");
        double unit = Double.parseDouble(getIntent().getStringExtra("UNIT")) * 10000;
        int unit2 = (int) unit;
        seekBar.setMax(unit2);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvUnit.setText(String.valueOf(progress / 10000.00));
                double nominal = (progress / 10000.00) * Double.parseDouble(getIntent().getStringExtra("PRICE"));
                NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                tvNominal.setText(currencyFormatter.format(nominal));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private class PostSell extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(SellAssetsActivity.this);
            progressDialog.setMessage("Please wait..");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... id) {
            HttpHandler httpHandler = new HttpHandler();
            String jsonString = httpHandler.PerformPostCall(BuildConfig.BASE_URL + "api/assets/" + getIntent().getStringExtra("ID") + "/sell"+
                            "?id=" + getIntent().getStringExtra("ID") +
                            "&quantity=" + tvUnit.getText().toString(),
                    SellAssetsActivity.this);
            try {
                if (jsonString.equals("401")) {
                    runOnUiThread(() -> {
                        Toast.makeText(SellAssetsActivity.this, "Login Expired", Toast.LENGTH_SHORT);
                        startActivity(new Intent(SellAssetsActivity.this, MainActivity.class));
                        finish();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return jsonString;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result.equals("200")) {
                Toast.makeText(SellAssetsActivity.this, "Sell succesfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SellAssetsActivity.this, "Sell Failed", Toast.LENGTH_SHORT).show();
            }
            progressDialog.dismiss();
        }
    }

}