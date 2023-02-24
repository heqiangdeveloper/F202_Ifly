package com.chinatsp.ifly.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.chinatsp.ifly.R;

public class ClearEditText extends RelativeLayout {
    private EditText mEdittext;
    private LinearLayout mImageWrapper;
    private String hint;

    public ClearEditText(Context context) {
        this(context, null);
    }

    public ClearEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClearEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ClearEditText, defStyleAttr, 0);
        hint = a.getString(R.styleable.ClearEditText_hint);

        a.recycle();
        LayoutInflater.from(context).inflate(R.layout.layout_clear_edittext, this, true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mEdittext = findViewById(R.id.et_edit_content);
        mEdittext.setHint(hint);
        mImageWrapper = findViewById(R.id.ll_edit_image_wrapper);
        //设置事件监听
        mImageWrapper.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mEdittext.getEditableText().clear();
                //mEdittext.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
            }
        });

        //添加内容变化监听
        mEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count,int after) {
                //Log.d("xyj", "输入前字符串 [ " + text.toString() + " ]起始光标 [ " + start + " ]结束偏移量  [" + after + " ]");
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int count,int after) {
                //Log.d("xyj", "输入后字符串 [ " + text.toString() + " ] 起始光标 [ " + start + " ] 输入数量 [ " + count + " ]");
                //内容不为空的时候显示清除按钮
//                if (!TextUtils.isEmpty(charSequence)) {
//                    mImageWrapper.setVisibility(VISIBLE);
//                } else {
//                    mImageWrapper.setVisibility(INVISIBLE);
//                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //Log.d("xyj", "输入结束后的内容为 [" + editable.toString() + "] 即将显示在屏幕上");
                if(mInputTextListener != null) {
                    mInputTextListener.afterTextChanged(editable.toString());
                }
            }
        });
    }

    public String getText(){
        return mEdittext == null?"":mEdittext.getText().toString().trim();
    }

    public String getHint() {
        return mEdittext == null ? "" : mEdittext.getHint().toString().trim();
    }

    public void setText(String text){
        mEdittext.setText(text);
    }

    public void setHint(String text) {
        mEdittext.setHint(text);
    }

    private InputTextListener mInputTextListener;
    public void setInputTextListener(InputTextListener listener) {
        this.mInputTextListener = listener;
    }

    public interface InputTextListener {
        void afterTextChanged(String text);
    }
}