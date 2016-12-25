package com.udacity.stockhawk.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String STOCK_URI = "stock_uri";
    private static final int STOCK_DETAILS_LOADER = 55;
    private Uri mStockUri;

    @BindView(R.id.details_activity_data_ready)
    TextView mDataReady;

    @BindView(R.id.details_activity_chart)
    LineChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);
        mStockUri = getIntent().getParcelableExtra(STOCK_URI);
        Timber.d("Loading data for " + mStockUri);
        if (getLoaderManager().getLoader(STOCK_DETAILS_LOADER) == null) {
            getLoaderManager().initLoader(STOCK_DETAILS_LOADER, null, this);
        } else {
            getLoaderManager().restartLoader(STOCK_DETAILS_LOADER, null, this);
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if (mStockUri != null) {
            return new CursorLoader(this, mStockUri, null, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            if (PrefUtils.getDataState(this) == PrefUtils.DATA_STATE_ERROR) {
                mDataReady.setVisibility(View.VISIBLE);
            } else {
                mDataReady.setVisibility(View.GONE);
            }



            showData(cursor);
        }
    }

    private void showData(Cursor cursor) {
        setTitle(cursor.getString(Contract.Quote.POSITION_SYMBOL));
        String rawHistory = cursor.getString(Contract.Quote.POSITION_HISTORY);
        String[] historyList = rawHistory.split("\n");
        Calendar calendar = Calendar.getInstance();
        List<Entry> entries = new ArrayList<Entry>();
        for (String rawData : historyList) {
            String[] val = rawData.split(", ", 2);
            calendar.setTimeInMillis(Long.parseLong(val[0]));
            Date date = calendar.getTime();
            entries.add(new Entry(date.getTime(), Float.parseFloat(val[1])));
        }
        Collections.sort(entries, new EntryXComparator());
        String endDate = historyList[0].split(",")[0];
        String beginningDate = historyList[historyList.length - 1].split(",")[0];
        Date end = new Date(Long.parseLong(endDate));
        Date start = new Date(Long.parseLong(beginningDate));
        DateFormat f = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
        Description description = new Description();
        description.setText(f.format(start) + " -- " + f.format(end));
        description.setTextColor(getResources().getColor(R.color.colorAccent));
        LineDataSet dataSet = new LineDataSet(entries, cursor.getString(Contract.Quote.POSITION_SYMBOL));
        dataSet.setColor(getResources().getColor(R.color.colorPrimary));
        dataSet.setValueTextColor(getResources().getColor(R.color.colorAccent));
        mChart.setDescription(description);
        mChart.getLegend().setEnabled(true);
        mChart.setAutoScaleMinMaxEnabled(true);
        mChart.getAxisLeft().setDrawLabels(false);
        final XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis((long) value);
                DateFormat f = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
                return f.format(calendar.getTime());
            }
        });
        LineData lineData = new LineData(dataSet);
        mChart.setData(lineData);
        mChart.setFitsSystemWindows(true);
        mChart.getAxisLeft().setEnabled(true);
        mChart.invalidate(); // refresh
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
