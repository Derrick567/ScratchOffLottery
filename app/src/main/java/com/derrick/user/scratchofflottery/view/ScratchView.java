package com.derrick.user.scratchofflottery.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.derrick.user.scratchofflottery.R;

/**
 * Created by user on 2015/9/3.
 */
public class ScratchView extends View {
    private static String TAG = "ScratchView";
    private Paint mOutterPaint;
    private Paint mCoverPaint;
    private Path mPath;
    private Canvas mCanvas;
    private Bitmap mBitmap;

    private int mLastX;
    private int mLastY;

    private Bitmap mOutterBitmap;
    //--------------------------------------------------
    //private Bitmap bitmap ;
    private String mText;
    private Paint mBackPaint;
    //記錄刮獎結果的文字框寬高
    private Rect mTextBound;
    private int mTextSize;
    private int mTextColor;
    private volatile boolean mComplete = false;


    //when scratch off callback
    public interface OnScratchCompleteListener {
        void complete();

    }

    private OnScratchCompleteListener mListener;

    public void setOnScratchCompleteListener(OnScratchCompleteListener mListener) {
        this.mListener = mListener;
    }

    public ScratchView(Context context) {
        this(context, null);
    }

    public ScratchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScratchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        TypedArray array = null;
        try {
            array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ScratchView, defStyleAttr, 0);
            int n = array.getIndexCount();
            for (int i = 0; i < n; i++) {
                int attr = array.getIndex(i);
                switch (attr) {
                    case R.styleable.ScratchView_text:
                        mText = array.getString(attr);
                        break;
                    case R.styleable.ScratchView_textSize:
                        //this text size is px unit!
                        mTextSize = array.getDimensionPixelSize(attr,
                                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 22, getResources().getDisplayMetrics()));
                        Log.d(TAG, "mTextSize=" + mTextSize);
                        break;
                    case R.styleable.ScratchView_textColor:
                        mTextColor = array.getColor(attr, 0x0000);
                        break;
                }
            }
        } finally {

            if (array != null)
                array.recycle();
        }


    }
    public void setText(String mText){
        this.mText = mText;
        mBackPaint.getTextBounds(mText, 0, mText.length(), mTextBound);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        setupOutterPaint();
        setupBackPaint();
        setupmCoverPaint();
        //mCanvas.drawColor(Color.parseColor("#c0c0c0"));
        mCanvas.drawRoundRect(new RectF(0, 0, width, height), 30, 30, mCoverPaint);
        mCanvas.drawBitmap(mOutterBitmap, null, new Rect(0, 0, width, height), null);

    }

    private void setupmCoverPaint() {
        mCoverPaint.setColor(Color.parseColor("#c0c0c0"));
        mCoverPaint.setAntiAlias(true);
        mCoverPaint.setDither(true);
        // mCoverPaint.setStrokeJoin(Paint.Join.ROUND);
        // mCoverPaint.setStrokeCap(Paint.Cap.ROUND);
        mCoverPaint.setStyle(Paint.Style.FILL);
        // mCoverPaint.setStrokeWidth(20);
    }

    //設置刮獎結果文字畫筆的屬性
    private void setupBackPaint() {
        mBackPaint.setColor(mTextColor);
        mBackPaint.setStyle(Paint.Style.FILL);
        mBackPaint.setTextSize(mTextSize);
        mBackPaint.getTextBounds(mText, 0, mText.length(), mTextBound);
    }

    private void setupOutterPaint() {
        //set properties of paint
        mOutterPaint.setColor(Color.parseColor("#c0c0c0"));
        mOutterPaint.setAntiAlias(true);
        mOutterPaint.setDither(true);
        mOutterPaint.setStrokeJoin(Paint.Join.ROUND);
        mOutterPaint.setStrokeCap(Paint.Cap.ROUND);
        mOutterPaint.setStyle(Paint.Style.STROKE);
        mOutterPaint.setStrokeWidth(60);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastX = x;
                mLastY = y;
                mPath.moveTo(mLastX, mLastY);
                break;

            case MotionEvent.ACTION_MOVE:
                int dx = Math.abs(x - mLastX);
                int dy = Math.abs(y - mLastY);

                //滑動夠大才進行處理
                if (dx > 3 || dy > 3) {
                    mPath.lineTo(x, y);
                }
                mLastX = x;
                mLastY = y;
                break;

            case MotionEvent.ACTION_UP:
                new Thread(mRunnable).start();
                break;
            default:
                break;
        }
        invalidate();
        return true;
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            int w = getWidth();
            int h = getHeight();
            float wipeArea = 0;
            float totalArea = w * h;
            Bitmap bitmap = mBitmap;
            int[] mPixels = new int[w * h];
            bitmap.getPixels(mPixels, 0, w, 0, 0, w, h);
            for (int i = 0; i < mPixels.length; i++) {
                if (mPixels[i] == 0) {
                    wipeArea++;
                }
            }
            if (wipeArea > 0 && totalArea > 0) {
                int percent = (int) (wipeArea * 100 / totalArea);
                Log.d("TAG", percent + "");
                if (percent > 60) {
                    //clear cover layer area
                    mComplete = true;
                    postInvalidate();


                }
            }
        }
    };


    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw()");
        // canvas.drawBitmap(bitmap, 0, 0, null);
        //文字是從左下角開始繪製， 注意y軸起點
        canvas.drawText(mText, (getWidth() - mTextBound.width()) / 2, getHeight() / 2 + mTextBound.height() / 2, mBackPaint);
        if (!mComplete) {
            canvas.drawBitmap(mBitmap, 0, 0, null);
            drawPath();
        } else {
            if (mListener != null)
                mListener.complete();
        }
    }

    private void drawPath() {
        mOutterPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        mCanvas.drawPath(mPath, mOutterPaint);
    }

    private void init() {
        mOutterPaint = new Paint();
        mCoverPaint = new Paint();
        mPath = new Path();
        //bitmap= BitmapFactory.decodeResource(getResources(), R.mipmap.t2);
        mOutterBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.fg_guaguaka);
        mText = "謝謝惠顧";
        mTextBound = new Rect();
        mBackPaint = new Paint();
        mTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 22, getResources().getDisplayMetrics());
    }
}
