package com.example.simpleratingbar;

import android.os.Bundle;
import android.support.annotation.Dimension;
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
          ratingBar1.setStarsSeparation(ratingBar1.getStarsSeparation(Dimension.DP) + 4, Dimension.DP);
          ratingBar2.setStarsSeparation(ratingBar2.getStarsSeparation(Dimension.DP) + 4, Dimension.DP);
          ratingBar3.setStarsSeparation(ratingBar3.getStarsSeparation(Dimension.DP) + 4, Dimension.DP);
          ratingBar4.setStarsSeparation(ratingBar4.getStarsSeparation(Dimension.DP) + 4, Dimension.DP);
          ratingBar5.setStarsSeparation(ratingBar5.getStarsSeparation(Dimension.DP) + 4, Dimension.DP);
        } else {
          ratingBar1.setStarsSeparation(4, Dimension.DP);
          ratingBar2.setStarsSeparation(6, Dimension.DP);
          ratingBar3.setStarsSeparation(8, Dimension.DP);
          ratingBar4.setStarsSeparation(10, Dimension.DP);
          ratingBar5.setStarsSeparation(12, Dimension.DP);
        }
        return false;
      default:
        return super.onOptionsItemSelected(item);
    }
  }
}
