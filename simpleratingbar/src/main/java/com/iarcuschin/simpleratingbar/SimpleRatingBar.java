package com.iarcuschin.simpleratingbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.util.TypedValue.applyDimension;

public class SimpleRatingBar extends View {

  // Configurable variables
  @ColorInt int starsColor;
  @ColorInt int backgroundColor;
  int numberOfStars;
  float starsSeparation;
  float maxStarSize;
  float rating;
  boolean isIndicator;

  // Internal variables
  private Paint paintStar;
  private Paint paintBackground;
  private Path path;
  private float starSize;
  // Internal variables used to speed up drawing. They all depend on  the value of starSize
  private float bottomFromMargin;
  private float triangleSide;
  private float half;
  private float tipVerticalMargin;
  private float tipHorizontalMargin;
  private float innerUpHorizontalMargin;
  private float innerBottomHorizontalMargin;
  private float innerBottomVerticalMargin;
  private float innerCenterVerticalMargin;
  private float[] starVertex;
  private RectF starsDrawingSpace;

  // in order to delete some drawing, and keep transparency
  // http://stackoverflow.com/a/21865858/2271834
  private Canvas internalCanvas;
  private Bitmap internalBitmap;

  public SimpleRatingBar(Context context) {
    super(context);

    initView();
  }

  public SimpleRatingBar(Context context, AttributeSet attrs) {
    super(context, attrs);

    parseAttrs(attrs);

    initView();
  }

  public SimpleRatingBar(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    parseAttrs(attrs);

    initView();
  }

  private void initView() {
    paintStar = new Paint(Paint.ANTI_ALIAS_FLAG);
    path = new Path();
    paintStar.setAntiAlias(true);
    paintStar.setDither(true);
    paintStar.setStyle(Paint.Style.STROKE);
    paintStar.setStrokeJoin(Paint.Join.ROUND);
    paintStar.setStrokeCap(Paint.Cap.ROUND);
    paintStar.setPathEffect(new CornerPathEffect(6));
    paintStar.setStrokeWidth(5);
    paintStar.setColor(starsColor);

    paintBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
    paintBackground.setStyle(Paint.Style.FILL_AND_STROKE);
  }

  private void parseAttrs(AttributeSet attrs) {
    TypedArray arr = getContext().obtainStyledAttributes(attrs, R.styleable.SimpleRatingBar);

    starsColor = arr.getColor(R.styleable.SimpleRatingBar_starsColor, getResources().getColor(R.color.golden_stars));
    backgroundColor = arr.getColor(R.styleable.SimpleRatingBar_backgroundColor, Color.TRANSPARENT);
    numberOfStars = arr.getInteger(R.styleable.SimpleRatingBar_numberOfStars, 5);

    float starsSeparationDp = arr.getDimension(R.styleable.SimpleRatingBar_starsSeparation, 4);
    starsSeparation = applyDimension(COMPLEX_UNIT_DIP, starsSeparationDp, getResources().getDisplayMetrics());
    float maxStarSizeDp = arr.getDimension(R.styleable.SimpleRatingBar_maxStarSize, 35);
    maxStarSize = applyDimension(COMPLEX_UNIT_DIP, maxStarSizeDp, getResources().getDisplayMetrics());

    rating = arr.getFloat(R.styleable.SimpleRatingBar_rating, 0f);
    isIndicator = arr.getBoolean(R.styleable.SimpleRatingBar_isIndicator, false);

    arr.recycle();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int desiredWidth = Math.round(maxStarSize * numberOfStars + starsSeparation * (numberOfStars -1));
    int desiredHeight = Math.round(maxStarSize);

    int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    int widthSize = MeasureSpec.getSize(widthMeasureSpec);
    int heightMode = MeasureSpec.getMode(heightMeasureSpec);
    int heightSize = MeasureSpec.getSize(heightMeasureSpec);

    int width;
    int height;

    //Measure Width
    if (widthMode == MeasureSpec.EXACTLY) {
      //Must be this size
      width = widthSize;
    } else if (widthMode == MeasureSpec.AT_MOST) {
      //Can't be bigger than...
      width = Math.min(desiredWidth, widthSize);
    } else {
      //Be whatever you want
      width = desiredWidth;
    }

    //Measure Height
    if (heightMode == MeasureSpec.EXACTLY) {
      //Must be this size
      height = heightSize;
    } else if (heightMode == MeasureSpec.AT_MOST) {
      //Can't be bigger than...
      height = Math.min(desiredHeight, heightSize);
    } else {
      //Be whatever you want
      height = desiredHeight;
    }

    starSize = calculateBestStarSize(width, height);
    performStarSizeAssociatedCalculations(width, height);
    //MUST CALL THIS
    setMeasuredDimension(width, height);
  }

