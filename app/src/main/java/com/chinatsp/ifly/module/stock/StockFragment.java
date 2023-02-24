package com.chinatsp.ifly.module.stock;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.chinatsp.ifly.DatastatManager;
import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.FullScreenActivity;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.base.BaseFragment;
import com.chinatsp.ifly.entity.EventTrackingEntity;
import com.chinatsp.ifly.entity.StockEntity;
import com.chinatsp.ifly.utils.EventBusUtils;
import com.chinatsp.ifly.utils.GsonUtil;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;
import com.chinatsp.ifly.voice.platformadapter.manager.GDSdkManager;
import com.chinatsp.ifly.voice.platformadapter.manager.MXSdkManager;
import com.chinatsp.ifly.voice.platformadapter.utils.MultiInterfaceUtils;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;
import com.iflytek.adapter.common.TspSceneAdapter;
import com.iflytek.adapter.mvw.MVWAgent;
import java.util.List;
import butterknife.BindView;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.*;

public class StockFragment extends BaseFragment implements StockContract.View {

    private static final String TAG = "StockFragment";
    @BindView(R.id.tv_stock_open_price)
    TextView mTvStockOpenPrice;
    @BindView(R.id.tv_stock_name)
    TextView mTvStockName;
    @BindView(R.id.tv_stock_code)
    TextView mTvStockCode;
    @BindView(R.id.tv_stock_price)
    TextView mTvStockPrice;
    @BindView(R.id.iv_stock_down_up)
    ImageView mIvStockDownUp;
    @BindView(R.id.tv_stock_range)
    TextView mTvStockRange;
    @BindView(R.id.tv_stock_range_rate)
    TextView mTvStockRangeRate;
    @BindView(R.id.tv_stock_price_max)
    TextView mTvStockPriceMax;
    @BindView(R.id.tv_stock_price_min)
    TextView mTvStockPriceMin;
    @BindView(R.id.tv_stock_quantity_relative)
    TextView mTvStockQuantityRelative;
    @BindView(R.id.tv_stock_turnover)
    TextView mTvStockTurnover;
    @BindView(R.id.tv_stock_turnover_rate)
    TextView mTvStockTurnoverRate;
    @BindView(R.id.tv_stock_total_price)
    TextView mTvStockTotalPrice;
    @BindView(R.id.tv_stock_trading_suspension)
    TextView mTvStockTradingSuspension;
    private FullScreenActivity activity;
    private StockContract.Presenter presenter = new StockPresenter(this);
    private final static int MSG_START_TTS = 1000;

