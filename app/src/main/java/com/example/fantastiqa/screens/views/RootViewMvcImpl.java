package com.example.fantastiqa.screens.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.fantastiqa.GameState.Card;
import com.example.fantastiqa.GameState.Player;
import com.example.fantastiqa.GameState.Quest;
import com.example.fantastiqa.GameState.Region;
import com.example.fantastiqa.GameState.Road;
import com.example.fantastiqa.GameState.Symbol;
import com.example.fantastiqa.GameState.RegionName;
import com.example.fantastiqa.GameState.TowerName;
import com.example.fantastiqa.R;
import com.example.fantastiqa.screens.GameStatus;
import com.example.fantastiqa.screens.spaceRegion;
import com.example.fantastiqa.screens.spaceRoad;
import com.example.fantastiqa.screens.views.EqualSpacingItemDecoration;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/*
 * Very simple MVC view containing just single FrameLayout
 */
public class RootViewMvcImpl implements ViewMvc, handAdapter.HandClickListener  {

    private View mRootView;
    private ViewMvcListener mListener;

    private Button moveButton;
    private Button towerButton;
    private Button storeCardsButton;
    private Button doneButton;
    private Button storeQuestButton;
    private Button carpetButton;
    
    private TextView land1;
    private TextView land2;
    private TextView land3;
    private TextView land4;
    private TextView land5;
    private TextView land6;

    private ConstraintLayout land1c;
    private ConstraintLayout land2c;
    private ConstraintLayout land3c;
    private ConstraintLayout land4c;
    private ConstraintLayout land5c;
    private ConstraintLayout land6c;

    private TextView publicQuest1;
    private TextView publicQuest2;

    private TextView road1;
    private TextView road2;
    private TextView road3;
    private TextView road4;
    private TextView road5;
    private TextView road6;
    private TextView road7;

    private CardView road1c;
    private CardView road2c;
    private CardView road3c;
    private CardView road4c;
    private CardView road5c;
    private CardView road6c;
    private CardView road7c;

	private CardView publicQuest1c;
	private CardView publicQuest2c;
	
    LinkedList<TextView> theHandViews;
    LinkedList<TextView> storage;
    LinkedList<TextView> quests;
    LinkedList<TextView> quest1Storage;
    LinkedList<TextView> quest0Storage;

    private TextView status;
    private TextView tvGems;
    private TextView tvVps;
    private TextView tvDeck;
    private TextView tvDiscard;

    private Context mContext;
    Map<spaceRegion, TextView> regionTextView = new EnumMap<>(spaceRegion.class);
    Map<TextView, spaceRegion> textViewRegion = new HashMap<>();



    private  static final int MAX_HAND_SIZE = 5;
    private handAdapter mAdapter;
    private RecyclerView mHandRV;


    public RootViewMvcImpl(Context context, ViewGroup container) {
        mRootView = LayoutInflater.from(context).inflate(R.layout.activity_main, container);
        mContext = context;

        mHandRV = mRootView.findViewById(R.id.rvHand);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context,RecyclerView.HORIZONTAL,false);
        mHandRV.setLayoutManager(layoutManager);
        mHandRV.setHasFixedSize( false);
        mHandRV.addItemDecoration(new EqualSpacingItemDecoration(16));
        mAdapter = new handAdapter(MAX_HAND_SIZE, this);
        mHandRV.setAdapter(mAdapter);

