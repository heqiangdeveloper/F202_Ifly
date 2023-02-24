package com.chinatsp.ifly.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chinatsp.ifly.R;

public class PageNumIndicator extends RelativeLayout implements View.OnClickListener {

    private ImageView ivLastPage;
    private ImageView ivNextPage;
    private TextView tvPageNum;
    private TextView tvPageTotal;
    private int mTotalPage;
    private InnerOnClickListener listener;

    public void setInnerOnClickListener(InnerOnClickListener listener) {
        this.listener = listener;
    }

    public PageNumIndicator(Context context) {
        this(context, null);
    }

    public PageNumIndicator(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageNumIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.layout_pagenum_indicator, this, true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        ivLastPage = findViewById(R.id.iv_last_page);
        ivLastPage.setOnClickListener(this);
        ivNextPage = findViewById(R.id.iv_next_page);
        ivNextPage.setOnClickListener(this);

        tvPageNum = findViewById(R.id.tv_pagenum);
        tvPageTotal = findViewById(R.id.tv_page_total);
    }

    public void setPageTotal(int pageTotal) {
        tvPageTotal.setText(String.format("%02d", pageTotal));
        mTotalPage = pageTotal;
    }

    public void setPageNum(int pageNum) {
        tvPageNum.setText(String.format("%02d", (pageNum + 1)));

        if(mTotalPage == 0) {
            Toast.makeText(getContext(), "总页数为0", Toast.LENGTH_SHORT).show();
        } else if(mTotalPage == 1) {
            ivLastPage.setEnabled(false);
            ivNextPage.setEnabled(false);
        } else {
            if (pageNum == 0) {
                ivLastPage.setEnabled(false);
                ivNextPage.setEnabled(true);
            } else if(pageNum + 1 == mTotalPage) {
                ivLastPage.setEnabled(true);
                ivNextPage.setEnabled(false);
            } else {
                ivLastPage.setEnabled(true);
                ivNextPage.setEnabled(true);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_last_page:
                if (listener != null) {
                    listener.onLast();
                }
                break;
            case R.id.iv_next_page:
                if (listener != null) {
                    listener.onNext();
                }
                break;
        }
    }

    public interface InnerOnClickListener {
        void onLast();
        void onNext();
    }
}
