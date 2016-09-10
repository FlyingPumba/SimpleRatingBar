package com.example.simpleratingbar;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.Dimension;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class InstrumentationTest {

  Context mockContext;

  @Before
  public void setUp() {
    mockContext = new RenamingDelegatingContext(InstrumentationRegistry.getTargetContext(), "test_");
  }

  @Test public void constructorDefaults() {
    SimpleRatingBar ratingBar = new SimpleRatingBar(mockContext);

    assertEquals(5, ratingBar.getNumberOfStars());
    assertEquals(0f, ratingBar.getRating());
    assertEquals(Float.MAX_VALUE, ratingBar.getStepSize());

    assertEquals(false, ratingBar.isIndicator());
    assertEquals(true, ratingBar.isDrawBorderEnabled());

    //assertEquals(30f, ratingBar.getStarSize(Dimension.DP));
    //assertEquals(Integer.MAX_VALUE, ratingBar.getMaxStarSize());
    assertEquals(5f, ratingBar.getStarBorderWidth(Dimension.PX));
    assertEquals(6f, ratingBar.getStarCornerRadius(Dimension.PX));
    assertEquals(4f, ratingBar.getStarsSeparation(Dimension.DP));

    @ColorInt int defaultColor = mockContext.getResources().getColor(com.iarcuschin.simpleratingbar.R.color.golden_stars);
    assertEquals(defaultColor, ratingBar.getFillColor());
    assertEquals(defaultColor, ratingBar.getBorderColor());
    assertEquals(Color.TRANSPARENT, ratingBar.getBackgroundColor());
    assertEquals(Color.TRANSPARENT, ratingBar.getStarBackgroundColor());
    assertEquals(defaultColor, ratingBar.getPressedFillColor());
    assertEquals(defaultColor, ratingBar.getPressedBorderColor());
    assertEquals(Color.TRANSPARENT, ratingBar.getPressedBackgroundColor());
    assertEquals(Color.TRANSPARENT, ratingBar.getPressedStarBackgroundColor());
  }
}