package com.alice.android.swapify;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class ShopifyProductFetcher extends AsyncTask<String, String, String> {
    private TaskDelegate delegate;
    private static final String shopifyUrl = "https://shopicruit.myshopify.com/admin/products.json?page=1&access_token=c32313df0d0ef512ca64d5b336a0d7c6";

    public ShopifyProductFetcher(TaskDelegate delegate, ProgressBar loadingBar) {
        super();
        this.delegate = delegate;
    }

    protected void onPreExecute() {
        super.onPreExecute();
        //context.startActivity(new Intent(context, LoadingActivity.class));
        Log.d("update: ","About to start fetching Shopify stuff");

//        pd = new ProgressDialog(MainActivity.this);
//        pd.setMessage("Please wait");
//        pd.setCancelable(false);
//        pd.show();
    }

    private String fetchJsonData() {
        HttpURLConnection connection = null;
        BufferedReader in = null;

        try {
            URL url = new URL(shopifyUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            InputStream stream = connection.getInputStream();
            in = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();
            String inputLine = "";

            while ((inputLine = in.readLine()) != null) {
                buffer.append(inputLine + "\n");
                Log.d("Response: ", "> " + inputLine);
            }

            return buffer.toString();
        } catch (MalformedURLException e) {
            Log.d("Error:", "MalformedURLException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("Error:", "IOException");
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                Log.d("Error:", "IOException");
                e.printStackTrace();
            }
        }

        return null;
    }

    protected String doInBackground(String... params) {
        return fetchJsonData();
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.d("Finished:", result);

        ArrayList<String> imageURLs = new ArrayList<>();
        try {
            JSONObject shopifyJsonResponse = new JSONObject(result);
            JSONArray shopifyProducts = shopifyJsonResponse.getJSONArray("products");
            Log.d("Products:", shopifyProducts.toString());
            for (int i = 0; i < shopifyProducts.length(); i++) {
                imageURLs.add(shopifyProducts.getJSONObject(i).getJSONObject("image").getString("src"));
            }
            delegate.taskCompletionResult(imageURLs.toArray(new String[imageURLs.size()]));
            return;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("Error:", "JSONException; error parsing JSON response");
        }
        delegate.taskCompletionResult(null);
    }
}
