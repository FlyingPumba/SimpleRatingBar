package com.example.simpleratingbar;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;

public class NumberOfStarsFragment extends Fragment {

  @BindView(R.id.ratingBar1) SimpleRatingBar ratingBar1;
  @BindView(R.id.ratingBar2) SimpleRatingBar ratingBar2;
  @BindView(R.id.ratingBar3) SimpleRatingBar ratingBar3;
  @BindView(R.id.ratingBar4) SimpleRatingBar ratingBar4;
  @BindView(R.id.ratingBar5) SimpleRatingBar ratingBar5;
  private Unbinder unbinder;

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_number_of_stars, container, false);
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
        if (ratingBar1.getNumberOfStars() == 5) {
          ratingBar1.setNumberOfStars(1);
          ratingBar2.setNumberOfStars(2);
          ratingBar3.setNumberOfStars(3);
          ratingBar4.setNumberOfStars(4);
          ratingBar5.setNumberOfStars(5);
        } else {
          ratingBar1.setNumberOfStars(5);
          ratingBar2.setNumberOfStars(4);
          ratingBar3.setNumberOfStars(3);
          ratingBar4.setNumberOfStars(2);
          ratingBar5.setNumberOfStars(1);
        }
        return false;
      default:
        return super.onOptionsItemSelected(item);
    }
  }
}
