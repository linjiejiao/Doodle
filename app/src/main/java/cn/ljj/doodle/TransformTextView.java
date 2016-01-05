package cn.ljj.doodle;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.TextView;

public class TransformTextView extends TextView implements TextPropertyDialog.OnPropertyChangedListener {
    private int mTouchState = MotionEvent.ACTION_UP;
    private int mLeftMargin = 0;
    private int mTopMargin = 0;
    private float mLastX = 0;
    private float mLastY = 0;
    private static final long DOUBLE_CLICK_INTERVAL = 300;
    private boolean mClicked = false;
    private TextPropertyDialog mTextPropertyDialog = null;
    private TextPropertyDialog.TextProperty mTextProperty = null;
    private OnTextStateChangeListener mOnTextStateChangeListener = null;

    private Runnable mDoubleClickRunnable = new Runnable() {
        @Override
        public void run() {
            mClicked = false;
        }
    };

    public TransformTextView(Context context) {
        super(context);
        setBackground(null);
        setTextSize(20);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastX = event.getX();
                mLastY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                if (checkDoubleClick()) {
                    showTextPropertyDialog(true);
                }
                ;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mTouchState != MotionEvent.ACTION_UP) {
                    float deltaX = event.getX() - mLastX;
                    float deltaY = event.getY() - mLastY;
                    mLeftMargin += deltaX;
                    mTopMargin += deltaY;
                    updatePosition();
                }
                break;
        }
        mTouchState = event.getAction();
        return true;
    }

    private boolean checkDoubleClick() {
        if (mClicked) {
            return true;
        }
        mClicked = true;
        getHandler().removeCallbacks(mDoubleClickRunnable);
        getHandler().postDelayed(mDoubleClickRunnable, DOUBLE_CLICK_INTERVAL);
        notifyFocused();
        return false;
    }

    private void updatePosition() {
        FrameLayout.LayoutParams param = (FrameLayout.LayoutParams) getLayoutParams();
        param.setMargins(mLeftMargin, mTopMargin, 0, 0);
        setLayoutParams(param);
    }

    private void showTextPropertyDialog(boolean show) {
        if (mTextPropertyDialog == null) {
            mTextPropertyDialog = new TextPropertyDialog(getContext(), mTextProperty);
            mTextPropertyDialog.setOnPropertyChangedListener(this);
        }
        if (show) {
            mTextPropertyDialog.show();
        } else {
            mTextPropertyDialog.dismiss();
        }
    }

    @Override
    public void onPropertyChanged(TextPropertyDialog.TextProperty property) {
        mTextProperty = property;
        setTextSize(mTextProperty.textSize);
        setTextColor(mTextProperty.getColorInt());
        setRotation(mTextProperty.textRotation);
    }

    public void setOnTextStateChangeListener(OnTextStateChangeListener listener){
        mOnTextStateChangeListener = listener;
    }

    private void notifyFocused(){
        if(mOnTextStateChangeListener != null){
            mOnTextStateChangeListener.onTextFocused(this);
        }
    }

    private void notifyDeleted(){
        if(mOnTextStateChangeListener != null){
            mOnTextStateChangeListener.onTextDeleted(this);
        }
    }

    interface OnTextStateChangeListener {
        void onTextDeleted(TransformTextView deleteView);

        void onTextFocused(TransformTextView focusView);
    }
}