  private float calculateBestStarSize(int width, int height) {
    float desiredTotalSize = maxStarSize * numberOfStars + starsSeparation * (numberOfStars -1);
    if (desiredTotalSize > width) {
      // we need to shrink the size of the stars
      return (width - starsSeparation * (numberOfStars -1))/numberOfStars;
    } else {
      return maxStarSize;
    }
  }

  private void performStarSizeAssociatedCalculations(int width, int height) {
    float totalStarsSize = starSize * numberOfStars + starsSeparation * (numberOfStars -1);
    float startingX = width/2 - totalStarsSize/2;
    float startingY = 0;
    starsDrawingSpace = new RectF(startingX, startingY, startingX + totalStarsSize, startingY + starSize);

    bottomFromMargin = starSize*0.2f;
    triangleSide = starSize*0.35f;
    half = starSize * 0.5f;
    tipVerticalMargin = starSize * 0.05f;
    tipHorizontalMargin = starSize * 0.03f;
    innerUpHorizontalMargin = starSize * 0.38f;
    innerBottomHorizontalMargin = starSize * 0.32f;
    innerBottomVerticalMargin = starSize * 0.55f;
    innerCenterVerticalMargin = starSize * 0.27f;

    starVertex = new float[] {
        tipHorizontalMargin, innerUpHorizontalMargin, // top left
        tipHorizontalMargin + triangleSide, innerUpHorizontalMargin,
        half, tipVerticalMargin, // top tip
        starSize - tipHorizontalMargin - triangleSide, innerUpHorizontalMargin,
        starSize - tipHorizontalMargin, innerUpHorizontalMargin, // top right
        starSize - innerBottomHorizontalMargin, innerBottomVerticalMargin,
        starSize - bottomFromMargin, starSize - tipVerticalMargin, // bottom right
        half, starSize - innerCenterVerticalMargin,
        bottomFromMargin, starSize - tipVerticalMargin, // bottom left
        innerBottomHorizontalMargin, innerBottomVerticalMargin,
        tipHorizontalMargin, innerUpHorizontalMargin, // top left
    };
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);

    if (internalBitmap != null) {
      // avoid leaking memory after losing the reference
      internalBitmap.recycle();
    }

