package com.example.fantastiqa.screens.views;

import androidx.core.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.content.Context;
import android.widget.TextView;
import android.widget.PopupMenu;

import com.example.fantastiqa.R;

class TowerMenu extends ActionProvider {

	private Context mContext;
	public TowerMenu(Context context) {
		super(context);
		mContext = context;
	}
	
	@Override
	public View onCreateActionView() {
		TextView result = new TextView(mContext);
		result.setText("HI");
		return result;
		/*
		 * View view = View.inflate(mContext, R.layout.tower_menu, null);

		final PopupMenu menu = new PopupMenu(mContext, view);
		menu.inflate(R.layout.submenu);
		menu.setOnMenuItemClickListener(this);

		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v){
				menu.show();
			}
		});

		return view;
		*/
	}
	
	
}
