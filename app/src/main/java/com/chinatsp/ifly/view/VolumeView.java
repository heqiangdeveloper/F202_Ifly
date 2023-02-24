package com.chinatsp.ifly.view;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.voice.platformadapter.controller.CMDController;

public class VolumeView{
	private Handler mHandler;
	private TextView mTvVolume;
	private TextView mTvMin;
	private TextView mTvMax;
	private ProgressBar mPbVolume;
	private Context mContext;
	private Resources mResources;
	private View mView;
	private WindowManager mWindowManager;
	private boolean isShow = false;
	private View mLlBottom;
	private ImageView mIvIcon;

	public VolumeView(Context context, Handler handler) {
		mHandler = handler;
		mContext = context;
		mResources = context.getResources();
		boolean isDark = Settings.System.getInt(mContext.getContentResolver(), AppConstant.SHOW_MODE, 0) == 0;
		if(!isDark) {
			mView = View.inflate(context, R.layout.volume_view_layout_day, null);
		} else {
			mView = View.inflate(context, R.layout.volume_view_layout, null);
		}
		mIvIcon = (ImageView)mView.findViewById(R.id.volume_icon);
		mTvVolume = (TextView)mView.findViewById(R.id.tv_volume);
		mTvMin = (TextView)mView.findViewById(R.id.tv_min);
		mTvMax = (TextView)mView.findViewById(R.id.tv_max);
		mPbVolume = (ProgressBar)mView.findViewById(R.id.pb_volume);
		mLlBottom = mView.findViewById(R.id.ll_bottom);
		mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	}

	public void show(int groupType, int value) {
		String str = mResources.getString(R.string.pull_volume_media);
		int max = 40;
		switch (groupType) {
			case CMDController.STREAM_SYSTEM:
			str = mResources.getString(R.string.pull_volume_system);
			break;
		case CMDController.STREAM_NAVI:
			str = mResources.getString(R.string.pull_volume_navi);
			break;
		case CMDController.STREAM_PHONE:
			str = mResources.getString(R.string.pull_volume_phone);
			break;
		default:
			break;
		}
		mTvVolume.setText(str);
		mTvMax.setText( ""+ value);
		mPbVolume.setMax(max);
		mPbVolume.setProgress(value);

		if (!isShow) {
			mWindowManager.addView(mView, getPeekWindowParams());
			isShow = true;
		}

		mHandler.removeCallbacks(mDismissRunnable);
		mHandler.postDelayed(mDismissRunnable, 3000);
	}

	private Runnable mDismissRunnable = new Runnable() {

		@Override
		public void run() {
			if (isShow) {
				mWindowManager.removeView(mView);
				isShow = false;
			}
		}
	};

	private WindowManager.LayoutParams getPeekWindowParams() {
		WindowManager.LayoutParams params = new WindowManager.LayoutParams();
		params.height = WindowManager.LayoutParams.WRAP_CONTENT;
		params.width = WindowManager.LayoutParams.WRAP_CONTENT;
		params.format = PixelFormat.TRANSLUCENT;
		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				// 表示window不需要获取焦点，也不需要接收各种输入事件          
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				| WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
				| WindowManager.LayoutParams.FLAG_SPLIT_TOUCH;
		params.gravity = Gravity.TOP |Gravity.CENTER_HORIZONTAL;
		//添加动画
		params.windowAnimations = R.style.wn_anim_view;
		params.y = 10;
		return params;
	}

//	public void onStyleChanged(int showMode) {
//		switch (showMode) {
//		case ConstantsSystemUI.SHOW_MODE_DAYTIME:
//			mIvIcon.setImageResource(R.drawable.volume_layout_icon_day);
//			mTvVolume.setBackgroundResource(R.drawable.volume_layout_top_bg_day);
//			mTvMin.setTextColor(mContext.getResources().getColor(R.color.text_color_status_bar_time_day));
//			mTvMax.setTextColor(mContext.getResources().getColor(R.color.text_color_status_bar_time_day));
//			mPbVolume.setProgressDrawable(mContext.getResources().getDrawable(R.drawable.volume_pb_progress_drawable_day));
//			mLlBottom.setBackgroundResource(R.drawable.volume_layout_bottom_bg_day);
//			break;
//		case ConstantsSystemUI.SHOW_MODE_NIGHT:
//			mIvIcon.setImageResource(R.drawable.volume_layout_icon);
//			mTvVolume.setBackgroundResource(R.drawable.volume_layout_top_bg);
//			mTvMin.setTextColor(mContext.getResources().getColor(R.color.text_color_status_bar_time_night));
//			mTvMax.setTextColor(mContext.getResources().getColor(R.color.text_color_status_bar_time_night));
//			mPbVolume.setProgressDrawable(mContext.getResources().getDrawable(R.drawable.volume_pb_progress_drawable));
//			mLlBottom.setBackgroundResource(R.drawable.volume_layout_bottom_bg);
//			break;
//
//		default:
//			break;
//		}
//	}

//	public void close() {
//		mHandler.removeCallbacks(mDismissRunnable);
//		mDismissRunnable.run();
//	}


}