    if (w > 0 && h > 0) {
      internalBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
      internalBitmap.eraseColor(Color.TRANSPARENT);
      internalCanvas = new Canvas(internalBitmap);
    }
  }

  @Override protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    int height = getHeight();
    int width = getWidth();

    if (width == 0 || height == 0) {
      return;
    }

    internalCanvas.drawColor(Color.argb(0, 0, 0, 0));

    float remainingTotalRaiting = rating;
    float startingX = starsDrawingSpace.left;
    float startingY = starsDrawingSpace.top;
    for (int i = 0; i < numberOfStars; i++) {
      if (remainingTotalRaiting >= 1) {
        drawStar(internalCanvas, startingX, startingY, 1f);
        remainingTotalRaiting -= 1;
      } else {
        drawStar(internalCanvas, startingX, startingY, remainingTotalRaiting);
        remainingTotalRaiting = 0;
      }
      startingX += starSize;
      if (i < numberOfStars -1) {
        drawSeparator(internalCanvas, startingX, startingY);
        startingX += starsSeparation;
      }
    }
    canvas.drawBitmap(internalBitmap, 0, 0, null);
  }

  private void drawStar(Canvas canvas, float x, float y, float filled) {
    path.reset();

    // draw fill
    if (backgroundColor == Color.TRANSPARENT) {
      paintBackground.setXfermode(null);
    }
    paintBackground.setColor(starsColor);
    paintBackground.setStrokeWidth(0);
    canvas.drawRect(x, y, x + starSize * filled, y + starSize, paintBackground);
    paintBackground.setColor(backgroundColor);
    paintBackground.setStrokeWidth(1);
    if (backgroundColor == Color.TRANSPARENT) {
      paintBackground.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }
    canvas.drawRect(x + starSize * filled, y, x + starSize, y + starSize, paintBackground);

    // clean outside of star
    path.reset();
    path.moveTo(x + starVertex[0], y + starVertex[1]);
    for (int i = 2; i < starVertex.length - 2; i = i + 2) {
      path.lineTo(x + starVertex[i], y + starVertex[i + 1]);
    }
    path.lineTo(x, y + starVertex[starVertex.length - 3]); // reach the closest border
    path.lineTo(x, y + starSize); // bottom left corner
    path.lineTo(x + starSize, y + starSize); // bottom right corner
    path.lineTo(x + starSize, y); // top right corner
    path.lineTo(x, y); // top left corner
    path.lineTo(x, y + starVertex[1]);
    path.close();
    canvas.drawPath(path, paintBackground);

    // finish clean up outside
    path.reset();
    path.moveTo(x + starVertex[0], y + starVertex[1]);
    path.lineTo(x, y + starVertex[1]);
    path.lineTo(x, y + starVertex[starVertex.length - 5]);
    path.lineTo(x + starVertex[starVertex.length - 4], y + starVertex[starVertex.length - 3]);
    path.close();
    canvas.drawPath(path, paintBackground);

    // draw star on top
    path.reset();
    path.moveTo(x + starVertex[0], y + starVertex[1]);
    for(int i = 2; i < starVertex.length; i=i+2) {
      path.lineTo(x + starVertex[i], y + starVertex[i+1]);
    }
    path.close();
    canvas.drawPath(path, paintStar);
  }

  private void drawSeparator(Canvas canvas, float x, float y) {
    paintBackground.setColor(backgroundColor);
    paintBackground.setStrokeWidth(1);
    if (backgroundColor == Color.TRANSPARENT) {
      paintBackground.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }
    canvas.drawRect(x, y, x + starsSeparation, y + starSize, paintBackground);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (isIndicator) {
      return false;
    }

    // check if action is performed above stars
    if (!starsDrawingSpace.contains(event.getX(), event.getY())) {
      return false;
    }

    int action = event.getAction() & MotionEvent.ACTION_MASK;
    switch(action) {
      case MotionEvent.ACTION_DOWN:
        setNewRatingFromTouch(event.getX(), event.getY());
        break;
      case MotionEvent.ACTION_MOVE:
        setNewRatingFromTouch(event.getX(), event.getY());
        break;
      case MotionEvent.ACTION_UP:
        setNewRatingFromTouch(event.getX(), event.getY());
        break;

    }

    invalidate();
    return true;
  }

  private void setNewRatingFromTouch(float x, float y) {
    // normalize x to inside starsDrawinSpace
    x = x - starsDrawingSpace.left;
    rating = (float)numberOfStars / starsDrawingSpace.width() * x;
  }

  /**
   * Sets rating
   * @param rating value between 0 and numberOfStars
   */
  public void setRating(float rating) {
    if (rating > numberOfStars) {
      throw new IllegalArgumentException("rating must be between 0 and numberOfStars");
    }
    this.rating = rating;
    invalidate();
  }

  public float getRating(){
    return rating;
  }
}
