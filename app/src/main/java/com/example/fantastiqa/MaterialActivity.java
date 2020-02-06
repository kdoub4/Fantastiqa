package com.example.fantastiqa;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.view.View;

public class MaterialActivity extends AppCompatActivity {
	
	View tvExample;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.material_l);
		tvExample = findViewById(R.id.card_view);
    }
    
    public void onButtonClick(View v){
		tvExample.setEnabled(!tvExample.isEnabled());
	}
}
