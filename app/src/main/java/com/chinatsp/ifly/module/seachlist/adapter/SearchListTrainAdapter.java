package com.chinatsp.ifly.module.seachlist.adapter;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.entity.TrainEntity;
import com.chinatsp.ifly.entity.TrainTicketInfo;
import com.chinatsp.ifly.utils.SpannableUtils;
import com.chinatsp.ifly.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchListTrainAdapter extends SearchListRecyclerViewAdapter<SearchListTrainAdapter.ViewHolder> {

    private List<TrainEntity> mData;
    private int pageNum = 0;
    private int selectedItem = -1;

    public SearchListTrainAdapter(List<TrainEntity> list) {
        mData = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_searchlist_train, parent, false);
        return new ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int allPos = position + pageNum * AppConstant.MAX_PER_PAGE_3;
        TrainEntity entity = mData.get(allPos);
        holder.tvTrainNumber.setText(String.format("%02d", (position + 1)));
        holder.tvTrainOriginTime.setText(Utils.getTimeForHour(entity.getStartTime()));
        int day = Utils.getDay(entity.getStartTime(), entity.getArrivalTime());
        if (day > 0) {
            holder.tvTrainDestRange.setText(" +"+day );
            holder.tvTrainDestRange.setVisibility(View.VISIBLE);
        }else{
            holder.tvTrainDestRange.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(entity.getOriginStation())) {
            holder.tvTrainOriginSite.setText(entity.getOriginStation());
        }

        holder.tvTrainDuringTime.setText(Utils.getTimeForDay(entity.getRunTime()));
        holder.tvTrainDestTime.setText(Utils.getTimeForHour(entity.getArrivalTime()));
        if (!TextUtils.isEmpty(entity.getTerminalStation())) {
            holder.tvTrainDestSite.setText(entity.getTerminalStation());

        }
        ArrayList<TrainTicketInfo> trainTicketInfos = (ArrayList<TrainTicketInfo>) entity.getPrice();
        if (trainTicketInfos != null && trainTicketInfos.size() > 0) {
            for (int i = 0; i < trainTicketInfos.size(); i++) {
                TrainTicketInfo trainTicketInfo = trainTicketInfos.get(i);
                SpannableStringBuilder span1 = null;
                int remainingStatus = Integer.parseInt(trainTicketInfo.getRemainingStatus());
                if (remainingStatus > 0) {
                    String seat1 = String.format("%s: %d 张", trainTicketInfo.getName(), remainingStatus);
                    span1 = SpannableUtils.formatString(seat1, seat1.indexOf(": "), seat1.indexOf("张"), Color.WHITE);
                } else {
                    String seat1 = String.format("%s: %d 张 ( 抢 )", trainTicketInfo.getName(), remainingStatus);
                    span1 = SpannableUtils.formatString(seat1, seat1.indexOf("张") + 1, seat1.length(), Color.RED);
                }
                if (span1 != null) {
                    switch (i) {
                        case 0:
                            holder.tvTrainPrice.setText(String.format("¥%s", trainTicketInfo.getValue()));
                            holder.tvTrainSeat1.setText(span1);
                            continue;
                        case 1:
                            holder.tvTrainSeat2.setText(span1);
                            continue;
                        case 2:
                            holder.tvTrainSeat3.setText(span1);
                            continue;
                        case 3:
                            holder.tvTrainSeat4.setText(span1);
                            continue;
                    }
                }
            }
        }
        holder.tvTrainInfo.setText(entity.getTrainNo());
        //高铁或动车不显示第三种座位
        if (entity.getTrainNo().startsWith("G") || entity.getTrainNo().startsWith("D")) {
            holder.tvTrainSeat3.setVisibility(View.INVISIBLE);
        } else {
            holder.tvTrainSeat3.setVisibility(View.VISIBLE);
        }
        if (selectedItem == allPos) {
            holder.itemView.setBackgroundResource(R.drawable.bg_list_selected);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.bg_list);
        }
    }

    @Override
    public int getItemCount() {
        int count = mData.size() >= (pageNum + 1) * AppConstant.MAX_PER_PAGE_3 ?
                AppConstant.MAX_PER_PAGE_3 : mData.size() % AppConstant.MAX_PER_PAGE_3;
        return count;
    }

    @Override
    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    @Override
    public void selectedItem(int position) {
        this.selectedItem = position;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_train_number)
        TextView tvTrainNumber;
        @BindView(R.id.tv_train_origin_time)
        TextView tvTrainOriginTime;
        @BindView(R.id.tv_train_origin_site)
        TextView tvTrainOriginSite;
        @BindView(R.id.tv_train_during_time)
        TextView tvTrainDuringTime;
        @BindView(R.id.tv_train_dest_time)
        TextView tvTrainDestTime;
        @BindView(R.id.tv_train_dest_range)
        TextView tvTrainDestRange;
        @BindView(R.id.tv_train_dest_site)
        TextView tvTrainDestSite;
        @BindView(R.id.tv_train_seat_2)
        TextView tvTrainSeat2;
        @BindView(R.id.tv_train_seat_1)
        TextView tvTrainSeat1;
        @BindView(R.id.tv_train_seat_4)
        TextView tvTrainSeat4;
        @BindView(R.id.tv_train_seat_3)
        TextView tvTrainSeat3;
        @BindView(R.id.tv_train_price)
        TextView tvTrainPrice;
        @BindView(R.id.tv_train_info)
        TextView tvTrainInfo;

        public ViewHolder(final View itemView, final SearchListTrainAdapter adapter) {
            super(itemView);
            ButterKnife.bind(this, itemView);

//            ivPoiDial.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    adapter.onItemChildViewClick(ViewHolder.this, v);
//                }
//            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    adapter.onItemHolderClick(ViewHolder.this);
                }
            });
        }

    }
}
