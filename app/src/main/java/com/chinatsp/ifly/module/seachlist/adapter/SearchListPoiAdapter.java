package com.chinatsp.ifly.module.seachlist.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.entity.PoiEntity;
import com.chinatsp.ifly.utils.StarEnum;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchListPoiAdapter extends SearchListRecyclerViewAdapter<SearchListPoiAdapter.ViewHolder> {

    private List<PoiEntity> mData;
    private int pageNum = 0;
    private int selectedItem = -1;

    private static int  size;

    public SearchListPoiAdapter(List<PoiEntity> list) {
        mData = list;
        size=mData.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_searchlist_poi, parent, false);
        return new ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int allPos = position + pageNum * AppConstant.MAX_PER_PAGE_4;
        PoiEntity entity = mData.get(allPos);
        holder.tvPoiNumber.setText(String.format("%02d", (position + 1)));
        holder.tvPoiName.setText(entity.getName());
//        holder.tvPoiTag.setText(entity.getTag());
        holder.tvPoiAddress.setText(entity.getAddress());

        String distanceStr = entity.getDistance();
        if(!TextUtils.isEmpty(distanceStr)) {
            double distance = Double.parseDouble(distanceStr) / 1000.0f;
            holder.tvPoiDistance.setText(String.format("%.2f", distance));
        } else {
            holder.tvPoiDistance.setVisibility(View.INVISIBLE);
        }
//        setRateStar(holder, entity.getRate());
        if(TextUtils.isEmpty(entity.getPhone())) {
            holder.ivPoiDial.setVisibility(View.INVISIBLE);
        }
       /* if(!TextUtils.isEmpty( entity.getPrice())) {
            holder.tvPoiCost.setText("人均： ¥" + entity.getPrice());
        } else {
            holder.tvPoiCost.setVisibility(View.INVISIBLE);
        }*/

        if(!TextUtils.isEmpty(entity.getScore())) {
            float a = Float.valueOf(entity.getScore());
            float b = (float) (Math.floor(a * 2) / 2.0f);
//            setRateStar(holder, b);
        } else {
            holder.rootRateStar.setVisibility(View.INVISIBLE);
        }
        holder.tvPoiTag.setVisibility(View.INVISIBLE);


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
//        return mData.get(position).getType();
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

    private void setRateStar(ViewHolder holder, float rateStar) {
        if (StarEnum.isInclude(rateStar)) {
            if (StarEnum.HALF_STAR.getValue() == rateStar) {
                holder.ivPoiStar1.setVisibility(View.VISIBLE);
                holder.ivPoiStar2.setVisibility(View.INVISIBLE);
                holder.ivPoiStar3.setVisibility(View.INVISIBLE);
                holder.ivPoiStar4.setVisibility(View.INVISIBLE);
                holder.ivPoiStar5.setVisibility(View.INVISIBLE);
                holder.ivPoiStar1.setImageResource(R.drawable.ic_star_half);
            } else if (StarEnum.ONE_STAR.getValue() == rateStar) {
                holder.ivPoiStar1.setVisibility(View.VISIBLE);
                holder.ivPoiStar2.setVisibility(View.INVISIBLE);
                holder.ivPoiStar3.setVisibility(View.INVISIBLE);
                holder.ivPoiStar4.setVisibility(View.INVISIBLE);
                holder.ivPoiStar5.setVisibility(View.INVISIBLE);
                holder.ivPoiStar1.setImageResource(R.drawable.ic_star_full);
            } else if (StarEnum.ONE_HALF_STAR.getValue() == rateStar) {
                holder.ivPoiStar1.setVisibility(View.VISIBLE);
                holder.ivPoiStar2.setVisibility(View.VISIBLE);
                holder.ivPoiStar3.setVisibility(View.INVISIBLE);
                holder.ivPoiStar4.setVisibility(View.INVISIBLE);
                holder.ivPoiStar5.setVisibility(View.INVISIBLE);
                holder.ivPoiStar1.setImageResource(R.drawable.ic_star_full);
                holder.ivPoiStar2.setImageResource(R.drawable.ic_star_half);
            } else if (StarEnum.TWO_STAR.getValue() == rateStar) {
                holder.ivPoiStar1.setVisibility(View.VISIBLE);
                holder.ivPoiStar2.setVisibility(View.VISIBLE);
                holder.ivPoiStar3.setVisibility(View.INVISIBLE);
                holder.ivPoiStar4.setVisibility(View.INVISIBLE);
                holder.ivPoiStar5.setVisibility(View.INVISIBLE);
                holder.ivPoiStar1.setImageResource(R.drawable.ic_star_full);
                holder.ivPoiStar2.setImageResource(R.drawable.ic_star_full);
            } else if (StarEnum.TWO_HALF_STAR.getValue() == rateStar) {
                holder.ivPoiStar1.setVisibility(View.VISIBLE);
                holder.ivPoiStar2.setVisibility(View.VISIBLE);
                holder.ivPoiStar3.setVisibility(View.VISIBLE);
                holder.ivPoiStar4.setVisibility(View.INVISIBLE);
                holder.ivPoiStar5.setVisibility(View.INVISIBLE);
                holder.ivPoiStar1.setImageResource(R.drawable.ic_star_full);
                holder.ivPoiStar2.setImageResource(R.drawable.ic_star_full);
                holder.ivPoiStar3.setImageResource(R.drawable.ic_star_half);
            } else if (StarEnum.THREE_STAR.getValue() == rateStar) {
                holder.ivPoiStar1.setVisibility(View.VISIBLE);
                holder.ivPoiStar2.setVisibility(View.VISIBLE);
                holder.ivPoiStar3.setVisibility(View.VISIBLE);
                holder.ivPoiStar4.setVisibility(View.INVISIBLE);
                holder.ivPoiStar5.setVisibility(View.INVISIBLE);
                holder.ivPoiStar1.setImageResource(R.drawable.ic_star_full);
                holder.ivPoiStar2.setImageResource(R.drawable.ic_star_full);
                holder.ivPoiStar3.setImageResource(R.drawable.ic_star_full);
            } else if (StarEnum.THREE_HALF_STAR.getValue() == rateStar) {
                holder.ivPoiStar1.setVisibility(View.VISIBLE);
                holder.ivPoiStar2.setVisibility(View.VISIBLE);
                holder.ivPoiStar3.setVisibility(View.VISIBLE);
                holder.ivPoiStar4.setVisibility(View.VISIBLE);
                holder.ivPoiStar5.setVisibility(View.INVISIBLE);
                holder.ivPoiStar1.setImageResource(R.drawable.ic_star_full);
                holder.ivPoiStar2.setImageResource(R.drawable.ic_star_full);
                holder.ivPoiStar3.setImageResource(R.drawable.ic_star_full);
                holder.ivPoiStar4.setImageResource(R.drawable.ic_star_half);
            } else if (StarEnum.FOUR_STAR.getValue() == rateStar) {
                holder.ivPoiStar1.setVisibility(View.VISIBLE);
                holder.ivPoiStar2.setVisibility(View.VISIBLE);
                holder.ivPoiStar3.setVisibility(View.VISIBLE);
                holder.ivPoiStar4.setVisibility(View.VISIBLE);
                holder.ivPoiStar5.setVisibility(View.INVISIBLE);
                holder.ivPoiStar1.setImageResource(R.drawable.ic_star_full);
                holder.ivPoiStar2.setImageResource(R.drawable.ic_star_full);
                holder.ivPoiStar3.setImageResource(R.drawable.ic_star_full);
                holder.ivPoiStar4.setImageResource(R.drawable.ic_star_full);
            } else if (StarEnum.FOUR_HALF_STAR.getValue() == rateStar) {
                holder.ivPoiStar1.setVisibility(View.VISIBLE);
                holder.ivPoiStar2.setVisibility(View.VISIBLE);
                holder.ivPoiStar3.setVisibility(View.VISIBLE);
                holder.ivPoiStar4.setVisibility(View.VISIBLE);
                holder.ivPoiStar5.setVisibility(View.VISIBLE);
                holder.ivPoiStar1.setImageResource(R.drawable.ic_star_full);
                holder.ivPoiStar2.setImageResource(R.drawable.ic_star_full);
                holder.ivPoiStar3.setImageResource(R.drawable.ic_star_full);
                holder.ivPoiStar4.setImageResource(R.drawable.ic_star_full);
                holder.ivPoiStar5.setImageResource(R.drawable.ic_star_half);
            } else if (StarEnum.FIVE_STAR.getValue() == rateStar) {
                holder.ivPoiStar1.setVisibility(View.VISIBLE);
                holder.ivPoiStar2.setVisibility(View.VISIBLE);
                holder.ivPoiStar3.setVisibility(View.VISIBLE);
                holder.ivPoiStar4.setVisibility(View.VISIBLE);
                holder.ivPoiStar5.setVisibility(View.VISIBLE);
                holder.ivPoiStar1.setImageResource(R.drawable.ic_star_full);
                holder.ivPoiStar2.setImageResource(R.drawable.ic_star_full);
                holder.ivPoiStar3.setImageResource(R.drawable.ic_star_full);
                holder.ivPoiStar4.setImageResource(R.drawable.ic_star_full);
                holder.ivPoiStar5.setImageResource(R.drawable.ic_star_full);
            }
        } else {
            Log.e("xyj", "invalid value :" + rateStar);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_poi_number)
        TextView tvPoiNumber;
        @BindView(R.id.tv_poi_name)
        TextView tvPoiName;
        @BindView(R.id.tv_poi_tag)
        TextView tvPoiTag;
        @BindView(R.id.tv_poi_address)
        TextView tvPoiAddress;
        @BindView(R.id.root_rate_star)
        LinearLayout rootRateStar;
        @BindView(R.id.iv_poi_star1)
        ImageView ivPoiStar1;
        @BindView(R.id.iv_poi_star2)
        ImageView ivPoiStar2;
        @BindView(R.id.iv_poi_star3)
        ImageView ivPoiStar3;
        @BindView(R.id.iv_poi_star4)
        ImageView ivPoiStar4;
        @BindView(R.id.iv_poi_star5)
        ImageView ivPoiStar5;
        @BindView(R.id.tv_poi_price)
        TextView tvPoiCost;
        @BindView(R.id.iv_poi_dial)
        ImageView ivPoiDial;
        @BindView(R.id.tv_poi_distance)
        TextView tvPoiDistance;

        public ViewHolder(final View itemView, final SearchListPoiAdapter adapter) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            ivPoiDial.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ViewHolder.this.getAdapterPosition() <size){
                        adapter.onItemChildViewClick(ViewHolder.this, v);
                    }
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    adapter.onItemHolderClick(ViewHolder.this);
                }
            });
        }

    }
}
