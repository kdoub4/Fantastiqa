package com.example.fantastiqa.screens.views;

import android.graphics.Color;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fantastiqa.R;
import com.example.fantastiqa.GameState.Card;
import com.example.fantastiqa.GameState.CreatureCard;
import com.example.fantastiqa.GameState.Symbol;

public class handAdapter extends RecyclerView.Adapter<handAdapter.cardViewHolder>{

	private Card[] mDataset;
    private int mNumberItems;
    final private HandClickListener mListener;
    private CardView theCard;

	private Boolean isClickable=false;

    public handAdapter(int numberitems, HandClickListener aListener) {
        mNumberItems = numberitems;
        mListener = aListener;
    }

	public void setDataset(Card[] aDataset){
			mDataset = aDataset;
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
		if (mDataset.length <= position) {
			holder.setVisibility(View.GONE);
		} 
		else if ((mDataset[position]) instanceof CreatureCard) {
 			holder.setVisibility(View.VISIBLE);
 			CreatureCard boundCreature = (CreatureCard)mDataset[position];
			//TODO extract into method
			setImage(boundCreature.values.get(0),holder.power1);
			
			if (boundCreature.values.size() >1) {
				holder.power2.setVisibility(View.VISIBLE);
				setImage(((CreatureCard)mDataset[position]).values.get(1),holder.power2);
			} else {
				holder.power2.setVisibility(View.INVISIBLE);
			}
			
			if (boundCreature.subduedBy == Symbol.NONE) {
				holder.subdueBy.setVisibility(View.INVISIBLE);
			} else {
				holder.subdueBy.setVisibility(View.VISIBLE);
				setImage(((CreatureCard)mDataset[position]).subduedBy,holder.subdueBy);
			}
			if (boundCreature.gem) {
				holder.gems.setVisibility(View.VISIBLE);
			}
			else {
				holder.gems.setVisibility(View.INVISIBLE);
			}
		}
		holder.setClickable(isClickable); 
		holder.setBackground(isClickable ? Color.GREEN : Color.WHITE);
    }

	public void setHandClicks(Boolean enable){
			isClickable = enable;
			notifyDataSetChanged();
	}
	
    private void setImage(Symbol aSymbol, ImageView theView){
		if (aSymbol == Symbol.NONE) {
			theView.setVisibility(View.INVISIBLE);
		}
		else {
			theView.setVisibility(View.VISIBLE);
		switch (aSymbol) {
			case SWORD : 
				theView.setImageResource(R.drawable.sword);
				break;
			case WAND : 
				theView.setImageResource(R.drawable.wand);
				break;
			case BAT : 
				theView.setImageResource(R.drawable.club);
				break;
			case HELMET : 
				theView.setImageResource(R.drawable.helmet);
				break;
			case NET : 
				theView.setImageResource(R.drawable.net);
				break;
			case TOOTH : 
				theView.setImageResource(R.drawable.tooth);
				break;
			case BROOM : 
				theView.setImageResource(R.drawable.broom);
				break;
			case WATER : 
				theView.setImageResource(R.drawable.water);
				break;
			case FIRE : 
				theView.setImageResource(R.drawable.fire);
				break;	
		}
		}
	}

    @Override
    public int getItemCount() {
        return mDataset.length;
    }

	public interface HandClickListener {
		void onHandClick(View v );
	}
    class cardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CardView listHandCardView;
    public ImageView power1;
    public ImageView power2;
    public ImageView ability;
    public ImageView subdueBy;
    public ImageView gems;

        public cardViewHolder(@NonNull View view) {
            super(view);
            listHandCardView = (CardView) view.findViewById(R.id.cv);
            power1 = (ImageView) view.findViewById(R.id.power1);
        power2 = (ImageView) view.findViewById(R.id.power2);
        ability = (ImageView) view.findViewById(R.id.ability);
        subdueBy = (ImageView) view.findViewById(R.id.subdueBy);
        gems = (ImageView) view.findViewById(R.id.gem);
        
            itemView.setOnClickListener(this);
        }
        
        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            v.setTag(mDataset[getAdapterPosition()]);
            mListener.onHandClick(v);
            v.setTag(null);
        }
        
        public void setClickable(Boolean enable){
			listHandCardView.setClickable(enable);
		}
        
        public void setVisibility(int set){
			listHandCardView.setVisibility(set);
		}
		
		public void setBackground(int color){
			listHandCardView.setCardBackgroundColor(color);
		}
    }
}
