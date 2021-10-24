package com.yorren.esemkainvestasi.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.icu.number.NumberFormatter;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.yorren.esemkainvestasi.BuildConfig;
import com.yorren.esemkainvestasi.HttpHandler;
import com.yorren.esemkainvestasi.R;
import com.yorren.esemkainvestasi.activity.AssetByTypeActivity;
import com.yorren.esemkainvestasi.activity.LoginActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;


public class AssetFragment extends Fragment {
    ListView listView;
    ArrayList<HashMap<String, String>> typeAsset;
    HashMap<String, String> type;
    ProgressDialog progressDialog;

    public AssetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_asset, container, false);

        listView = v.findViewById(R.id.list_asset);
        typeAsset = new ArrayList<>();

        new GetType().execute();

        return v;
    }

    private class GetType extends AsyncTask<Void, Void, HashMap<String, String>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage("Please wait..");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected HashMap<String, String> doInBackground(Void... voids) {
            HttpHandler httpHandler = new HttpHandler();
            String jsonPasarUang = httpHandler.MakeServiceCall(BuildConfig.BASE_URL + "api/assets/bytype/pasaruang", getContext());
            String jsonObligasi = httpHandler.MakeServiceCall(BuildConfig.BASE_URL + "api/assets/bytype/obligasi", getContext());
            String jsonSaham = httpHandler.MakeServiceCall(BuildConfig.BASE_URL + "api/assets/bytype/saham", getContext());
            try {
                if (jsonPasarUang.equals("401")) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Login expired", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getContext(), LoginActivity.class));
                        getActivity().finish();
                    });
                }
                typeAsset.add(Formatter(jsonPasarUang, "Pasar Uang"));
                typeAsset.add(Formatter(jsonObligasi, "Obligasi"));
                typeAsset.add(Formatter(jsonSaham, "Saham"));

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(HashMap result) {
            super.onPostExecute(result);
            progressDialog.dismiss();

            ListAdapter adapter = new SimpleAdapter(getContext(), typeAsset,
                    R.layout.item_asset, new String[]{"type", "nominal", "unit", "keuntungan", "presentase"},
                    new int[]{R.id.tvType, R.id.tvNominal, R.id.tvUnit, R.id.tvKeuntungan, R.id.tvPresentase}){
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View v = super.getView(position, convertView, parent);
                    TextView tvKeuntungan = v.findViewById(R.id.tvKeuntungan);
                    TextView tvPresentase = v.findViewById(R.id.tvPresentase);

                    if (tvPresentase.getText().toString().contains("-")){
                        tvKeuntungan.setTextColor(Color.RED);
                        tvPresentase.setTextColor(Color.RED);
                    }
                    else{
                        tvKeuntungan.setTextColor(Color.GREEN);
                        tvPresentase.setTextColor(Color.GREEN);
                    }

                    return v;
                }
            };

            listView.setAdapter(adapter);
            listView.setOnItemClickListener((parent, view, position, id) -> {
                HashMap<String, String> item = (HashMap<String, String>) parent.getItemAtPosition(position);
                String type = item.get("type");

                Intent intent = new Intent(getContext(), AssetByTypeActivity.class);
                intent.putExtra("TYPE", type);
                startActivity(intent);
            });
        }
    }



    private HashMap<String, String> Formatter(String jsonPasarUang, String typeString) throws JSONException {
        double totalUnitPasarUang = 0;
        double totalBPricePasarUang = 0;
        double totalAPricePasarUang = 0;
        JSONArray jsonArrayP = new JSONArray(jsonPasarUang);
        for (int i = 0; i < jsonArrayP.length(); i++) {
            JSONObject jsonObject = jsonArrayP.getJSONObject(i);
            double buyPrice = jsonObject.getDouble("price");
            double quantity = jsonObject.getDouble("quantity");
            JSONObject product = jsonObject.getJSONObject("product");
            double nowPrice = product.getDouble("price");

            totalBPricePasarUang += buyPrice;
            totalAPricePasarUang += nowPrice;
            totalUnitPasarUang += quantity;
        }
        double nominal = totalAPricePasarUang * totalUnitPasarUang;
        boolean isProfit = false;
        double keuntungan = 0;
        double presentase = 0;
        if (totalAPricePasarUang > totalBPricePasarUang) {
            keuntungan = totalAPricePasarUang - totalBPricePasarUang;
            presentase = (keuntungan / totalAPricePasarUang) * 100;
            isProfit = true;
        }
        else if (totalBPricePasarUang > totalAPricePasarUang) {
            keuntungan = totalBPricePasarUang - totalAPricePasarUang;
            presentase = (keuntungan / totalBPricePasarUang) * 100;
            isProfit = false;
        }

        DecimalFormat currencyFormatter = (DecimalFormat) NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        type = new HashMap();
        type.put("type", typeString);
        type.put("nominal", currencyFormatter.format(nominal));
        type.put("unit", String.valueOf(totalUnitPasarUang));
        if (isProfit){

            type.put("keuntungan", currencyFormatter.format(keuntungan).replace("Rp", "Rp "));
            type.put("presentase", String.valueOf(presentase).substring(0, 3) + "%");
        }else{
            currencyFormatter.setNegativePrefix("");
            type.put("keuntungan", currencyFormatter.format(keuntungan).replace("Rp", "Rp -"));
            type.put("presentase", "-" + String.valueOf(presentase).substring(0, 3) + "%");
        }
        return type;
    }
}