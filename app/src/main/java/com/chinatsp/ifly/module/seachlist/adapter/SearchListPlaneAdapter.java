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
import com.chinatsp.ifly.entity.FlightEntity;
import com.chinatsp.ifly.entity.FlightTicketInfo;
import com.chinatsp.ifly.utils.SpannableUtils;
import com.chinatsp.ifly.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchListPlaneAdapter extends SearchListRecyclerViewAdapter<SearchListPlaneAdapter.ViewHolder> {

    private List<FlightEntity> mData;
    private int pageNum = 0;
    private int selectedItem = -1;

    public SearchListPlaneAdapter(List<FlightEntity> list) {
        mData = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_searchlist_plane, parent, false);
        return new ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int allPos = position + pageNum * AppConstant.MAX_PER_PAGE_3;
        FlightEntity entity = mData.get(allPos);
        ArrayList<FlightTicketInfo> ticketInfos = entity.getTikectInfo();
        FlightTicketInfo ticketInfo = null;
        if (ticketInfos != null && ticketInfos.size()>0) {
            ticketInfo = entity.getTikectInfo().get(ticketInfos.size() - 1);
        }
        holder.tvPlaneNumber.setText(String.format("%02d", (position + 1)));
        holder.tvPlaneOriginSite.setText(entity.getdPort());
        holder.tvPlaneOriginTime.setText(Utils.getTimeForHour(entity.getStartTime()));
        holder.tvPlaneDestTime.setText(Utils.getTimeForHour(entity.getArriveTime()));
        holder.tvPlaneDestSite.setText(entity.getaPort());
        if (ticketInfo != null){
            String seatType = ticketInfo.getSeatType();
            if (seatType.contains(":")){
                int index = seatType.indexOf(":");
                seatType = seatType.substring(index+1);
            }
            holder.tvPlaneSeat.setText(seatType + " " + ticketInfo.getDiscount());
            String text = ticketInfo.getRemainingStatus();
            if (!TextUtils.isEmpty(text)) {
                text = text+" 张";
                int start = 0;
                if (text.startsWith(">")) {
                    start = 1;
                }
                SpannableStringBuilder string = SpannableUtils.formatString(text, start, text.indexOf("张"), Color.WHITE);

                holder.tvPlaneTickets.setText(string);
            }
            holder.tvPlanePrice.setText(String.format("¥%s", ticketInfo.getPrice()));
        }

        holder.tvPlaneAccurate.setText(String.format("准点率 %s", entity.getPunctualityRate()));
        String planeInfo = "";
        if (!TextUtils.isEmpty(entity.getAirline())){
            planeInfo = entity.getAirline();
        }
        if (!TextUtils.isEmpty(entity.getFilght())){
            if (!TextUtils.isEmpty(planeInfo)){
                planeInfo = planeInfo+" ";
            }
            planeInfo = planeInfo+entity.getFilght();
        }
        holder.tvPlaneInfo.setText(planeInfo);
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

        @BindView(R.id.tv_plane_number)
        TextView tvPlaneNumber;
        @BindView(R.id.tv_plane_origin_time)
        TextView tvPlaneOriginTime;
        @BindView(R.id.tv_plane_origin_site)
        TextView tvPlaneOriginSite;
        @BindView(R.id.tv_plane_dest_time)
        TextView tvPlaneDestTime;
        @BindView(R.id.tv_plane_dest_site)
        TextView tvPlaneDestSite;
        @BindView(R.id.tv_plane_seat)
        TextView tvPlaneSeat;
        @BindView(R.id.tv_plane_tickets)
        TextView tvPlaneTickets;
        @BindView(R.id.tv_plane_accurate)
        TextView tvPlaneAccurate;
        @BindView(R.id.tv_plane_price)
        TextView tvPlanePrice;
        @BindView(R.id.tv_plane_info)
        TextView tvPlaneInfo;

        public ViewHolder(final View itemView, final SearchListPlaneAdapter adapter) {
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
