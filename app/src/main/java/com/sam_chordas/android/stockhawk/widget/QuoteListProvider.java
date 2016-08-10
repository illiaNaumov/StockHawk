package com.sam_chordas.android.stockhawk.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;
import com.sam_chordas.android.stockhawk.ui.StockValueChartActivity;
/**
 * Created by ilyua on 09.08.2016.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class QuoteListProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        try{


        for(int appWidgetId : appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), appWidgetId);

            Intent intent = new Intent(context, MyStocksActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            remoteViews.setOnClickPendingIntent(R
                    .id.widget, pendingIntent);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                setRemoteAdapter(context, remoteViews);
            } else {
                setRemoteAdapterV11(context, remoteViews);
            }

            boolean useDetailActivity = context.getResources()
                .getBoolean(R.bool.stock_hawk_use_detail_activity);
            Intent clickIntentTemplate = useDetailActivity
                    ? new Intent(context, StockValueChartActivity.class)
                    : new Intent(context, MyStocksActivity.class);
            PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(clickIntentTemplate)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setPendingIntentTemplate(R.id.widget_list, clickPendingIntentTemplate);
            remoteViews.setEmptyView(R.id.widget_list, R.id.widget_empty);

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

        }

        }catch(Exception e){
        e.printStackTrace();
    }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void setRemoteAdapter(Context context, @NonNull final RemoteViews remoteViews){
        remoteViews.setRemoteAdapter(R.id.widget_list,
                new Intent(context, StockQuotesRemoteViewService.class));
    }

    @SuppressWarnings("deprecation")
    public void setRemoteAdapterV11(Context context, @NonNull final RemoteViews remoteViews){
        remoteViews.setRemoteAdapter(0, R.id.widget_list,
                new Intent(context, StockQuotesRemoteViewService.class));
    }
}
