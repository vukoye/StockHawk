package com.udacity.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;


public class StockDataRemoteViewService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        final Context context = this;
        return new RemoteViewsFactory() {
            private Cursor cursor;

            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if (cursor != null) {
                    cursor.close();
                }
                final long identityToken = Binder.clearCallingIdentity();
                Uri uri = Contract.Quote.URI;
                cursor = getContentResolver().query(uri, null, null, null, null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (cursor != null) {
                    cursor.close();
                }
                cursor = null;
            }

            @Override
            public int getCount() {
                return (cursor == null ? 0 : cursor.getCount());
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        cursor == null || !cursor.moveToPosition(position)) {
                    return null;
                }

                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_list_item_quote);
                DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                DecimalFormat dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dollarFormatWithPlus.setPositivePrefix("+$");
                DecimalFormat percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
                percentageFormat.setMaximumFractionDigits(2);
                percentageFormat.setMinimumFractionDigits(2);
                percentageFormat.setPositivePrefix("+");

                views.setTextViewText(R.id.widget_symbol, cursor.getString(Contract.Quote.POSITION_SYMBOL));
                views.setTextViewText(R.id.widget_symbol, dollarFormat.format(cursor.getFloat(Contract.Quote.POSITION_PRICE)));

                float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
                float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

                if (rawAbsoluteChange > 0) {
                    views.setInt(R.id.widget_change, "setBackgroundResource", R.drawable.percent_change_pill_green);
                } else {
                    views.setInt(R.id.widget_change, "setBackgroundResource", R.drawable.percent_change_pill_red);
                }

                String change = dollarFormatWithPlus.format(rawAbsoluteChange);
                String percentage = percentageFormat.format(percentageChange / 100);

                if (PrefUtils.getDisplayMode(context).equals(context.getString(R.string.pref_display_mode_absolute_key))) {
                    views.setTextViewText(R.id.widget_change, change);;
                } else {
                    views.setTextViewText(R.id.widget_change, percentage);
                }

                final Intent fillInIntent = new Intent();
                Uri contentUri = Contract.Quote.makeUriForStock(cursor.getString(Contract.Quote.POSITION_SYMBOL));
                fillInIntent.setData(contentUri);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_list_item_quote);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int i) {
                return (cursor.moveToPosition(i) ? cursor.getLong(Contract.Quote.POSITION_ID) : i);
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
