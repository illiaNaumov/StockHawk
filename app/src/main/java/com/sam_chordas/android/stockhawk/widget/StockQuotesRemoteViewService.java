package com.sam_chordas.android.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.QuoteCursorAdapter;
import com.sam_chordas.android.stockhawk.rest.Utils;

/**
 * Created by ilyua on 09.08.2016.
 */
public class StockQuotesRemoteViewService extends RemoteViewsService {
    public static final String LOG_TAG = StockQuotesRemoteViewService.class.getSimpleName();

    public static final String [] STOCK_COLUMNS = {QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
            QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP};


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                //Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if(data != null){
                    data.close();
                }

                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                        STOCK_COLUMNS,
                        QuoteColumns.ISCURRENT + " = ?",
                        new String[]{"1"},
                        null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if(data != null){
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if(AdapterView.INVALID_POSITION == position || data == null
                        || !data.moveToPosition(position)){
                    return null;
                }
                RemoteViews remoteViews = new RemoteViews(getPackageName(),
                        R.layout.widget_quotes_list);

                String symbol = data.getString(data.getColumnIndex("symbol"));
                String bidPrice = data.getString(data.getColumnIndex("bid_price"));
                remoteViews.setTextViewText(R.id.stock_symbol,symbol);
                remoteViews.setTextViewText(R.id.bid_price,bidPrice);

                int sdk = Build.VERSION.SDK_INT;
                if (data.getInt(data.getColumnIndex("is_up")) == 1){
                        remoteViews.setInt(R.id.change, "setBackgroundDrawable", R.drawable.percent_change_pill_green);
                } else{
                        remoteViews.setInt(R.id.change, "setBackgroundDrawable", R.drawable.percent_change_pill_red);
                }
                if (Utils.showPercent){
                    remoteViews.setTextViewText(R.id.change, data.getString(data.getColumnIndex("percent_change")));
                } else{
                    remoteViews.setTextViewText(R.id.change, data.getString(data.getColumnIndex("change")));
                }

                return remoteViews;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_quotes_list);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                 if(data.moveToPosition(position))
                    return data.getLong(data.getColumnIndex("_id"));
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return false;
            }
        };
    }
}
