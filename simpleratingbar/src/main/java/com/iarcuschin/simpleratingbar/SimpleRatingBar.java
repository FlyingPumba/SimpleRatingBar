package com.iarcuschin.simpleratingbar;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
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
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.Dimension;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.util.TypedValue.COMPLEX_UNIT_SP;
import static android.util.TypedValue.applyDimension;

/**
 * A simple RatingBar for Android.
 */
public class SimpleRatingBar extends View {

  /**
   * Represents gravity of the fill in the bar.
   */
  public enum Gravity {
    /**
     * Left gravity is default: the bar will be filled starting from left to right.
     */
    Left(0),
    /**
     * Right gravity: the bar will be filled starting from right to left.
     */
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
  private @ColorInt int borderColor;
  private @ColorInt int fillColor;
  private @ColorInt int backgroundColor;
  private @ColorInt int starBackgroundColor;
  private @ColorInt int pressedBorderColor;
  private @ColorInt int pressedFillColor;
  private @ColorInt int pressedBackgroundColor;
  private @ColorInt int pressedStarBackgroundColor;
  private int numberOfStars;
  private float starsSeparation;
  private float desiredStarSize;
  private float maxStarSize;
  private float stepSize;
  private float rating;
  private boolean isIndicator;
  private Gravity gravity;
  private float starBorderWidth;
  private float starCornerRadius;
  private boolean drawBorderEnabled;

  // Internal variables
  private float currentStarSize;
  private float defaultStarSize;
  private Paint paintStarOutline;
  private Paint paintStarBorder;
  private Paint paintStarFill;
  private Paint paintStarBackground;
  private CornerPathEffect cornerPathEffect;
  private Path starPath;
  private ValueAnimator ratingAnimator;
  private OnRatingBarChangeListener ratingListener;
  private OnClickListener clickListener;
  private boolean touchInProgress;
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

