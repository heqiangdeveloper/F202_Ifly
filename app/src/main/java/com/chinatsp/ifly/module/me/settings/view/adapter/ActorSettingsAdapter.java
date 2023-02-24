package com.chinatsp.ifly.module.me.settings.view.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.chinatsp.ifly.R;
import com.chinatsp.ifly.base.BaseRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ActorSettingsAdapter extends BaseRecyclerViewAdapter<ActorSettingsAdapter.ViewHolder> {

    private List<String> mActors;
    private List<Integer> isCheck = new ArrayList<>();

    public ActorSettingsAdapter(List<String> datas) {
        this.mActors = datas;
    }

    public void setCurrentActor(int position) {
        this.isCheck.clear();
        this.isCheck.add(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_settings_actor, parent, false);
        return new ViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvActorName.setText(mActors.get(position));
        if(isCheck.contains(position)) {
            holder.cbActor.setChecked(true);
            holder.itemView.setBackgroundResource(R.drawable.bg_list_selected);
        } else {
            holder.cbActor.setChecked(false);
            holder.itemView.setBackgroundResource(android.R.color.transparent);
        }
    }

    @Override
    public int getItemCount() {
        return mActors.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_actor_name)
        TextView tvActorName;
        @BindView(R.id.cb_actor)
        CheckBox cbActor;

        ViewHolder(View itemView, final ActorSettingsAdapter adapter) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!adapter.isCheck.contains(getAdapterPosition())) {
                        adapter.onItemHolderClick(ViewHolder.this);
                        adapter.isCheck.clear();
                        adapter.isCheck.add(getAdapterPosition());
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }
}
