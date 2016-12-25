package com.udacity.stockhawk;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;

import java.io.IOException;

import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

/**
 * Created by Nemanja on 12/25/2016.
 */

public class ValidateStock extends IntentService {
    public static final String STOCK_ID = "stock_id";
    public static final String RECEIVER = "receiver";
    public static final int STOCK_NOT_FOUND = 0;
    public static final int STOCK_FOUND = 1;
    ResultReceiver receiver;
    String stockId;

    public ValidateStock() {
        super(ValidateStock.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        receiver = intent.getParcelableExtra(RECEIVER);
        stockId = intent.getStringExtra(STOCK_ID);
        boolean isFound = false;
        try {
            Stock stock = YahooFinance.get(stockId);
            isFound =  stock.getQuote().getPrice() != null;
        } catch (IOException e) {
            Timber.d("Error while validating stock");
            e.printStackTrace();
        } finally {
            Timber.d("Is stock found: " + isFound);
            Bundle bundle = new Bundle();
            bundle.putString(STOCK_ID, stockId);
            receiver.send(isFound ? STOCK_FOUND : STOCK_NOT_FOUND, new Bundle());
        }
    }
}
