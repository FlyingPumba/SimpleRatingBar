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

public class BorderWidthFragment extends Fragment {

  @BindView(R.id.ratingBar1) SimpleRatingBar ratingBar1;
  @BindView(R.id.ratingBar2) SimpleRatingBar ratingBar2;
  @BindView(R.id.ratingBar3) SimpleRatingBar ratingBar3;
  @BindView(R.id.ratingBar4) SimpleRatingBar ratingBar4;
  @BindView(R.id.ratingBar5) SimpleRatingBar ratingBar5;
  private Unbinder unbinder;

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_border_width, container, false);
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
        if (ratingBar1.getStarBorderWidth() <= 9) {
          ratingBar1.setStarBorderWidth(ratingBar1.getStarBorderWidth() + 1);
          ratingBar2.setStarBorderWidth(ratingBar2.getStarBorderWidth() + 1);
          ratingBar3.setStarBorderWidth(ratingBar3.getStarBorderWidth() + 1);
          ratingBar4.setStarBorderWidth(ratingBar4.getStarBorderWidth() + 1);
          ratingBar5.setStarBorderWidth(ratingBar5.getStarBorderWidth() + 1);
        } else {
          ratingBar1.setStarBorderWidth(5);
          ratingBar2.setStarBorderWidth(6);
          ratingBar3.setStarBorderWidth(7);
          ratingBar4.setStarBorderWidth(8);
          ratingBar5.setStarBorderWidth(9);
        }
        return false;
      default:
        return super.onOptionsItemSelected(item);
    }
  }
}
