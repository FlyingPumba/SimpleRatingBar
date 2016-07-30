package com.example.simpleratingbar;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.iarcuschin.simpleratingbar.SimpleRatingBar;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.ratingBar1)
    SimpleRatingBar ratingBar1;
    @Bind(R.id.ratingBar2)
    SimpleRatingBar ratingBar2;
    @Bind(R.id.ratingBar3)
    SimpleRatingBar ratingBar3;
    @Bind(R.id.ratingBar4)
    SimpleRatingBar ratingBar4;
    @Bind(R.id.ratingBar5)
    SimpleRatingBar ratingBar5;
    @Bind(R.id.ratingBar6)
    SimpleRatingBar ratingBar6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        SimpleRatingBar.AnimationBuilder builder = ratingBar2.getAnimationBuilder()
                .setRatingTarget(4)
                .setRepeatCount(5)
                .setRepeatMode(ValueAnimator.RESTART);
        builder.start();
    }
}
