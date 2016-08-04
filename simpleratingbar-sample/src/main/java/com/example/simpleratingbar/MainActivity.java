package com.example.simpleratingbar;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

  @BindView(R.id.viewpager) NonSwipeableViewPager viewpager;
  @BindView(R.id.tab_layout) TabLayout tabLayout;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);

    viewpager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
      @Override public Fragment getItem(int position) {
        switch (position) {
          case 0:
            return new AnimatedFragment();
          case 1:
            return new NumberOfStarsFragment();
          case 2:
            return new SizeOfStarsFragment();
          case 3:
            return new StarsSeparationFragment();
          case 4:
            return new BorderWidthFragment();
          default:
            return new StepSizeAndGravityFragment();
        }
      }

      @Override public int getCount() {
        return 6;
      }

      @Override public CharSequence getPageTitle(int position) {
        switch (position) {
          case 0:
            return "Animated";
          case 1:
            return "Number of Stars";
          case 2:
            return "Size of Stars";
          case 3:
            return "Separation";
          case 4:
            return "Border width";
          default:
            return "Step size and Gravity";
        }
      }
    });
    tabLayout.setupWithViewPager(viewpager);

    getSupportActionBar().setElevation(0f);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu_main, menu);
    return true;
  }
}
