package com.example.lsl.cameratoollsl.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.example.lsl.cameratoollsl.SimpleValueAnimator;
import com.example.lsl.cameratoollsl.utils.ScreenUtils;


/**
 * Created by lsl on 17-10-15.
 */

public class CameraPreView extends SurfaceView {
    private Context mContext;

    /**
     * 画笔对象
     */
    private Paint mPaint;
    /**
     * 裁剪区域
     */
    private Rect mRect;

    private Rect mCurrentRect;
    /**
     * 裁剪模式
     */
    private CropMode mCropMode = CropMode.NORMAL;

    private float viewH, viewW;//预览界面的长宽

    private final String TAG = "info------>surface";
    private int CropWidth;
    private int CropHeigth;

    private int centerX, centerY;
    private int raduis;

    private SimpleValueAnimator mAnimator;
    private final Interpolator DEFAULT_INTERPOLATOR = new DecelerateInterpolator();
    private Interpolator mInterpolator = DEFAULT_INTERPOLATOR;
    private boolean mIsAnimating = false;


    public CameraPreView(Context context) {
        this(context, null, 0);
    }

    public CameraPreView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraPreView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initPaint();
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setColor(Color.GREEN);
        mPaint.setStrokeWidth(3f);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);

        initdata();
    }

    private void initdata() {
        CropWidth = ScreenUtils.dp2px(mContext, 100f);
        CropHeigth = CropWidth;

        raduis = (int) (CropWidth * 0.7);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);

        viewW = w - getPaddingLeft() - getPaddingRight();
        viewH = h - getPaddingTop() - getPaddingBottom();

        Log.e(TAG, "viewW:" + viewW + " viewH" + viewH);

        centerX = (int) (viewW / 2);
        centerY = (int) (viewH / 2);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        setLayout();
    }

    /**
     * 设置布局位置
     */
    private void setLayout() {
        if (CropWidth <= 0 || CropHeigth <= 0) {
            return;
        }
        int left = centerX - CropWidth;
        int top = centerY - CropHeigth;
        int rigth = centerX + CropWidth;
        int bottom = centerY + CropHeigth;
        mRect = new Rect(left, top, rigth, bottom);
        mCurrentRect = mRect;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawFrame(canvas);
    }

    private void drawFrame(Canvas canvas) {
        if (mCropMode == CropMode.NORMAL) clearCanvas(canvas);
        if (mCropMode == CropMode.RECTANGLE) drawRectangle(canvas);
        if (mCropMode == CropMode.SQUARE) drawRectangle(canvas);
        if (mCropMode == CropMode.CIRCLE) drawCircle(canvas);
    }


    /**
     * 画长方形
     *
     * @param canvas
     */
    public void drawRectangle(Canvas canvas) {
        canvas.drawRect(mCurrentRect, mPaint);
    }

    /**
     * 清除画布
     *
     * @param canvas
     */
    public void clearCanvas(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }

    /**
     * 画圆形
     *
     * @param canvas
     */
    public void drawCircle(Canvas canvas) {
        int left = centerX - raduis;
        int top = centerY - raduis;
        int right = left + raduis;
        int bottom = top + raduis;
        mCurrentRect = new Rect(left, top, right, bottom);
        canvas.drawCircle(centerX, centerY, raduis, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                int x = (int) event.getX();
                int y = (int) event.getY();
                changePoint(x, y);
                break;
        }
        return true;
    }

    /**
     * 改变中心点
     *
     * @param x x坐标
     * @param y y坐标
     */
    private void changePoint(int x, int y) {
        centerX = x;
        centerY = y;
        if (mCropMode == CropMode.SQUARE || mCropMode == CropMode.RECTANGLE) {
            setLayout();
        }
        invalidate();
    }


    /**
     * 裁剪模式RECTANGLE 长方形，SQUARE正方形，CIRCLE圆形,NORMAL 无
     */
    public enum CropMode {
        RECTANGLE(0), SQUARE(1), CIRCLE(2), NORMAL(3);
        private final int ID;

        CropMode(final int id) {
            this.ID = id;
        }

        public int getId() {
            return ID;
        }
    }

    /**
     * 设置拍照裁剪模式
     *
     * @param mode
     */
    public void setCropMode(CropMode mode) {
        this.mCropMode = mode;
        centerX = (int) (viewW / 2);
        centerY = (int) (viewH / 2);
        initdata();
        if (this.mCropMode == CropMode.SQUARE) {
            setLayout();
        }
        if (this.mCropMode == CropMode.RECTANGLE) {
            CropHeigth = (int) (CropWidth * 0.8);
            setLayout();
        }
        invalidate();
//        recalculateFrameRect(100);
    }

    /**
     * 获取当前模式
     * @return
     */
    public CropMode getCropMode() {
        return mCropMode;
    }

    /**
     * 获取矩形区域
     *
     * @return
     */
    public Rect getRect() {
        return mCurrentRect;
    }


    public void zoomOut() {
        if (mCropMode == CropMode.CIRCLE) {
            raduis += 4;
        }
        if (mCropMode == CropMode.SQUARE) {
            CropWidth += 3;
            CropHeigth += 3;
            setLayout();
        }

        if (mCropMode == CropMode.RECTANGLE) {
            CropWidth += 4;
            CropHeigth += 2;
            setLayout();
        }
        invalidate();
    }

    public void zoomIn() {
        if (mCropMode == CropMode.CIRCLE) {
            raduis -= 4;
        }
        if (mCropMode == CropMode.SQUARE) {
            CropWidth -= 3;
            CropHeigth -= 3;
            setLayout();
        }

        if (mCropMode == CropMode.RECTANGLE) {
            CropWidth -= 4;
            CropHeigth -= 2;
            setLayout();
        }
        invalidate();
    }

}
