package com.chinatsp.ifly.module.me.recommend.view;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.VideoPlayActivty;
import com.chinatsp.ifly.module.me.recommend.db.VideoListUtil;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;

/**
 * ClassName: //TODO
 * Function: //TODO
 * Reason: //TODO
 *
 * @author: qlf
 * @version: v1.0
 * @date : 2020/7/6
 */

public class VideoView extends LinearLayout implements View.OnClickListener{

    private final static String TAG ="VideoView";
    private ImageView dialog_video_cover;
    private ImageView dialog_video_close;
    private ImageView dialog_video_play;
    private Context mContext;
    private String path;

    public VideoView(Context context,String path) {
        super(context);
        mContext = context;
        this.path =path;
        Log.d(TAG,"path ="+path);
        initView(path);
        initListener();
    }

    private void initListener() {
        dialog_video_close.setOnClickListener(this);
        dialog_video_play.setOnClickListener(this);
    }

    private void initView(String path) {
        inflate(getContext(), R.layout.video_view, this);
        dialog_video_cover = (ImageView)findViewById(R.id.dialog_video_cover);
        dialog_video_close = (ImageView)findViewById(R.id.dialog_video_close);
        dialog_video_play = (ImageView)findViewById(R.id.dialog_video_play);
        Glide.with(mContext)
                .load(path)
                .asBitmap()
                .skipMemoryCache(true)
                .into(dialog_video_cover);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.dialog_video_close:
                SharedPreferencesUtils.saveBoolean(mContext,"isRemind",false);
                ManageFloatWindow.getInstance(mContext).removeVideoView();
                break;
            case R.id.dialog_video_play:
                ManageFloatWindow.getInstance(mContext).removeVideoView();
                VideoListUtil.getInstance(mContext).update("true",path);
                Intent intent = new Intent(mContext, VideoPlayActivty.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
                intent.putExtra("path",path.replace("png","mp4").replace("videocover","videocontent"));
                mContext.startActivity(intent);
                break;
        }
    }
}
