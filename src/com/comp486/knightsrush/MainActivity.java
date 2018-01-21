package com.comp486.knightsrush;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class MainActivity extends Activity {

	// gameView will be the view of the game
	// It will also hold the logic of the game
	// and respond to screen touches as well
	GameView gameView;
	RelativeLayout rl;
	RelativeLayout overlay;
	RelativeLayout menubar;
	RelativeLayout skillStat;
	RelativeLayout inventory;
	ContextParameters params;
	ImageButton pauseBtn, inventoryBtn, spellBtn, skillBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Setup Window Properties
		// Keep Screen On
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		Intent intent = getIntent();
		pauseBtn = new ImageButton(this);
		inventoryBtn = new ImageButton(this);
		spellBtn = new ImageButton(this);
		skillBtn = new ImageButton(this);
		// Get DisplayMetrics and store as GameView parameter
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		params = new ContextParameters();
		params.context = this;
		params.viewWidth = metrics.widthPixels;
		params.viewHeight = metrics.heightPixels;
		params.mDensity = metrics.densityDpi;
		params.columns = params.viewWidth;
		params.rows = params.viewHeight;
		params.viewWidth = params.viewWidth + Utils.getNavBarHeight(this, getResources());
		if (intent.getBooleanExtra("newGame", false) == false) {
			params.difficulty = intent.getIntExtra(LevelPreferences.PREFERENCE_DIFFICULTY, 1);
			params.difficultyCompleted = intent.getIntExtra(LevelPreferences.PREFERENCE_DIFFICULTY_COMPLETED, 0);

			params.act = intent.getIntExtra(LevelPreferences.PREFERENCE_ACT, 0);
			params.area = intent.getIntExtra(LevelPreferences.PREFERENCE_AREA, 0);
		} else {
			params.difficulty = 1;
			params.difficultyCompleted = 0;
			params.act = 0;
			params.area = 0;
		}
		params.intent = intent;
		// Initialize gameView and set it as the view
		gameView = savedInstanceState == null ? new GameView(this, params) : gameView;

		// Hide back stop and resume buttons
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			gameView.setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			gameView.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);

		rl = new RelativeLayout(this);
		LinearLayout itemll = new LinearLayout(this);
		inventory = new RelativeLayout(this);
		overlay = new RelativeLayout(this);
		skillStat = new RelativeLayout(this);
		menubar = new RelativeLayout(this);

		LayoutParams lParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		LayoutParams lParams2 = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lParams2.addRule(RelativeLayout.ALIGN_LEFT);
		itemll.setLayoutParams(lParams);
		skillStat.setAlpha(1);
		rl.setLayoutParams(lParams);
		rl.addView(gameView);
		inventory.setLayoutParams(lParams2);
		overlay.setLayoutParams(lParams2);
		skillStat.setLayoutParams(lParams2);
		itemll.addView(overlay);
		itemll.addView(inventory);
		itemll.addView(menubar);
		itemll.addView(skillStat);
		inventory.setBackgroundColor(Color.WHITE);
		rl.addView(itemll);
		final Bitmap pause = BitmapFactory.decodeResource(getResources(), R.drawable.pause);
		final Bitmap pause2 = BitmapFactory.decodeResource(getResources(), R.drawable.pause2);
		final Bitmap spell = BitmapFactory.decodeResource(getResources(), R.drawable.fire);
		final Bitmap inv = BitmapFactory.decodeResource(getResources(), R.drawable.inv);
		final Bitmap skill = BitmapFactory.decodeResource(getResources(), R.drawable.add_button);
		pauseBtn.setImageBitmap(pause);
		pauseBtn.setPadding(0, 0, 0, 0);
		pauseBtn.setX(params.viewWidth - pause.getWidth());
		pauseBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (gameView.playing) {
					pauseBtn.setImageBitmap(pause2);
					gameView.pauseScreen();
				} else {
					pauseBtn.setImageBitmap(pause);
					gameView.resumeScreen();
				}
			}
		});
		pauseBtn.setLayoutParams(lParams2);
		rl.addView(pauseBtn);
		spellBtn.setImageBitmap(spell);
		spellBtn.setPadding(0, 0, 0, 0);
		spellBtn.setX(params.viewWidth - spell.getWidth() * 3);
		spellBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (gameView.playing) {
					gameView.skillsScreen = true;
					gameView.statsScreen = false;
					gameView.pauseScreen();
				} else {
					if (gameView.statsScreen) {
						gameView.skillsScreen = true;
						gameView.statsScreen = false;
						gameView.replaceScreen();
					}

				}
			}
		});

		spellBtn.setLayoutParams(lParams2);
		rl.addView(spellBtn);
		inventoryBtn.setImageBitmap(inv);
		inventoryBtn.setPadding(0, 0, 0, 0);
		inventoryBtn.setX(params.viewWidth - pause.getWidth() * 2);
		inventoryBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (gameView.playing) {
					gameView.inventoryScreen = gameView.inventoryScreen ? false : true;
				} else {
					gameView.inventoryScreen = true;
					gameView.resumeScreen();
				}
			}
		});
		inventoryBtn.setLayoutParams(lParams2);
		rl.addView(inventoryBtn);
		setContentView(rl);
		skillBtn.setImageBitmap(skill);
		skillBtn.setPadding(0, 0, 0, 0);
		skillBtn.setX(params.viewWidth - pause.getWidth() * 4);
		skillBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (gameView.playing) {
					gameView.pauseScreen();
				} else {
					if (gameView.skillsScreen) {
						gameView.statsScreen = true;
						gameView.skillsScreen = false;
						gameView.replaceScreen();
					}

				}
			}
		});
		skillBtn.setLayoutParams(lParams2);
		rl.addView(skillBtn);
		setContentView(rl);

	}

	/*
	 * When the game thread needs to stop its own execution (to go to a new
	 * level, or restart the current level), it registers a runnable on the main
	 * thread which orders the action via this function.
	 */

	// This method executes when the player starts the game
	@Override
	protected void onResume() {

		// Tell the gameView resume method to execute
		if (gameView.gameThread != null) {
			gameView.resume();
		}
		super.onResume();
	}

	// This method executes when the player quits the game
	@Override
	protected void onPause() {

		// Tell the gameView pause method to execute
		gameView.saveGame();

		// clean up memory
		finish();
		super.onPause();
	}

	// This method executes when the player uses android nav bar
	@Override
	protected void onStop() {

		gameView.pause();
		// Memory cleanup, mostly bitmaps that use massive memory
		if (gameView.prefs != null) {
			gameView.saveGame();
		}
		gameView.getKnight().destroy();
		gameView.recycleEnemyBmps();
		if (gameView.boss != null) {
			gameView.boss.recycleBmps();
		}
		// gameView.bmpNPC.recycle();
		// gameView.bmpNPCLeft.recycle();
		gameView.groundTile.recycle();
		gameView.groundTile2.recycle();
		gameView.potionSm.recycle();
		gameView.potionLg.recycle();
		gameView.enemies.clear();
		gameView.potions.clear();
		gameView.healthBar.recycle();
		gameView.fireButton.recycle();
		gameView.lightButton.recycle();
		gameView.zealButton.recycle();
		gameView.questComplete.recycle();
		gameView.quest_active.recycle();
		gameView.emptyInventory.recycle();
		gameView.tome.recycle();
		gameView.background.recycle();

		if (gameView.weaponBitmap != null) {
			gameView.weaponBitmap.recycle();
		}
		if (gameView.shieldBitmap != null) {
			gameView.shieldBitmap.recycle();
		}
		if (gameView.helmetBitmap != null) {
			gameView.helmetBitmap.recycle();
		}
		// gameView.quests2Complete.clear();
		finish();
		super.onStop();

	}

	// This method executes when the player restarts the app
	@Override
	protected void onRestart() {

		// Tell the gameView pause method to execute
		// gameView.saveGame();
		finish();

		super.onRestart();
	}

	// This method executes when the player destoys the app
	// ensure thread turns off. fail-safe
	@Override
	protected void onDestroy() {

		// Tell the gameView pause method to execute
		gameView.saveGame();
		gameView.pause();
		finish();
		super.onDestroy();
	}

}
