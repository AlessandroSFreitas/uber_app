package com.example.uber.activity;

import androidx.appcompat.app.AppCompatActivity;
import com.example.uber.R;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    getSupportActionBar().hide();
  }
}
