package com.iarcuschin.simpleratingbar;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
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
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.util.TypedValue.applyDimension;

public class SimpleRatingBar extends View {

  public enum Gravity {
    Left(0),
    Right(1);

    int id;
    Gravity(int id) {
      this.id = id;
    }

    static Gravity fromId(int id) {
      for (Gravity f : values()) {
        if (f.id == id) return f;
      }
      // default value
      Log.w("SimpleRatingBar", String.format("Gravity chosen is neither 'left' nor 'right', I will set it to Left"));
      return Left;
    }
  }

  // Configurable variables
  @ColorInt int borderColor;
  @ColorInt int fillColor;
  @ColorInt int backgroundColor;
  int numberOfStars;
  float starsSeparation;
  float starSize;
  float maxStarSize;
  float stepSize;
  float rating;
  boolean isIndicator;
  Gravity gravity;
  float starBorderWidth;

  // Internal variables
  private Paint paintStar;
  private Paint paintStarFill;
  private Paint paintBackground;
  private Path path;
  private float defaultStarSize;
  private ValueAnimator ratingAnimator;
  private OnRatingBarChangeListener listener;
  private boolean touchInProgress;
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
  private RectF starsTouchSpace;

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
    paintStar.setStrokeWidth(starBorderWidth);
    paintStar.setColor(borderColor);

    paintBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
    paintBackground.setStyle(Paint.Style.FILL_AND_STROKE);
    paintBackground.setStrokeWidth(1);
    paintBackground.setColor(backgroundColor);
    if (backgroundColor == Color.TRANSPARENT) {
      paintBackground.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    paintStarFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    paintStarFill.setStyle(Paint.Style.FILL_AND_STROKE);
    paintStarFill.setStrokeWidth(0);
    paintStarFill.setColor(fillColor);

    defaultStarSize = applyDimension(COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
  }

  private void parseAttrs(AttributeSet attrs) {
    TypedArray arr = getContext().obtainStyledAttributes(attrs, R.styleable.SimpleRatingBar);

    borderColor = arr.getColor(R.styleable.SimpleRatingBar_borderColor, getResources().getColor(R.color.golden_stars));
    fillColor = arr.getColor(R.styleable.SimpleRatingBar_fillColor, borderColor);
    backgroundColor = arr.getColor(R.styleable.SimpleRatingBar_backgroundColor, Color.TRANSPARENT);
    numberOfStars = arr.getInteger(R.styleable.SimpleRatingBar_numberOfStars, 5);

    float starsSeparationDp = arr.getDimension(R.styleable.SimpleRatingBar_starsSeparation, 4);
    starsSeparation = applyDimension(COMPLEX_UNIT_DIP, starsSeparationDp, getResources().getDisplayMetrics());
    maxStarSize = arr.getDimensionPixelSize(R.styleable.SimpleRatingBar_maxStarSize, Integer.MAX_VALUE);
    starSize = arr.getDimensionPixelSize(R.styleable.SimpleRatingBar_starSize, Integer.MAX_VALUE);
    stepSize = arr.getFloat(R.styleable.SimpleRatingBar_stepSize, Float.MAX_VALUE);
    starBorderWidth = arr.getInteger(R.styleable.SimpleRatingBar_starBorderWidth, 5);

    rating = normalizeRating(arr.getFloat(R.styleable.SimpleRatingBar_rating, 0f));
    isIndicator = arr.getBoolean(R.styleable.SimpleRatingBar_isIndicator, false);
    gravity = Gravity.fromId(arr.getInt(R.styleable.SimpleRatingBar_gravity, Gravity.Left.id));

    arr.recycle();

    validateAttrs();
  }

  private void validateAttrs() {
    if (numberOfStars <= 0) {
      throw new IllegalArgumentException(String.format("SimpleRatingBar initialized with invalid value for numberOfStars. Found %d, but should be greater than 0", numberOfStars));
    }
    if (starSize != Integer.MAX_VALUE && maxStarSize != Integer.MAX_VALUE && starSize > maxStarSize) {
      Log.w("SimpleRatingBar", String.format("Initialized with conflicting values: starSize is greater than maxStarSize (%f > %f). I will ignore maxStarSize", starSize, maxStarSize));
    }
    if (stepSize <= 0) {
      throw new IllegalArgumentException(String.format("SimpleRatingBar initialized with invalid value for stepSize. Found %f, but should be greater than 0", stepSize));
    }
    if (starBorderWidth <= 0) {
      throw new IllegalArgumentException(String.format("SimpleRatingBar initialized with invalid value for starBorderWidth. Found %f, but should be greater than 0",
          starBorderWidth));
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
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
      if (starSize != Integer.MAX_VALUE) {
        // user specified a specific star size, so there is a desired width
        int desiredWidth = Math.round(starSize * numberOfStars + starsSeparation * (numberOfStars -1));
        width = Math.min(desiredWidth, widthSize);
      } else if (maxStarSize != Integer.MAX_VALUE) {
        // user specified a max star size, so there is a desired width
        int desiredWidth = Math.round(maxStarSize * numberOfStars + starsSeparation * (numberOfStars -1));
        width = Math.min(desiredWidth, widthSize);
      } else {
        // using defaults
        int desiredWidth = Math.round(defaultStarSize * numberOfStars + starsSeparation * (numberOfStars -1));
        width = Math.min(desiredWidth, widthSize);
      }
    } else {
      //Be whatever you want
      if (starSize != Integer.MAX_VALUE) {
        // user specified a specific star size, so there is a desired width
        int desiredWidth = Math.round(starSize * numberOfStars + starsSeparation * (numberOfStars -1));
        width = desiredWidth;
      } else if (maxStarSize != Integer.MAX_VALUE) {
        // user specified a max star size, so there is a desired width
        int desiredWidth = Math.round(maxStarSize * numberOfStars + starsSeparation * (numberOfStars -1));
        width = desiredWidth;
      } else {
        // using defaults
        int desiredWidth = Math.round(defaultStarSize * numberOfStars + starsSeparation * (numberOfStars -1));
        width = desiredWidth;
      }
    }

    //Measure Height
    if (heightMode == MeasureSpec.EXACTLY) {
      //Must be this size
      height = heightSize;
    } else if (heightMode == MeasureSpec.AT_MOST) {
      //Can't be bigger than...
      if (starSize != Integer.MAX_VALUE) {
        // user specified a specific star size, so there is a desired width
        int desiredHeight = Math.round(starSize);
        height = Math.min(desiredHeight, heightSize);
      } else if (maxStarSize != Integer.MAX_VALUE) {
        // user specified a max star size, so there is a desired width
        int desiredHeight = Math.round(maxStarSize);
        height = Math.min(desiredHeight, heightSize);
      } else {
        // using defaults
        int desiredHeight = Math.round(defaultStarSize);
        height = Math.min(desiredHeight, heightSize);
      }
    } else {
      //Be whatever you want
      if (starSize != Integer.MAX_VALUE) {
        // user specified a specific star size, so there is a desired width
        int desiredHeight = Math.round(starSize);
        height = desiredHeight;
      } else if (maxStarSize != Integer.MAX_VALUE) {
        // user specified a max star size, so there is a desired width
        int desiredHeight = Math.round(maxStarSize);
        height = desiredHeight;
      } else {
        // using defaults
        int desiredHeight = Math.round(defaultStarSize);
        height = desiredHeight;
      }
    }

    if (starSize == Integer.MAX_VALUE) {
      starSize = calculateBestStarSize(width, height);
    }
    performStarSizeAssociatedCalculations(width, height);
    //MUST CALL THIS
    setMeasuredDimension(width, height);
  }

  private float calculateBestStarSize(int width, int height) {
    if (maxStarSize != Integer.MAX_VALUE) {
      float desiredTotalSize = maxStarSize * numberOfStars + starsSeparation * (numberOfStars - 1);
      if (desiredTotalSize > width) {
        // we need to shrink the size of the stars
        return (width - starsSeparation * (numberOfStars - 1)) / numberOfStars;
      } else {
        return maxStarSize;
      }
    } else {
      // expand the most we can
      return (width - starsSeparation * (numberOfStars - 1)) / numberOfStars;
    }
  }

  private void performStarSizeAssociatedCalculations(int width, int height) {
    float totalStarsSize = starSize * numberOfStars + starsSeparation * (numberOfStars -1);
    float startingX = width/2 - totalStarsSize/2;
    float startingY = 0;
    starsDrawingSpace = new RectF(startingX, startingY, startingX + totalStarsSize, startingY + starSize);
    float aux = starsDrawingSpace.width() * 0.05f;
    starsTouchSpace = new RectF(starsDrawingSpace.left - aux, starsDrawingSpace.top, starsDrawingSpace.right + aux, starsDrawingSpace.bottom);

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

    if (gravity == Gravity.Left) {
      drawFromLeftToRight(internalCanvas);
    } else {
      drawFromRightToLeft(internalCanvas);
    }

    canvas.drawBitmap(internalBitmap, 0, 0, null);
  }

  private void drawFromLeftToRight(Canvas internalCanvas) {
    float remainingTotalRating = getRatingToDraw();
    float startingX = starsDrawingSpace.left;
    float startingY = starsDrawingSpace.top;
    for (int i = 0; i < numberOfStars; i++) {
      if (remainingTotalRating >= 1) {
        drawStar(internalCanvas, startingX, startingY, 1f, gravity);
        remainingTotalRating -= 1;
      } else {
        drawStar(internalCanvas, startingX, startingY, remainingTotalRating, gravity);
        remainingTotalRating = 0;
      }
      startingX += starSize;
      if (i < numberOfStars -1) {
        drawSeparator(internalCanvas, startingX, startingY);
        startingX += starsSeparation;
      }
    }
  }

  private void drawFromRightToLeft(Canvas internalCanvas) {
    float remainingTotalRating = getRatingToDraw();
    float startingX = starsDrawingSpace.right - starSize;
    float startingY = starsDrawingSpace.top;
    for (int i = 0; i < numberOfStars; i++) {
      if (remainingTotalRating >= 1) {
        drawStar(internalCanvas, startingX, startingY, 1f, gravity);
        remainingTotalRating -= 1;
      } else {
        drawStar(internalCanvas, startingX, startingY, remainingTotalRating, gravity);
        remainingTotalRating = 0;
      }
      if (i < numberOfStars -1) {
        startingX -= starsSeparation;
        drawSeparator(internalCanvas, startingX, startingY);
      }
      startingX -= starSize;
    }
  }

  private float getRatingToDraw() {
    if (stepSize != Float.MAX_VALUE) {
      return rating - (rating % stepSize);
    } else {
      return rating;
    }
  }

  private void drawStar(Canvas canvas, float x, float y, float filled, Gravity gravity) {
    // draw fill
    float fill = starSize * filled;
    if (gravity == Gravity.Left) {
      canvas.drawRect(x, y, x + fill, y + starSize, paintStarFill);
      canvas.drawRect(x + fill, y, x + starSize, y + starSize, paintBackground);
    } else {
      canvas.drawRect(x, y, x + starSize - fill, y + starSize, paintBackground);
      canvas.drawRect(x + starSize - fill, y, x + starSize, y + starSize, paintStarFill);
    }

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
    canvas.drawRect(x, y, x + starsSeparation, y + starSize, paintBackground);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (isIndicator  || (ratingAnimator != null && ratingAnimator.isRunning())) {
      return false;
    }

    int action = event.getAction() & MotionEvent.ACTION_MASK;
    switch(action) {
      case MotionEvent.ACTION_DOWN:
      case MotionEvent.ACTION_MOVE:
        // check if action is performed on stars
        if (starsTouchSpace.contains(event.getX(), event.getY())) {
          touchInProgress = true;
          setNewRatingFromTouch(event.getX(), event.getY());
        } else {
          if (touchInProgress && listener != null) {
            listener.onRatingChanged(this, rating, false);
          }
          touchInProgress = false;
          return false;
        }
        break;
      case MotionEvent.ACTION_UP:
        setNewRatingFromTouch(event.getX(), event.getY());
      case MotionEvent.ACTION_CANCEL:
        if (listener != null) {
          listener.onRatingChanged(this, rating, false);
        }
        touchInProgress = false;
        break;

    }

    invalidate();
    return true;
  }

  private void setNewRatingFromTouch(float x, float y) {
    // normalize x to inside starsDrawinSpace
    if (gravity != Gravity.Left) {
      x = getWidth() - x;
    }

    if (x < starsDrawingSpace.left) {
      rating = 0;
      return;
    } else if (x >  starsDrawingSpace.right) {
      rating = numberOfStars;
      return;
    }

    x = x - starsDrawingSpace.left;
    // reduce the width to allow the user reach the top and bottom values of rating (0 and numberOfStars)
    rating = (float)numberOfStars / starsDrawingSpace.width() * x;
    if (stepSize != Float.MAX_VALUE) {
      rating -= rating % stepSize;
    }
  }

  /* ----------- GETTERS AND SETTERS ----------- */

  /**
   * Sets rating
   * @param rating value between 0 and numberOfStars
   */
  public void setRating(float rating) {
    this.rating = normalizeRating(rating);
    if (stepSize != Float.MAX_VALUE) {
      rating -= rating % stepSize;
    }
    invalidate();
    if (listener != null && (ratingAnimator == null || !ratingAnimator.isRunning())) {
      listener.onRatingChanged(this, rating, false);
    }
  }

  public float getRating(){
    return rating;
  }

  public boolean isIndicator() {
    return isIndicator;
  }

  public void setIndicator(boolean indicator) {
    isIndicator = indicator;
  }

  public float getMaxStarSize() {
    return maxStarSize;
  }

  public void setMaxStarSize(float maxStarSize) {
    this.maxStarSize = maxStarSize;
    if (starSize > maxStarSize) {
      invalidate();
    }
  }

  public float getStarSize() {
    return starSize;
  }

  public void setStarSize(float starSize) {
    this.starSize = starSize;
    invalidate();
  }

  public float getStepSize() {
    return stepSize;
  }

  public void setStepSize(float stepSize) {
    this.stepSize = stepSize;
    invalidate();
  }

  public float getStarsSeparation() {
    return starsSeparation;
  }

  public void setStarsSeparation(float starsSeparation) {
    this.starsSeparation = starsSeparation;
    invalidate();
  }

  public int getNumberOfStars() {
    return numberOfStars;
  }

  public void setNumberOfStars(int numberOfStars) {
    this.numberOfStars = numberOfStars;
    invalidate();
  }

  public @ColorInt int getBackgroundColor() {
    return backgroundColor;
  }

  @Override public void setBackgroundColor(@ColorInt int backgroundColor) {
    this.backgroundColor = backgroundColor;
    invalidate();
  }

  public @ColorInt int getBorderColor() {
    return borderColor;
  }

  public void setBorderColor(@ColorInt int borderColor) {
    this.borderColor = borderColor;
    invalidate();
  }

  public float getStarBorderWidth() {
    return starBorderWidth;
  }

  public void setStarBorderWidth(float starBorderWidth) {
    this.starBorderWidth = starBorderWidth;
    invalidate();
  }

  public @ColorInt int getFillColor() {
    return fillColor;
  }

  public void setFillColor(@ColorInt int fillColor) {
    this.fillColor = fillColor;
    invalidate();
  }

  public Gravity getGravity() {
    return gravity;
  }

  public void setGravity(Gravity gravity) {
    this.gravity = gravity;
    invalidate();
  }

  private void animateRating(AnimationBuilder builder) {
    builder.ratingTarget = normalizeRating(builder.ratingTarget);
    ratingAnimator = ValueAnimator.ofFloat(0, builder.ratingTarget);
    ratingAnimator.setDuration(builder.duration);
    ratingAnimator.setRepeatCount(builder.repeatCount);
    ratingAnimator.setRepeatMode(builder.repeatMode);

    // Callback that executes on animation steps.
    ratingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        float value = ((Float) (animation.getAnimatedValue())).floatValue();
        setRating(value);
      }
    });

    if (builder.interpolator != null) {
      ratingAnimator.setInterpolator(builder.interpolator);
    }
    if (builder.animatorListener != null) {
      ratingAnimator.addListener(builder.animatorListener);
    }
    ratingAnimator.addListener(new AnimatorListener() {
      @Override
      public void onAnimationStart(Animator animator) {

      }

      @Override
      public void onAnimationEnd(Animator animator) {
        if (listener != null) {
          listener.onRatingChanged(SimpleRatingBar.this, rating, false);
        }
      }

      @Override
      public void onAnimationCancel(Animator animator) {

      }

      @Override
      public void onAnimationRepeat(Animator animator) {

      }
    });
    ratingAnimator.start();
  }

  public AnimationBuilder getAnimationBuilder() {
    return new AnimationBuilder(this);
  }

  private float normalizeRating(float rating) {
    if (rating < 0) {
      Log.w("SimpleRatingBar", String.format("Assigned rating is less than 0 (%f < 0), I will set it to exactly 0", rating));
      return 0;
    } else if (rating > numberOfStars) {
      Log.w("SimpleRatingBar", String.format("Assigned rating is greater than numberOfStars (%f > %d), I will set it to exactly numberOfStars", rating, numberOfStars));
      return numberOfStars;
    } else {
      return rating;
    }
  }

  public void setOnRatingBarChangeListener(OnRatingBarChangeListener listener) {
    this.listener = listener;
  }

  public interface OnRatingBarChangeListener {

    /**
     * Notification that the rating has changed. Clients can use the
     * fromUser parameter to distinguish user-initiated changes from those
     * that occurred programmatically. This will not be called continuously
     * while the user is dragging, only when the user finalizes a rating by
     * lifting the touch.
     *
     * @param simpleRatingBar The RatingBar whose rating has changed.
     * @param rating The current rating. This will be in the range
     *            0..numStars.
     * @param fromUser True if the rating change was initiated by a user's
     *            touch gesture or arrow key/horizontal trackbell movement.
     */
    void onRatingChanged(SimpleRatingBar simpleRatingBar, float rating, boolean fromUser);

  }

  public class AnimationParameters {
    private long duration;
    private Interpolator interpolator;
  }

  public class AnimationBuilder {
    private SimpleRatingBar ratingBar;
    private long duration;
    private Interpolator interpolator;
    private float ratingTarget;
    private int repeatCount;
    private int repeatMode;
    private AnimatorListener animatorListener;

    private AnimationBuilder(SimpleRatingBar ratingBar) {
      this.ratingBar = ratingBar;
      this.duration = 2000;
      this.interpolator = new BounceInterpolator();
      this.ratingTarget = ratingBar.getNumberOfStars();
      this.repeatCount = 1;
      this.repeatMode = ValueAnimator.REVERSE;
    }

    public AnimationBuilder setDuration(long duration) {
      this.duration = duration;
      return this;
    }

    public AnimationBuilder setInterpolator(Interpolator interpolator) {
      this.interpolator = interpolator;
      return this;
    }

    public AnimationBuilder setRatingTarget(float ratingTarget) {
      this.ratingTarget = ratingTarget;
      return this;
    }

    public AnimationBuilder setRepeatCount(int repeatCount) {
      this.repeatCount = repeatCount;
      return this;
    }

    public AnimationBuilder setRepeatMode(int repeatMode) {
      this.repeatMode = repeatMode;
      return this;
    }

    public AnimationBuilder setAnimatorListener(AnimatorListener animatorListener) {
      this.animatorListener = animatorListener;
      return this;
    }

    public void start() {
      ratingBar.animateRating(this);
    }
  }
}
