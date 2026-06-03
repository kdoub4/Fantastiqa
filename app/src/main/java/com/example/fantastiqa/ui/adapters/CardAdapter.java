package com.example.fantastiqa.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fantastiqa.GameState.Card;
import com.example.fantastiqa.GameState.CreatureCard;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying cards.
 * Used in card selection, hand display, and deck browsing.
 */
public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {
    
    private List<Card> cards = new ArrayList<>();
    private OnCardClickListener listener;
    private boolean multiSelectMode = false;
    private List<Card> selectedCards = new ArrayList<>();
    
    public interface OnCardClickListener {
        void onCardClick(Card card);
    }
    
    public CardAdapter(OnCardClickListener listener) {
        this.listener = listener;
    }
    
    public void setCards(List<Card> newCards) {
        this.cards = new ArrayList<>(newCards);
        notifyDataSetChanged();
    }
    
    public void setMultiSelectMode(boolean enabled) {
        this.multiSelectMode = enabled;
        if (!enabled) {
            selectedCards.clear();
        }
        notifyDataSetChanged();
    }
    
    public List<Card> getSelectedCards() {
        return new ArrayList<>(selectedCards);
    }
    
    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // TODO: Inflate card_item layout
        // View view = LayoutInflater.from(parent.getContext())
        //     .inflate(R.layout.card_item, parent, false);
        // return new CardViewHolder(view);
        return null;
    }
    
    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        Card card = cards.get(position);
        holder.bind(card);
    }
    
    @Override
    public int getItemCount() {
        return cards.size();
    }
    
    public class CardViewHolder extends RecyclerView.ViewHolder {
        // TODO: Define your card UI components
        // private ImageView cardImage;
        // private TextView cardName;
        // private TextView cardValue;
        
        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            // TODO: Initialize views
            // cardImage = itemView.findViewById(R.id.card_image);
            // cardName = itemView.findViewById(R.id.card_name);
            // cardValue = itemView.findViewById(R.id.card_value);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Card card = cards.get(position);
                    
                    if (multiSelectMode) {
                        if (selectedCards.contains(card)) {
                            selectedCards.remove(card);
                        } else {
                            selectedCards.add(card);
                        }
                        notifyItemChanged(position);
                    } else {
                        if (listener != null) {
                            listener.onCardClick(card);
                        }
                    }
                }
            });
        }
        
        public void bind(Card card) {
            // TODO: Update UI with card data
            // cardName.setText(card.toString());
            // if (card instanceof CreatureCard) {
            //     CreatureCard creature = (CreatureCard) card;
            //     cardValue.setText(creature.values.toString());
            // }
            
            // Highlight selected cards in multi-select mode
            if (multiSelectMode && selectedCards.contains(card)) {
                itemView.setAlpha(1.0f);
                itemView.setElevation(8f);
            } else if (multiSelectMode) {
                itemView.setAlpha(0.5f);
                itemView.setElevation(0f);
            }
        }
    }
}
