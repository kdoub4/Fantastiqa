package com.example.fantastiqa.screens.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fantastiqa.R;

public class handAdapter extends RecyclerView.Adapter<handAdapter.cardViewHolder>{

    private int mNumberItems;

    public handAdapter(int numberitems) {
        mNumberItems = numberitems;
    }

    @NonNull
    @Override
    public cardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachParentImmediately = false;

        View view = inflater.inflate(R.layout.fcard2, parent, shouldAttachParentImmediately );
        return new cardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull cardViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return mNumberItems;
    }

    class cardViewHolder extends RecyclerView.ViewHolder {
        CardView listHandCardView;

        public cardViewHolder(@NonNull View itemView) {
            super(itemView);
            listHandCardView = (CardView) itemView.findViewById(R.id.cv);
        }
    }
}
