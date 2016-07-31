package com.example.simpleratingbar;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RatingBar;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity
    implements SimpleRatingBar.OnRatingBarChangeListener {

  @Bind(R.id.ratingBar1) SimpleRatingBar ratingBar1;
  @Bind(R.id.ratingBar2) SimpleRatingBar ratingBar2;
  @Bind(R.id.ratingBar3) SimpleRatingBar ratingBar3;
  @Bind(R.id.ratingBar4) SimpleRatingBar ratingBar4;
  @Bind(R.id.ratingBar5) SimpleRatingBar ratingBar5;
  @Bind(R.id.ratingBar6) SimpleRatingBar ratingBar6;
  @Bind(R.id.textRating1) TextView textRating1;
  @Bind(R.id.textRating2) TextView textRating2;
  @Bind(R.id.textRating3) TextView textRating3;
  @Bind(R.id.textRating4) TextView textRating4;
  @Bind(R.id.textRating5) TextView textRating5;
  @Bind(R.id.textRating6) TextView textRating6;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);

    SimpleRatingBar.AnimationBuilder builder = ratingBar3.getAnimationBuilder()
        .setRatingTarget(4)
        .setRepeatCount(5)
        .setRepeatMode(ValueAnimator.RESTART);
    builder.start();

    for (SimpleRatingBar x : Arrays.asList(ratingBar1, ratingBar2, ratingBar3, ratingBar4, ratingBar5, ratingBar6)) {
      x.setOnRatingBarChangeListener(this);
    }
  }

  @Override
  public void onRatingChanged(SimpleRatingBar simpleRatingBar, float rating, boolean fromUser) {
    if (simpleRatingBar == ratingBar1) {
      textRating1.setText(String.format("Rating: %f", rating));
    } else if (simpleRatingBar == ratingBar2) {
      textRating2.setText(String.format("Rating: %f", rating));
    } else if (simpleRatingBar == ratingBar3) {
      textRating3.setText(String.format("Rating: %f", rating));
    } else if (simpleRatingBar == ratingBar4) {
      textRating4.setText(String.format("Rating: %f", rating));
    } else if (simpleRatingBar == ratingBar5) {
      textRating5.setText(String.format("Rating: %f", rating));
    } else if (simpleRatingBar == ratingBar6) {
      textRating6.setText(String.format("Rating: %f", rating));
    }
  }
}
