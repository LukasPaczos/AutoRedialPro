package com.lukaspaczos.autoredialpro;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity {

  private EditText inputPhone;
  private Spinner inputTimes;
  private Spinner inputDelay;
  private Button inputButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    inputPhone = findViewById(R.id.input_phone);
    inputTimes = findViewById(R.id.input_times);
    inputDelay = findViewById(R.id.input_delay);
    inputButton = findViewById(R.id.input_button);

    inputButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
          ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 1);
          return;
        }
        startService();
      }
    });

    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
          .setAction("Action", null).show();
      }
    });
  }

  private void startService() {
    Intent intent = new Intent(MainActivity.this, RedialService.class);
    intent.setAction(RedialService.ACTION_NEW_DATA);
    intent.putExtra(RedialService.PARAM_NUMBER, inputPhone.getText().toString());
    intent.putExtra(RedialService.PARAM_LOOPS, Integer.valueOf((String) inputTimes.getSelectedItem()));
    String delayString = (String) inputDelay.getSelectedItem();
    delayString = delayString.substring(0, delayString.length() - 1);
    intent.putExtra(RedialService.PARAM_DELAY, Long.valueOf(delayString) * 1000);
    startService(intent);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    for (int result : grantResults) {
      if (result == PackageManager.PERMISSION_DENIED)
        finish();
    }

    startService();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }


}
