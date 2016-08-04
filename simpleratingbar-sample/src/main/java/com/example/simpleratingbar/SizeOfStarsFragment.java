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

public class SizeOfStarsFragment extends Fragment {

  @BindView(R.id.ratingBar1) SimpleRatingBar ratingBar1;
  @BindView(R.id.ratingBar2) SimpleRatingBar ratingBar2;
  @BindView(R.id.ratingBar3) SimpleRatingBar ratingBar3;
  @BindView(R.id.ratingBar4) SimpleRatingBar ratingBar4;
  @BindView(R.id.ratingBar5) SimpleRatingBar ratingBar5;
  private Unbinder unbinder;

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_size_of_stars, container, false);
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
    float limit = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
    // Handle item selection
    switch (item.getItemId()) {
      case R.id.menu_action_refresh:
        if (ratingBar1.getStarSize() <= limit) {
          ratingBar1.setStarSize(ratingBar1.getStarSize() + 20);
          ratingBar2.setStarSize(ratingBar2.getStarSize() + 20);
          ratingBar3.setStarSize(ratingBar3.getStarSize() + 20);
          ratingBar4.setStarSize(ratingBar4.getStarSize() + 20);
          ratingBar5.setStarSize(ratingBar5.getStarSize() + 20);
        } else {
          ratingBar1.setStarSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics()));
          ratingBar2.setStarSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics()));
          ratingBar3.setStarSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics()));
          ratingBar4.setStarSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics()));
          ratingBar5.setStarSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics()));
        }
        return false;
      default:
        return super.onOptionsItemSelected(item);
    }
  }
}
