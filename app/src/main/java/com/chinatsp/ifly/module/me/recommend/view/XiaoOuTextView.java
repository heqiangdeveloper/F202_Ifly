package com.chinatsp.ifly.module.me.recommend.view;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chinatsp.ifly.R;
import com.chinatsp.ifly.SettingsActivity;

/**
 * ClassName: //TODO
 * Function: //小欧消息内容显示
 * Reason: //TODO
 *
 * @author: qlf
 * @version: v1.0
 * @date : 2020/7/2
 */

public class XiaoOuTextView extends LinearLayout{
    private Context mContext;
    private TextView xiaoou_text;
    public XiaoOuTextView(Context context,String textContent) {
        super(context);
        initView(textContent);
        mContext = context;
    }

    private void initView(String textContent) {
        inflate(getContext(), R.layout.xiaoou_text_floatview, this);
        xiaoou_text = (TextView) findViewById(R.id.xiaoou_text);
        xiaoou_text.setText(textContent);
        xiaoou_text.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, SettingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        });
    }
}
