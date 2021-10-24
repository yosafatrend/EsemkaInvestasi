package com.yorren.esemkainvestasi.fragment;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.yorren.esemkainvestasi.BuildConfig;
import com.yorren.esemkainvestasi.HttpHandler;
import com.yorren.esemkainvestasi.R;
import com.yorren.esemkainvestasi.activity.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class HomeFragment extends Fragment {
    TextView tvGreeting, tvBirthDate, tvNominal,
            tvKeuntungan, tvPresentase;
    private String TAG = MainActivity.class.getSimpleName();
    private ProgressDialog progressDialog;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        tvGreeting = v.findViewById(R.id.tvHello);
        tvBirthDate = v.findViewById(R.id.tvBirth);
        tvNominal = v.findViewById(R.id.tvNominal);
        tvKeuntungan = v.findViewById(R.id.tvKeuntungan);
        tvPresentase = v.findViewById(R.id.tvPresentase);

        new GetGreetings().execute();

        return v;
    }

    private class GetGreetings extends AsyncTask<Void, Void, HashMap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage("Please wait..");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected HashMap doInBackground(Void... voids) {
            HttpHandler httpHandler = new HttpHandler();
            String jsonString = httpHandler.MakeServiceCall(BuildConfig.BASE_URL + "api/Auth/Me", getContext());
            String jsonAsset = httpHandler.MakeServiceCall(BuildConfig.BASE_URL + "api/assets", getContext());
            HashMap<String, String> user = new HashMap<>();
            Log.d("HOME", jsonString);
            if (jsonString != null) {
                try {
                    double totalUnit = 0;
                    double totalBuyPrice = 0;
                    double totalNowPrice = 0;

                    JSONArray jsonArray = new JSONArray(jsonAsset);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        totalUnit += jsonObject.getDouble("quantity");
                        totalBuyPrice += jsonObject.getDouble("price");
                        JSONObject jsonProduct = jsonObject.getJSONObject("product");
                        totalNowPrice += jsonProduct.getDouble("price");
                    }

                    double nominal = totalNowPrice * totalUnit;
                    double keuntungan = 0;
                    double presentase = 0;
                    boolean isProfit = false;

                    if (totalBuyPrice > totalNowPrice) {
                        keuntungan = totalBuyPrice - totalNowPrice;
                        presentase = keuntungan / totalBuyPrice * 100;
                        isProfit = false;
                    } else {
                        keuntungan = totalNowPrice - totalBuyPrice;
                        presentase = keuntungan / totalNowPrice * 100;
                        isProfit = true;
                    }

                    JSONObject response = new JSONObject(jsonString);
                    String email = response.getString("email");
                    String name = response.getString("name");
                    String gender = response.getString("gender");
                    String birthdate = response.getString("birthdate");

                    //date formating
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
                    Date date = format.parse(birthdate);
                    DateFormat format1 = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
                    String finalBirthdate = format1.format(date);

                    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                    user.put("email", email);
                    user.put("name", name);
                    user.put("gender", gender);
                    user.put("birthdate", finalBirthdate);
                    if (isProfit) {
                        user.put("nominal", currencyFormat.format(nominal).replace("Rp", "Rp "));
                        user.put("keuntungan", currencyFormat.format(keuntungan).replace("Rp", "Rp "));
                        user.put("presentase", String.valueOf(presentase).substring(0, 4) + "%");
                    } else {
                        user.put("nominal", currencyFormat.format(nominal).replace("Rp", "Rp "));
                        user.put("keuntungan", currencyFormat.format(keuntungan).replace("Rp", "Rp -"));
                        user.put("presentase", "-" + String.valueOf(presentase).substring(0, 4) + "%");
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(),
                            "Json parsing error: " + e.getMessage(),
                            Toast.LENGTH_LONG)
                            .show());
                } catch (Exception e) {

                }
            }
            return user;
        }

        @Override
        protected void onPostExecute(HashMap hashMap) {
            super.onPostExecute(hashMap);
            progressDialog.dismiss();
            if (hashMap.get("gender").equals("Female")) {
                tvGreeting.setText("Hello, Mrs " + hashMap.get("name"));
            } else if (hashMap.get("gender").equals("Male")) {
                tvGreeting.setText("Hello, Mr " + hashMap.get("name"));
            }
            tvBirthDate.setText(hashMap.get("birthdate").toString());
            tvNominal.setText(hashMap.get("nominal").toString());
            tvKeuntungan.setText(hashMap.get("keuntungan").toString());
            tvPresentase.setText(hashMap.get("presentase").toString());

            if (tvKeuntungan.getText().toString().contains("-")){
                tvKeuntungan.setTextColor(Color.RED);
                tvPresentase.setTextColor(Color.RED);
            }
            else{
                tvKeuntungan.setTextColor(Color.GREEN);
                tvPresentase.setTextColor(Color.GREEN);
            }
        }
    }
}