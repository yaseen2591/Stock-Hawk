package com.sam_chordas.android.stockhawk.ui;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.utils.CustomSpinnerAdapter;
import com.sam_chordas.android.stockhawk.utils.Utility;
import com.sam_chordas.android.stockhawk.model.DateHigh;
import com.sam_chordas.android.stockhawk.model.DateHighMain;
import com.sam_chordas.android.stockhawk.model.Quote;
import com.sam_chordas.android.stockhawk.model.QuoteInfo;
import com.sam_chordas.android.stockhawk.rest.QuoteService;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.github.mikephil.charting.data.Entry;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A placeholder fragment containing a simple view.
 */
public class StockDetailActivityFragment extends Fragment implements Callback<QuoteInfo> {
    public static String symbol;
    DateHighMain dhm = new DateHighMain();
    public static final String TAG = StockDetailActivityFragment.class.getName();
    public ArrayList<Entry> valueSet;
    LineChart mChart;
    List<String> upperLowerLimitList = new ArrayList<String>();
    ProgressBar progressBar;


    public StockDetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_stock_detail, container, false);
        setHasOptionsMenu(true);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        symbol = getActivity().getIntent().getExtras().getString("symbol");

        mChart = (LineChart) view.findViewById(R.id.chart);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(symbol);

        return view;
    }


    @Override
    public void onResponse(Call<QuoteInfo> call, Response<QuoteInfo> response) {
        progressBar.setVisibility(View.GONE);
        QuoteInfo quoteInfo = response.body();
        //Storing the response if any
        valueSet = new ArrayList<>();
        ArrayList<Quote> quoteArray = quoteInfo.query.results.quote;
        int i;
        dhm.getDatehigh().clear();
        for (i = 0; i < quoteArray.size(); i++
                ) {
            DateHigh dh = new DateHigh();
            dh.setQuoteDate(quoteArray.get(i).quote_date);
            dh.setQuoteHighValue(quoteArray.get(i).high);
            dhm.getDatehigh().add(dh);
        }

        LineData data = new LineData(getXAxisValues(), getDataSet());
        setUpChart(mChart, data);

    }


    @Override
    public void onFailure(Call<QuoteInfo> call, Throwable t) {
        Toast.makeText(getActivity(), t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
    }


    private void callRetrofitFetch(String symbol, String startDate, String endDate) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://query.yahooapis.com/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        QuoteService stackOverflowAPI = retrofit.create(QuoteService.class);
//        String q = "select%20*%20from%20yahoo.finance.historicaldata%20where%20symbol%20%3D%20%22YHOO%22%20and%20startDate%20%3D%20%222016-03-21%22%20and%20endDate%20%3D%20%222016-03-25%22&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";
//        Call<QuoteInfo> call = restClient.getService().getObjectWithNestedArraysAndObject();
        String q = "select * from yahoo.finance.historicaldata where symbol = \"" + symbol + "\" and startDate = \"" + endDate + "\" and endDate = \"" + startDate + "\"";
        String diagnostics = "true";
        String env = "store://datatables.org/alltableswithkeys";
        String format = "json";
        Call<QuoteInfo> call = stackOverflowAPI.getObjectWithNestedArraysAndObject(q, diagnostics, env, format);
        //asynchronous call
        call.enqueue(this);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuItem item = menu.findItem(R.id.spinner);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);

        ArrayList<String> list = new ArrayList<String>();
        list.add(getString(R.string.one_week));
        list.add(getString(R.string.one_month));
        list.add(getString(R.string.three_month));
        list.add(getString(R.string.six_month));
        list.add(getString(R.string.one_year));
        CustomSpinnerAdapter spinAdapter = new CustomSpinnerAdapter(
                getActivity().getApplicationContext(), list);
        spinner.setAdapter(spinAdapter); // set the adapter to provide layout of rows and content
        String startDate = Utility.getFormattedDate(System.currentTimeMillis());
        Date date = new Date();

        callRetrofitFetch(symbol, startDate, Utility.get1WeekBackDate(date));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapter, View v,
                                       int position, long id) {
                // On selecting a spinner item
                String item = adapter.getItemAtPosition(position).toString();
                String startDate = Utility.getFormattedDate(System.currentTimeMillis());
                Date date = new Date();
                progressBar.setVisibility(View.VISIBLE);

                switch (item) {
                    case "1 Week":
                        callRetrofitFetch(symbol, startDate, Utility.get1WeekBackDate(date));
                        break;
                    case "1 Month":
                        callRetrofitFetch(symbol, startDate, Utility.get1MonthBackDate(date));
                        break;
                    case "3 Months":
                        callRetrofitFetch(symbol, startDate, Utility.get3MonthsBackDate(date));
                        break;
                    case "6 Months":
                        callRetrofitFetch(symbol, startDate, Utility.get6MonthsBackDate(date));
                        break;
                    case "1 Year":
                        callRetrofitFetch(symbol, startDate, Utility.get1YearBackDate(date));
                        break;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

    }

    private void setUpChart(LineChart mChart, LineData data) {
        mChart.setData(data);
        mChart.setDescription(getString(R.string.quote_history_text));
        mChart.setNoDataTextDescription(getString(R.string.quote_history_error));
        mChart.setDrawGridBackground(false);

        ArrayList<ILineDataSet> sets = (ArrayList<ILineDataSet>) mChart.getData()
                .getDataSets();

        for (ILineDataSet set : sets) {
            set.setDrawFilled(true);
        }
        // enable touch gestures
        mChart.setTouchEnabled(true);
        data.setDrawValues(true);
        // enable scaling and dragging
        mChart.setDragEnabled(false);
        mChart.setScaleEnabled(false);
        mChart.setPinchZoom(false);


        mChart.setBackgroundColor(Color.BLACK);
        Legend l = mChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextSize(11f);
        l.setTextColor(Color.WHITE);
        l.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setEnabled(false);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaxValue(Float.parseFloat(Collections.max(upperLowerLimitList)) + 50f);
        leftAxis.setDrawGridLines(true);

        //  adding limit lines
        LimitLine ll1 = new LimitLine(Float.parseFloat(Collections.max(upperLowerLimitList)), Collections.max(upperLowerLimitList));
        ll1.setLineWidth(4f);
        ll1.enableDashedLine(10f, 10f, 0f);
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        ll1.setTextSize(10f);
        ll1.setLineColor(Color.GREEN);

        LimitLine ll2 = new LimitLine(Float.parseFloat(Collections.min(upperLowerLimitList)), Collections.min(upperLowerLimitList));
        ll2.setLineWidth(4f);
        ll2.enableDashedLine(10f, 10f, 0f);
        ll2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        ll2.setTextSize(10f);
        ll2.setLineColor(Color.RED);


        YAxis rightAxis = mChart.getAxisRight();

        rightAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        rightAxis.addLimitLine(ll1);
        rightAxis.addLimitLine(ll2);
        rightAxis.setDrawLimitLinesBehindData(true);
        rightAxis.setTextColor(Color.WHITE);
        rightAxis.setAxisMaxValue(Float.parseFloat(Collections.max(upperLowerLimitList)) + 50f);
        rightAxis.enableGridDashedLine(10f, 10f, 0f);
        mChart.animateXY(2000, 2000);
        mChart.invalidate();
    }

    private ArrayList<ILineDataSet> getDataSet() {
        ArrayList<ILineDataSet> dataSets = null;

        ArrayList<Entry> valueSet = new ArrayList<>();
        int i = 0;
        upperLowerLimitList.clear();
        for (DateHigh dh : dhm.datehigh
                ) {
            valueSet.add(new Entry(Float.parseFloat(dh.getQuoteHighValue()), i++));
            upperLowerLimitList.add(dh.getQuoteHighValue());
        }

        LineDataSet lineDataSet = new LineDataSet(valueSet, symbol);
        lineDataSet.setColor(Color.rgb(0, 155, 0));

        dataSets = new ArrayList<>();
        dataSets.add(lineDataSet);
        return dataSets;
    }

    private ArrayList<String> getXAxisValues() {
        ArrayList<String> xAxis = new ArrayList<>();

        for (DateHigh dh : dhm.datehigh
                ) {
            xAxis.add("A");
        }
        return xAxis;
    }


}