  /**
   * Inits paint objects and default values.
   */
  private void initView() {
    starPath = new Path();
    cornerPathEffect = new CornerPathEffect(starCornerRadius);

    paintStarOutline = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    paintStarOutline.setStyle(Paint.Style.FILL_AND_STROKE);
    paintStarOutline.setAntiAlias(true);
    paintStarOutline.setDither(true);
    paintStarOutline.setStrokeJoin(Paint.Join.ROUND);
    paintStarOutline.setStrokeCap(Paint.Cap.ROUND);
    paintStarOutline.setColor(Color.BLACK);
    paintStarOutline.setPathEffect(cornerPathEffect);

    paintStarBorder = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    paintStarBorder.setStyle(Paint.Style.STROKE);
    paintStarBorder.setStrokeJoin(Paint.Join.ROUND);
    paintStarBorder.setStrokeCap(Paint.Cap.ROUND);
    paintStarBorder.setStrokeWidth(starBorderWidth);
    paintStarBorder.setPathEffect(cornerPathEffect);

    paintStarBackground = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    paintStarBackground.setStyle(Paint.Style.FILL_AND_STROKE);
    paintStarBackground.setAntiAlias(true);
    paintStarBackground.setDither(true);
    paintStarBackground.setStrokeJoin(Paint.Join.ROUND);
    paintStarBackground.setStrokeCap(Paint.Cap.ROUND);

    paintStarFill = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    paintStarFill.setStyle(Paint.Style.FILL_AND_STROKE);
    paintStarFill.setAntiAlias(true);
    paintStarFill.setDither(true);
    paintStarFill.setStrokeJoin(Paint.Join.ROUND);
    paintStarFill.setStrokeCap(Paint.Cap.ROUND);

    defaultStarSize = applyDimension(COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
  }

  /**
   * Parses attributes defined in XML.
   */
  private void parseAttrs(AttributeSet attrs) {
    TypedArray arr = getContext().obtainStyledAttributes(attrs, R.styleable.SimpleRatingBar);

    borderColor = arr.getColor(R.styleable.SimpleRatingBar_srb_borderColor, getResources().getColor(R.color.golden_stars));
    fillColor = arr.getColor(R.styleable.SimpleRatingBar_srb_fillColor, borderColor);
    starBackgroundColor = arr.getColor(R.styleable.SimpleRatingBar_srb_starBackgroundColor, Color.TRANSPARENT);
    backgroundColor = arr.getColor(R.styleable.SimpleRatingBar_srb_backgroundColor, Color.TRANSPARENT);

    pressedBorderColor = arr.getColor(R.styleable.SimpleRatingBar_srb_pressedBorderColor, borderColor);
    pressedFillColor = arr.getColor(R.styleable.SimpleRatingBar_srb_pressedFillColor, fillColor);
    pressedStarBackgroundColor = arr.getColor(R.styleable.SimpleRatingBar_srb_pressedStarBackgroundColor, starBackgroundColor);
    pressedBackgroundColor = arr.getColor(R.styleable.SimpleRatingBar_srb_pressedBackgroundColor, backgroundColor);

    numberOfStars = arr.getInteger(R.styleable.SimpleRatingBar_srb_numberOfStars, 5);

    starsSeparation = arr.getDimensionPixelSize(R.styleable.SimpleRatingBar_srb_starsSeparation, (int)valueToPixels(4, Dimension.DP));
    maxStarSize = arr.getDimensionPixelSize(R.styleable.SimpleRatingBar_srb_maxStarSize, Integer.MAX_VALUE);
    desiredStarSize = arr.getDimensionPixelSize(R.styleable.SimpleRatingBar_srb_starSize, Integer.MAX_VALUE);
    stepSize = arr.getFloat(R.styleable.SimpleRatingBar_srb_stepSize, 0.1f);
    starBorderWidth = arr.getFloat(R.styleable.SimpleRatingBar_srb_starBorderWidth, 5f);
    starCornerRadius = arr.getFloat(R.styleable.SimpleRatingBar_srb_starCornerRadius, 6f);

    rating = normalizeRating(arr.getFloat(R.styleable.SimpleRatingBar_srb_rating, 0f));
    isIndicator = arr.getBoolean(R.styleable.SimpleRatingBar_srb_isIndicator, false);
    drawBorderEnabled = arr.getBoolean(R.styleable.SimpleRatingBar_srb_drawBorderEnabled, true);
    gravity = Gravity.fromId(arr.getInt(R.styleable.SimpleRatingBar_srb_gravity, Gravity.Left.id));

    arr.recycle();

    validateAttrs();
  }

  /**
   * Validates parsed attributes. It will throw IllegalArgumentException if severe inconsistency is found.
   * Warnings will be logged to LogCat.
   */
  private void validateAttrs() {
    if (numberOfStars <= 0) {
      throw new IllegalArgumentException(String.format("SimpleRatingBar initialized with invalid value for numberOfStars. Found %d, but should be greater than 0", numberOfStars));
    }
    if (desiredStarSize != Integer.MAX_VALUE && maxStarSize != Integer.MAX_VALUE && desiredStarSize
        > maxStarSize) {
      Log.w("SimpleRatingBar", String.format("Initialized with conflicting values: starSize is greater than maxStarSize (%f > %f). I will ignore maxStarSize", desiredStarSize, maxStarSize));
    }
    if (stepSize <= 0) {
      throw new IllegalArgumentException(String.format("SimpleRatingBar initialized with invalid value for stepSize. Found %f, but should be greater than 0", stepSize));
    }
    if (starBorderWidth <= 0) {
      throw new IllegalArgumentException(String.format("SimpleRatingBar initialized with invalid value for starBorderWidth. Found %f, but should be greater than 0",
          starBorderWidth));
    }
    if (starCornerRadius < 0) {
      throw new IllegalArgumentException(String.format("SimpleRatingBar initialized with invalid value for starCornerRadius. Found %f, but should be greater or equal than 0",
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
      if (desiredStarSize != Integer.MAX_VALUE) {
        // user specified a specific star size, so there is a desired width
        int desiredWidth = calculateTotalWidth(desiredStarSize, numberOfStars, starsSeparation, true);
        width = Math.min(desiredWidth, widthSize);
      } else if (maxStarSize != Integer.MAX_VALUE) {
        // user specified a max star size, so there is a desired width
        int desiredWidth = calculateTotalWidth(maxStarSize, numberOfStars, starsSeparation, true);
        width = Math.min(desiredWidth, widthSize);
      } else {
        // using defaults
        int desiredWidth = calculateTotalWidth(defaultStarSize, numberOfStars, starsSeparation, true);
        width = Math.min(desiredWidth, widthSize);
      }
    } else {
      //Be whatever you want
      if (desiredStarSize != Integer.MAX_VALUE) {
        // user specified a specific star size, so there is a desired width
        int desiredWidth = calculateTotalWidth(desiredStarSize, numberOfStars, starsSeparation, true);
        width = desiredWidth;
      } else if (maxStarSize != Integer.MAX_VALUE) {
        // user specified a max star size, so there is a desired width
        int desiredWidth = calculateTotalWidth(maxStarSize, numberOfStars, starsSeparation, true);
        width = desiredWidth;
      } else {
        // using defaults
        int desiredWidth = calculateTotalWidth(defaultStarSize, numberOfStars, starsSeparation, true);
        width = desiredWidth;
      }
    }

    float tentativeStarSize = (width - getPaddingLeft() - getPaddingRight() - starsSeparation * (numberOfStars - 1)) / numberOfStars;

    //Measure Height
    if (heightMode == MeasureSpec.EXACTLY) {
      //Must be this size
      height = heightSize;
    } else if (heightMode == MeasureSpec.AT_MOST) {
      //Can't be bigger than...
      if (desiredStarSize != Integer.MAX_VALUE) {
        // user specified a specific star size, so there is a desired width
        int desiredHeight = calculateTotalHeight(desiredStarSize, numberOfStars, starsSeparation, true);
        height = Math.min(desiredHeight, heightSize);
      } else if (maxStarSize != Integer.MAX_VALUE) {
        // user specified a max star size, so there is a desired width
        int desiredHeight = calculateTotalHeight(maxStarSize, numberOfStars, starsSeparation, true);
        height = Math.min(desiredHeight, heightSize);
      } else {
        // using defaults
        int desiredHeight = calculateTotalHeight(tentativeStarSize, numberOfStars, starsSeparation, true);
        height = Math.min(desiredHeight, heightSize);
      }
    } else {
      //Be whatever you want
      if (desiredStarSize != Integer.MAX_VALUE) {
        // user specified a specific star size, so there is a desired width
        int desiredHeight = calculateTotalHeight(desiredStarSize, numberOfStars, starsSeparation, true);
        height = desiredHeight;
      } else if (maxStarSize != Integer.MAX_VALUE) {
        // user specified a max star size, so there is a desired width
        int desiredHeight = calculateTotalHeight(maxStarSize, numberOfStars, starsSeparation, true);
        height = desiredHeight;
      } else {
        // using defaults
        int desiredHeight = calculateTotalHeight(tentativeStarSize, numberOfStars, starsSeparation, true);
        height = desiredHeight;
      }
    }

    //MUST CALL THIS
    setMeasuredDimension(width, height);
  }

  @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);

    int width = getWidth();
    int height = getHeight();
    if (desiredStarSize == Integer.MAX_VALUE) {
      currentStarSize = calculateBestStarSize(width, height);
    } else {
      currentStarSize = desiredStarSize;
    }
    performStarSizeAssociatedCalculations(width, height);
  }

  /**
   * Calculates largest possible star size, based on chosen width and height.
   * If maxStarSize is present, it will be considered and star size will not be greater than this value.
   * @param width
   * @param height
   * */
  private float calculateBestStarSize(int width, int height) {
    if (maxStarSize != Integer.MAX_VALUE) {
      float desiredTotalWidth = calculateTotalWidth(maxStarSize, numberOfStars, starsSeparation, true);
      float desiredTotalHeight = calculateTotalHeight(maxStarSize, numberOfStars, starsSeparation, true);
      if (desiredTotalWidth >= width || desiredTotalHeight >= height) {
        // we need to shrink the size of the stars
        float sizeBasedOnWidth = (width - getPaddingLeft() - getPaddingRight() - starsSeparation * (numberOfStars - 1)) / numberOfStars;
        float sizeBasedOnHeight = height - getPaddingTop() - getPaddingBottom();
        return Math.min(sizeBasedOnWidth, sizeBasedOnHeight);
      } else {
        return maxStarSize;
      }
    } else {
      // expand the most we can
      float sizeBasedOnWidth = (width - getPaddingLeft() - getPaddingRight() - starsSeparation * (numberOfStars - 1)) / numberOfStars;
      float sizeBasedOnHeight = height - getPaddingTop() - getPaddingBottom();
      return Math.min(sizeBasedOnWidth, sizeBasedOnHeight);
    }
  }

  /**
   * Performs auxiliary calculations to later speed up drawing phase.
   * @param width
   * @param height
   */
  private void performStarSizeAssociatedCalculations(int width, int height) {
    float totalStarsWidth = calculateTotalWidth(currentStarSize, numberOfStars, starsSeparation, false);
    float totalStarsHeight = calculateTotalHeight(currentStarSize, numberOfStars, starsSeparation, false);
    float startingX = (width - getPaddingLeft() - getPaddingRight())/2 - totalStarsWidth/2 + getPaddingLeft();
    float startingY = (height - getPaddingTop() - getPaddingBottom())/2 - totalStarsHeight/2 + getPaddingTop();
    starsDrawingSpace = new RectF(startingX, startingY, startingX + totalStarsWidth, startingY + totalStarsHeight);
    float aux = starsDrawingSpace.width() * 0.05f;
    starsTouchSpace = new RectF(starsDrawingSpace.left - aux, starsDrawingSpace.top, starsDrawingSpace.right + aux, starsDrawingSpace.bottom);

    float bottomFromMargin = currentStarSize * 0.2f;
    float triangleSide = currentStarSize * 0.35f;
    float half = currentStarSize * 0.5f;
    float tipVerticalMargin = currentStarSize * 0.05f;
    float tipHorizontalMargin = currentStarSize * 0.03f;
    float innerUpHorizontalMargin = currentStarSize * 0.38f;
    float innerBottomHorizontalMargin = currentStarSize * 0.32f;
    float innerBottomVerticalMargin = currentStarSize * 0.6f;
    float innerCenterVerticalMargin = currentStarSize * 0.27f;

    starVertex = new float[] {
        tipHorizontalMargin, innerUpHorizontalMargin, // top left
        tipHorizontalMargin + triangleSide, innerUpHorizontalMargin,
        half, tipVerticalMargin, // top tip
        currentStarSize - tipHorizontalMargin - triangleSide, innerUpHorizontalMargin,
        currentStarSize - tipHorizontalMargin, innerUpHorizontalMargin, // top right
        currentStarSize - innerBottomHorizontalMargin, innerBottomVerticalMargin,
        currentStarSize - bottomFromMargin, currentStarSize - tipVerticalMargin, // bottom right
        half, currentStarSize - innerCenterVerticalMargin,
        bottomFromMargin, currentStarSize - tipVerticalMargin, // bottom left
        innerBottomHorizontalMargin, innerBottomVerticalMargin
    };
  }

  /**
   * Calculates total width to occupy based on several parameters
   * @param starSize
   * @param numberOfStars
   * @param starsSeparation
   * @param padding
   * @return
   */
  private int calculateTotalWidth(float starSize, int numberOfStars, float starsSeparation, boolean padding) {
    return Math.round(starSize * numberOfStars + starsSeparation * (numberOfStars -1))
        +  (padding ? getPaddingLeft() + getPaddingRight() : 0);
  }

  /**
   * Calculates total height to occupy based on several parameters
   * @param starSize
   * @param numberOfStars
   * @param starsSeparation
   * @param padding
   * @return
   */
  private int calculateTotalHeight(float starSize, int numberOfStars, float starsSeparation, boolean padding) {
    return Math.round(starSize) + (padding ? getPaddingTop() + getPaddingBottom() : 0);
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);

    generateInternalCanvas(w, h);
  }