    public static StockFragment newInstance(String dataList, String answer) {
        StockFragment fragment = new StockFragment();
        Bundle bundle = new Bundle();
        bundle.putString(FullScreenActivity.DATA_LIST_STR, dataList);
        bundle.putString(FullScreenActivity.ANSWER_STR, answer);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (FullScreenActivity) context;
        presenter.bindActivity(activity);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter.subscribe();
//        EventBus.getDefault().register(this);
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_START_TTS:
                    startTTS((String) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    private void startTTS(String tts) {
        Utils.startTTSOnly(tts, new TTSController.OnTtsStoppedListener() {
            @Override
            public void onPlayStopped() {
//                //重新计算超时
//                SRAgent.getInstance().resetSrTimeCount();
//                String text = "你还可以查询明天的天气哦";
//                TimeoutManager.saveSrState(mContext, TimeoutManager.UNDERSTAND_ONCE, text);
//                FloatViewManager.getInstance(getContext()).hide();
                Utils.exitVoiceAssistant();
            }
        });
    }

    int current = -1;
    @Override
    public void onResume() {
        super.onResume();

        // 保存当前场景
        current = TspSceneAdapter.getTspScene(getContext());

        //todo 确保当前是在SELECT场景
        MVWAgent.getInstance().stopMVWSession();
        MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_SELECT);
        //todo 确保界面起来时悬浮窗为打开状态
        if (FloatViewManager.getInstance(activity).isHide()) {
            FloatViewManager.getInstance(activity).show(FloatViewManager.WARE_BY_OTHER);
        }
        EventBusUtils.sendMainMessage("你还可以查询明天的股票哦");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.unSubscribe();
//        EventBus.getDefault().unregister(this);
        //todo
        MultiInterfaceUtils.getInstance(activity).uploadCmdDefaultData();
        handler.removeCallbacks(null);


        if (MXSdkManager.getInstance(activity).isForeground() || MXSdkManager.getInstance(activity).isNaving()) {
            MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_NAVI);
        }
        /***********欧尚修改开始*****************/
        else if(GDSdkManager.getInstance(activity).isGDForeground()){
            MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_NAVI);
        }
        /***********欧尚修改结束*****************/
        else if (current != -1) {
            MVWAgent.getInstance().startMVWSession(current);
        }  else {
            MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_GLOBAL);
        }

        current = -1;

    }

    @Override
    protected void initData() {
        String result = getArguments().getString(FullScreenActivity.DATA_LIST_STR);
        if (!TextUtils.isEmpty(result)) {
            List<StockEntity> stockListEntity = GsonUtil.stringToList(result, StockEntity.class);
            if (stockListEntity == null || stockListEntity.size() == 0) return;
            StockEntity stockEntity = stockListEntity.get(0);
            mTvStockOpenPrice.setText("今开" + stockEntity.getOpeningPrice() + "元");
            mTvStockName.setText(stockEntity.getName());
            mTvStockCode.setText(stockEntity.getStockCode());
            mTvStockPrice.setText(stockEntity.getCurrentPrice());
            float value = 0;
            String conditionId = "";
            String tts = String.format(getString(R.string.stock_rise), stockEntity.getName(), stockEntity.getCurrentPrice(), stockEntity.getRiseValue(), stockEntity.getRiseRate());
            float currentPrice = 0;
            if (!TextUtils.isEmpty(stockEntity.getCurrentPrice())) {
                currentPrice = Float.parseFloat(stockEntity.getCurrentPrice());
            }
            if (currentPrice == 0.0) {//停牌
                conditionId = TtsConstant.STOCKC3CONDITION;
                tts = String.format(getString(R.string.stock_trading_suspension), stockEntity.getName());
                mTvStockPrice.setText(stockEntity.getClosingPrice());
                mTvStockRange.setVisibility(View.GONE);
                mTvStockRangeRate.setVisibility(View.GONE);
                mIvStockDownUp.setVisibility(View.GONE);
                mTvStockTradingSuspension.setVisibility(View.VISIBLE);
                getTtsMessage(STOCKC3CONDITION, tts, stockEntity,"",R.string.skill_stock, R.string.scene_stock, R.string.object_stock, R.string.condition_stock3);
                return;
            } else {
                mTvStockRange.setVisibility(View.VISIBLE);
                mTvStockRangeRate.setVisibility(View.VISIBLE);
                mIvStockDownUp.setVisibility(View.VISIBLE);
                mTvStockTradingSuspension.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(stockEntity.getRiseValue())) {
                value = Float.parseFloat(stockEntity.getRiseValue());
            }
            //根据涨幅值来判断是上涨,下跌或者持平
            if (value > 0) {
                //上涨
                mIvStockDownUp.setImageDrawable(getResources().getDrawable(R.drawable.ic_stock_rise));
                mTvStockPrice.setTextColor(getResources().getColor(R.color.stock_rise_color));
                mTvStockOpenPrice.setTextColor(getResources().getColor(R.color.stock_rise_color));
                mTvStockRange.setTextColor(getResources().getColor(R.color.stock_rise_color));
                mTvStockRangeRate.setTextColor(getResources().getColor(R.color.stock_rise_color));
                getTtsMessage(STOCKC1CONDITION, tts, stockEntity,"",R.string.skill_stock, R.string.scene_stock, R.string.object_stock, R.string.condition_stock1);
            } else if (value < 0) {
                //下跌
                tts = String.format(getString(R.string.stock_down), stockEntity.getName(), stockEntity.getCurrentPrice(), stockEntity.getRiseValue(), stockEntity.getRiseRate());
                mIvStockDownUp.setImageDrawable(getResources().getDrawable(R.drawable.ic_stock_down));
                mTvStockPrice.setTextColor(getResources().getColor(R.color.stock_down_color));
                mTvStockOpenPrice.setTextColor(getResources().getColor(R.color.stock_down_color));
                mTvStockRange.setTextColor(getResources().getColor(R.color.stock_down_color));
                mTvStockRangeRate.setTextColor(getResources().getColor(R.color.stock_down_color));
                getTtsMessage(STOCKC2CONDITION, tts, stockEntity,"",R.string.skill_stock, R.string.scene_stock, R.string.object_stock, R.string.condition_stock2);
            } else {
                //持平
                mIvStockDownUp.setImageDrawable(getResources().getDrawable(R.drawable.ic_stock_ping));
                mTvStockPrice.setTextColor(getResources().getColor(R.color.stock_tie_color));
                mTvStockOpenPrice.setTextColor(getResources().getColor(R.color.stock_tie_color));
                mTvStockRange.setTextColor(getResources().getColor(R.color.stock_tie_color));
                mTvStockRangeRate.setTextColor(getResources().getColor(R.color.stock_tie_color));
                getTtsMessage(STOCKC1CONDITION, tts, stockEntity,"",R.string.skill_stock, R.string.scene_stock, R.string.object_stock, R.string.condition_stock1);
            }
            mTvStockRange.setText(stockEntity.getRiseValue());
            mTvStockRangeRate.setText(stockEntity.getRiseRate());
            mTvStockPriceMax.setText(stockEntity.getHighPrice());
            mTvStockPriceMin.setText(stockEntity.getLowPrice());
        } else {
            String name = getArguments().getString(FullScreenActivity.ANSWER_STR);
            String tts = String.format(getString(R.string.stock_no_data), name);
            getTtsMessage(STOCKC4CONDITION, tts, null,name,R.string.skill_stock, R.string.scene_stock, R.string.object_stock, R.string.condition_stock4);
        }
    }

    private void getTtsMessage(String conditionId,String defaultTts, StockEntity stockEntity,String name,int appName, int scene, int object, int condition) {
        Utils.getMessageWithoutTtsSpeak(getContext(), conditionId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String defaultText = tts;
                if (TextUtils.isEmpty(defaultText)) {
                    defaultText = defaultTts;
                } else {
                    if (stockEntity != null) {
                        defaultText = Utils.replaceTts(defaultText, "#STOCK#", stockEntity.getName());
                        defaultText = Utils.replaceTts(defaultText, "#PRICE#", stockEntity.getCurrentPrice());
                        defaultText = Utils.replaceTts(defaultText, "#VALUE#", stockEntity.getRiseValue());
                        defaultText = Utils.replaceTts(defaultText, "#RATE#", stockEntity.getRiseRate());
                    }else{
                        defaultText = Utils.replaceTts(defaultText, "#STOCK#", name);
                    }
                }
                defaultText = defaultText.replace("-","");
                Utils.eventTrack(getContext(),appName,scene,object,conditionId,condition,defaultText);
                Message msg = handler.obtainMessage(MSG_START_TTS, defaultText);
                handler.sendMessage(msg);
            }
        });
    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initView(View view) {

    }

    @Override
    protected int getContentView() {
        return R.layout.fragment_stock;
    }
}
