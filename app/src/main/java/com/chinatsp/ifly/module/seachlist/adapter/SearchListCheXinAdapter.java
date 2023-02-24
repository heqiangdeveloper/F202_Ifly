package com.chinatsp.ifly.module.seachlist.adapter;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.base.BaseApplication;
import com.chinatsp.ifly.entity.CheXinEntity;

import java.util.List;

public class SearchListCheXinAdapter extends SearchListRecyclerViewAdapter<SearchListCheXinAdapter.ViewHolder> {

    private List<CheXinEntity> mData;
    private int pageNum = 0;
    private int selectedItem = -1;

    public SearchListCheXinAdapter(List<CheXinEntity> list) {
        mData = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_searchlist_chexin, parent, false);
        return new ViewHolder(view, this);
    }


    @Override
    public void onBindViewHolder(@NonNull SearchListCheXinAdapter.ViewHolder holder, int position) {
        int allPos = position + pageNum * AppConstant.MAX_PER_PAGE_4;
        CheXinEntity entity = mData.get(allPos);
        holder.tvNumber.setText(String.valueOf(position + 1));
        holder.tvName.setText(entity.name);
        Glide.with(BaseApplication.getInstance().getApplicationContext())
                .load(entity.originalname)
                .asBitmap()
                .placeholder(R.drawable.default_avatar)
                .into(new BitmapImageViewTarget(holder.ivPortrait){
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(BaseApplication.getInstance().getApplicationContext().getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        holder.ivPortrait.setImageDrawable(circularBitmapDrawable);
                    }

                });

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
    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    @Override
    public void selectedItem(int position) {
        this.selectedItem = position;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvNumber;
        private TextView tvName;
        private ImageView ivPortrait;

        public ViewHolder(View itemView, final SearchListCheXinAdapter adapter) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tv_contact_number);
            tvName = itemView.findViewById(R.id.tv_chexin_name);
            ivPortrait = itemView.findViewById(R.id.iv_chexin_portrait);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.onItemHolderClick(ViewHolder.this);
                }
            });
        }
    }
}