  /**
   * Generates internal canvas on which the ratingbar will be drawn.
   * @param w
   * @param h
   */
  private void generateInternalCanvas(int w, int h) {
    if (internalBitmap != null) {
      // avoid leaking memory after losing the reference
      internalBitmap.recycle();
    }

    if (w > 0 && h > 0) {
      // if width == 0 or height == 0 we don't need internal bitmap, cause view won't be drawn anyway.
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
      // don't draw view with width or height equal zero.
      return;
    }

    // clean internal canvas
    internalCanvas.drawColor(0, PorterDuff.Mode.CLEAR);

    // choose colors
    setupColorsInPaint();

    // draw stars
    if (gravity == Gravity.Left) {
      drawFromLeftToRight(internalCanvas);
    } else {
      drawFromRightToLeft(internalCanvas);
    }

    // draw view background color
    if (touchInProgress) {
      canvas.drawColor(pressedBackgroundColor);
    } else {
      canvas.drawColor(backgroundColor);
    }

    // draw internal bitmap to definite canvas
    canvas.drawBitmap(internalBitmap, 0, 0, null);
  }

  /**
   * Sets the color for the different paints depending on whether current state is pressed or normal.
   */
  private void setupColorsInPaint() {
    if (touchInProgress) {
      paintStarBorder.setColor(pressedBorderColor);
      paintStarFill.setColor(pressedFillColor);
      if (pressedFillColor != Color.TRANSPARENT) {
        paintStarFill.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
      } else {
        paintStarFill.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
      }
      paintStarBackground.setColor(pressedStarBackgroundColor);
      if (pressedStarBackgroundColor != Color.TRANSPARENT) {
        paintStarBackground.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
      } else {
        paintStarBackground.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
      }
    } else {
      paintStarBorder.setColor(borderColor);
      paintStarFill.setColor(fillColor);
      if (fillColor != Color.TRANSPARENT) {
        paintStarFill.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
      } else {
        paintStarFill.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
      }
      paintStarBackground.setColor(starBackgroundColor);
      if (starBackgroundColor != Color.TRANSPARENT) {
        paintStarBackground.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
      } else {
        paintStarBackground.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
      }
    }
  }

