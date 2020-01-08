package com.example.fantastiqa.screens.controllers;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fantastiqa.GameState.Area;
import com.example.fantastiqa.GameState.Game;
import com.example.fantastiqa.GameState.Quest;
import com.example.fantastiqa.GameState.Road;
import com.example.fantastiqa.R;
import com.example.fantastiqa.screens.views.RootViewMvcImpl;
import com.example.fantastiqa.screens.views.ViewMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button buttonMove;
    private Game theGame;
    private com.example.fantastiqa.screens.views.ViewMvc rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rootView = new RootViewMvcImpl(this, null);
        setContentView(rootView.getRootView());
        //setContentView(R.layout.activity_main);

        theGame = new Game();

            for (Area anArea: theGame.board.locations
                 ) {
                    rootView.bindLand(theGame.board.locations.indexOf(anArea)+1, anArea);
            }

            for (Road aRoad: theGame.board.roads) {
                rootView.bindRoad(theGame.board.roads.indexOf(aRoad)+1, aRoad);
            }

            for (Quest aQuest : theGame.board.quests) {
                rootView.bindQuest(theGame.board.quests.indexOf(aQuest)+1, aQuest);
            }

            rootView.bindHand(theGame.players.get(0).hand);

        buttonMove = findViewById(R.id.buttonMove);
        buttonMove.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        TextView currentLocation= null;
        TextView nextLocation = null;
        LinkedList<View> locations = new LinkedList<View>(){ {
            add(findViewById(R.id.land1));
            add(findViewById(R.id.land2));
            add(findViewById(R.id.land3));
            add(findViewById(R.id.land4));
            add(findViewById(R.id.land5));
            add(findViewById(R.id.land6));
        }};
        currentLocation = (TextView)locations.getFirst();
        int i = 1;
        while (!currentLocation.getText().toString().contains("P1") && i<6) {
            currentLocation = (TextView) locations.get(i);
            i++;
        }
        if (currentLocation.getText().toString().contains("P1")) {
            currentLocation.setText(currentLocation.getText().toString().replace("P1",""));
            nextLocation = (TextView) locations.get(i<6 ? i : 0);
            nextLocation.setText(nextLocation.getText().toString().contains("P") ?
                    nextLocation.getText().toString().concat(" P1"):
                    nextLocation.getText().toString().concat("P1"));

        }

    }
}
