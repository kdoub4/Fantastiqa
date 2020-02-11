package com.example.fantastiqa;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.os.Bundle;
import android.view.DragEvent;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

public class MaterialActivity extends AppCompatActivity implements View.OnDragListener {
	
	View tvExample;
	View tvHandCard1;
	View tvLandCard1;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.material_l);
		//tvExample = findViewById(R.id.card_view);
        tvHandCard1 = findViewById(R.id.aHandCard);
        tvLandCard1 = findViewById(R.id.landm1);
        tvHandCard1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                v.startDragAndDrop(null,new View.DragShadowBuilder(v),v,0);
                return true;
            }
        });
        tvLandCard1.setOnDragListener(this);
    }
    
    //public void onButtonClick(View v){
//		tvExample.setEnabled(!tvExample.isEnabled());
//	}

    @Override
    public boolean onDrag(View v, DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                return true;

            case DragEvent.ACTION_DROP:
//                if (v != event.getLocalState()) {
//                    Toast.makeText(this, v.toString(), Toast.LENGTH_SHORT).show();
//                    return true;
//                }
//                else
//                    return false;

                View source = (View) event.getLocalState();
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)source.getLayoutParams();
                params.startToStart = v.getId();
                params.endToStart = v.getId();
                params.topToTop = v.getId();
                source.setLayoutParams(params);
                source.setPivotX(source.getWidth() * 1.0f);
                source.setPivotY(source.getHeight() * 1.0f);
                source.setRotation(90);
                source.setScaleX(0.5f);
                source.setScaleY(0.5f);
                return true;

            case DragEvent.ACTION_DRAG_ENDED:
                Toast.makeText(this, v.toString(), Toast.LENGTH_SHORT).show();
                if (event.getResult() && v == event.getLocalState()) {
                }
                return true;
        }

            return false;
    }
}
