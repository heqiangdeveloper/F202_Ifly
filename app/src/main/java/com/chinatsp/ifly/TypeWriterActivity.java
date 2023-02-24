package com.chinatsp.ifly;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.adapter.sr.SRAgent;
import com.iflytek.speech.libisssr;

/**
 * 打字机功能测试
 */
public class TypeWriterActivity extends AppCompatActivity implements View.OnClickListener {

    private Context mContext;
    private TextView textView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        mContext = this;

        Button start = findViewById(R.id.start_btn);
        Button stop = findViewById(R.id.stop_btn);
        textView = findViewById(R.id.text_tv);
        start.setOnClickListener(this);
        stop.setOnClickListener(this);
        SRAgent.getInstance().textView = textView;

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_btn:
                //开始转写
                int result = libisssr.setParam(libisssr.ISS_SR_PARAM_PGS, libisssr.ISS_SR_PARAM_VALUE_ON);
                if (result == 0) {
                    textView.setText("");
                    Toast.makeText(mContext, "开始转写", Toast.LENGTH_LONG).show();
                    SRAgent.getInstance().startSRSession1();
                } else {
                    Toast.makeText(mContext, "出现错误 result = " + result, Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.stop_btn:
                //停止转写
                SRAgent.getInstance().stopSRSession1();
                break;
        }
    }
}