  /**
   * Draws the view when gravity is Left
   * @param internalCanvas
   */
  private void drawFromLeftToRight(Canvas internalCanvas) {
    float remainingTotalRating = rating;
    float startingX = starsDrawingSpace.left;
    float startingY = starsDrawingSpace.top;
    for (int i = 0; i < numberOfStars; i++) {
      if (remainingTotalRating >= 1) {
        drawStar(internalCanvas, startingX, startingY, 1f, Gravity.Left);
        remainingTotalRating -= 1;
      } else {
        drawStar(internalCanvas, startingX, startingY, remainingTotalRating, Gravity.Left);
        remainingTotalRating = 0;
      }
      startingX += starsSeparation + currentStarSize;
    }
  }

  /**
   * Draws the view when gravity is Right
   * @param internalCanvas
   */
  private void drawFromRightToLeft(Canvas internalCanvas) {
    float remainingTotalRating = rating;
    float startingX = starsDrawingSpace.right - currentStarSize;
    float startingY = starsDrawingSpace.top;
    for (int i = 0; i < numberOfStars; i++) {
      if (remainingTotalRating >= 1) {
        drawStar(internalCanvas, startingX, startingY, 1f, Gravity.Right);
        remainingTotalRating -= 1;
      } else {
        drawStar(internalCanvas, startingX, startingY, remainingTotalRating, Gravity.Right);
        remainingTotalRating = 0;
      }
      startingX -= starsSeparation + currentStarSize;
    }
  }

