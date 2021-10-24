package com.yorren.esemkainvestasi.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.yorren.esemkainvestasi.BuildConfig;
import com.yorren.esemkainvestasi.HttpHandler;
import com.yorren.esemkainvestasi.R;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DetailProductActivity extends AppCompatActivity {
    TextView tvName, tvPrice, tvType;
    EditText edtNominal;
    Button btnConfirm;
    ProgressDialog progressDialog;
  //  JSONObject jsonBuy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_product);

        tvName = findViewById(R.id.tvName);
        tvPrice = findViewById(R.id.tvPrice);
        tvType = findViewById(R.id.tvType);
        edtNominal = findViewById(R.id.edtNominal);
        btnConfirm = findViewById(R.id.btnConfirm);

        tvName.setText(getIntent().getStringExtra("NAME"));
        tvPrice.setText(getIntent().getStringExtra("PRICE"));
        tvType.setText(getIntent().getStringExtra("TYPE"));

        btnConfirm.setOnClickListener(v -> {
            BuyProduct();
        });
    }

    private void BuyProduct(){
        String nominal = edtNominal.getText().toString();

        if (nominal.isEmpty()){
            edtNominal.setError("Please insert nominal");
        }
        else if (Integer.parseInt(nominal) < 20000){
            edtNominal.setError("Minimum nominal is 20000");
        }
        else{
            new PostProduct().execute();
        }
//        try {
//            jsonBuy = new JSONObject();
//            jsonBuy.put("id", getIntent().getStringExtra("ID"));
//            jsonBuy.put("nominal", edtNominal.getText().toString());
//            jsonBuy.put("date", finalDate);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private class PostProduct extends AsyncTask<Void, Void, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(DetailProductActivity.this);
            progressDialog.setMessage("Please Wait..");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            HttpHandler httpHandler = new HttpHandler();
            Date currentDate = Calendar.getInstance().getTime();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String finalDate = dateFormat.format(currentDate);
            String jsonResponse = httpHandler.PerformPostCall(BuildConfig.BASE_URL + "api/products/"+
                            getIntent().getStringExtra("ID")+"/buy?id="+
                            getIntent().getStringExtra("ID")+"&nominal="+
                            edtNominal.getText().toString()+"&date="+
                            finalDate,
                    DetailProductActivity.this);

            return jsonResponse;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();

            if (s.equals("201")){
                Toast.makeText(DetailProductActivity.this, "Purchase successfull", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(DetailProductActivity.this, "Purchase failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}