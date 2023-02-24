package com.chinatsp.ifly.module.me.settings.view.fragment;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.TabLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chinatsp.ifly.MainActivity;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.SettingsActivity;
import com.chinatsp.ifly.base.BaseFragment;
import com.chinatsp.ifly.db.CommandProvider;
import com.chinatsp.ifly.db.entity.CommandInfo;
import com.chinatsp.ifly.entity.JumpEntity;
import com.chinatsp.ifly.module.me.recommend.model.HuVoiceAsssitContentModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class CommandFragment extends BaseFragment implements HuVoiceAsssitContentModel.onCommandCallbackInterface {

    private static final String TAG = "CommandFragment";
    private List<String> mCommandTypes;
    private Context mContext;
    private ViewPager mViewPager;
    private TabLayout mTbCommandType;
    private MyFragAdapter mAdapter;

    private List<String> mTitles;
    private JumpEntity mJumpEntity;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = (SettingsActivity) context;
        HuVoiceAsssitContentModel.getInstance().setOnCommandCallbackListener(this);
    }

    public void putExtra(JumpEntity entity){
        mJumpEntity = entity;
    }
    @Override
    protected void initData() {

        Observable.just("").map(new Function<String, List<String>>() {
            @Override
            public List<String> apply(String s) throws Exception {
                return CommandProvider.getInstance(mContext).getCommandTypes();
            }
        }).observeOn(AndroidSchedulers.mainThread()

        ).subscribeOn(Schedulers.io()).subscribe(new Consumer<List<String>>() {
            @Override
            public void accept(List<String> strings) throws Exception {
                int index = 0;
                mTitles = strings;
                List<Fragment> fragments = new ArrayList<>();
                for (int i = 0; i < mTitles.size(); i++) {
                
                    CommandProvider.getInstance(mContext).insertData(mTitles.get(i));
                    ContentFragment fragment = ContentFragment.newInstance(mTitles.get(i));
                    fragments.add(fragment);
                    if(mJumpEntity!=null&&mJumpEntity.moduleName!=null&&mJumpEntity.moduleName.equals(mTitles.get(i)))
                        index = i;
                }
                mAdapter = new MyFragAdapter(getChildFragmentManager(), fragments, mTitles);
                mViewPager.setAdapter(mAdapter);

                for (int i = 0; i < mTbCommandType.getTabCount(); i++) {
                    View cusView = LayoutInflater.from(mContext).inflate(R.layout.item_command, null);
                    LinearLayout mTabView = (LinearLayout) cusView.findViewById(R.id.item_command_root);
                    TextView mTabText = (TextView) cusView.findViewById(R.id.tv_command_title);
                    mTabText.setText(mTitles.get(i));
                    mTbCommandType.getTabAt(i).setCustomView(cusView);
                }

                mTbCommandType.getTabAt(index).select();
            }
        });


    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initView(View view) {

        mViewPager = view.findViewById(R.id.vw_command_type);
        mTbCommandType = view.findViewById(R.id.tb_command);
        mTbCommandType.setupWithViewPager(mViewPager);

    }

    @Override
    protected int getContentView() {
        return R.layout.fragment_settings_command;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        HuVoiceAsssitContentModel.getInstance().setOnCommandCallbackListener(null);
    }

    @Override
    public void onCommandDownloadStatus(boolean finish) {
        Log.d(TAG, "onCommandDownloadStatus() called with: finish = [" + finish + "]");
        if(finish)
           initData();
    }
}
