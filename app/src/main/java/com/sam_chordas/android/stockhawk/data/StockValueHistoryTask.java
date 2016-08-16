package com.sam_chordas.android.stockhawk.data;

import android.content.Context;
import android.os.AsyncTask;

import com.github.mikephil.charting.data.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by ilyua on 27.07.2016.
 */
public class StockValueHistoryTask extends AsyncTask<String, Void, ArrayList<Entry>> {
    Context mContext;

    public StockValueHistoryTask(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    protected ArrayList<Entry> doInBackground(String... params) {
        String stockSymbol = params[0];
        String startDate = params[1];
        String endDate = params[2];

        StringBuilder urlStringBuilder = new StringBuilder();
        HttpURLConnection httpURLConnection = null;

        try {
            urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
            urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.historicaldata " +
                    "where symbol = \"" + stockSymbol + "\" and startDate = \"" + startDate + "\"" +
                    " and endDate = \"" + endDate + "\"", "UTF-8"));
            urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=");

            URL url = new URL(urlStringBuilder.toString());
            httpURLConnection = (HttpURLConnection) url.openConnection();
           httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();
            int connectionStatus = httpURLConnection.getResponseCode();

            String jsonString = "";
            if (connectionStatus == HttpURLConnection.HTTP_OK || connectionStatus == HttpURLConnection.HTTP_CREATED) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;

                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    bufferedReader.close();
                jsonString = sb.toString();
            }
            ArrayList<Entry> values = getDataFromJSON(jsonString);
            return values;

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(httpURLConnection != null){
                httpURLConnection.disconnect();
            }
        }

        return null;
    }

    private ArrayList<Entry> getDataFromJSON(String jsonString){
        ArrayList<Entry> list = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONObject jsonQuery = jsonObject.getJSONObject("query");
            JSONObject jsonResults = jsonQuery.getJSONObject("results");
            JSONArray jsonArray = jsonResults.getJSONArray("quote");

            JSONObject quoteJson;
            for(int i = 0; i < jsonArray.length(); i++){
                quoteJson = jsonArray.getJSONObject(i);
                String value = quoteJson.getString("Close");
                //String date = quoteJson.getString("Date").replace("-", "").substring(2, 8);
                list.add(0, new Entry(jsonArray.length() - i - 1, Float.parseFloat(value)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;
    }
}
