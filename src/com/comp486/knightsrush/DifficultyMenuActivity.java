package com.comp486.knightsrush;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.xmlpull.v1.XmlPullParserException;

import com.comp486.knightsrush.dummy.Items;
import com.comp486.knightsrush.dummy.Items.Item;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class DifficultyMenuActivity extends Activity {
	private View mNormalButton;
	private View mNightmareButton;
	private View mHellButton;
	private View mBackground;
	private View mNormalText;
	private View mNightmareText;
	private View mHellText;
	private Animation mButtonFlickerAnimation;
	private Animation mFadeOutAnimation;
	private Animation mAlternateFadeOutAnimation;

	private View.OnClickListener sNormalButtonListener = new View.OnClickListener() {
		public void onClick(View v) {

			// update level tree and dialog to reflect difficulty
			GameAct.loadLevelTree(getApplicationContext(), DifficultyConstants.NORMAL, true);
			Intent i = new Intent(getBaseContext(), LevelSelectActivity.class);
			i.putExtras(getIntent());
			i.putExtra(LevelPreferences.PREFERENCE_DIFFICULTY, DifficultyConstants.NORMAL);

			SharedPreferences prefs = getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
			i.putExtra(LevelPreferences.PREFERENCE_DIFFICULTY_COMPLETED,
					prefs.getInt(LevelPreferences.PREFERENCE_DIFFICULTY_COMPLETED, 0));
			int difficulty = prefs.getInt(LevelPreferences.PREFERENCE_DIFFICULTY, 0);
			if (difficulty != DifficultyConstants.NORMAL || difficulty == 0) {
				difficulty = DifficultyConstants.NORMAL;

				// set difficulty in shared preferences
				prefs.edit().putInt(LevelPreferences.PREFERENCE_DIFFICULTY, DifficultyConstants.NORMAL).commit();

				// set distance walked to zero
				prefs.edit().putInt(PreferenceConstants.PREFERENCE_DISTANCEWALKED, 0).commit();
			}

			v.startAnimation(mButtonFlickerAnimation);
			mFadeOutAnimation.setAnimationListener(new StartActivityAfterAnimation(i));
			mBackground.startAnimation(mFadeOutAnimation);
			mNightmareButton.startAnimation(mAlternateFadeOutAnimation);
			mHellButton.startAnimation(mAlternateFadeOutAnimation);

			mNormalText.startAnimation(mAlternateFadeOutAnimation);
			mNightmareText.startAnimation(mAlternateFadeOutAnimation);
			mHellText.startAnimation(mAlternateFadeOutAnimation);
		}
	};

	private View.OnClickListener sNightmareButtonListener = new View.OnClickListener() {
		public void onClick(View v) {

			// load level tree and dialog
			GameAct.loadLevelTree(getApplicationContext(), DifficultyConstants.NIGHTMARE, true);
			Intent i = new Intent(getBaseContext(), LevelSelectActivity.class);
			i.putExtras(getIntent());
			i.putExtra(LevelPreferences.PREFERENCE_DIFFICULTY, DifficultyConstants.NIGHTMARE);
			SharedPreferences prefs = getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
			i.putExtra(LevelPreferences.PREFERENCE_DIFFICULTY_COMPLETED,
					prefs.getInt(LevelPreferences.PREFERENCE_DIFFICULTY_COMPLETED, DifficultyConstants.NORMAL));
			int difficulty = prefs.getInt(LevelPreferences.PREFERENCE_DIFFICULTY, 0);
			if (difficulty != DifficultyConstants.NIGHTMARE || difficulty == 0) {

				SharedPreferences.Editor editor = prefs.edit();
				// save preferences with current difficulty
				editor.putInt(LevelPreferences.PREFERENCE_DIFFICULTY, DifficultyConstants.NIGHTMARE);

				// set distance walked to zero
				editor.putInt(PreferenceConstants.PREFERENCE_DISTANCEWALKED, 0);
				editor.commit();
			}
			v.startAnimation(mButtonFlickerAnimation);
			mFadeOutAnimation.setAnimationListener(new StartActivityAfterAnimation(i));
			mBackground.startAnimation(mFadeOutAnimation);
			mNormalButton.startAnimation(mAlternateFadeOutAnimation);
			mHellButton.startAnimation(mAlternateFadeOutAnimation);

			mNormalText.startAnimation(mAlternateFadeOutAnimation);
			mNightmareText.startAnimation(mAlternateFadeOutAnimation);
			mHellText.startAnimation(mAlternateFadeOutAnimation);

		}
	};

	private View.OnClickListener sHellButtonListener = new View.OnClickListener() {
		public void onClick(View v) {
			GameAct.loadLevelTree(getBaseContext(), DifficultyConstants.HELL, true);
			Intent i = new Intent(getBaseContext(), LevelSelectActivity.class);
			i.putExtras(getIntent());
			i.putExtra("difficulty", 3);
			SharedPreferences prefs = getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, Context.MODE_PRIVATE);
			i.putExtra(LevelPreferences.PREFERENCE_DIFFICULTY_COMPLETED,
					prefs.getInt(LevelPreferences.PREFERENCE_DIFFICULTY_COMPLETED, DifficultyConstants.NIGHTMARE));

			int difficulty = prefs.getInt(LevelPreferences.PREFERENCE_DIFFICULTY, 0);
			if (difficulty != DifficultyConstants.HELL || difficulty == 0) {
				difficulty = DifficultyConstants.HELL;
				prefs.edit().putInt(LevelPreferences.PREFERENCE_DIFFICULTY, DifficultyConstants.HELL).commit();
				// set distance walked to zero
				prefs.edit().putInt(PreferenceConstants.PREFERENCE_DISTANCEWALKED, 0).commit();
			}
			v.startAnimation(mButtonFlickerAnimation);
			mFadeOutAnimation.setAnimationListener(new StartActivityAfterAnimation(i));
			mBackground.startAnimation(mFadeOutAnimation);
			mNormalButton.startAnimation(mAlternateFadeOutAnimation);
			mNightmareButton.startAnimation(mAlternateFadeOutAnimation);

			mNormalText.startAnimation(mAlternateFadeOutAnimation);
			mNightmareText.startAnimation(mAlternateFadeOutAnimation);
			mHellText.startAnimation(mAlternateFadeOutAnimation);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.difficulty_menu);

		mNormalButton = findViewById(R.id.normalButton);
		mNightmareButton = findViewById(R.id.nightmareButton);
		mHellButton = findViewById(R.id.hellButton);
		mNormalText = findViewById(R.id.normalText);
		mNightmareText = findViewById(R.id.nightmareText);
		mHellText = findViewById(R.id.hellText);
		mBackground = findViewById(R.id.mainMenuBackground);

		// Hide back stop and resume buttons
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			mBackground.setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			mBackground.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
		mNormalButton.setOnClickListener(sNormalButtonListener);
		SharedPreferences prefs2 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences prefs = getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, Context.MODE_PRIVATE);
		int difficulty = prefs.getInt(LevelPreferences.PREFERENCE_DIFFICULTY_COMPLETED, 0);
		if (prefs2.getBoolean("pref_unlock", false)) {
			//if the unlock all levels preference is activated. let us play all difficulties
			mNightmareButton.setOnClickListener(sNightmareButtonListener);
			mHellButton.setOnClickListener(sHellButtonListener);

		} else {

			// Disable buttons if they haven't completed requisite difficulty
			if (difficulty >= 1) {
				mNightmareButton.setOnClickListener(sNightmareButtonListener);
			} else {
				mNightmareButton.setAlpha(0.6F);
				mNightmareText.setAlpha(0.6F);

			}
			if (difficulty >= 2) {
				mHellButton.setOnClickListener(sHellButtonListener);
			} else {
				// Reduce opacity and don't set onClickListener if we haven't
				// completed difficulty
				mHellButton.setAlpha(0.6F);
				mHellText.setAlpha(0.6F);

			}
		}
		mButtonFlickerAnimation = AnimationUtils.loadAnimation(this, R.anim.button_flicker);
		mFadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
		mAlternateFadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean result = true;
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();

			if (UIConstants.mOverridePendingTransition != null) {
				try {
					UIConstants.mOverridePendingTransition.invoke(DifficultyMenuActivity.this, R.anim.activity_fade_in,
							R.anim.activity_fade_out);
				} catch (InvocationTargetException ite) {
					// Log.d("Activity Transition", "Invocation Target
					// Exception");
				} catch (IllegalAccessException ie) {
					// Log.d("Activity Transition", "Illegal Access Exception");
				}
			}
		} else {
			result = super.onKeyDown(keyCode, event);
		}
		return result;
	}

	protected class StartActivityAfterAnimation implements Animation.AnimationListener {
		private Intent mIntent;

		StartActivityAfterAnimation(Intent intent) {
			mIntent = intent;
		}

		public void onAnimationEnd(Animation animation) {
			mNormalButton.setVisibility(View.INVISIBLE);
			mNormalButton.clearAnimation();
			mNightmareButton.setVisibility(View.INVISIBLE);
			mNightmareButton.clearAnimation();
			mHellButton.setVisibility(View.INVISIBLE);
			mHellButton.clearAnimation();
			startActivity(mIntent);
			finish(); // This activity dies when it spawns a new intent.

			if (UIConstants.mOverridePendingTransition != null) {
				try {
					UIConstants.mOverridePendingTransition.invoke(DifficultyMenuActivity.this, R.anim.activity_fade_in,
							R.anim.activity_fade_out);
				} catch (InvocationTargetException ite) {
					// DebugLog.d("Activity Transition", "Invocation Target
					// Exception");
				} catch (IllegalAccessException ie) {
					// DebugLog.d("Activity Transition", "Illegal Access
					// Exception");
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
