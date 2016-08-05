package com.example.simpleratingbar;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.iarcuschin.simpleratingbar.SimpleRatingBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class CornerRadiusFragment extends Fragment {

  @BindView(R.id.ratingBar1) SimpleRatingBar ratingBar1;
  @BindView(R.id.ratingBar2) SimpleRatingBar ratingBar2;
  @BindView(R.id.ratingBar3) SimpleRatingBar ratingBar3;
  @BindView(R.id.ratingBar4) SimpleRatingBar ratingBar4;
  @BindView(R.id.ratingBar5) SimpleRatingBar ratingBar5;
  private Unbinder unbinder;

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_corner_radius, container, false);
    unbinder = ButterKnife.bind(this, view);

    setHasOptionsMenu(true);

    return view;
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle item selection
    switch (item.getItemId()) {
      case R.id.menu_action_refresh:
        if (ratingBar1.getStarCornerRadius() <= 10) {
          ratingBar1.setStarCornerRadius(ratingBar1.getStarCornerRadius() + 2);
          ratingBar2.setStarCornerRadius(ratingBar2.getStarCornerRadius() + 2);
          ratingBar3.setStarCornerRadius(ratingBar3.getStarCornerRadius() + 2);
          ratingBar4.setStarCornerRadius(ratingBar4.getStarCornerRadius() + 2);
          ratingBar5.setStarCornerRadius(ratingBar5.getStarCornerRadius() + 2);
        } else {
          ratingBar1.setStarCornerRadius(0);
          ratingBar2.setStarCornerRadius(2);
          ratingBar3.setStarCornerRadius(4);
          ratingBar4.setStarCornerRadius(6);
          ratingBar5.setStarCornerRadius(8);
        }
        return false;
      default:
        return super.onOptionsItemSelected(item);
    }
  }
}
