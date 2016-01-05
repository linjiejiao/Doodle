package cn.ljj.doodle;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class TextPropertyDialog extends Dialog implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private TextProperty mProperty;
    private Context mContext;
    private OnPropertyChangedListener mListener;

    private TextView mTextSize;
    private TextView mTextRotation;
    private TextView mTextColor;
    private SeekBar mSeekSize;
    private SeekBar mSeekRotation;
    private SeekBar mSeekColorRed;
    private SeekBar mSeekColorGreen;
    private SeekBar mSeekColorBlue;
    private Button mBtnOk;
    private Button mBtnCancel;

    public TextPropertyDialog(Context context, TextProperty property) {
        super(context, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        setContentView(R.layout.dialog_text_property);
        mContext = context;
        setTitle(R.string.text_property_dialog_title);
        if (property == null) {
            mProperty = new TextProperty();
        } else {
            mProperty = property;
        }
        initViews();
        setupProperties(true);
    }

    private void initViews() {
        mTextSize = (TextView) findViewById(R.id.text_size);
        mTextRotation = (TextView) findViewById(R.id.text_rotation);
        mTextColor = (TextView) findViewById(R.id.text_color);
        mSeekSize = (SeekBar) findViewById(R.id.seek_text_size);
        mSeekSize.setOnSeekBarChangeListener(this);
        mSeekRotation = (SeekBar) findViewById(R.id.seek_text_rotation);
        mSeekRotation.setOnSeekBarChangeListener(this);
        mSeekColorRed = (SeekBar) findViewById(R.id.seek_text_color_red);
        mSeekColorRed.setOnSeekBarChangeListener(this);
        mSeekColorGreen = (SeekBar) findViewById(R.id.seek_text_color_green);
        mSeekColorGreen.setOnSeekBarChangeListener(this);
        mSeekColorBlue = (SeekBar) findViewById(R.id.seek_text_color_blue);
        mSeekColorBlue.setOnSeekBarChangeListener(this);
        mBtnOk = (Button) findViewById(R.id.btn_ok);
        mBtnOk.setOnClickListener(this);
        mBtnCancel = (Button) findViewById(R.id.btn_cancel);
        mBtnCancel.setOnClickListener(this);
    }

    private void setupProperties(boolean updateProgress) {
        mTextSize.setText(mContext.getString(R.string.text_size) + mProperty.textSize);
        mTextRotation.setText(mContext.getString(R.string.text_rotation, mProperty.textRotation));
        String colorStr = mProperty.textColorRed + "," + mProperty.textColorGreen + "," + mProperty.textColorBlue;
        mTextColor.setText(mContext.getString(R.string.text_color_r_g_b, colorStr));
        mTextColor.setTextColor(mProperty.getColorInt());
        if (updateProgress) {
            mSeekSize.setProgress(mProperty.textSize * 2);
            mSeekRotation.setProgress(mProperty.textRotation * 100 / 360);
            mSeekColorRed.setProgress(mProperty.textColorRed * 100 / 256);
            mSeekColorGreen.setProgress(mProperty.textColorGreen * 100 / 256);
            mSeekColorBlue.setProgress(mProperty.textColorBlue * 100 / 256);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                if (mListener != null) {
                    mListener.onPropertyChanged(mProperty);
                }
                dismiss();
                break;
            case R.id.btn_cancel:
                dismiss();
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.seek_text_size:
                mProperty.textSize = progress / 2;
                break;
            case R.id.seek_text_rotation:
                mProperty.textRotation = progress * 360 / 100;
                if(mProperty.textRotation > 360){
                    mProperty.textRotation = 360;
                }
                break;
            case R.id.seek_text_color_red:
                mProperty.textColorRed = progress * 256 / 100;
                if(mProperty.textColorRed > 255){
                    mProperty.textColorRed = 255;
                }
                break;
            case R.id.seek_text_color_green:
                mProperty.textColorGreen = progress * 256 / 100;
                if(mProperty.textColorGreen > 255){
                    mProperty.textColorGreen = 255;
                }
                break;
            case R.id.seek_text_color_blue:
                mProperty.textColorBlue = progress * 256 / 100;
                if(mProperty.textColorBlue > 255){
                    mProperty.textColorBlue = 255;
                }
                break;
        }
        setupProperties(false);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public class TextProperty {
        int textSize = 20;
        int textRotation = 0;
        int textColorRed = 0;
        int textColorGreen = 0;
        int textColorBlue = 0;

        public int getColorInt() {
            return 0xff000000
                    + (textColorRed * 256 * 256)
                    + (textColorGreen * 256)
                    + textColorBlue;
        }

        @Override
        public String toString() {
            return "TextProperty{" +
                    "textSize=" + textSize +
                    ", textRotation=" + textRotation +
                    ", textColorRed=" + textColorRed +
                    ", textColorGreen=" + textColorGreen +
                    ", textColorBlue=" + textColorBlue +
                    ", getColorInt()=" + getColorInt() +
                    '}';
        }
    }

    public void setOnPropertyChangedListener(OnPropertyChangedListener listener) {
        mListener = listener;
    }

    interface OnPropertyChangedListener {
        void onPropertyChanged(TextProperty property);
    }
}
