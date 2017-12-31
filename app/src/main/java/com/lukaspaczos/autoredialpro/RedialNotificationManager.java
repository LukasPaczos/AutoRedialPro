package com.lukaspaczos.autoredialpro;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

public class RedialNotificationManager {
  private Context context;
  private RemoteViews remoteViews;

  public RedialNotificationManager(Context context) {
    this.context = context;
    remoteViews = new RemoteViews(context.getPackageName(), R.layout.view_notification);
  }

  public Notification getNotification() {
    initChannels(context);

    return new NotificationCompat.Builder(context, "default")
        .setSmallIcon(R.mipmap.ic_launcher_round)
      // TODO: 31/12/2017 set content intent
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setTicker("ticker")
        .build();
  }

  private void initChannels(Context context) {
    if (Build.VERSION.SDK_INT < 26) {
      return;
    }
    NotificationManager notificationManager =
      (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    NotificationChannel channel = new NotificationChannel("default",
      "Redial channel",
      NotificationManager.IMPORTANCE_HIGH);
    channel.setDescription("Redial channel");
    notificationManager.createNotificationChannel(channel);
  }

  public void resume() {
    remoteViews.setOnClickPendingIntent(R.id.notification_button_1, generatePendingIntent(RedialService.SERVICE_STATE_PAUSED, 1));
    remoteViews.setTextViewText(R.id.notification_button_1, "PAUSE");
  }

  public void pause() {
    remoteViews.setOnClickPendingIntent(R.id.notification_button_1, generatePendingIntent(RedialService.SERVICE_STATE_REPEATING, 2));
    remoteViews.setTextViewText(R.id.notification_button_1, "START");
  }

  private PendingIntent generatePendingIntent(@RedialService.RedialServiceState int param, int requestCode) {
    Intent notificationIntent = new Intent(context, RedialService.class);
    notificationIntent.setAction(RedialService.ACTION_BUTTON);
    notificationIntent.putExtra(RedialService.PARAM_REQUESTED_STATE, param);
    return PendingIntent.getService(context, requestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
  }
}
