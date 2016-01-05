package cn.ljj.doodle;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends Activity implements View.OnClickListener, TextWatcher, TransformTextView.OnTextStateChangeListener {
    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String IMAGE_DIRECTORY = Environment.getExternalStorageDirectory().getPath() + "/.drawing_temp/";
    private static final int REQ_CODE_GET_IMG = 1;
    private FrameLayout mParent;
    private ImageView mBackgroundLayer;
    private EditText mEditText;
    private Button mBtnAddText;
    private Button mBtnsetBackground;
    private Button mBtnOK;
    private TransformTextView mCurrentText = null;
    private ProgressDialog mProgressDialog = null;

    private String getImgPath() {
        return Environment.getExternalStorageDirectory().getPath() + "/.drawing_temp/temp.png";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mParent = (FrameLayout) findViewById(R.id.fl_parent);
        mBackgroundLayer = (ImageView) findViewById(R.id.img_background);
        mEditText = (EditText) findViewById(R.id.edit_text);
        mBtnAddText = (Button) findViewById(R.id.btn_add_text);
        mBtnOK = (Button) findViewById(R.id.btn_ok);
        mBtnsetBackground = (Button) findViewById(R.id.btn_set_bg);
        mBtnsetBackground.setOnClickListener(this);
        mBtnAddText.setOnClickListener(this);
        mBtnOK.setOnClickListener(this);
        mEditText.addTextChangedListener(this);
        Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();
            if (Intent.ACTION_SEND.equals(action)) {
                Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (uri != null) {
                    setBackground(uri);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_text:
                String content = mEditText.getText().toString();
                if (TextUtils.isEmpty(content)) {
                    return;
                }
                addNewText(content);
                break;
            case R.id.btn_set_bg:
                try {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, REQ_CODE_GET_IMG);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_ok:
                showWaitingDialog(true);
                new SaveAndShareTask().execute();
                break;
        }
    }

    private boolean openExactActivity(Uri uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/*");
            intent.setComponent(ComponentName.unflattenFromString("com.tencent.mm/.ui.tools.ShareImgUI"));
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void openChooserActivity(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(intent, getTitle()));
    }

    private void addNewText(String content) {
        FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        TransformTextView text = new TransformTextView(this);
        text.setText(content);
        text.setOnTextStateChangeListener(this);
        mParent.addView(text, param);
        mCurrentText = text;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_GET_IMG) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                setBackground(uri);
            }
        }
    }

    private void setBackground(Uri uri) {
        ContentResolver cr = getContentResolver();
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
            mBackgroundLayer.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            Log.e("Exception", e.getMessage(), e);
        }
    }

    private String saveToFile() {
        FileOutputStream fOps = null;
        try {
            String path = getImgPath();
            File file = new File(IMAGE_DIRECTORY);
            if (!file.exists()) {
                file.mkdirs();
                file = new File(path);
                file.createNewFile();
            }
            fOps = new FileOutputStream(path);
            loadBitmapFromView(mParent).compress(Bitmap.CompressFormat.PNG, 80, fOps);
            return path;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fOps != null) {
                try {
                    fOps.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private Bitmap loadBitmapFromView(View v) {
        Bitmap bmp = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        c.translate(-v.getScrollX(), -v.getScrollY());
        v.draw(c);
        return bmp;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (mCurrentText != null) {
            mCurrentText.setText(s);
        }
    }

    @Override
    public void onTextDeleted(TransformTextView deleteView) {
        mParent.removeView(deleteView);
    }

    @Override
    public void onTextFocused(TransformTextView focusView) {
        mCurrentText = focusView;
        mEditText.setText(mCurrentText.getText());
    }

    class SaveAndShareTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            return saveToFile();
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            String path = (String) o;
            File f = new File(path);
            if (f != null && f.exists() && f.isFile()) {
                Uri uri = Uri.fromFile(f);
//                if (!openExactActivity(uri)) {
                    openChooserActivity(uri);
//                }
            }
            showWaitingDialog(false);
        }
    }

    private void showWaitingDialog(boolean show) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        }
        if (show) {
            mProgressDialog.show();
        } else {
            mProgressDialog.dismiss();
        }
    }
}
