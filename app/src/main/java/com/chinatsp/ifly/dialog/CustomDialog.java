package com.chinatsp.ifly.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chinatsp.ifly.DatastatManager;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;

import org.w3c.dom.Text;

public class CustomDialog extends Dialog {


    public CustomDialog(Context context) {
        super(context);
    }

    public CustomDialog(Context context, int theme) {
        super(context, theme);
    }

    public static class Builder {
        private Context context;
        private CheckBox mCbShown;
        private DialogInterface.OnClickListener positiveButtonClickListener;
        private DialogInterface.OnClickListener negativeButtonClickListener;
        private LinearLayout ll_select_shown;

        public Builder(Context context) {
            this.context = context;
        }


        public Builder setPositiveButton(
                DialogInterface.OnClickListener listener) {
            this.positiveButtonClickListener = listener;
            return this;
        }

        public Builder setNegativeButton(DialogInterface.OnClickListener listener) {
            this.negativeButtonClickListener = listener;
            return this;
        }


        public CustomDialog create() {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final CustomDialog dialog = new CustomDialog(context, R.style.style_relief);
            View layout = inflater.inflate(R.layout.dialog_detial_relief, null);
            dialog.setContentView(layout);
            mCbShown = ((CheckBox)layout.findViewById(R.id.ck_relief));
            ll_select_shown =(LinearLayout)layout.findViewById(R.id.ll_select_shown);
            ll_select_shown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCbShown.setChecked(!mCbShown.isChecked());
                }
            });
            if (positiveButtonClickListener != null) {
                ((Button) layout.findViewById(R.id.btn_relief_sure))
                        .setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                Log.d("liqw", "onClick() called with: v = [" + mCbShown.isChecked() + "]");
                                SharedPreferencesUtils.saveBoolean(context,AppConstant.KEY_RELIEF_SHOWN,!mCbShown.isChecked());
                                positiveButtonClickListener.onClick(dialog,
                                        DialogInterface.BUTTON_POSITIVE);
                            }
                        });
            }


            if (negativeButtonClickListener != null) {
                ((Button) layout.findViewById(R.id.btn_relief_cancel))
                        .setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                negativeButtonClickListener.onClick(dialog,
                                        DialogInterface.BUTTON_NEGATIVE);
                            }
                        });
            }

            dialog.setContentView(layout);
            DatastatManager.getInstance().recordUI_event(context, context.getString(R.string.event_id_never_notice), "");
            return dialog;
        }
    }
}