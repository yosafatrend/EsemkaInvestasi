package com.yorren.esemkainvestasi.fragment;

import android.app.AsyncNotedAppOp;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.yorren.esemkainvestasi.BuildConfig;
import com.yorren.esemkainvestasi.HttpHandler;
import com.yorren.esemkainvestasi.R;
import com.yorren.esemkainvestasi.activity.DetailProductActivity;
import com.yorren.esemkainvestasi.activity.LoginActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ProductFragment extends Fragment {
    ListView listView;
    ArrayList<HashMap<String, String>> productJsonList;
    ProgressDialog progressDialog;

    public ProductFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_product, container, false);

        listView = v.findViewById(R.id.listViewProduct);
        productJsonList = new ArrayList<>();

        new GetProduct().execute();

        return v;
    }

    private class GetProduct extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage("Please wait..");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler httpHandler = new HttpHandler();
            String jsonProduct = httpHandler.MakeServiceCall(BuildConfig.BASE_URL + "api/products", getContext());

            if (jsonProduct != null) {
                try {
                    if (jsonProduct.equals("401")) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Login expired", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getContext(), LoginActivity.class));
                            getActivity().finish();
                        });
                    }
                    JSONArray arrayProduct = new JSONArray(jsonProduct);

                    for (int i = 0; i < arrayProduct.length(); i++) {
                        JSONObject product = arrayProduct.getJSONObject(i);
                        String id = product.getString("id");
                        String name = product.getString("name");
                        String price = product.getString("price");
                        String type = product.getString("type");

                        HashMap<String, String> productMap = new HashMap<>();
                        productMap.put("id", id);
                        productMap.put("name", name);
                        productMap.put("price", "Rp " + price);
                        productMap.put("type", type);

                        productJsonList.add(productMap);
                    }

                } catch (Exception e) {
                    Log.e("PRODUCT", "Json parsing error: " + e.getMessage());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(),
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

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            ListAdapter adapter = new SimpleAdapter(getContext(), productJsonList,
                    R.layout.item_product, new String[]{"name", "price", "type", "id"}, new int[]{R.id.tvName, R.id.tvPrice, R.id.tvType});

            listView.setAdapter(adapter);
            listView.setOnItemClickListener((parent, view, position, id) -> {
                HashMap<String, String> item = (HashMap<String, String>) parent.getItemAtPosition(position);
                String idp = item.get("id");
                String name = item.get("name");
                String price = item.get("price");
                String type = item.get("type");

                Intent intent = new Intent(getContext(), DetailProductActivity.class);
                intent.putExtra("ID", idp);
                intent.putExtra("NAME", name);
                intent.putExtra("PRICE", price);
                intent.putExtra("TYPE", type);

                startActivity(intent);
            });
        }
    }
}