package com.example.fantastiqa.screens.views;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.example.fantastiqa.GameState.Card;
import com.example.fantastiqa.GameState.Player;
import com.example.fantastiqa.GameState.Quest;
import com.example.fantastiqa.GameState.Region;
import com.example.fantastiqa.GameState.Road;
import com.example.fantastiqa.GameState.CreatureCard;
import com.example.fantastiqa.R;

import com.example.fantastiqa.screens.deckCards;
import com.example.fantastiqa.screens.spaceRoad;
import com.example.fantastiqa.screens.spaceRegion;
import com.example.fantastiqa.screens.gameStatus;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.EnumMap;
import java.util.HashMap;
/*
 * Very simple MVC view containing just single FrameLayout
 */
public class RootViewMvcImpl implements ViewMvc  {

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

    private TextView publicQuest1;
    private TextView publicQuest2;

    private TextView road1;
    private TextView road2;
    private TextView road3;
    private TextView road4;
    private TextView road5;
    private TextView road6;
    private TextView road7;

    LinkedList<TextView> theHandViews;
    LinkedList<TextView> storage;
    LinkedList<TextView> quests;
    LinkedList<TextView> quest1Storage;
    LinkedList<TextView> quest0Storage;
    
    Map<spaceRegion, TextView> regionTextView = new EnumMap<>(spaceRegion.class);
    Map<TextView, spaceRegion> textViewRegion = new HashMap<>();
    
    

    public RootViewMvcImpl(Context context, ViewGroup container) {
        mRootView = LayoutInflater.from(context).inflate(R.layout.activity_main, container);
		
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
        
        road1 = mRootView.findViewById(R.id.road1);
        road2 = mRootView.findViewById(R.id.road2);
        road3 = mRootView.findViewById(R.id.road3);
        road4 = mRootView.findViewById(R.id.road4);
        road5 = mRootView.findViewById(R.id.road5);
        road6 = mRootView.findViewById(R.id.road6);
        road7 = mRootView.findViewById(R.id.road7);

        publicQuest1 = mRootView.findViewById(R.id.publicQuest1);
        publicQuest2 = mRootView.findViewById(R.id.publicQuest2);

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
    }

	@Override
	public void onMoveClick(View v) {
		for (spaceRegion aRegion : mListener.getValidAdventuring()) {
			regionTextView.get(aRegion).setTextColor(Color.YELLOW);
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
					case 1: beginTowerCards(); break;// mListener.beginVisitBazaar(); break;
					case 2: //purge
					break;
				}
			}
		}
		);
		builder.show();
	} 
	
	private void beginTowerCards() {
		mListener.beginVisitBazaar();
	}
	
	@Override
	public void selectCard(List<Card> towerCards) {
		CharSequence[] dialogItems = new CharSequence[towerCards.size()];
		for (int i=0; i<towerCards.size(); i++) {
			dialogItems[i]=towerCards.get(i).name;
		}
		AlertDialog.Builder buyDialog = new AlertDialog.Builder(mRootView.getContext());
		buyDialog.setItems(dialogItems, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				mListener.endVisitBazaar(which);
			}
		  }
		);
		buyDialog.show();
	}
	
	@Override
	public void onStoreCardsClick() {
		mListener.beginStoreCardsPrivate();
	}
	
	@Override
	public void onStoreQuestClick() {
		mListener.beginStoreCardsQuest();
	}
	
	@Override
	public void onHandClick(View v) {
		//TODO enable click
		if (v.getTag() instanceof Card )
			mListener.storeCard((Card)v.getTag());
	}
	
	@Override
	public void onLandClick(View v) {
		if (mListener.doMove(textViewRegion.get(v))) {
				//setLandColor(Color.WHITE);
		}
	}
	
	@Override
	public void gameStateChange(gameStatus newState) {
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
			break;
		case STORING_PRIVATE:
			//TODO is this controller logic
			setHandColor(Color.YELLOW);
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
	}
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
                setLandText(details, strPlayers, land1);
                break;
            case E:
                setLandText(details, strPlayers, land2);
                break;
            case SE:
                setLandText(details, strPlayers, land3);
                break;
            case SW:
                setLandText(details, strPlayers, land4);
                break;
            case W:
                setLandText(details, strPlayers, land5);
                break;
            case NW:
                setLandText(details, strPlayers, land6);
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
                road1.setText(details.creature.toString());break;
            case NE:
                road2.setText(details.creature.toString());break;
            case SE:
                road3.setText(details.creature.toString());break;
            case S:
                road4.setText(details.creature.toString());break;
            case SW:
                road5.setText(details.creature.toString());break;
            case NW:
                road6.setText(details.creature.toString());break;
            case MID:
                road7.setText(details.creature.toString());break;
        }
    }

    @Override
    public void bindQuest(int location, Quest details) {
        switch (location) {
            case 1:
                publicQuest1.setText(details.name);break;
            case 2:
                publicQuest2.setText(details.name);break;
        }
    }

    @Override
    public void bindHand(List<Card> hand) {
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
    public void bindStorage(List<Card> hand) {
        ListIterator<TextView> listIter = storage.listIterator(0);
        while (listIter.hasNext()) {
            listIter.next().setText("");
        }
        listIter = storage.listIterator(0);
        for (Card aCard: hand
             ) {
            listIter.next().setText(aCard.toString());
        }
    }
    
    @Override
    public void bindPlayerQuest(List<Card> theHand) {
		//TODO multiple quests
		quests.get(0).setText(theHand.get(0).name);
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
	
	private void setHandColor(Integer aColor) {
		ListIterator<TextView> listIter = theHandViews.listIterator(0);
        while (listIter.hasNext()) {
            ((TextView)listIter.next()).setTextColor(aColor);
        }
	}
	private void setLandColor(Integer aColor) {
		land1.setTextColor(aColor);
		land2.setTextColor(aColor);
		land3.setTextColor(aColor);
		land4.setTextColor(aColor);
		land5.setTextColor(aColor);
		land6.setTextColor(aColor);
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

}