        status = mRootView.findViewById(R.id.status);
        tvGems = mRootView.findViewById(R.id.gems);
        tvVps = mRootView.findViewById(R.id.vps);
        tvDeck    = mRootView.findViewById(R.id.deck);
        tvDiscard = mRootView.findViewById(R.id.discard);
		moveButton = mRootView.findViewById(R.id.buttonMove);
        moveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onMoveClick(view);
                /*
                if (mListener != null) {
                    mListener.onMoveClick();
                }
                */
            }
        });

        towerButton = mRootView.findViewById(R.id.buttonTower);
        towerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
				onTowerVisitClick();
            }
        });
        
        storeCardsButton = mRootView.findViewById(R.id.buttonStoreCards);
        storeCardsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onStoreCardsClick();
            }
        });
        
        storeQuestButton = mRootView.findViewById(R.id.buttonStoreQuestCards);
        storeQuestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onStoreQuestClick();
            }
        });
        
        doneButton = mRootView.findViewById(R.id.buttonDone);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDoneClick();
            }
        });

		carpetButton = mRootView.findViewById(R.id.buttonFlyingCarpet);
        carpetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFlyingCarpetClick();
            }
        });
        
        land1 = (TextView) mRootView.findViewById(R.id.land1);
        regionTextView.put(spaceRegion.NE,land1);
        textViewRegion.put(land1,spaceRegion.NE);
        land2 = (TextView) mRootView.findViewById(R.id.land2);
        regionTextView.put(spaceRegion.E,land2);
        textViewRegion.put(land2,spaceRegion.E);
        land3 = (TextView) mRootView.findViewById(R.id.land3);
        regionTextView.put(spaceRegion.SE,land3);
        textViewRegion.put(land3,spaceRegion.SE);
        land4 = (TextView) mRootView.findViewById(R.id.land4);
        regionTextView.put(spaceRegion.SW,land4);
        textViewRegion.put(land4,spaceRegion.SW);
        land5 = (TextView) mRootView.findViewById(R.id.land5);
        regionTextView.put(spaceRegion.W,land5);
        textViewRegion.put(land5,spaceRegion.W);
        land6 = (TextView) mRootView.findViewById(R.id.land6);
        regionTextView.put(spaceRegion.NW,land6);
        textViewRegion.put(land6,spaceRegion.NW);
        
        land1c = (ConstraintLayout) mRootView.findViewById(R.id.landc1);
        land1c.setTag(spaceRegion.NE);
        land2c = (ConstraintLayout) mRootView.findViewById(R.id.landc2);
        land2c.setTag(spaceRegion.E);
        land3c = (ConstraintLayout) mRootView.findViewById(R.id.landc3);
        land3c.setTag(spaceRegion.SE);
        land4c = (ConstraintLayout) mRootView.findViewById(R.id.landc4);
        land4c.setTag(spaceRegion.SW);
        land5c = (ConstraintLayout) mRootView.findViewById(R.id.landc5);
        land5c.setTag(spaceRegion.W);
        land6c = (ConstraintLayout) mRootView.findViewById(R.id.landc6);
        land6c.setTag(spaceRegion.NW);
        
        land1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
				onLandClick(view);
            }
        });
        land2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    onLandClick(view);
                
            }
        });
        land3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    onLandClick(view);
                
            }
        });
        land4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    onLandClick(view);
                
            }
        });
        land5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    onLandClick(view);
                
            }
        });
        land6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    onLandClick(view);
                
            }
        });

        land1c.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
				onLandClick(view);
            }
        });
        land2c.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    onLandClick(view);
                
            }
        });
        land3c.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    onLandClick(view);
                
            }
        });
        land4c.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    onLandClick(view);
                
            }
        });
        land5c.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    onLandClick(view);
                
            }
        });
        land6c.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    onLandClick(view);
                
            }
        });
        
        road1 = mRootView.findViewById(R.id.road1);
        road2 = mRootView.findViewById(R.id.road2);
        road3 = mRootView.findViewById(R.id.road3);
        road4 = mRootView.findViewById(R.id.road4);
        road5 = mRootView.findViewById(R.id.road5);
        road6 = mRootView.findViewById(R.id.road6);
        road7 = mRootView.findViewById(R.id.road7);

        road1c = mRootView.findViewById(R.id.roadc1);
        road2c = mRootView.findViewById(R.id.roadc2);
        road3c = mRootView.findViewById(R.id.roadc3);
        road4c = mRootView.findViewById(R.id.roadc4);
        road5c = mRootView.findViewById(R.id.roadc5);
        road6c = mRootView.findViewById(R.id.roadc6);
        road7c = mRootView.findViewById(R.id.roadc7);

        publicQuest1 = mRootView.findViewById(R.id.publicQuest1);
        publicQuest2 = mRootView.findViewById(R.id.publicQuest2);

		publicQuest1c = mRootView.findViewById(R.id.questc1);
        publicQuest2c = mRootView.findViewById(R.id.questc2);
        
        publicQuest1c.findViewById(R.id.ability).setVisibility(View.GONE);
        publicQuest1c.findViewById(R.id.subdueBy).setVisibility(View.GONE);
        publicQuest2c.findViewById(R.id.ability).setVisibility(View.GONE);
        publicQuest2c.findViewById(R.id.subdueBy).setVisibility(View.GONE);

        theHandViews = new LinkedList<TextView>(){ {
            add((TextView)mRootView.findViewById(R.id.hand1));
            add((TextView)mRootView.findViewById(R.id.hand2));
            add((TextView)mRootView.findViewById(R.id.hand3));
            add((TextView)mRootView.findViewById(R.id.hand4));
            add((TextView)mRootView.findViewById(R.id.hand5));
            add((TextView)mRootView.findViewById(R.id.hand6));
            add((TextView)mRootView.findViewById(R.id.hand7));
            add((TextView)mRootView.findViewById(R.id.hand8));
            add((TextView)mRootView.findViewById(R.id.hand9));
            add((TextView)mRootView.findViewById(R.id.hand10));
        }};
        
        storage = new LinkedList<TextView>() { {
			add((TextView)mRootView.findViewById(R.id.stored0));
			add((TextView)mRootView.findViewById(R.id.stored1));
        	add((TextView)mRootView.findViewById(R.id.stored2));
        	add((TextView)mRootView.findViewById(R.id.stored3));
        	add((TextView)mRootView.findViewById(R.id.stored4));
        }};
        
        for (TextView tvStorage : storage) {
			tvStorage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    onStorageClick(view);
                
            }
        });

		}

        
        quests = new LinkedList<TextView>() { {
			add((TextView)mRootView.findViewById(R.id.quest0));
			add((TextView)mRootView.findViewById(R.id.quest1));
		}};
                
        quest0Storage = new LinkedList<TextView>() { {
			add((TextView)mRootView.findViewById(R.id.quest00));
			add((TextView)mRootView.findViewById(R.id.quest01));
			add((TextView)mRootView.findViewById(R.id.quest02));
			add((TextView)mRootView.findViewById(R.id.quest03));
			add((TextView)mRootView.findViewById(R.id.quest04));
		}};
        
        quest1Storage = new LinkedList<TextView>() { {
			add((TextView)mRootView.findViewById(R.id.quest10));
			add((TextView)mRootView.findViewById(R.id.quest11));
			add((TextView)mRootView.findViewById(R.id.quest12));
			add((TextView)mRootView.findViewById(R.id.quest13));
			add((TextView)mRootView.findViewById(R.id.quest14));
		}};
		
        ListIterator<TextView> listIter = theHandViews.listIterator(0);
        while (listIter.hasNext()) {
			((TextView)listIter.next()).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mListener != null) {
						onHandClick(v);
					}
				}
			});
		}

        listIter = quests.listIterator(0);;
        while (listIter.hasNext()) {
            listIter.next().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        onQuestClick(v);
                    }
                }
            });
        }
        publicQuest1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    onQuestClick(v);
                }
            }
        });
        publicQuest2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    onQuestClick(v);
                }
            }
        });
    }

    public void onQuestClick(View v) {
        v.setClickable(false);
        mListener.beginCompleteQuest((Quest)v.getTag());
    }
	
	@Override
	public void onMoveClick(View v) {
		onMoveClick();
	}

	@Override
	public void onMoveClick(){
		for (spaceRegion aRegion : mListener.getValidAdventuring()) {
			regionTextView.get(aRegion).setTextColor(Color.YELLOW);
			setLandBackground(aRegion,Color.GREEN);
		}
	}
	
	private void setLandBackground(spaceRegion aRegion, int aColor) {
		switch (aRegion) {
			case NE:
				land1c.setBackgroundColor(aColor);
				break;
			case E:
				land2c.setBackgroundColor(aColor);
				break;
			case SE:
				land3c.setBackgroundColor(aColor);
				break;
			case SW:
				land4c.setBackgroundColor(aColor);
				break;
			case W:
				land5c.setBackgroundColor(aColor);
				break;
			case NW:
				land6c.setBackgroundColor(aColor);
				break;
		}

	}
	
	@Override
	public void onFlyingCarpetClick() {
		for (spaceRegion aRegion : mListener.beginFlyingCarpet()) {
			regionTextView.get(aRegion).setTextColor(Color.YELLOW);
		}
	}

	@Override
	public void onTowerVisitClick() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mRootView.getContext());
		CharSequence[] items = {"Teleport","Cards","Remove"}; 
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
					case 0: mListener.towerTeleport(); break;
					case 1: beginTowerCards(); break;// mListener.beginVisitTowerCards(); break;
                    case 2:
                        beginReleaseCards();
                        break;

				}
			}
		}
		);
		builder.show();
    }

	@Override
    public void beginReleaseCards() {
        for (Card releasable : mListener.beginReleaseCard()) {
			mAdapter.setCardEnabled(releasable,true);
            for (TextView tvHand : theHandViews) {
                if (tvHand.getTag() == releasable) {
                    tvHand.setTextColor(Color.YELLOW);
                }
            }
        }
    }
	
	private void beginTowerCards() {
		mListener.beginVisitTowerCards();
	}
	
	@Override
    public void selectCard(List<? extends Card> towerCards, List<Boolean> enabled) {
		CharSequence[] dialogItems = new CharSequence[towerCards.size()];
        int checkedItem = 0;
		for (int i=0; i<towerCards.size(); i++) {
			dialogItems[i]=towerCards.get(i).name;
		}
		AlertDialog.Builder buyDialog = new AlertDialog.Builder(mRootView.getContext());
        buyDialog.setItems(dialogItems, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				mListener.endVisitTowerCards(which);
			}
		  }
		);
        AlertDialog buyD = buyDialog.create();
        buyD.show();
        ListView list = buyD.getListView();
        final ListAdapter adaptor = buyD.getListView().getAdapter();
        for (int i = 0; i < dialogItems.length; i++) { // index
            if (enabled.get(i) == false) {
                // Disable choice in dialog
                adaptor.getView(i, null, list).setEnabled(false);
                adaptor.getView(i, null, list).setBackgroundColor(Color.BLACK);

            } else {
                // Enable choice in dialog
                adaptor.getView(i, null, list).setEnabled(true);
            }
        }

	}
	
	@Override
	public void onStoreCardsClick() {
		mListener.beginStoreCardsPrivate();
	}
	
	@Override
	public void onStoreQuestClick() {
		mListener.beginStoreCardsQuest();
	}

