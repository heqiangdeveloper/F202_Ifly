package com.chinatsp.ifly.module.me.recommend.view;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
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
 * @date : 2020/8/13
 */

public class VideoDialog extends Dialog implements View.OnClickListener {
    private final static String TAG ="VideoDialog";
    private ImageView dialog_video_cover;
    private ImageView dialog_video_close;
    private ImageView dialog_video_play;
    private TextView tvVideoName;
    private Context mContext;
    private String path="",videoName;
    public VideoDialog(@NonNull Context context) {
        super(context,R.style.style_relief);
        initView(context);
        initListener();
    }


    private void initListener() {
        dialog_video_close.setOnClickListener(this);
        dialog_video_play.setOnClickListener(this);
    }

    private void initView(Context context) {
        mContext = context;
        View contentView=  getLayoutInflater().inflate(R.layout.video_view, null,false);
        setContentView(contentView);
        dialog_video_cover = (ImageView)findViewById(R.id.dialog_video_cover);
        dialog_video_close = (ImageView)findViewById(R.id.dialog_video_close);
        dialog_video_play = (ImageView)findViewById(R.id.dialog_video_play);
    }

    public void initData(String path,String videoName){
        if(this.path .equalsIgnoreCase(path))
            return;
        this.path = path;
        tvVideoName = (TextView)findViewById(R.id.tvVideoName);
        tvVideoName.setText("“"+videoName+"”");
        Glide.with(mContext)
                .load(path)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        if (!isShowing()){
                            show();
                        }
                        return false;
                    }
                })
                .into(dialog_video_cover);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.dialog_video_close:
                if (mClickListener!=null){
                    mClickListener.onCancel();
                }
                dismiss();
                break;
            case R.id.dialog_video_play:
                 if (mClickListener!=null){
                     mClickListener.onPlay();
                     dismiss();
                 }
                break;
        }
    }

    public void setClickListener(ClickListener clickListener){
        mClickListener = clickListener;
    }
    private ClickListener mClickListener;
    public interface ClickListener{
           void onPlay();
           void onCancel();
    }
}
