package com.hamit.moviearc.IntroUI.IntroAdapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.hamit.moviearc.R;
import com.hamit.moviearc.Utils.AvatarUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ActorRecycler extends RecyclerView.Adapter<ActorRecycler.MyViewHolder> {

    private final OnActorSelectedListener listener;
    List<String> actorList= Arrays.asList("Will Smith", "Keanu Reeves", "Brad Pitt", "Jennifer Lawrence",
            "Samuel L. Jackson", "Betty White", "Robert Downey Jr.");

    private List<String> selectedActors = new ArrayList<>();

    public interface OnActorSelectedListener{
        void onActorCountChanged(int count);
    }

    public ActorRecycler(OnActorSelectedListener listener){
        this.listener= listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MyViewHolder holder= new MyViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.actor_item, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String actor_name= actorList.get(position);

        holder.actorName.setText(actor_name);

        // in case url is available, it can replace null
        AvatarUtils.setAvatar(holder.avatarImage, actor_name, null);

        // Reset background if resued
        if (selectedActors.contains(actor_name)) {
            holder.following.setVisibility(View.VISIBLE);
            holder.actorPicker.setBackgroundResource(R.drawable.red_bg);
        } else {
            holder.following.setVisibility(View.GONE);
            holder.actorPicker.setBackgroundResource(R.drawable.light_dark_bg);
        }

        holder.itemView.setOnClickListener(v->{
            // show the follow Icon
            if (selectedActors.contains(actor_name)) {
                selectedActors.remove(actor_name);
                holder.following.setVisibility(View.GONE);
                holder.actorPicker.setBackgroundResource(R.drawable.light_dark_bg);
            } else {
                selectedActors.add(actor_name);
                holder.following.setVisibility(View.VISIBLE);
                holder.actorPicker.setBackgroundResource(R.drawable.red_bg);
            }

            // notify fragment
            if (listener != null) {
                listener.onActorCountChanged(selectedActors.size());
            }

        });
    }

    @Override
    public int getItemCount() {
        return actorList == null ? 0: actorList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private ShapeableImageView avatarImage;
        private TextView actorName;
        private TextView following;
        private RelativeLayout actorPicker;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            avatarImage= itemView.findViewById(R.id.avatar_imageView);
            actorName= itemView.findViewById(R.id.actorName);
            following= itemView.findViewById(R.id.followTextView);
            actorPicker= itemView.findViewById(R.id.actorPicker);
        }
    }
}
