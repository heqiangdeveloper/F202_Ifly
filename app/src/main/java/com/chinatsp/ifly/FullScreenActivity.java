package com.chinatsp.ifly;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.TextView;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.base.BaseFragment;
import com.chinatsp.ifly.module.seachlist.SearchListFragment;
import com.chinatsp.ifly.module.stock.StockFragment;
import com.chinatsp.ifly.module.weather.WeatherFragment;
import com.chinatsp.ifly.utils.ActivityUtils;
import com.chinatsp.ifly.utils.SpannableUtils;
import com.chinatsp.ifly.utils.Utils;
public class FullScreenActivity extends AppCompatActivity implements View.OnClickListener {

    private int mType = 0;
    private String dataStr;
    private String semanticStr;
    private String answerText;
    private String topic;
    private BaseFragment mFragment;
    /**
     * 显示类型
     */
    public static final String TYPE = "type";

    /**
     * 结果数据字符串
     */
    public static final String DATA_LIST_STR = "data_list_str";
    /**
     * 语义数据字符串
     */
    public static final String SEMANTIC_STR = "semantic";
    public static final String ANSWER_STR = "answer";

    /**
     * POI类型
     */
    public static final String TOPIC = "topic";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initIntentData();
        initView();
        initFragment();

        //todo 确保界面起来时悬浮窗为打开状态
        if (FloatViewManager.getInstance(this).isHide()) {
            FloatViewManager.getInstance(this).show(FloatViewManager.WARE_BY_OTHER);
        }
    }

    private void initView() {
        findViewById(R.id.iv_exit).setOnClickListener(this);

        TextView tvDesc = findViewById(R.id.tv_header_title);
        if (mType <= AppConstant.MAX_FRAGMENT_SEARCH_LIST_TYPE) {
            String text = "选择请说 “ 第几个 ”    翻页请说 “ 下一页 ”";
            SpannableStringBuilder string = SpannableUtils.formatString(text, 5, 12, Color.parseColor("#00a1ff"), 20, text.length(), Color.parseColor("#00a1ff"));
            tvDesc.setText(string);
        }else if (mType == AppConstant.TYPE_FRAGMENT_WEATHER) {
            String text = getString(R.string.weather_hint);
            SpannableStringBuilder string = SpannableUtils.formatString(text, 5, text.length(), Color.parseColor("#00a1ff"));
            tvDesc.setText(string);
        } else if (mType == AppConstant.TYPE_FRAGMENT_STOCK) {
            tvDesc.setText("");
        }
    }

    private void initFragment() {
        if (mType <= AppConstant.MAX_FRAGMENT_SEARCH_LIST_TYPE) {
            mFragment = SearchListFragment.newInstance(mType, dataStr, semanticStr, topic);
        } else if (mType == AppConstant.TYPE_FRAGMENT_STOCK) {
            mFragment = StockFragment.newInstance(dataStr,answerText);
        } else if (mType == AppConstant.TYPE_FRAGMENT_WEATHER) {
            mFragment = WeatherFragment.newInstance(FullScreenActivity.this,dataStr, semanticStr);
        }
        ActivityUtils.replaceFragmentToActivity(getSupportFragmentManager(), mFragment, R.id.framelayout_content);
    }

    private void initIntentData() {
        if (getIntent() != null) {
            mType = getIntent().getIntExtra(TYPE, AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CONTACT);
            dataStr = getIntent().getStringExtra(DATA_LIST_STR);
            semanticStr = getIntent().getStringExtra(SEMANTIC_STR);
            answerText = getIntent().getStringExtra(ANSWER_STR);
            topic = getIntent().getStringExtra(TOPIC);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_exit:
                finish();
                Utils.exitVoiceAssistant();
                break;
        }
    }

    public String getSemanticStr() {
        return semanticStr;
    }

    public String getTopic() {
        return topic;
    }
}
