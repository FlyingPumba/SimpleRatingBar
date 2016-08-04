package com.example.simpleratingbar;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;

public class AnimatedFragment extends Fragment {

  @BindView(R.id.ratingBar1) SimpleRatingBar ratingBar1;
  @BindView(R.id.ratingBar2) SimpleRatingBar ratingBar2;
  @BindView(R.id.ratingBar3) SimpleRatingBar ratingBar3;
  @BindView(R.id.ratingBar4) SimpleRatingBar ratingBar4;
  @BindView(R.id.ratingBar5) SimpleRatingBar ratingBar5;
  private Unbinder unbinder;

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_animated, container, false);
    unbinder = ButterKnife.bind(this, view);

    startAnimations();

    return view;
  }

  private void startAnimations() {
    ratingBar1.getAnimationBuilder()
        .setRepeatCount(ValueAnimator.INFINITE)
        .setRepeatMode(ValueAnimator.REVERSE)
        .setInterpolator(new LinearInterpolator())
        .setRatingTarget(4)
        .start();

    ratingBar2.getAnimationBuilder()
        .setRepeatCount(ValueAnimator.INFINITE)
        .setRepeatMode(ValueAnimator.REVERSE)
        .setInterpolator(new AccelerateInterpolator(1.5f))
        .setRatingTarget(4)
        .start();

    ratingBar3.getAnimationBuilder()
        .setRepeatCount(ValueAnimator.INFINITE)
        .setRepeatMode(ValueAnimator.REVERSE)
        .setInterpolator(new DecelerateInterpolator(1.5f))
        .setRatingTarget(4)
        .setDuration(1500)
        .start();

    ratingBar4.getAnimationBuilder()
        .setRepeatCount(ValueAnimator.INFINITE)
        .setRepeatMode(ValueAnimator.RESTART)
        .setInterpolator(new BounceInterpolator())
        .setRatingTarget(4)
        .start();

    ratingBar5.getAnimationBuilder()
        .setRepeatCount(ValueAnimator.INFINITE)
        .setRepeatMode(ValueAnimator.RESTART)
        .setInterpolator(new OvershootInterpolator(2f))
        .setRatingTarget(4)
        .start();
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
  }
}
