package com.example.simpleratingbar;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;

public class StarsSeparationFragment extends Fragment {

  @BindView(R.id.ratingBar1) SimpleRatingBar ratingBar1;
  @BindView(R.id.ratingBar2) SimpleRatingBar ratingBar2;
  @BindView(R.id.ratingBar3) SimpleRatingBar ratingBar3;
  @BindView(R.id.ratingBar4) SimpleRatingBar ratingBar4;
  @BindView(R.id.ratingBar5) SimpleRatingBar ratingBar5;
  private Unbinder unbinder;

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_stars_separation, container, false);
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
    float limit = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, getResources().getDisplayMetrics());
    // Handle item selection
    switch (item.getItemId()) {
      case R.id.menu_action_refresh:
        if (ratingBar1.getStarsSeparation() <= limit) {
          ratingBar1.setStarsSeparation(ratingBar1.getStarsSeparation() + 4);
          ratingBar2.setStarsSeparation(ratingBar2.getStarsSeparation() + 4);
          ratingBar3.setStarsSeparation(ratingBar3.getStarsSeparation() + 4);
          ratingBar4.setStarsSeparation(ratingBar4.getStarsSeparation() + 4);
          ratingBar5.setStarsSeparation(ratingBar5.getStarsSeparation() + 4);
        } else {
          ratingBar1.setStarsSeparation(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics()));
          ratingBar2.setStarsSeparation(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics()));
          ratingBar3.setStarsSeparation(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
          ratingBar4.setStarsSeparation(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics()));
          ratingBar5.setStarsSeparation(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics()));
        }
        return false;
      default:
        return super.onOptionsItemSelected(item);
    }
  }
}
