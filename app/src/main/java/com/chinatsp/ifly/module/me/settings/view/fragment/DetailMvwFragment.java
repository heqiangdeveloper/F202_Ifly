package com.chinatsp.ifly.module.me.settings.view.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chinatsp.ifly.DatastatManager;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.SettingsActivity;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.base.BaseFragment;
import com.chinatsp.ifly.db.entity.CommandInfo;
import com.chinatsp.ifly.dialog.CustomDialog;
import com.chinatsp.ifly.utils.ImageUtils;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;
import com.iflytek.adapter.sr.SRAgent;
import com.iflytek.sr.SrSession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DetailMvwFragment extends BaseFragment implements View.OnClickListener {

    private static final String TAG = "DetailMvwFragment";
    private ArrayList<CommandInfo> mCommandInfos;
    private Context mContext;
    private SettingsActivity mActivity;
    private TextView mTvTitle,mTvSkillName,mTvContent1,mTvContent2,mTvContent3,mTvDesc1,mTvDesc2;
    private ImageView mIvIcon,mIvBack;
    private Button mBtnUser;

    private static final  int MSG_ATTENTION = 1001;
    private static final  int TIME_DELAY_SHOWING = 600;
    private ContentAdapter mContentAdapter;
    private RecyclerView rcMvm;
    private LinearLayout ll_detail_back;
/*    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String tts = (String) msg.obj;
            Utils.startTTSOnly(tts, new TTSController.OnTtsStoppedListener() {
                @Override
                public void onPlayStopped() {
                    Utils.exitVoiceAssistant();
                }
            });
        }
    };*/
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            SRAgent.getInstance().setSrArgu_New(SrSession.ISS_SR_SCENE_ALL, "");
            String tts = (String) msg.obj;
            Utils.startTTS(tts, null);
        }
    };

    public DetailMvwFragment(){

    }

    public static DetailMvwFragment newInstance(ArrayList<CommandInfo> infos) {
        DetailMvwFragment fragment = new DetailMvwFragment();
        fragment.mCommandInfos = infos;
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = (SettingsActivity) context;
        mActivity = (SettingsActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  super.onCreateView(inflater, container, savedInstanceState);
        if(mCommandInfos==null)
            mCommandInfos = savedInstanceState.getParcelableArrayList("info");
        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("info",mCommandInfos);
    }


    @Override
    protected void initData() {
        CommandInfo info = mCommandInfos.get(0);
        DatastatManager.getInstance().command_event(mActivity,info.getModuleName(),info.getSkillName());
        mTvTitle.setText(info.getModuleName()+"-"+info.getSkillName());
        mIvIcon.setImageBitmap(ImageUtils.getCompressBitmap(info.getIconPath()));
        mBtnUser.setVisibility(("1".equals(info.getIsdisplay())?View.VISIBLE:View.GONE));
        mTvSkillName.setText(info.getSkillName());
//        Collections.shuffle(mCommandInfos);
/*        if(mCommandInfos.size()>=1)
            mTvContent1.setText(mCommandInfos.get(0).getInstructContent());
            mTvContent1.setTag(R.string.conteng_tag_1,mCommandInfos.get(0).getInstructTeach());
        Log.d(TAG, "initData() called::"+mCommandInfos.get(0).getInstructTeach());
            mTvDesc1.setText(mCommandInfos.get(0).getInstructdesc());
        if(mCommandInfos.size()>=2){
            Log.d(TAG, "initData() called::"+mCommandInfos.get(1).getInstructTeach());
            mTvContent2.setVisibility(View.VISIBLE);
            mTvContent2.setText(mCommandInfos.get(1).getInstructContent());
            mTvContent2.setTag(R.string.conteng_tag_2,mCommandInfos.get(1).getInstructTeach());
//            mTvDesc2.setText("2."+mCommandInfos.get(1).getInstructdesc());
        }
        if(mCommandInfos.size()>=3){
            Log.d(TAG, "initData() called::"+mCommandInfos.get(2).getInstructTeach());
            mTvContent3.setVisibility(View.VISIBLE);
            mTvContent3.setText(mCommandInfos.get(2).getInstructContent());
            mTvContent3.setTag(R.string.conteng_tag_3,mCommandInfos.get(2).getInstructTeach());
        }*/
        mTvDesc1.setText(mCommandInfos.get(0).getInstructdesc());
        mContentAdapter = new ContentAdapter(this);
        rcMvm.setLayoutManager(new GridLayoutManager(getContext(),
                3, LinearLayoutManager.VERTICAL, false));
        rcMvm.addItemDecoration(new GridSpacingItemDecoration(3, 60, 43,false));
        rcMvm.setAdapter(mContentAdapter);
        mContentAdapter.setData(mCommandInfos);
    }

    @Override
    protected void initListener() {
        mIvBack.setOnClickListener(this);
        mBtnUser.setOnClickListener(this);
        mTvContent1.setOnClickListener(this);
        mTvContent2.setOnClickListener(this);
        mTvContent3.setOnClickListener(this);
        ll_detail_back.setOnClickListener(this);
    }

    @Override
    protected void initView(View view) {
        mTvTitle = view.findViewById(R.id.tv_detail_title);
        mTvSkillName = view.findViewById(R.id.tv_skill_name);
        mBtnUser = view.findViewById(R.id.btn_skill_user);
        mIvIcon = view.findViewById(R.id.iv_skill_icon);
        mIvBack = view.findViewById(R.id.iv_detail_back);
        mTvContent1 = view.findViewById(R.id.tv_mvw_user1);
        mTvContent2 = view.findViewById(R.id.tv_mvw_user2);
        mTvContent3 = view.findViewById(R.id.tv_mvw_user3);
        mTvDesc1 = view.findViewById(R.id.tv_mvw_instrument1);
        mTvDesc2 = view.findViewById(R.id.tv_mvw_instrument2);
        rcMvm = (RecyclerView)view.findViewById(R.id.rc_mvw);
        ll_detail_back = view.findViewById(R.id.ll_detail_back);
    }

    @Override
    protected int getContentView() {
        return R.layout.fragment_mvw_detail;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_skill_user:
                Random ra =new Random();
                int index = ra.nextInt(mCommandInfos.size());
                if(SharedPreferencesUtils.getBoolean(mContext, AppConstant.KEY_RELIEF_SHOWN,true))
                    showReliefDialog(mCommandInfos.get(index).getInstructTeach());
                else{
                    showRetiefUI(mCommandInfos.get(index).getInstructTeach());
                }
                break;
            case R.id.ll_detail_back:
            case R.id.iv_detail_back:
                mActivity.switchFragment(AppConstant.VOICE_COMMAND_ID);
                break;
            case R.id.tv_mvw_user1:
                if(SharedPreferencesUtils.getBoolean(mContext, AppConstant.KEY_RELIEF_SHOWN,true))
                    showReliefDialog((String) mTvContent1.getTag(R.string.conteng_tag_1));
                else
                    showRetiefUI((String) mTvContent1.getTag(R.string.conteng_tag_1));
                break;
            case R.id.tv_mvw_user2:
                if(SharedPreferencesUtils.getBoolean(mContext, AppConstant.KEY_RELIEF_SHOWN,true))
                    showReliefDialog((String) mTvContent2.getTag(R.string.conteng_tag_2));
                else
                    showRetiefUI((String) mTvContent2.getTag(R.string.conteng_tag_2));
                break;
            case R.id.tv_mvw_user3:
                if(SharedPreferencesUtils.getBoolean(mContext, AppConstant.KEY_RELIEF_SHOWN,true))
                    showReliefDialog((String) mTvContent2.getTag(R.string.conteng_tag_2));
                else
                    showRetiefUI((String) mTvContent3.getTag(R.string.conteng_tag_3));
                break;
        }
    }


    private void showReliefDialog(String tag){
        CustomDialog.Builder builder = new CustomDialog.Builder(mContext);
        builder.setPositiveButton( new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which==-1){
                    showRetiefUI(tag);
                }
                dialog.dismiss();

            }
        });

        builder.setNegativeButton(
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which==-1){
                            showRetiefUI(tag);
                        }
                        dialog.dismiss();
                    }
                });

        builder.create().show();

    }


    private static class ContentAdapter extends RecyclerView.Adapter<ContentAdapterVh> {

        private List<CommandInfo> mData;
        private DetailMvwFragment fragment;

        public ContentAdapter(DetailMvwFragment fragment) {
            this.fragment = fragment;
        }

        @NonNull
        @Override
        public ContentAdapterVh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_detail_sr, parent, false);
            return new ContentAdapterVh(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ContentAdapterVh holder, int position) {
            holder.tvInstrutContent.setText(" \" "+mData.get(position).getInstructContent()+" \" ");
            holder.tvInstrutContent.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    String instructTeach = mData.get(position).getInstructTeach();
                    Log.d(TAG,"instructTeach ="+instructTeach);
                    if (instructTeach.isEmpty()){
                        return;
                    }
                    if(SharedPreferencesUtils.getBoolean(fragment.getContext(), AppConstant.KEY_RELIEF_SHOWN,true))
                        fragment.showReliefDialog(instructTeach);
                    else{
                        fragment.showRetiefUI(instructTeach);
                    }
                    Log.d(TAG, "onClick: ");
                }
            });
            Log.d(TAG, "onBindViewHolder() called with: holder = [" + mData.get(position).getInstructContent() + "], position = [" + position + "]");
        }

        @Override
        public int getItemCount() {
            if(mData == null)
                return 0;
            Log.d(TAG,"getItemCount ="+mData.size());
            return mData.size();
        }

        public void setData(List<CommandInfo> mData) {
            this.mData = mData;
            Log.d(TAG,"setData ="+mData.size());
            notifyDataSetChanged();
        }
    }

    private static class ContentAdapterVh extends RecyclerView.ViewHolder {


        public TextView tvInstrutContent;
        public ContentAdapterVh(View itemView) {
            super(itemView);
            tvInstrutContent = itemView.findViewById(R.id.tv_detail_content);
        }
    }

    private void showRetiefUI(String text){
        DatastatManager.getInstance().command_event(mActivity,mCommandInfos.get(0).getModuleName(),mCommandInfos.get(0).getSkillName());
        if(text==null||"".equals(text)){
            Log.e(TAG, "showRetiefUI: the text is null");
            return;
        }
        Log.d(TAG, "showRetiefUI() called with: text = [" + text + "]");
        Intent broad = new Intent(AppConstant.ACTION_SHOW_ASSISTANT);
        broad.putExtra(AppConstant.EXTRA_SHOW_TYPE,AppConstant.SHOW_BY_OTHER);
        broad.putExtra(AppConstant.EXTRA_SHOW_TTS,text);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(broad);

        mHandler.removeCallbacksAndMessages(null);
        Message msg = mHandler.obtainMessage();
        msg.what = MSG_ATTENTION;
        msg.obj = text;
        mHandler.sendMessageDelayed(msg,TIME_DELAY_SHOWING);
    }
}
