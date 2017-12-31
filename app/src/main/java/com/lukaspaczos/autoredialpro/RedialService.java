package com.lukaspaczos.autoredialpro;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.v4.app.ActivityCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public class RedialService extends Service {

  public static final String ACTION_NEW_DATA = "action_new_data";
  public static final String ACTION_BUTTON = "action_button";

  public static final String PARAM_NUMBER = "param_number";
  public static final String PARAM_LOOPS = "param_loops";
  public static final String PARAM_DELAY = "param_delay";

  public static final String PARAM_REQUESTED_STATE = "param_requested_state";

  private final ServiceBinder binder = new ServiceBinder();

  @Retention(SOURCE)
  @IntDef( {SERVICE_STATE_STOPPED, SERVICE_STATE_PAUSED, SERVICE_STATE_REPEATING})
  public @interface RedialServiceState {
  }

  public static final int SERVICE_STATE_STOPPED = 0;
  public static final int SERVICE_STATE_PAUSED = 1;
  public static final int SERVICE_STATE_REPEATING = 2;

  @RedialServiceState
  private int serviceState;

  private final Handler handler = new Handler();
  private TelephonyManager telephonyManager;
  private RedialNotificationManager redialNotificationManager;

  private String number;
  private int loops;
  private int currentLoop;
  private long delay; //in ms

  @Override
  public void onCreate() {
    super.onCreate();

    telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    redialNotificationManager = new RedialNotificationManager(this);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent.getAction().equals(ACTION_NEW_DATA)) {
      clear();
      Bundle bundle = intent.getExtras();
      if (bundle != null) {
        number = bundle.getString(PARAM_NUMBER);
        loops = bundle.getInt(PARAM_LOOPS);
        delay = bundle.getLong(PARAM_DELAY);
      }

      startForeground(1, redialNotificationManager.getNotification());

      telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
      handler.post(callRunnable);
    } else if (intent.getAction().equals(ACTION_BUTTON)) {
      @RedialServiceState int requestedState = intent.getIntExtra(PARAM_REQUESTED_STATE, -1);
      switch (requestedState) {
        case SERVICE_STATE_REPEATING:
          resume();
          break;
        case SERVICE_STATE_PAUSED:
          pause();
          break;
        case SERVICE_STATE_STOPPED:
          stop();
          break;
      }
    }

    return START_NOT_STICKY;
  }

  private final Runnable callRunnable = new Runnable() {
    @SuppressLint("MissingPermission")
    @Override
    public void run() {
      Intent intent = new Intent(Intent.ACTION_CALL);
      intent.setData(Uri.parse("tel:" + number));
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      RedialService.this.startActivity(intent);
    }
  };

  private final PhoneStateListener phoneStateListener = new PhoneStateListener() {
    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
      switch (state) {
        case TelephonyManager.CALL_STATE_IDLE:
          resume();
          break;

        case TelephonyManager.CALL_STATE_OFFHOOK:
          pause();
          break;

        case TelephonyManager.CALL_STATE_RINGING:
          pause();
          break;
      }
    }
  };

  private void pause() {
    serviceState = SERVICE_STATE_PAUSED;
    redialNotificationManager.pause();
    handler.removeCallbacksAndMessages(null);
  }

  private void resume() {
    serviceState = SERVICE_STATE_REPEATING;
    redialNotificationManager.resume();
    handler.postDelayed(callRunnable, delay);
  }

  private void stop() {
    telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
    clear();
    stopSelf();
  }

  private void clear() {
    serviceState = SERVICE_STATE_STOPPED;
    handler.removeCallbacksAndMessages(null);
    currentLoop = 0;
  }

  @Override
  public IBinder onBind(Intent intent) {
    return binder;
  }

  public class ServiceBinder extends Binder {
    RedialService getService() {
      return RedialService.this;
    }
  }

  public int getServiceState() {
    return serviceState;
  }

  public String getNumber() {
    return number;
  }

  public int getLoops() {
    return loops;
  }

  public int getCurrentLoop() {
    return currentLoop;
  }

  public long getDelay() {
    return delay;
  }
}