package com.example.simpleratingbar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;

public class MainActivity extends AppCompatActivity {

  //@Bind(R.id.ratingBar1) SimpleRatingBar ratingBar1;
  //@Bind(R.id.ratingBar2) SimpleRatingBar ratingBar2;
  //@Bind(R.id.ratingBar3) SimpleRatingBar ratingBar3;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
  }
}
