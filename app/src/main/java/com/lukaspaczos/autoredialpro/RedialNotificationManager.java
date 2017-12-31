package com.lukaspaczos.autoredialpro;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

public class RedialNotificationManager {
  private Context context;

  public RedialNotificationManager(Context context) {
    this.context = context;
  }

  public Notification getNotification() {
    Intent notificationIntent = new Intent(context, RedialService.class);
    PendingIntent pendingIntent =
      PendingIntent.getService(context, 0, notificationIntent, 0);

    initChannels(context);

    return new NotificationCompat.Builder(context, "default")
        .setSmallIcon(R.mipmap.ic_launcher_round)
        .setContentIntent(pendingIntent)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setTicker(context.getText(R.string.ticker_text))
        .build();
  }

  public void initChannels(Context context) {
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
}
