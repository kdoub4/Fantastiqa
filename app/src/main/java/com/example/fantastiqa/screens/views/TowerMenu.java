package com.example.fantastiqa.screens.views;

import androidx.core.view.ActionProvider;

import android.view.MenuItem;
import android.view.View;
import android.content.Context;
import android.widget.PopupMenu;

import com.example.fantastiqa.R;

class TowerMenu extends ActionProvider implements PopupMenu.OnMenuItemClickListener {

	private Context mContext;
	public TowerMenu(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		return false;
	}

	@Override
	public View onCreateActionView() {
		View view = View.inflate(mContext, R.layout.tower_options, null);

		final PopupMenu menu = new PopupMenu(mContext, view);
		//menu.inflate(R.menu.tower_submenu);
		menu.setOnMenuItemClickListener(this);
		view.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v){
				menu.show();
			}
		});

		return view;
	}
	
	
}
