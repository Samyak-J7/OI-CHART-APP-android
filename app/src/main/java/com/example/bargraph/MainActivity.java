package com.example.bargraph;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
public class MainActivity extends AppCompatActivity implements OnChartValueSelectedListener{
    String selected_exp; int selected_index =0; int ind;
    List<String> striqList = new ArrayList<>();
    List<String> cOiList = new ArrayList<>();
    List<String> pList = new ArrayList<>();
    List<String> c_rs = new ArrayList<>();
    List<String> p_rs = new ArrayList<>();
    List<String> week= new ArrayList<>();
    List<BarEntry> entries = new ArrayList<>();
    List<BarEntry> entries2 = new ArrayList<>();
    String jsonData =""; String api_data =""; String scrip=""; ImageButton b1; String ts=""; String cmp=""; int cutoff=0; TextView tv;
    boolean ch=false;
        @SuppressLint("MissingInflatedId")
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            ProgressBar loadingIndicator = findViewById(R.id.loadingIndicator);
            loadingIndicator.setVisibility(View.VISIBLE);

            HorizontalBarChart barChart = findViewById(R.id.barChart);
            barChart.clear();
            barChart.setData(null);
            barChart.setNoDataText("");
            barChart.setNoDataTextColor(Color.BLUE);

            tv = findViewById(R.id.textView);
            b1=findViewById(R.id.button);
            b1.setBackgroundColor(getResources().getColor(android.R.color.white));

            Spinner spinnerOptions2 = findViewById(R.id.spinnerOptions2);
            Drawable roundedBackground = getResources().getDrawable(R.drawable.rounded_spinner_bg);
            spinnerOptions2.setBackground(roundedBackground);

