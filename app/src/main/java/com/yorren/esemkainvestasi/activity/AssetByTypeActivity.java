package com.yorren.esemkainvestasi.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.yorren.esemkainvestasi.BuildConfig;
import com.yorren.esemkainvestasi.HttpHandler;
import com.yorren.esemkainvestasi.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class AssetByTypeActivity extends AppCompatActivity {
    ListView listView;
    ArrayList<HashMap<String, String>> assets;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_by_type);

        listView = findViewById(R.id.list_asset_by_type);
        assets = new ArrayList<>();

        new GetAssetByType().execute();
    }

    private class GetAssetByType extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(AssetByTypeActivity.this);
            progressDialog.setMessage("Please wait..");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(String... type) {
            HttpHandler httpHandler = new HttpHandler();
            String jsonString = httpHandler.MakeServiceCall(
                    BuildConfig.BASE_URL + "api/assets/bytype/" + getIntent().getStringExtra("TYPE")
                            .replaceAll("\\s", ""),
                    AssetByTypeActivity.this);

            if (jsonString != null) {
                try {
                    if (jsonString.equals("401")) {
                        Log.d("ASSETBYTYPE", "Login expired");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AssetByTypeActivity.this, "Login expired", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(AssetByTypeActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }

                    JSONArray jsonArray = new JSONArray(jsonString);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String id = jsonObject.getString("id");
                        String bPrice = jsonObject.getString("price");
                        String quantity = jsonObject.getString("quantity");

                        JSONObject productObject = jsonObject.getJSONObject("product");
                        String name = productObject.getString("name");
                        String aPrice = productObject.getString("price");

                        double nominal = Double.parseDouble(aPrice) * Double.parseDouble(quantity);
                        double nominalBefore = Double.parseDouble(bPrice) * Double.parseDouble(quantity);
                        double distance = 0;
                        double percentage = 0;
                        boolean isProfit = false;
                        if (nominal > nominalBefore) {
                            distance = nominal - nominalBefore;
                            percentage = (distance / nominal) * 100;
                            isProfit = true;
                        } else if (nominalBefore > nominal) {
                            distance = nominalBefore - nominal;
                            percentage = (distance / nominalBefore) * 100;
                            isProfit = false;
                        }
                        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                        HashMap<String, String> asset = new HashMap<>();
                        asset.put("id", id);
                        asset.put("name", name);
                        asset.put("nominal", currencyFormat.format(nominal).replace("Rp", "Rp "));
                        asset.put("quantity", quantity);
                        asset.put("aprice", aPrice);
                        asset.put("type", getIntent().getStringExtra("TYPE"));
                        if (isProfit) {
                            asset.put("keuntungan", currencyFormat.format(distance).replace("Rp", "Rp "));
                            asset.put("presentase", String.valueOf(percentage).substring(0, 5) + "%");
                        } else {
                            asset.put("keuntungan", currencyFormat.format(distance).replace("Rp", "Rp -"));
                            asset.put("presentase", "-" + String.valueOf(percentage).substring(0, 5) + "%");
                        }

                        assets.add(asset);
                    }
                } catch (final JSONException e) {
                    Log.e("ASSETBYTYPE", "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();

            ListAdapter adapter = new SimpleAdapter(AssetByTypeActivity.this, assets,
                    R.layout.item_asset_by_type, new String[]{"name", "nominal", "quantity", "keuntungan", "presentase", "id", "type"},
                    new int[]{R.id.tvName, R.id.tvNominal, R.id.tvUnit, R.id.tvKeuntungan, R.id.tvPresentase, R.id.tvId}) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View v = super.getView(position, convertView, parent);

                    TextView tvKeuntungan = v.findViewById(R.id.tvKeuntungan);
                    TextView tvPresentase = v.findViewById(R.id.tvPresentase);

                    if (tvKeuntungan.getText().toString().contains("-")) {
                        tvKeuntungan.setTextColor(Color.RED);
                        tvPresentase.setTextColor(Color.RED);
                    }
                    else {
                        tvKeuntungan.setTextColor(Color.GREEN);
                        tvPresentase.setTextColor(Color.GREEN);
                    }
                    return v;
                }
            };

            listView.setAdapter(adapter);
            listView.setOnItemClickListener((parent, view, position, id) -> {
                HashMap<String, String> item = (HashMap<String, String>) parent.getItemAtPosition(position);
                String ida = item.get("id");
                String name = item.get("name");
                String type = item.get("type");
                String unit = item.get("quantity");
                String price = item.get("aprice");

                Intent intent = new Intent(AssetByTypeActivity.this, SellAssetsActivity.class);
                intent.putExtra("ID", ida);
                intent.putExtra("NAME", name);
                intent.putExtra("UNIT", unit);
                intent.putExtra("TYPE", type);
                intent.putExtra("PRICE", price);

                startActivity(intent);
            });
        }
    }


}