//graphic hand click
	/*@Override
	public void onHandClick(int position) {
		
	}
	*/
	@Override
	public void onHandClick(View v) {
		//TODO enable click
		Log.d("fantastiqa : handClick", v.toString());
		if (v instanceof CardView) {
			Log.d("fantastiqa", "handclick");
			mListener.handClick((Card) v.getTag());
		} else
        if (v.getTag() instanceof Card && ((TextView) v).getCurrentTextColor() == Color.YELLOW)
            mListener.handClick((Card) v.getTag());
	}
	
	@Override
	public void onLandClick(View v) {
		if (v instanceof ConstraintLayout) {
			mListener.doMove((spaceRegion)v.getTag());
			//setLandBackground((spaceRegion)v.getTag(),Color.WHITE);
		}
		else {
			if (mListener.doMove(textViewRegion.get(v))) {
				//setLandColor(Color.WHITE);
			}
		}
	}
	
	public void onStorageClick(View v){
		if (v.getTag() instanceof Card) mListener.discardFromStorage((Card)v.getTag());
	}
	
	@Override
    public void gameStateChange(GameStatus newState) {
		status.setText(newState.toString());
        switch (newState) {
		case MOVING:
			moveButton.setEnabled(false);
			towerButton.setEnabled(false);
			storeCardsButton.setEnabled(false);
			storeQuestButton.setEnabled(false);
			doneButton.setEnabled(true);
			break;
		case OPEN :
			moveButton.setEnabled(true);
			towerButton.setEnabled(true);
			storeCardsButton.setEnabled(true);
			storeQuestButton.setEnabled(true);
			doneButton.setEnabled(true);
			setHandColor(Color.WHITE);
			setLandColor(Color.WHITE);
			setStoredColor(Color.WHITE);
			enableHandClicks(false);
			break;
		case STORING_PRIVATE:
			//TODO is this controller logic
			setHandColor(Color.YELLOW);
			enableHandClicks(true);
			break;
		case STORING_QUESTCARDS:
			for (Card aCard : mListener.getValidQuestCards()) {
				for (TextView tv : theHandViews) {
					if (tv.getTag() instanceof Card &&
						(Card)tv.getTag() == aCard) {
						tv.setTextColor(Color.YELLOW);
					}
				}
			}
			break;
        case DISCARD:
			enableHandClicks(true);
            setHandColor(Color.YELLOW);
            setStoredColor(Color.YELLOW);
            break;
	}
	}


    @Override
    public void updateGems(int gems) {
        tvGems.setText(mContext.getString(R.string.Gems, gems));
    }

    @Override
    public void updateVps(int vps) {
        tvVps.setText(mContext.getString(R.string.Vps, vps));
    }

    @Override
    public void updateDeck(int size) {
        tvDeck.setText(mContext.getString(R.string.Deck, size));
    }

    @Override
    public void updateDiscard(int size) {
        tvDiscard.setText(mContext.getString(R.string.Discard, size));
    }

    @Override
    public View getRootView() {
        return mRootView;
    }

    @Override
    public Bundle getViewState() {
        // This MVC view has no state that could be retrieved
        return null;
    }

    @Override
    public void highlightLand(int location) {
        switch (location) {
            case 1: land1.setTextColor(Color.YELLOW);break;
            case 2: land2.setTextColor(Color.YELLOW);break;
            case 3: land3.setTextColor(Color.YELLOW);break;
            case 4: land4.setTextColor(Color.YELLOW);break;
            case 5: land5.setTextColor(Color.YELLOW);break;
            case 6: land6.setTextColor(Color.YELLOW);break;
        }
    }

    @Override
    public void bindLand(spaceRegion location, Region details) {

        String strPlayers = "";
        for (Player p : details.players
        ) {
            strPlayers += p.toString() + " ";
        }
        switch (location) {
            case NE:
				bindLandCard(details, land1c);
                setLandText(details, strPlayers, land1);
                break;
            case E:
				bindLandCard(details, land2c);
                setLandText(details, strPlayers, land2);
                break;
            case SE:
				bindLandCard(details, land3c);
                setLandText(details, strPlayers, land3);
                break;
            case SW:
				bindLandCard(details, land4c);
                setLandText(details, strPlayers, land4);
                break;
            case W:
				bindLandCard(details, land5c);
                setLandText(details, strPlayers, land5);
                break;
            case NW:
				bindLandCard(details, land6c);
                setLandText(details, strPlayers, land6);
                break;
        }
    }

	private void bindLandCard(Region details, View land) {
		setLandImage((ImageView)land.findViewById(R.id.landImage), details.name);
		TextView p1 = (TextView)land.findViewById(R.id.player1);
		p1.setVisibility(details.players.size()>0 ? View.VISIBLE : View.INVISIBLE);
		p1.setText(details.getPlayersString());
		setTowerImage((ImageView)land.findViewById(R.id.tower), details.tower);
	}
	
	private void setTowerImage(ImageView view, TowerName tname){
		switch (tname) {
			case BAZAAR:
				view.setImageResource(R.drawable.bazaar);
				break;
			case ARTIFACT:
				view.setImageResource(R.drawable.artifact);
				break;
			case QUEST:
				view.setImageResource(R.drawable.quest);
				break;
		}
	}
	private void setLandImage(ImageView view, RegionName tname){
		switch (tname) {
			case HIGHLANDS:
				view.setImageResource(R.drawable.highlands);
				break;
			case HILLS:
				view.setImageResource(R.drawable.hills);
				break;
			case TUNDRA:
				view.setImageResource(R.drawable.tundra);
				break;
			case FIELDS:
				view.setImageResource(R.drawable.plains);
				break;
			case WETLANDS:
				view.setImageResource(R.drawable.wetlands);
				break;
			case FOREST:
				view.setImageResource(R.drawable.woods);
				break;
		}
	}
	
    private void setLandText(Region details, String strPlayers, TextView land) {
        land.setTextColor(Color.WHITE);
        land.setText(details.name.toString() + "\n" + details.tower.toString() + "\n" + strPlayers);
    }

    @Override
    public void bindRoad(spaceRoad location, Road details) {
        switch (location) {
            case N:
				bindRoadCard(road1c, details);
                road1.setText(details.creature.toString());break;
            case NE:
                bindRoadCard(road2c, details);
                road2.setText(details.creature.toString());break;
            case SE:
                bindRoadCard(road3c, details);
                road3.setText(details.creature.toString());break;
            case S:
                bindRoadCard(road4c, details);
                road4.setText(details.creature.toString());break;
            case SW:
                bindRoadCard(road5c, details);
                road5.setText(details.creature.toString());break;
            case NW:
                bindRoadCard(road6c, details);
                road6.setText(details.creature.toString());break;
            case MID:
                bindRoadCard(road7c, details);
                road7.setText(details.creature.toString());break;
        }
    }
    
    private void bindRoadCard(CardView view, Road details){
		setImage((ImageView)view.findViewById(R.id.power1),details.creature.values.get(0));
		if (details.creature.values.size()>1){
			setImage((ImageView)view.findViewById(R.id.power2),details.creature.values.get(1));
		}
		//setImage((ImageView)view.findViewById(R.id.ability),details.creature.ability);
		setImage((ImageView)view.findViewById(R.id.subdueBy),details.creature.subduedBy);
		view.findViewById(R.id.gem).setVisibility(
			details.creature.gem ? View.VISIBLE : View.INVISIBLE);
		
	}
	
	    private void setImage(ImageView theView, Symbol aSymbol){
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
    public void bindQuest(int location, Quest details, Boolean canComplete) {
        switch (location) {
            case 1:
				bindQuestCard(publicQuest1c, details, canComplete);
                publicQuest1.setText(details.name);
                publicQuest1.setTag(details);
                publicQuest1.setTextColor(canComplete ? Color.GREEN : Color.WHITE);
                publicQuest1.setClickable(canComplete);
                break;
            case 2:
				bindQuestCard(publicQuest2c, details, canComplete);
                publicQuest2.setText(details.name);
                publicQuest2.setTag(details);
                publicQuest2.setTextColor(canComplete ? Color.GREEN : Color.WHITE);
                publicQuest2.setClickable(canComplete);
                break;
        }
    }
    
    private void bindQuestCard(CardView qCard, Quest details, Boolean canComplete ){
		setImage((ImageView)qCard.findViewById(R.id.power1), details.getDoubleRequirement());
		setImage((ImageView)qCard.findViewById(R.id.power2), details.getDoubleRequirement());
		qCard.setCardBackgroundColor(canComplete ? Color.GREEN : Color.WHITE);
		//TODO other quest types
	}

    @Override
    public void bindHand(List<Card> hand) {
		mAdapter.setDataset(hand.toArray(new Card[0]), true);
		mAdapter.notifyDataSetChanged();
        ListIterator<TextView> listIter = theHandViews.listIterator(0);
        while (listIter.hasNext()) {
            ((TextView)listIter.next()).setText("");
        }
        listIter = theHandViews.listIterator(0);
        for (Card aCard: hand
             ) {
			TextView tvHandCard = (TextView)listIter.next();
			tvHandCard.setText(aCard.toString());
            tvHandCard.setTag(aCard);
        }
    }

    @Override
    public void removeHandCard(Card aCard, List<Card> theHand) {
        for (TextView tvHand : theHandViews) {
            if (tvHand.getTag() == aCard) {
                tvHand.setTag(null);
                tvHand.setText("");
            }
        }
        mAdapter.setDataset(theHand.toArray(new Card[0]));
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void bindStorage(List<Card> hand) {
        ListIterator<TextView> listIter = storage.listIterator(0);
        while (listIter.hasNext()) {
            listIter.next().setText("");
        }
        listIter = storage.listIterator(0);
        for (Card aCard: hand
             ) {
            TextView tvStorageCard = (TextView)listIter.next();
			tvStorageCard.setText(aCard.toString());
            tvStorageCard.setTag(aCard);
        }
    }
    
    @Override
    public void bindPlayerStorage(List<Card> theHand) {
		//TODO multiple quests
        int i=0;
		for (Card aQuest : theHand) {
            quests.get(i++).setText(aQuest.name);
            if (i==2) break;
        }
	}
	
	@Override
	public void bindQuestStorage(Quest theQuest, List<Card> theCards) {
		//TODO confirm the quest to use
		clearTextViews(quest0Storage);
		for (Card aCard : theCards) {
		for (TextView questStorage : quest0Storage) {
			if (questStorage.getText()=="") {
				questStorage.setText(aCard.name);
				break;
			}
		}
		}
	}
	
	private void clearTextViews(List<TextView> theList) {
		for (TextView tv : theList) {
			tv.setText("");
		}
	}

    @Override
    public void setListener(ViewMvcListener listner) {
        mListener = listner;
    }
	
	@Override
	public void onDoneClick() {
		mListener.finishPhase();
	}


	private void setStoredColor(Integer aColor) {
        setTextViewListColor(aColor, storage);
        setTextViewListColor(aColor, quest0Storage);
        setTextViewListColor(aColor, quest1Storage);
    }

	private void setHandColor(Integer aColor) {
        setTextViewListColor(aColor, theHandViews);
    }
    
    private void enableHandClicks(Boolean enable) {
		mAdapter.setHandClicks(true);
		mAdapter.notifyDataSetChanged();
		Log.d("fantastiqa", "setHandClicks");
	}

    private void setTextViewListColor(Integer aColor, List<TextView> tvList) {
        ListIterator<TextView> listIter = tvList.listIterator(0);
        while (listIter.hasNext()) {
            listIter.next().setTextColor(aColor);
        }
    }

    private void setLandColor(Integer aColor) {
		land1.setTextColor(aColor);
		land2.setTextColor(aColor);
		land3.setTextColor(aColor);
		land4.setTextColor(aColor);
		land5.setTextColor(aColor);
		land6.setTextColor(aColor);
		
		land1c.setBackgroundColor(aColor);
		land2c.setBackgroundColor(aColor);
		land3c.setBackgroundColor(aColor);
		land4c.setBackgroundColor(aColor);
		land5c.setBackgroundColor(aColor);
		land6c.setBackgroundColor(aColor);
		
	}

	@Override
	public void validCardsForQuest(List<Card> matches) {
		for (View handCardText : theHandViews) {
			for (Card questCard : matches) {
				if (((TextView)handCardText).getText()==questCard.name) {
					((TextView)handCardText).setTextColor(Color.YELLOW);
					break;
				}
			} 
		}
	}
	/*
	public void addQuestStorage() {
			//TODO multiple quests
		for (Card questCard : theHand) {
			//for (TextView questText : quests)
			TextView questText = quests.get(0); {
				//if match add to next blank quest card spot
				if (questCard.name = questText.getText()) {
					for (TextView questCardText : quest0Storage) {
						if (questCardText.getText()=="") {
							questCardText.setText(questCard.name);
							break;
					}
				}
			}
		}
	}
	*/

	@Override
	public void setTowerTeleport(Boolean value) {
		//towerButton.setEnabled(value);
		//towerButton.setVisibility(value ? View.VISIBLE : View.GONE);
	}
	
	@Override
	public void selectKeyCards (final List<Card> selectFrom) {

		CharSequence[] dialogItems = new CharSequence[selectFrom.size()];
		for (int i=0; i<selectFrom.size(); i++) {
			dialogItems[i]=selectFrom.get(i).name;
		}
		AlertDialog.Builder selectDialog = new AlertDialog.Builder(mRootView.getContext());
		final ArrayList<Integer> selectedItems = new ArrayList<Integer>();
		selectDialog.setMultiChoiceItems(dialogItems, null,
			new DialogInterface.OnMultiChoiceClickListener() {
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				if (isChecked) {
					selectedItems.add(which);
				}
				else if (selectedItems.contains(which)) {
					selectedItems.remove(Integer.valueOf(which));
				}
			}
		  }
		)
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		  @Override
		  public void onClick(DialogInterface dialog, int id) {
			List<Card> selectedCards = new ArrayList<>();
			for (Integer item : selectedItems) {
				selectedCards.add(selectFrom.get(item));
			}
			mListener.onSelectedKeyCards(selectedCards);
		  }	
		});
		
		selectDialog.show();
		
	}

    @Override
    public void selectSubdueOption(final List<Set<Card>> choices, final Road toSubdue)  {
        CharSequence[] dialogItems = new CharSequence[choices.size()];
            int i=0;
            for (Set<Card> aChoice : choices) {
                Iterator setIter = aChoice.iterator();
                String strChoice = "";
                while (setIter.hasNext()) {
                    strChoice = strChoice + setIter.next().toString() + " ";
                }
                dialogItems[i]=strChoice;
                i++;
            }
        
	    AlertDialog.Builder chooseBuilder = new AlertDialog.Builder(mRootView.getContext());
        chooseBuilder.setItems(dialogItems,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mListener.subdue(toSubdue, choices.get(which));
            }
        });
        chooseBuilder.show();
    }

}