            List<String> scrips= Arrays.asList("NIFTY","BANKNIFTY","FINNIFTY");
            ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,scrips);
            adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerOptions2.setAdapter(adapter2);
            spinnerOptions2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    scrip=scrips.get(position);

                    if (make_graph()){
                        selected_index =position;}
                    else{

                        spinnerOptions2.setSelection(selected_index);}
                    }
                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });
           b1.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   Toast.makeText(getApplicationContext(),"Refreshing Data",Toast.LENGTH_SHORT).show();
                   ch=true;
                   if (make_graph()){
                   Toast.makeText(getApplicationContext(),"Data updated",Toast.LENGTH_SHORT).show();}
               }
           });
        }
    @Override
    public void onValueSelected(Entry e, Highlight h) {
        if (e instanceof BarEntry) {
            BarEntry barEntry = (BarEntry) e;
            int value = (int) barEntry.getY();
            int index = pList.indexOf(String.valueOf(value));
            if (index==-1){
                index = cOiList.indexOf(String.valueOf(value));
            }
            String value2= striqList.get(index);
            int lot=0;
            switch (scrip){
                case "NIFTY"    : lot=50; break;
                case "BANKNIFTY": lot=15; break;
                case "FINNIFTY" : lot=40; break;
            }
            PopupFragment popupFragment = PopupFragment.newInstance(week.get(index),value2, String.valueOf(lot));
            popupFragment.show(getSupportFragmentManager(), "popup");
        }
    }
    @Override
    public void onNothingSelected() {
    }
    public Boolean make_graph(){
        HorizontalBarChart barChart = findViewById(R.id.barChart);
        ProgressBar loadingIndicator = findViewById(R.id.loadingIndicator);
        barChart.setOnChartValueSelectedListener((OnChartValueSelectedListener) this);
        api_data ="";jsonData="";cutoff=0;
        Handler handler = new Handler(Looper.getMainLooper());
        new Thread(new Runnable() {
            @Override
            public void run() {
                fetchDataFromApi();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }
        }).start();
        long start_time= System.currentTimeMillis();
        while (jsonData == null || jsonData.isEmpty() && System.currentTimeMillis()-start_time<4500) {
            try {
                jsonData = api_data;

            } catch (Exception ignored) {
            }
        }
        if (jsonData == null || jsonData.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Check your connection and refresh", Toast.LENGTH_LONG).show();
            return false;
        }

        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsonData);
        } catch (JSONException ignored) {
        }
        List<String> expiry = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = null;
            try {
                jsonObject = jsonArray.getJSONObject(i);
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(),"An error occurred. Please Refresh",Toast.LENGTH_SHORT).show();
            }
            String exp="";
            try {
                exp = jsonObject.getString("expiry");
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(),"An error occurred. Please Refresh",Toast.LENGTH_SHORT).show();
            }
            if (!expiry.contains(exp)) {
                expiry.add(exp);}
        }
        Collections.sort(expiry);

        Spinner spinnerOptions = findViewById(R.id.spinnerOptions);
        Drawable roundedBackground = getResources().getDrawable(R.drawable.rounded_spinner_bg);
        spinnerOptions.setBackground(roundedBackground);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        ArrayAdapter<String> adapter = new ArrayAdapter<>(spinnerOptions.getContext(), android.R.layout.simple_spinner_item,expiry);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOptions.setAdapter(adapter);

        if (ch){
        spinnerOptions.setSelection(ind);
        ch=false;}

        spinnerOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selected_exp = expiry.get(position);
                ind=position;
                JSONArray jsonArray = null;
                try {
                    jsonArray = new JSONArray(jsonData);
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),"An error occurred. Please Refresh",Toast.LENGTH_SHORT).show();
                }
                c_rs.clear();p_rs.clear();cOiList.clear();striqList.clear();pList.clear();week.clear();ts="";cmp="";
                try{
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = jsonArray.getJSONObject(i);
                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(),"An error occurred. Please Refresh",Toast.LENGTH_SHORT).show();
                    }
                    try {
                        if (selected_exp.equals(jsonObject.getString("expiry"))) {
                            striqList.add(jsonObject.getString("striq"));
                            cOiList.add(jsonObject.getString("call_oi"));
                            pList.add(jsonObject.getString("put_oi"));
                            week.add(jsonObject.getString("week"));
                            c_rs.add(jsonObject.getString("call_rs"));
                            p_rs.add(jsonObject.getString("put_rs"));
                        }
                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(),"An error occurred. Please Refresh",Toast.LENGTH_SHORT).show();
                    }
                }}
                catch (Exception e){ Toast.makeText(getApplicationContext(),"An error occurred. Please Refresh",Toast.LENGTH_SHORT).show();}
                try{
                    JSONObject  jsonObject = jsonArray.getJSONObject(0);
                    ts=jsonObject.getString("ts");
                    cmp=jsonObject.getString("underlying");
                    tv.setText("TimeStamp: "+ts+"        CMP:"+cmp);
                }
                catch (Exception ignored) {
                }
                entries.clear();
                entries2.clear();
                XAxis xAxis = barChart.getXAxis();
                xAxis.setValueFormatter(new IndexAxisValueFormatter(striqList));
                xAxis.setLabelCount(striqList.size()); // Set the label count to the number of values
                xAxis.setGranularity(1f);
                xAxis.setGranularityEnabled(true);
                float barWidth = 0.4f;
                for (int i = 0; i < cOiList.size(); i++) {
                    entries.add(new BarEntry(i+ 0.3f, Float.parseFloat(cOiList.get(i))));
                    entries2.add(new BarEntry(i , Float.parseFloat(pList.get(i))));
                }
                BarDataSet dataSet = new BarDataSet(entries, "Call OI");
                dataSet.setColor(Color.rgb(155, 0, 0)); // Bar color
                dataSet.setDrawValues(false);
                BarDataSet dataSet2 = new BarDataSet(entries2, "Put OI");
                dataSet2.setColor(Color.rgb(0, 155, 0)); // Bar color
                dataSet2.setDrawValues(false);
                dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                YAxis rightYAxis = barChart.getAxisRight();
                rightYAxis.setEnabled(false);
                BarData barData = new BarData(dataSet,dataSet2);
                barData.setBarWidth(barWidth);
                barChart.setData(barData);
                barChart.getDescription().setEnabled(false);
                barChart.setFitBars(true);
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setDrawGridLines(false);
                YAxis leftYAxis = barChart.getAxisLeft();
                leftYAxis.setDrawGridLines(false);
                rightYAxis.setDrawGridLines(false);
                loadingIndicator.setVisibility(View.GONE);
                barChart.invalidate();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
 return true;
    }
    private void fetchDataFromApi() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String apiUrl = "api"+scrip;
                    URL url = new URL(apiUrl);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    try {
                        InputStream in = urlConnection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            stringBuilder.append(line).append("\n");
                        }
                        api_data = stringBuilder.toString();
                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });
                }
            }
        });
    }
}