  /**
   * Draws a star in the provided canvas.
   * @param canvas
   * @param x left of the star
   * @param y top of the star
   * @param filled between 0 and 1
   * @param gravity Left or Right
   */
  private void drawStar(Canvas canvas, float x, float y, float filled, Gravity gravity) {
    // calculate fill in pixels
    float fill = currentStarSize * filled;

    // prepare path for star
    starPath.reset();
    starPath.moveTo(x + starVertex[0], y + starVertex[1]);
    for(int i = 2; i < starVertex.length; i=i+2) {
      starPath.lineTo(x + starVertex[i], y + starVertex[i+1]);
    }
    starPath.close();

    // draw star outline
    canvas.drawPath(starPath, paintStarOutline);

    // Note: below, currentStarSize*0.02f is a minor correction so the user won't see a vertical black line in between the fill and empty color
    if (gravity == Gravity.Left) {
      // color star fill
      canvas.drawRect(x, y, x + fill + currentStarSize *0.02f, y + currentStarSize, paintStarFill);
      // draw star background
      canvas.drawRect(x + fill, y, x + currentStarSize, y + currentStarSize, paintStarBackground);
    } else {
      // color star fill
      canvas.drawRect(x + currentStarSize - (fill+ currentStarSize *0.02f), y, x + currentStarSize, y + currentStarSize, paintStarFill);
      // draw star background
      canvas.drawRect(x, y, x + currentStarSize - fill, y + currentStarSize, paintStarBackground);
    }

    // draw star border on top
    if (drawBorderEnabled) {
      canvas.drawPath(starPath, paintStarBorder);
    }
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
          if (touchInProgress && ratingListener != null) {
            ratingListener.onRatingChanged(this, rating, true);
          }
          touchInProgress = false;
          return false;
        }
        break;
      case MotionEvent.ACTION_UP:
        setNewRatingFromTouch(event.getX(), event.getY());
        if (clickListener != null) {
          clickListener.onClick(this);
        }
      case MotionEvent.ACTION_CANCEL:
        if (ratingListener != null) {
          ratingListener.onRatingChanged(this, rating, true);
        }
        touchInProgress = false;
        break;

    }

