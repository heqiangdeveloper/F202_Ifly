package com.chinatsp.ifly.utils;

import android.content.Context;
import android.graphics.Paint;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.chinatsp.ifly.R;

public class MyToast {
    private static final String TAG = MyToast.class.getSimpleName();

    public static void showToast(Context context, String text, boolean isCenter) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        ToastFactory.getInstance(context).show(context, text, isCenter);
    }

    private static final class ToastFactory {
        private Toast toast = null;
        private final View view;
        private final TextView textView;
        private static ToastFactory toastFactory;

        private ToastFactory(Context context) {
            // 单例持有, 创建全局Toast
            view = LayoutInflater.from(context).inflate(R.layout.eplay_toast, null);
            textView = view.findViewById(R.id.textView1);
        }


        private static ToastFactory getInstance(Context context) {
            if (toastFactory == null) {
                synchronized (ToastFactory.class) {
                    if (toastFactory == null) {
                        toastFactory = new ToastFactory(context);
                    }
                }
            }
            return toastFactory;
        }

        /**
         * 描述: 刷新显示数据和位置
         *
         * @user puzhenwei
         * @date 2019/8/9 15:33
         */
        private void show(Context context, String text, boolean isCenter) {
            synchronized (ToastFactory.class) {
                if (toast != null) {
                    toast.cancel();
                    toast = null;
                }
                toast = new Toast(context);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setView(view);
                textView.setText(text);
                setCusWidth(textView);//防止显示成2行
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }

        private void setCusWidth(TextView title){
            Paint mPaint = title.getPaint();
            ViewGroup.LayoutParams params = title.getLayoutParams();
            params.width = (int) mPaint.measureText(title.getText().toString());
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            title.setLayoutParams(params);
        }
    }

}
