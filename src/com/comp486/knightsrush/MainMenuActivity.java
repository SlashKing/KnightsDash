/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.comp486.knightsrush;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;

import org.xmlpull.v1.XmlPullParserException;

import com.comp486.knightsrush.R;
import com.comp486.knightsrush.dummy.Items;
import com.comp486.knightsrush.dummy.Items.Item;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class MainMenuActivity extends Activity {
	private boolean mPaused;
	private View mStartButton;
	private View mContinueButton;
	private View mOptionsButton;
	private View mBackground;
	private Animation mButtonFlickerAnimation;
	private Animation mFadeOutAnimation;
	private Animation mAlternateFadeOutAnimation;
	private Animation mFadeInAnimation;
	private boolean mJustCreated;

	// Setup button click listeners and animations for background and buttons
	private View.OnClickListener sContinueButtonListener = new View.OnClickListener() {
		public void onClick(View v) {
			if (!mPaused) {
				Intent i = new Intent(getBaseContext(), DifficultyMenuActivity.class);
				v.startAnimation(mButtonFlickerAnimation);
				mFadeOutAnimation.setAnimationListener(new StartActivityAfterAnimation(i));
				mBackground.startAnimation(mFadeOutAnimation);
				mOptionsButton.startAnimation(mAlternateFadeOutAnimation);
				mStartButton.startAnimation(mAlternateFadeOutAnimation);
				mPaused = true;
			}
		}
	};

	private View.OnClickListener sOptionButtonListener = new View.OnClickListener() {
		public void onClick(View v) {
			if (!mPaused) {
				Intent i = new Intent(getBaseContext(), SettingsActivity.class);

				v.startAnimation(mButtonFlickerAnimation);
				mFadeOutAnimation.setAnimationListener(new StartActivityAfterAnimation(i));
				mBackground.startAnimation(mFadeOutAnimation);
				mStartButton.startAnimation(mAlternateFadeOutAnimation);
				if (mContinueButton != null) {
					mContinueButton.startAnimation(mAlternateFadeOutAnimation);
				}
				mPaused = true;
			}
		}
	};

	private View.OnClickListener sStartButtonListener = new View.OnClickListener() {
		public void onClick(View v) {
			if (!mPaused) {
				Intent i = new Intent(getBaseContext(), MainActivity.class);
				i.putExtra("newGame", true);
				v.startAnimation(mButtonFlickerAnimation);
				SharedPreferences.Editor editor = getSharedPreferences(PreferenceConstants.PREFERENCE_NAME,
						MODE_PRIVATE).edit();
				editor.clear().commit();
				editor.apply();

				GameAct.loadLevelTree(getBaseContext(), DifficultyConstants.NORMAL, true);

				// delete and recreate the database
				Items.du.deleteDB(getBaseContext());
				Items.du.createDB(getBaseContext());
				mButtonFlickerAnimation.setAnimationListener(new StartActivityAfterAnimation(i));
				mOptionsButton.startAnimation(mAlternateFadeOutAnimation);
				if (mContinueButton != null) {
					mContinueButton.startAnimation(mAlternateFadeOutAnimation);
				}
				mPaused = true;

			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.mainmenu);
		View view = findViewById(R.id.main_frame);
		// Hide back stop and resume buttons
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			view.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
		mPaused = true;

		mContinueButton = findViewById(R.id.continueButton);
		mStartButton = findViewById(R.id.startButton);
		mOptionsButton = findViewById(R.id.optionButton);
		mBackground = findViewById(R.id.mainMenuBackground);

		if (mOptionsButton != null) {
			mOptionsButton.setOnClickListener(sOptionButtonListener);
		}
		// load items into memory
		if (!Items.isLoaded(R.raw.item_list)) {
			try {
				Items.loadItems(getApplicationContext(), R.raw.item_list);
				for (Item item : Items.ITEMS) {
					Log.d("Item", item.content);
				}
			} catch (FileNotFoundException | XmlPullParserException e) {

				Log.e("Error Loading Items", e.getStackTrace().toString());
			}
		}
		mButtonFlickerAnimation = AnimationUtils.loadAnimation(this, R.anim.button_flicker);
		mFadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
		mAlternateFadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
		mFadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);

		SharedPreferences prefs = getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
		if (getIntent().getBooleanExtra("newGame", false)) {

			SharedPreferences.Editor editor = prefs.edit();
			editor.clear().commit();
			editor.apply();
			editor = null;
		}
		// check act and area
		// to determine whether they have already started
		final int act = prefs.getInt(LevelPreferences.PREFERENCE_ACT, -1);
		final int area = prefs.getInt(LevelPreferences.PREFERENCE_AREA + "_" + act + "_"
				+ prefs.getInt(LevelPreferences.PREFERENCE_DIFFICULTY, 1), -1);

		if (act != -1 || area != -1) {
			((ImageView) mContinueButton).setImageDrawable(getResources().getDrawable(R.drawable.ui_button_continue));
			mContinueButton.setOnClickListener(sContinueButtonListener);
		}
		((ImageView) mStartButton).setImageDrawable(getResources().getDrawable(R.drawable.ui_button_start));
		mStartButton.setOnClickListener(sStartButtonListener);
		mJustCreated = true;

	}

	@Override
	protected void onPause() {
		super.onPause();
		mPaused = true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		mPaused = false;

		View view = findViewById(R.id.main_frame);
		// Hide back stop and resume buttons
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			view.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);

		mButtonFlickerAnimation.setAnimationListener(null);
		SharedPreferences prefs = getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, Context.MODE_PRIVATE);
		GameAct.loadLevelTree(this, prefs.getInt(LevelPreferences.PREFERENCE_DIFFICULTY, 1), false);
		if (mStartButton != null) {

			// Change "start" to "continue" if there's a saved game.
			final int act = prefs.getInt(LevelPreferences.PREFERENCE_ACT, -1);
			final int area = prefs.getInt(LevelPreferences.PREFERENCE_AREA + "_0_0_1", -1);
			if (act != -1 || area != -1) {
				((ImageView) mContinueButton)
						.setImageDrawable(getResources().getDrawable(R.drawable.ui_button_continue));
				mContinueButton.setOnClickListener(sContinueButtonListener);
			}
			((ImageView) mStartButton).setImageDrawable(getResources().getDrawable(R.drawable.ui_button_start));
			mStartButton.setOnClickListener(sStartButtonListener);

		}

		if (mBackground != null) {
			mBackground.clearAnimation();
		}

		if (mJustCreated) {
			if (mStartButton != null) {
				mStartButton.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_slide));
			}
			if (mContinueButton != null) {
				mContinueButton.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_slide));
			}

			if (mOptionsButton != null) {
				Animation anim = AnimationUtils.loadAnimation(this, R.anim.button_slide);
				anim.setStartOffset(1000L);
				mOptionsButton.startAnimation(anim);
			}
			mJustCreated = false;

		} else {
			mStartButton.clearAnimation();
			mContinueButton.clearAnimation();
			mOptionsButton.clearAnimation();
		}

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;

		dialog = super.onCreateDialog(id);

		return dialog;
	}

	protected class StartActivityAfterAnimation implements Animation.AnimationListener {
		private Intent mIntent;

		StartActivityAfterAnimation(Intent intent) {
			mIntent = intent;
		}

		public void onAnimationEnd(Animation animation) {

			startActivity(mIntent);

			if (UIConstants.mOverridePendingTransition != null) {
				try {
					UIConstants.mOverridePendingTransition.invoke(MainMenuActivity.this, R.anim.activity_fade_in,
							R.anim.activity_fade_out);
				} catch (InvocationTargetException ite) {
					ite.printStackTrace();
				} catch (IllegalAccessException ie) {
					ie.printStackTrace();
				}
			}
		}

		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub

		}

		public void onAnimationStart(Animation animation) {
			// TODO Auto-generated method stub

		}

	}
}
