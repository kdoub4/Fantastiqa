package com.example.fantastiqa.screens.views;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.fantastiqa.GameState.Card;
import com.example.fantastiqa.GameState.Player;
import com.example.fantastiqa.GameState.Quest;
import com.example.fantastiqa.GameState.Region;
import com.example.fantastiqa.GameState.Road;
import com.example.fantastiqa.R;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
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

    LinkedList<View> theHand;
    LinkedList<TextView> storage;
    LinkedList<TextView> quests;
    LinkedList<TextView> quest1Storage;
    LinkedList<TextView> quest0Storage;

    public RootViewMvcImpl(Context context, ViewGroup container) {
        mRootView = LayoutInflater.from(context).inflate(R.layout.activity_main, container);

        moveButton = mRootView.findViewById(R.id.buttonMove);
        moveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onMoveClick();
                }
            }
        });
        
        towerButton = mRootView.findViewById(R.id.buttonTower);
        towerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onTowerVisitClick();
                }
            }
        });

        storeCardsButton = mRootView.findViewById(R.id.buttonStoreCards);
        storeCardsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onStoreCardsClick();
                }
            }
        });
        
        storeQuestButton = mRootView.findViewById(R.id.buttonStoreQuestCards);
        storeQuestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onStoreQuestClick();
                }
            }
        });
        
        doneButton = mRootView.findViewById(R.id.buttonDone);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onDoneClick();
                }
            }
        });

        land1 = (TextView) mRootView.findViewById(R.id.land1);
        land2 = (TextView) mRootView.findViewById(R.id.land2);
        land3 = (TextView) mRootView.findViewById(R.id.land3);
        land4 = (TextView) mRootView.findViewById(R.id.land4);
        land5 = (TextView) mRootView.findViewById(R.id.land5);
        land6 = (TextView) mRootView.findViewById(R.id.land6);

        land1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onLandClick(view);
                }
            }
        });
        land2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onLandClick(view);
                }
            }
        });
        land3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onLandClick(view);
                }
            }
        });
        land4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onLandClick(view);
                }
            }
        });
        land5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onLandClick(view);
                }
            }
        });
        land6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onLandClick(view);
                }
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

        theHand = new LinkedList<View>(){ {
            add(mRootView.findViewById(R.id.hand1));
            add(mRootView.findViewById(R.id.hand2));
            add(mRootView.findViewById(R.id.hand3));
            add(mRootView.findViewById(R.id.hand4));
            add(mRootView.findViewById(R.id.hand5));
            add(mRootView.findViewById(R.id.hand6));
            add(mRootView.findViewById(R.id.hand7));
            add(mRootView.findViewById(R.id.hand8));
            add(mRootView.findViewById(R.id.hand9));
            add(mRootView.findViewById(R.id.hand10));
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
		
        ListIterator<View> listIter = theHand.listIterator(0);
        while (listIter.hasNext()) {
			((TextView)listIter.next()).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mListener != null) {
						mListener.onHandClick(v);
					}
				}
			});
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
    public void bindLand(int location, Region details) {

        String strPlayers = "";
        for (Player p : details.players
        ) {
            strPlayers += p.toString() + " ";
        }
        switch (location) {
            case 1:
                setLandText(details, strPlayers, land1);
                break;
            case 2:
                setLandText(details, strPlayers, land2);
                break;
            case 3:
                setLandText(details, strPlayers, land3);
                break;
            case 4:
                setLandText(details, strPlayers, land4);
                break;
            case 5:
                setLandText(details, strPlayers, land5);
                break;
            case 6:
                setLandText(details, strPlayers, land6);
                break;
        }
    }

    private void setLandText(Region details, String strPlayers, TextView land) {
        land.setTextColor(Color.WHITE);
        land.setText(details.name.toString() + "\n" + details.tower.toString() + "\n" + strPlayers);
    }

    @Override
    public void bindRoad(int location, Road details) {
        switch (location) {
            case 1:
                road1.setText(details.creature.toString());break;
            case 2:
                road2.setText(details.creature.toString());break;
            case 3:
                road3.setText(details.creature.toString());break;
            case 4:
                road4.setText(details.creature.toString());break;
            case 5:
                road5.setText(details.creature.toString());break;
            case 6:
                road6.setText(details.creature.toString());break;
            case 7:
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
        ListIterator<View> listIter = theHand.listIterator(0);
        while (listIter.hasNext()) {
            ((TextView)listIter.next()).setText("");
        }
        listIter = theHand.listIterator(0);
        for (Card aCard: hand
             ) {
            ((TextView)listIter.next()).setText(aCard.toString());
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
		quests.get(0).setText(theHand.get(0).name);
	}
	
	@Override
	public void bindQuestStorage(Quest theQuest, List<Card> theCards) {
		//TODO confirm the quest to use
		for (Card aCard : theCards) {
		for (TextView questStorage : quest0Storage) {
			if (questStorage.getText()=="") {
				questStorage.setText(aCard.name);
				break;
			}
		}
		}
	}

    @Override
    public void setListener(ViewMvcListener listner) {
        mListener = listner;
    }
	
	public void onDone(String state) {
		switch (state) {
			case "storingPrivate" : setHandColor(Color.WHITE);
				break;
		}
	}
	
	private void setHandColor(Integer aColor) {
		ListIterator<View> listIter = theHand.listIterator(0);
        while (listIter.hasNext()) {
            ((TextView)listIter.next()).setTextColor(aColor);
        }
	}
	
	@Override
	public void gameStateChange(String newState) {
		switch (newState) {
		case "storingPrivate":
			setHandColor(Color.YELLOW);
			break;
		case "storingQuestCards":
			break;
		case "open":
			setHandColor(Color.WHITE);
			break;
		}
	}
	
	@Override
	public void validCardsForQuest(List<Card> matches) {
		for (Card questCard : matches) {
			for (View handCardText : theHand) {
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
		towerButton.setEnabled(value);
		//towerButton.setVisibility(value ? View.VISIBLE : View.GONE);
	}
}