    invalidate();
    return true;
  }

  /**
   * Assigns a rating to the touch event.
   * @param x
   * @param y
   */
  private void setNewRatingFromTouch(float x, float y) {
    // normalize x to inside starsDrawinSpace
    if (gravity != Gravity.Left) {
      x = getWidth() - x;
    }

    // we know that touch was inside starsTouchSpace, but it might be outside starsDrawingSpace
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

    // correct rating in case step size is present
    float mod = rating % stepSize;
    if (mod < stepSize/4) {
      rating = rating - mod;
      rating = Math.max(0, rating);
    } else {
      rating =  rating - mod + stepSize;
      rating = Math.min(numberOfStars, rating);
    }
  }
  
  @Override
  protected Parcelable onSaveInstanceState() {
      Parcelable superState = super.onSaveInstanceState();
      SavedState savedState = new SavedState(superState);
      savedState.rating = getRating();
      return savedState;
    }

  @Override
  protected void onRestoreInstanceState(Parcelable state) {
      SavedState savedState = (SavedState) state;
      super.onRestoreInstanceState(savedState.getSuperState());
      setRating(savedState.rating);
  }

  private static class SavedState extends BaseSavedState {
      public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {
          @Override
          public SavedState createFromParcel(Parcel parcel) {
              return new SavedState(parcel);
          }

          @Override
          public SavedState[] newArray(int size) {
              return new SavedState[size];
          }
      };
      private float rating = 0.0f;

      protected SavedState(Parcel source) {
          super(source);
          rating = source.readFloat();
      }

      @TargetApi(Build.VERSION_CODES.N)
      protected SavedState(Parcel source, ClassLoader loader) {
          super(source, loader);
      }

      protected SavedState(Parcelable superState) {
          super(superState);
      }

      @Override
      public void writeToParcel(Parcel out, int flags) {
          super.writeToParcel(out, flags);
          out.writeFloat(rating);
      }
  }

  /* ----------- GETTERS AND SETTERS ----------- */

  public float getRating(){
    return rating;
  }

  /**
   * Sets rating.
   * If provided value is less than 0, rating will be set to 0.
   * * If provided value is greater than numberOfStars, rating will be set to numberOfStars.
   * @param rating
   */
  public void setRating(float rating) {
    this.rating = normalizeRating(rating);
    // request redraw of the view
    invalidate();
    if (ratingListener != null && (ratingAnimator == null || !ratingAnimator.isRunning())) {
      ratingListener.onRatingChanged(this, rating, false);
    }
  }

  public float getStepSize() {
    return stepSize;
  }

  /**
   * Sets step size of rating.
   * Throws IllegalArgumentException if provided value is less or equal than zero.
   * @param stepSize
   */
  public void setStepSize(float stepSize) {
    this.stepSize = stepSize;
    if (stepSize <= 0) {
      throw new IllegalArgumentException(String.format("SimpleRatingBar initialized with invalid value for stepSize. Found %f, but should be greater than 0", stepSize));
    }
    // request redraw of the view
    invalidate();
  }

  public boolean isIndicator() {
    return isIndicator;
  }

  /**
   * Sets indicator property.
   * If provided value is true, touch events will be deactivated, and thus user interaction will be deactivated.
   * @param indicator
   */
  public void setIndicator(boolean indicator) {
    isIndicator = indicator;
    touchInProgress = false;
  }

  /**
   * Returns max star size in pixels.
   * @return
   */
  public float getMaxStarSize() {
    return maxStarSize;
  }

  /**
   * Returns max star size in the requested dimension.
   * @param dimen
   * @return
   */
  public float getMaxStarSize(@Dimension int dimen) {
    return valueFromPixels(maxStarSize, dimen);
  }

  /**
   * Sets maximum star size in pixels.
   * If current star size is less than provided value, this has no effect on the view.
   * @param maxStarSize
   */
  public void setMaxStarSize(float maxStarSize) {
    this.maxStarSize = maxStarSize;
    if (currentStarSize > maxStarSize) {
      // force re-calculating the layout dimension
      requestLayout();
      generateInternalCanvas(getWidth(), getHeight());
      // request redraw of the view
      invalidate();
    }
  }

  /**
   * Sets maximum star size using the given dimension.
   * If current star size is less than provided value, this has no effect on the view.
   * @param maxStarSize
   */
  public void setMaxStarSize(float maxStarSize, @Dimension int dimen) {
    setMaxStarSize(valueToPixels(maxStarSize, dimen));
  }

  /**
   * Return star size in pixels.
   * @return
   */
  public float getStarSize() {
    return currentStarSize;
  }

  /**
   * Return star size in the requested dimension.
   * @param dimen
   * @return
   */
  public float getStarSize(@Dimension int dimen) {
    return valueFromPixels(currentStarSize, dimen);
  }

  /**
   * Sets exact star size in pixels.
   * @param starSize
   */
  public void setStarSize(float starSize) {
    this.desiredStarSize = starSize;
    if (starSize != Integer.MAX_VALUE && maxStarSize != Integer.MAX_VALUE && starSize > maxStarSize) {
      Log.w("SimpleRatingBar", String.format("Initialized with conflicting values: starSize is greater than maxStarSize (%f > %f). I will ignore maxStarSize", starSize, maxStarSize));
    }
    // force re-calculating the layout dimension
    requestLayout();
    generateInternalCanvas(getWidth(), getHeight());
    // request redraw of the view
    invalidate();
  }

  /**
   * Sets exact star size using the given dimension.
   * @param starSize
   * @param dimen
   */
  public void setStarSize(float starSize, @Dimension int dimen) {
    setStarSize(valueToPixels(starSize, dimen));
  }

  /**
   * Returns stars separation in pixels.
   * @return
   */
  public float getStarsSeparation() {
    return starsSeparation;
  }

  /**
   * Returns stars separation in the requested dimension.
   * @param dimen
   * @return
   */
  public float getStarsSeparation(@Dimension int dimen) {
    return valueFromPixels(starsSeparation, dimen);
  }

  /**
   * Sets separation between stars in pixels.
   * @param starsSeparation
   */
  public void setStarsSeparation(float starsSeparation) {
    this.starsSeparation = starsSeparation;
    // force re-calculating the layout dimension
    requestLayout();
    generateInternalCanvas(getWidth(), getHeight());
    // request redraw of the view
    invalidate();
  }

  /**
   * Sets separation between stars using the given dimension.
   * @param starsSeparation
   */
  public void setStarsSeparation(float starsSeparation, @Dimension int dimen) {
    setStarsSeparation(valueToPixels(starsSeparation, dimen));
  }

  public int getNumberOfStars() {
    return numberOfStars;
  }

  /**
   * Sets number of stars.
   * It also sets the rating to zero.
   * Throws IllegalArgumentException if provided value is less or equal than zero.
   * @param numberOfStars
   */
  public void setNumberOfStars(int numberOfStars) {
    this.numberOfStars = numberOfStars;
    if (numberOfStars <= 0) {
      throw new IllegalArgumentException(String.format("SimpleRatingBar initialized with invalid value for numberOfStars. Found %d, but should be greater than 0", numberOfStars));
    }
    this.rating = 0;
    // force re-calculating the layout dimension
    requestLayout();
    generateInternalCanvas(getWidth(), getHeight());
    // request redraw of the view
    invalidate();
  }

  /**
   * Returns star border width in pixels.
   * @return
   */
  public float getStarBorderWidth() {
    return starBorderWidth;
  }

  /**
   * Returns star border width in the requested dimension.
   * @param dimen
   * @return
   */
  public float getStarBorderWidth(@Dimension int dimen) {
    return valueFromPixels(starBorderWidth, dimen);
  }

  /**
   * Sets border width of stars in pixels.
   * Throws IllegalArgumentException if provided value is less or equal than zero.
   * @param starBorderWidth
   */
  public void setStarBorderWidth(float starBorderWidth) {
    this.starBorderWidth = starBorderWidth;
    if (starBorderWidth <= 0) {
      throw new IllegalArgumentException(String.format("SimpleRatingBar initialized with invalid value for starBorderWidth. Found %f, but should be greater than 0",
          starBorderWidth));
    }
    paintStarBorder.setStrokeWidth(starBorderWidth);
    // request redraw of the view
    invalidate();
  }

  /**
   * Sets border width of stars using the given dimension.
   * Throws IllegalArgumentException if provided value is less or equal than zero.
   * @param starBorderWidth
   * @param dimen
   */
  public void setStarBorderWidth(float starBorderWidth, @Dimension int dimen) {
    setStarBorderWidth(valueToPixels(starBorderWidth, dimen));
  }

  /**
   * Returns start corner radius in pixels,
   * @return
   */
  public float getStarCornerRadius() {
    return starCornerRadius;
  }

  /**
   * Returns start corner radius in the requested dimension,
   * @param dimen
   * @return
   */
  public float getStarCornerRadius(@Dimension int dimen) {
    return valueFromPixels(starCornerRadius, dimen);
  }

  /**
   * Sets radius of star corner in pixels.
   * Throws IllegalArgumentException if provided value is less than zero.
   * @param starCornerRadius
     */
  public void setStarCornerRadius(float starCornerRadius) {
    this.starCornerRadius = starCornerRadius;
    if (starCornerRadius < 0) {
      throw new IllegalArgumentException(String.format("SimpleRatingBar initialized with invalid value for starCornerRadius. Found %f, but should be greater or equal than 0",
              starCornerRadius));
    }
    cornerPathEffect = new CornerPathEffect(starCornerRadius);
    paintStarBorder.setPathEffect(cornerPathEffect);
    paintStarOutline.setPathEffect(cornerPathEffect);
    // request redraw of the view
    invalidate();
  }

  /**
   * Sets radius of star corner using the given dimension.
   * Throws IllegalArgumentException if provided value is less than zero.
   * @param starCornerRadius
   * @param dimen
   */
  public void setStarCornerRadius(float starCornerRadius, @Dimension int dimen) {
    setStarCornerRadius(valueToPixels(starCornerRadius, dimen));
  }

  public @ColorInt int getBorderColor() {
    return borderColor;
  }

  /**
   * Sets border color of stars in normal state.
   * @param borderColor
   */
  public void setBorderColor(@ColorInt int borderColor) {
    this.borderColor = borderColor;
    // request redraw of the view
    invalidate();
  }

  public @ColorInt int getFillColor() {
    return fillColor;
  }

  /**
   * Sets fill color of stars in normal state.
   * @param fillColor
   */
  public void setFillColor(@ColorInt int fillColor) {
    this.fillColor = fillColor;
    // request redraw of the view
    invalidate();
  }

  public @ColorInt int getStarBackgroundColor() {
    return starBackgroundColor;
  }

  /**
   * Sets background color of stars in normal state.
   * @param starBackgroundColor
   */
  public void setStarBackgroundColor(@ColorInt int starBackgroundColor) {
    this.starBackgroundColor = starBackgroundColor;
    // request redraw of the view
    invalidate();
  }

  public @ColorInt int getPressedBorderColor() {
    return pressedBorderColor;
  }

  /**
   * Sets border color of stars in pressed state.
   * @param pressedBorderColor
   */
  public void setPressedBorderColor(@ColorInt int pressedBorderColor) {
    this.pressedBorderColor = pressedBorderColor;
    // request redraw of the view
    invalidate();
  }

  public @ColorInt int getPressedFillColor() {
    return pressedFillColor;
  }

  /**
   * Sets fill color of stars in pressed state.
   * @param pressedFillColor
   */
  public void setPressedFillColor(@ColorInt int pressedFillColor) {
    this.pressedFillColor = pressedFillColor;
    // request redraw of the view
    invalidate();
  }

  public @ColorInt int getPressedStarBackgroundColor() {
    return pressedStarBackgroundColor;
  }

  /**
   * Sets background color of stars in pressed state.
   * @param pressedStarBackgroundColor
   */
  public void setPressedStarBackgroundColor(@ColorInt int pressedStarBackgroundColor) {
    this.pressedStarBackgroundColor = pressedStarBackgroundColor;
    // request redraw of the view
    invalidate();
  }

  public Gravity getGravity() {
    return gravity;
  }

  /**
   * Sets gravity of fill.
   * @param gravity
   */
  public void setGravity(Gravity gravity) {
    this.gravity = gravity;
    // request redraw of the view
    invalidate();
  }

  public boolean isDrawBorderEnabled() {
    return drawBorderEnabled;
  }

  /**
   * Sets drawBorder property.
   * If provided value is true, border will be drawn, otherwise it will be omithed.
   * @param drawBorderEnabled
   */
  public void setDrawBorderEnabled(boolean drawBorderEnabled) {
    this.drawBorderEnabled = drawBorderEnabled;
    // request redraw of the view
    invalidate();
  }

  /**
   * Convenience method to convert a value in the given dimension to pixels.
   * @param value
   * @param dimen
   * @return
   */
  private float valueToPixels(float value, @Dimension int dimen) {
    switch (dimen) {
      case Dimension.DP:
        return applyDimension(COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
      case Dimension.SP:
        return applyDimension(COMPLEX_UNIT_SP, value, getResources().getDisplayMetrics());
      default:
        return value;
    }
  }

  /**
   * Convenience method to convert a value from pixels to the given dimension.
   * @param value
   * @param dimen
   * @return
   */
  private float valueFromPixels(float value, @Dimension int dimen) {
    switch (dimen) {
      case Dimension.DP:
        return value / getResources().getDisplayMetrics().density;
      case Dimension.SP:
        return value / getResources().getDisplayMetrics().scaledDensity;
      default:
        return value;
    }
  }

  /**
   * Sets rating with animation.
   * @param builder
   */
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
        if (ratingListener != null) {
          ratingListener.onRatingChanged(SimpleRatingBar.this, rating, false);
        }
      }

      @Override
      public void onAnimationCancel(Animator animator) {
        if (ratingListener != null) {
          ratingListener.onRatingChanged(SimpleRatingBar.this, rating, false);
        }
      }

      @Override
      public void onAnimationRepeat(Animator animator) {
        if (ratingListener != null) {
          ratingListener.onRatingChanged(SimpleRatingBar.this, rating, false);
        }
      }
    });
    ratingAnimator.start();
  }

  /**
   * Returns a new AnimationBuilder.
   * @return
   */
  public AnimationBuilder getAnimationBuilder() {
    return new AnimationBuilder(this);
  }

  /**
   * Normalizes rating passed by argument between 0 and numberOfStars.
   * @param rating
   * @return
   */
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

  /**
   * Sets OnClickListener.
   * @param listener
   */
  @Override public void setOnClickListener(OnClickListener listener) {
    this.clickListener = listener;
  }

  /**
   * Sets OnRatingBarChangeListener.
   * @param listener
   */
  public void setOnRatingBarChangeListener(OnRatingBarChangeListener listener) {
    this.ratingListener = listener;
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

  /**
   * Helper class to build rating animation.
   * Provides good defaults:
   * - Target rating: numberOfStars
   * - Animation: Bounce
   * - Duration: 2s
   */
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

    /**
     * Sets duration of animation.
     * @param duration
     * @return
     */
    public AnimationBuilder setDuration(long duration) {
      this.duration = duration;
      return this;
    }

    /**
     * Sets interpolator for animation.
     * @param interpolator
     * @return
     */
    public AnimationBuilder setInterpolator(Interpolator interpolator) {
      this.interpolator = interpolator;
      return this;
    }

    /**
     * Sets rating after animation has ended.
     * @param ratingTarget
     * @return
     */
    public AnimationBuilder setRatingTarget(float ratingTarget) {
      this.ratingTarget = ratingTarget;
      return this;
    }

    /**
     * Sets repeat count for animation.
     * @param repeatCount must be a positive value or ValueAnimator.INFINITE
     * @return
     */
    public AnimationBuilder setRepeatCount(int repeatCount) {
      this.repeatCount = repeatCount;
      return this;
    }

    /**
     * Sets repeat mode for animation.
     * @param repeatMode must be ValueAnimator.RESTART or ValueAnimator.REVERSE
     * @return
     */
    public AnimationBuilder setRepeatMode(int repeatMode) {
      this.repeatMode = repeatMode;
      return this;
    }

    /**
     * Sets AnimatorListener.
     * @param animatorListener
     * @return
     */
    public AnimationBuilder setAnimatorListener(AnimatorListener animatorListener) {
      this.animatorListener = animatorListener;
      return this;
    }

    /**
     * Starts animation.
     */
    public void start() {
      ratingBar.animateRating(this);
    }
  }
}
