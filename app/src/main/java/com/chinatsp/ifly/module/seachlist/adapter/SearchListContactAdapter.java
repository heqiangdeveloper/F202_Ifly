package com.chinatsp.ifly.module.seachlist.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.entity.ContactEntity;

import java.util.List;

public class SearchListContactAdapter extends SearchListRecyclerViewAdapter<SearchListContactAdapter.ViewHolder> {

    private List<ContactEntity> mData;
    private int pageNum = 0;
    private int selectedItem = -1;

    public SearchListContactAdapter(List<ContactEntity> list) {
        mData = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_searchlist_contact, parent, false);
        return new ViewHolder(view, this);
    }


    @Override
    public void onBindViewHolder(@NonNull SearchListContactAdapter.ViewHolder holder, int position) {
        int allPos = position + pageNum * AppConstant.MAX_PER_PAGE_4;
        ContactEntity entity = mData.get(allPos);
        holder.tvNumber.setText(String.valueOf(position + 1));
        holder.tvName.setText(entity.name);
        holder.tvTelephone.setText(entity.phoneNumber);

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
        private TextView tvTelephone;

        public ViewHolder(View itemView, final SearchListContactAdapter adapter) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tv_contact_number);
            tvName = itemView.findViewById(R.id.tv_contact_name);
            tvTelephone = itemView.findViewById(R.id.tv_contact_telephone);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.onItemHolderClick(ViewHolder.this);
                }
            });
        }
    }
}
