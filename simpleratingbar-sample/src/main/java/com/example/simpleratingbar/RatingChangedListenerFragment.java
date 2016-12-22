package com.example.simpleratingbar;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;
import java.util.Arrays;

public class RatingChangedListenerFragment extends Fragment
    implements SimpleRatingBar.OnRatingBarChangeListener, View.OnClickListener {

  @BindView(R.id.ratingBar1) SimpleRatingBar ratingBar1;
  @BindView(R.id.ratingBar2) SimpleRatingBar ratingBar2;
  @BindView(R.id.ratingBar3) SimpleRatingBar ratingBar3;
  @BindView(R.id.ratingBar4) SimpleRatingBar ratingBar4;
  @BindView(R.id.ratingBar5) SimpleRatingBar ratingBar5;
  @BindView(R.id.rating1) TextView rating1;
  @BindView(R.id.rating2) TextView rating2;
  @BindView(R.id.rating3) TextView rating3;
  @BindView(R.id.rating4) TextView rating4;
  @BindView(R.id.rating5) TextView rating5;
  private Unbinder unbinder;

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_rating_changed_listener, container, false);
    unbinder = ButterKnife.bind(this, view);

    setHasOptionsMenu(true);

    for (SimpleRatingBar srb : Arrays.asList(ratingBar1, ratingBar2, ratingBar3, ratingBar4, ratingBar5)) {
      srb.setOnRatingBarChangeListener(this);
      // srb.setOnClickListener(this);
    }

    return view;
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
  }

  @Override
  public void onRatingChanged(SimpleRatingBar simpleRatingBar, float rating, boolean fromUser) {
    String text = String.format("%.2f", rating);
    if (simpleRatingBar == ratingBar1) {
      rating1.setText(text);
    } else if (simpleRatingBar == ratingBar2) {
      rating2.setText(text);
    } else if (simpleRatingBar == ratingBar3) {
      rating3.setText(text);
    } else if (simpleRatingBar == ratingBar4) {
      rating4.setText(text);
    } else {
      rating5.setText(text);
    }
  }

  @Override
  public void onClick(View view) {
    Toast.makeText(getContext(), "Clicked!", Toast.LENGTH_SHORT).show();
  }
}

