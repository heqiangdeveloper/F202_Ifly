package com.chinatsp.ifly.module.seachlist.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.entity.MXPoiEntity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchListMXPoiAdapter extends SearchListRecyclerViewAdapter<SearchListMXPoiAdapter.ViewHolder> {

    private List<MXPoiEntity> mData;
    private int pageNum = 0;
    private int selectedItem = -1;

    public SearchListMXPoiAdapter(List<MXPoiEntity> list) {
        mData = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_searchlist_mxpoi, parent, false);
        return new ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int allPos = position + pageNum * AppConstant.MAX_PER_PAGE_4;
        MXPoiEntity entity = mData.get(allPos);
        holder.tvPoiNumber.setText(String.format("%02d", (position + 1)));
        holder.tvPoiName.setText(entity.getName());
        holder.tvPoiAddress.setText(entity.getAddress());
        String distanceStr = entity.getDistance();
        if(!TextUtils.isEmpty(distanceStr)) {
            double distance = Double.parseDouble(distanceStr) / 1000.0f;
            holder.tvPoiDistance.setText(String.format("%.2f", distance));
        } else {
            holder.tvPoiDistance.setVisibility(View.INVISIBLE);
        }

        if(TextUtils.isEmpty(entity.getPhone())) {
            holder.ivPoiDial.setVisibility(View.INVISIBLE);
        }

        if (selectedItem == allPos) {
            holder.itemView.setBackgroundResource(R.drawable.bg_list_selected);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.bg_list);
        }
    }

    @Override
    public int getItemCount() {
        int count = mData.size() >= (pageNum + 1) * AppConstant.MAX_PER_PAGE_4 ?
                AppConstant.MAX_PER_PAGE_4 : mData.size() % AppConstant.MAX_PER_PAGE_4;
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
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

        @BindView(R.id.tv_poi_number)
        TextView tvPoiNumber;
        @BindView(R.id.tv_poi_name)
        TextView tvPoiName;
        @BindView(R.id.tv_poi_address)
        TextView tvPoiAddress;
        @BindView(R.id.iv_poi_dial)
        ImageView ivPoiDial;
        @BindView(R.id.tv_poi_distance)
        TextView tvPoiDistance;

        public ViewHolder(final View itemView, final SearchListMXPoiAdapter adapter) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    adapter.onItemHolderClick(ViewHolder.this);
                }
            });
        }

    }
}
