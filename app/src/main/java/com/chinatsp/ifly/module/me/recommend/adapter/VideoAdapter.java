package com.chinatsp.ifly.module.me.recommend.adapter;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chinatsp.ifly.DatastatManager;
import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.VideoPlayActivty;
import com.chinatsp.ifly.module.me.recommend.Utils.Constants;
import com.chinatsp.ifly.module.me.recommend.bean.VideoDataBean;
import com.chinatsp.ifly.module.me.recommend.db.VideoListUtil;
import com.chinatsp.ifly.module.me.recommend.view.CircleImageDrawable;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: //TODO
 * Function: //TODO
 * Reason: //TODO
 *
 * @author: qlf
 * @version: v1.0
 * @date : 2020/6/17
 */

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.Vh> {
    private final static String TAG ="VideoAdapter";
    private Context mContext;
    private List<VideoDataBean> videoDataBeanList;

    public VideoAdapter(List<VideoDataBean> videoDataBeanList, Context context) {
        setHasStableIds(true);
        this.videoDataBeanList = videoDataBeanList;
        Log.d(TAG,"videosBeanList:size="+videoDataBeanList.size());
        mContext =context;
    }

    @NonNull
    @Override
    public Vh onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_video_list,viewGroup,false);
        return new Vh(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Vh viewHolder, int position) {
        Glide.with(mContext)
                .load(videoDataBeanList.get(position).getPicPath())
                .asBitmap()
                .skipMemoryCache(true)
                .into(viewHolder.video_cover);
        viewHolder.video_name.setText(videoDataBeanList.get(position).getVideoName().replace(".mp4",""));
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(videoDataBeanList.get(position).getPicPath());
            Bitmap bitmap  = BitmapFactory.decodeStream(fis);
//            getReverseBitmapById(mContext,bitmap,30);
            viewHolder.iv_video_name.setBackground(new CircleImageDrawable(getTransAlphaBitmap(bitmap,30)));
            viewHolder.iv_video_name.setAlpha(0.47f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Log.d(TAG,"getRead="+videoDataBeanList.get(position).getRead());
        if (videoDataBeanList.get(position).getRead().equalsIgnoreCase("false")){
//            viewHolder.is_read.setText(mContext.getResources().getString(R.string.un_read));
            viewHolder.is_new.setVisibility(View.VISIBLE);
        }else {
//            viewHolder.is_read.setText(mContext.getResources().getString(R.string.hava_read));
            viewHolder.is_new.setVisibility(View.GONE);
        }
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            /*    StringBuffer stringBuffer =new StringBuffer("/storage/emulated/0/TEMPFILE/");
                stringBuffer.append(videoPathList.get(position).toString().replace("png","mp4"));
                String videoPath =new String(stringBuffer);*/
//                String videoPath=videoPathList.get(position).toString().replace("png","mp4").replace("videocover","videocontent");
//                VideoListUtil.getInstance(mContext).update("true",videoDataBeanList.get(position).getPicPath());
                String videoPath = videoDataBeanList.get(position).getVideoPath();
                Log.d(TAG,"videoPath ="+videoPath);
//                FloatViewManager.getInstance(mContext).hide();

/*                Intent intent = new Intent(mContext, VideoPlayActivty.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
                intent.putExtra("path",videoPath);
                mContext.startActivity(intent);*/
                String videoName =videoDataBeanList.get(position).getVideoName();
//                update(videoName);
                DatastatManager.getInstance().recordUI_event(mContext, mContext.getString(R.string.event_id_video_click), videoName);
                Intent intent = new Intent("com.coagent.intent.action.video");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                int ss = 0;
                for (int i = 0; i < videoDataBeanList.size(); i++) {
                    String path1 = videoDataBeanList.get(i).getVideoPath();
                    if (videoPath.equals(path1)) {
                        ss = i;
                    }
                }
                intent.putExtra("ps", ss+1);
                intent.putExtra("name", videoDataBeanList.get(position).getVideoName());
                intent.putExtra("data", "VideoData");
                intent.putExtra("dataType", 8);
                intent.putExtra("extra_vedio_from", "videoInIfly");
                intent.putExtra("id",VideoListUtil.getInstance(mContext).getIdByPath(videoPath)+"");
                mContext.startActivity(intent);

            }
        });
    }

    public void setData(List<VideoDataBean> videosBeanList){
        this.videoDataBeanList = videosBeanList;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {

        return videoDataBeanList==null ? 0:videoDataBeanList.size();
    }

    class Vh extends RecyclerView.ViewHolder{
        private ImageView video_cover,is_new;
        private TextView is_read,video_name;
        private ImageView iv_video_name;
        public Vh(@NonNull View itemView) {
            super(itemView);
            video_cover = (ImageView)itemView.findViewById(R.id.video_cover);
            is_read = (TextView)itemView.findViewById(R.id.is_read);
            video_name =(TextView)itemView.findViewById(R.id.video_name);
            is_new =(ImageView)itemView.findViewById(R.id.is_new);
            iv_video_name =(ImageView)itemView.findViewById(R.id.iv_video_name);
        }
    }

    public  Bitmap getTransAlphaBitmap(Bitmap sourceImg1,float percent) {
        Bitmap sourceImg =sourceImg1;
        Matrix matrix=new Matrix();
        matrix.setScale(1, -1);
        sourceImg=Bitmap.createBitmap(sourceImg, 0, (int) (sourceImg.getHeight()*(70)/100),
                sourceImg.getWidth(), (int) (sourceImg.getHeight()*30/100), matrix, false);

        int[] argb = new int[sourceImg.getWidth() * sourceImg.getHeight()];

        sourceImg.getPixels(argb, 0, sourceImg.getWidth(), 0, 0, sourceImg

                .getWidth(), sourceImg.getHeight());// 获得图片的ARGB值

        //number的范围为0-100,0为全透明，100为不透明
        float number = 100;

        //透明度数值
        float alpha = number * 255 / 100;

        //图片渐变的范围（只设置图片一半范围由上到下渐变，上面不渐变，即接近边缘的那一半）
        float range = sourceImg.getHeight() / 2.0f;
        //透明度渐变梯度，每次随着Y坐标改变的量，因为最终在边缘处要变为0
        float pos = (number * 1.0f) / range;

        //循环开始的下标，设置从什么时候开始改变
        int start = sourceImg.getWidth() * (sourceImg.getHeight() - (int) range);

        for (int i = start; i < argb.length; i++) {
            //同一行alpha数值不改变，因为是随着Y坐标从上到下改变的
            if (i % sourceImg.getWidth() == 0) {
                number = number - pos;
                alpha = number * 255 / 100;
            }
            argb[i] = ((int) alpha << 24) | (argb[i] & 0x00FFFFFF);
        }


        sourceImg = Bitmap.createBitmap(argb,sourceImg.getWidth(), sourceImg.getHeight(), Bitmap.Config.ARGB_8888);

        return sourceImg;
    }

    private void update(String videoName){
        ContentResolver contentResolver=mContext.getContentResolver();
        ContentValues values = new ContentValues();
        values.put("read","true");
        contentResolver.update(Constants.VIDEODATA_URL,values, "name=?",new String[]{videoName});
    }
}
