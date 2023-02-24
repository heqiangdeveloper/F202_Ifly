package com.chinatsp.ifly.module.me.settings.view.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chinatsp.ifly.R;
import com.chinatsp.ifly.module.me.settings.view.entity.VoiceHelperModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VoiceHelperAdapter extends BaseAdapter {
    private final static String TAG = "VoiceHelperAdapter";
    private List<VoiceHelperModel> voiceHelperModelList = new ArrayList<>();
    private Context mContext;
    private ViewHolder viewHolder = null;

    public VoiceHelperAdapter(Context context, List<VoiceHelperModel> voiceHelperModelList) {
        this.mContext = context;
        this.voiceHelperModelList = voiceHelperModelList;
    }

    @Override
    public int getCount() {
        return voiceHelperModelList == null ? 0 : voiceHelperModelList.size();
    }

    @Override
    public Object getItem(int position) {
        return voiceHelperModelList == null ? null : voiceHelperModelList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_voice_helper, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        VoiceHelperModel voiceHelperModel = voiceHelperModelList.get(position);
        if (voiceHelperModel.isCheck) { //选中
            viewHolder.mLl_item.setBackgroundResource(R.drawable.voice_help_press);
        } else { //未选中
            viewHolder.mLl_item.setBackgroundResource(R.drawable.voice_help);
        }
        viewHolder.tv_function_name.setText(voiceHelperModel.functionName);
        if (voiceHelperModel.picDrawable != null) {
            viewHolder.tv_function_name.setCompoundDrawablesWithIntrinsicBounds(voiceHelperModel.picDrawable, null, null, null);
        }
        viewHolder.tv_action_1.setText("“" + voiceHelperModel.action1 + "”");
        if(TextUtils.isEmpty(voiceHelperModel.action2)){
            viewHolder.tv_action_2.setText("");
            viewHolder.tv_action_3.setText("");
            viewHolder.tv_action_4.setText("");
        }else {
            viewHolder.tv_action_2.setText("“" + voiceHelperModel.action2 + "”");
            viewHolder.tv_action_3.setText("“" + voiceHelperModel.action3 + "”");
            viewHolder.tv_action_4.setText("“" + voiceHelperModel.action4 + "”");
        }
        return convertView;
    }

    public class ViewHolder {
        @BindView(R.id.tv_action_1)
        TextView tv_action_1;
        @BindView(R.id.tv_action_2)
        TextView tv_action_2;
        @BindView(R.id.tv_action_3)
        TextView tv_action_3;
        @BindView(R.id.tv_action_4)
        TextView tv_action_4;
        @BindView(R.id.tv_function_name)
        TextView tv_function_name;
        @BindView(R.id.ll_item)
        LinearLayout mLl_item;

        ViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}
