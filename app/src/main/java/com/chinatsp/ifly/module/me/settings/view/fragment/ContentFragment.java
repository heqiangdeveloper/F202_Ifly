package com.chinatsp.ifly.module.me.settings.view.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chinatsp.ifly.DatastatManager;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.SettingsActivity;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.base.BaseFragment;
import com.chinatsp.ifly.db.CommandProvider;
import com.chinatsp.ifly.db.entity.CommandInfo;
import com.chinatsp.ifly.utils.ImageUtils;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ContentFragment extends BaseFragment {
    private static final String TAG = "ContentFragment";
    private static final int MODE_TYPE_SR = 1;
    private static final int MODE_TYPE_MVW = 0;
    private RecyclerView mRyContent;
    private List<CommandInfo> mSkills;
    private ContentAdapter mContentAdapter;
    private Context mContext;
    private static SettingsActivity mActivity;
    private String mModel;
    private boolean mLastStatus = true;

    private boolean isViewCreated;

    private boolean isUIVisible;

    private static long lastClickTime = 0;

    private static int spaceTime = 500;


    public static ContentFragment newInstance(String title) {
        Log.d(TAG, "newInstance() called with: title = [" + title + "]");
        ContentFragment fragment = new ContentFragment();
        fragment.mModel = title;
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
        if(mModel==null)
            mModel = savedInstanceState.getString("model");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isViewCreated = true;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("model",mModel);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(TAG, "setUserVisibleHint() called with: isVisibleToUser = [" + isVisibleToUser + "]");
//        if(getUserVisibleHint()){
//            if(AppConstant.DOWNLOAD_FINISEH&&!mLastStatus)
//                initData();
//        }
        if (isVisibleToUser) {
            isUIVisible = true;
            initData();
        } else {
            isUIVisible = false;
        }
    }
    
    @Override
    protected void initData() {
        Log.d(TAG,"initData");
        if (isViewCreated && isUIVisible) {
            Log.d(TAG,"isViewCreated:isUIVisible");
            if(AppConstant.DOWNLOAD_FINISEH)
                mLastStatus = true;
            else
                mLastStatus = false;
         mContentAdapter.setData(CommandProvider.getInstance(mContext).getCommandInfo(mModel));
         DatastatManager.getInstance().recordUI_event(mActivity, mActivity.getResources().getString(R.string.event_id_module_name_click), mModel);
        }

    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initView(View view) {
        mRyContent = view.findViewById(R.id.recyContent);
        mRyContent.setHasFixedSize(true);
        mRyContent.setLayoutManager(new GridLayoutManager(getContext(),
                3, LinearLayoutManager.VERTICAL, false));
        mContentAdapter = new ContentAdapter(this);
        mRyContent.addItemDecoration(new GridSpacingItemDecoration(3, 64, 76,false));
        mRyContent.setAdapter(mContentAdapter);
    }

    @Override
    protected int getContentView() {
        return R.layout.fragment_command_content;
    }

    private static class ContentAdapter extends RecyclerView.Adapter<ContentAdapterVh> {

        private Map<String, ArrayList<CommandInfo>> mData;
        private List<String> mSkillNames;
        private ContentFragment mFragment;

        public ContentAdapter(ContentFragment fragment) {
            mFragment = fragment;
        }

        @NonNull
        @Override
        public ContentAdapterVh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_comand_content, parent, false);
            return new ContentAdapterVh(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ContentAdapterVh holder, int position) {
            ArrayList<CommandInfo> infos = mData.get(mSkillNames.get(position));
            holder.tvName.setText(mSkillNames.get(position));
            if(infos.size()>0)
               holder.ivIcon.setImageBitmap(ImageUtils.getCompressBitmap(infos.get(0).getIconPath()));
            Collections.shuffle(infos);
            holder.tvContent1.setVisibility(View.VISIBLE);
            holder.tvContent2.setVisibility(View.GONE);
            holder.tvContent3.setVisibility(View.GONE);
            holder.tvContent4.setVisibility(View.GONE);
            if(infos.size()>=1)
                if (mFragment.mModel.equalsIgnoreCase(mFragment.getResources().getString(R.string.no_wake_up))){
                    holder.tvContent1.setText(" \" "+infos.get(0).getInstructContent()+" \" ");
                }else {
                    holder.tvContent1.setText(" \" "+mFragment.getResources().getString(R.string.hello_xiaoou)+infos.get(0).getInstructContent()+" \" ");
                }
            if(infos.size()>=2){
                holder.tvContent2.setVisibility(View.VISIBLE);
                if (mFragment.mModel.equalsIgnoreCase(mFragment.getResources().getString(R.string.no_wake_up))){
                    holder.tvContent2.setText(" \" "+infos.get(1).getInstructContent()+" \" ");
                }else {
                    holder.tvContent2.setText(" \" "+mFragment.getResources().getString(R.string.hello_xiaoou)+infos.get(1).getInstructContent()+" \" ");
                }
            }
            if(infos.size()>=3){
                holder.tvContent3.setVisibility(View.VISIBLE);
                if (mFragment.mModel.equalsIgnoreCase(mFragment.getResources().getString(R.string.no_wake_up))){
                    holder.tvContent3.setText(" \" "+infos.get(2).getInstructContent()+" \" ");
                }else {
                    holder.tvContent3.setText(" \" "+mFragment.getResources().getString(R.string.hello_xiaoou)+infos.get(2).getInstructContent()+" \" ");
                }
            }

            if(infos.size()>=4){
                holder.tvContent4.setVisibility(View.VISIBLE);
                if (mFragment.mModel.equalsIgnoreCase(mFragment.getResources().getString(R.string.no_wake_up))){
                    holder.tvContent4.setText(" \" "+infos.get(3).getInstructContent()+" \" ");
                }else {
                    holder.tvContent4.setText(" \" "+mFragment.getResources().getString(R.string.hello_xiaoou)+infos.get(3).getInstructContent()+" \" ");
                }
            }

            View.OnClickListener mOnClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mFragment.isFastClick()){
                        Log.d(TAG,"isFastClick");
                        return;
                    }
                    showDetailFragment(infos);
                    SharedPreferencesUtils.saveBoolean(mFragment.mContext,mSkillNames.get(position),false);
                }
            };
            holder.btnMore.setOnClickListener(mOnClickListener);
            holder.rlRoot.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        public void setData(Map<String, ArrayList<CommandInfo>>mData) {
            this.mData = mData;
            mSkillNames =Stream.of(mData.keySet().toArray(new String[0])).collect(Collectors.toList());
            notifyDataSetChanged();
        }
    }

    private static class ContentAdapterVh extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName;
        TextView tvContent1,tvContent2,tvContent3,tvContent4;
        Button btnMore;
        RelativeLayout rlRoot;

        public ContentAdapterVh(View itemView) {
            super(itemView);
            rlRoot = itemView.findViewById(R.id.rl_skill_root);
            tvName = (TextView) itemView.findViewById(R.id.tv_skill_name);
            tvContent1 = (TextView) itemView.findViewById(R.id.tv_skill_content1);
            tvContent2 = (TextView) itemView.findViewById(R.id.tv_skill_content2);
            tvContent3 = (TextView) itemView.findViewById(R.id.tv_skill_content3);
            tvContent4 = (TextView) itemView.findViewById(R.id.tv_skill_content4);
            ivIcon = (ImageView) itemView.findViewById(R.id.iv_content_icon);
            btnMore = (Button) itemView.findViewById(R.id.btn_skill_more);
        }
    }

    public boolean isFastClick() {
        long currentTime = System.currentTimeMillis();
        boolean isAllowClick;
        if (currentTime - lastClickTime > spaceTime) {
            isAllowClick = false;
        } else {
            isAllowClick = true;
        }
        lastClickTime = currentTime;
        return isAllowClick;
    }


    private static void showDetailFragment(ArrayList<CommandInfo> infos){
        for (int i = 0; i <infos.size() ; i++) {
            Log.d(TAG, "showDetailFragment() called with: infos = [" + infos.get(i) + "]"+"....."+i);
        }
        if(infos!=null&&infos.size()>0){
            String moduleType = infos.get(0).getModuleType();
            if("0".equals(moduleType)){
                DetailMvwFragment fragment= DetailMvwFragment.newInstance(infos);
                mActivity.switchFragment(fragment);
            }else {
                DetailSrFragment fragment= DetailSrFragment.newInstance(infos);
                mActivity.switchFragment(fragment);
            }
        }
    }

}
