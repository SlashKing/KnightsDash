package com.comp486.knightsrush;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class StatsScreen {

	View rootView;

	public StatsScreen() {

	}

	public static View getStatsScreen(int viewWidth, int viewHeight, MainActivity activity, final KnightSprite knight,
			LayoutInflater inflater) {
		View rootView = inflater.inflate(R.layout.fragment_stats_screen, activity.skillStat, false);
		rootView.setVisibility(View.VISIBLE);
		Drawable myDrawable;
		// rootView.setMinimumHeight();
		rootView.setX(0);
		rootView.setBackgroundColor(Color.LTGRAY);
		rootView.setPadding(0, 120, 0, 0);
		myDrawable = ContextCompat.getDrawable(activity, R.drawable.add_button);
		myDrawable.setBounds(0, 0, myDrawable.getMinimumWidth(), myDrawable.getMinimumHeight());
		TextView txtDex = ((TextView) rootView.findViewById(R.id.char_dex));
		TextView txtStr = ((TextView) rootView.findViewById(R.id.char_str));
		TextView txtVit = ((TextView) rootView.findViewById(R.id.char_vit));
		TextView txtEnergy = ((TextView) rootView.findViewById(R.id.char_energy));
		((TextView) rootView.findViewById(R.id.char_statstouse))
				.setText(knight.statPointsToUse + " Stat Points Remaining");
		if (knight.statPointsToUse > 0) {
			((TextView) rootView.findViewById(R.id.char_statstouse)).setTextColor(Color.RED);
			txtEnergy.setCompoundDrawables(myDrawable, null, null, null);
			txtStr.setCompoundDrawables(myDrawable, null, null, null);
			txtVit.setCompoundDrawables(myDrawable, null, null, null);
			txtDex.setCompoundDrawables(myDrawable, null, null, null);
		}
		txtDex.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (knight.statPointsToUse > 0) {
					// removes a stat point adds to dex
					knight.addDexPoints();
					((TextView) v).setText(knight.dexterity + " Dexterity" + System.getProperty("line.separator")
							+ String.valueOf(
									(float) (1F - ((knight.frameLengthInMilliseconds - (knight.dexterity / 5) * 1F)
											/ knight.frameLengthInMilliseconds)) * 100F)
							+ " Percent Faster Attack Speed");
					// if its at zero, remove drawable
					if (knight.statPointsToUse == 0) {
						((TextView) v.getRootView().findViewById(R.id.char_statstouse)).setTextColor(Color.WHITE);
						resetDrawables(v);
					}
				}
				((TextView) v.getRootView().findViewById(R.id.char_statstouse))
						.setText(knight.statPointsToUse + " Stat Points Remaining");

			}

		});
		txtStr.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (knight.statPointsToUse > 0) {
					knight.addStrPoints();
					((TextView) v).setText(
							knight.strength + " Strength" + System.getProperty("line.separator") + "Current Damage: "
									+ ((int) (knight.BASE_DMG_MIN) + (int) ((double) knight.strength * 0.5F)) + " - "
									+ ((int) (knight.BASE_DMG_MAX) + (int) ((double) knight.strength * 0.5F)));

					if (knight.statPointsToUse == 0) {
						((TextView) v.getRootView().findViewById(R.id.char_statstouse)).setTextColor(Color.WHITE);
						resetDrawables(v);
					}
				}
				((TextView) v.getRootView().findViewById(R.id.char_statstouse))
						.setText(knight.statPointsToUse + " Stat Points Remaining");

			}

		});
		txtVit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (knight.statPointsToUse > 0) {
					knight.addVitPoints();
					((TextView) v).setText(knight.vitality + " Vitality" + System.getProperty("line.separator")
							+ "Life: " + knight.life);

					if (knight.statPointsToUse == 0) {
						((TextView) v.getRootView().findViewById(R.id.char_statstouse)).setTextColor(Color.WHITE);
						resetDrawables(v);
					}
				}
				((TextView) v.getRootView().findViewById(R.id.char_statstouse))
						.setText(knight.statPointsToUse + " Stat Points Remaining");

			}

		});
		txtEnergy.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (knight.statPointsToUse > 0) {
					knight.addEnergyPoints();
					((TextView) v).setText(
							knight.energy + " Energy" + System.getProperty("line.separator") + "Mana: " + knight.mana);

					if (knight.statPointsToUse == 0) {
						((TextView) v.getRootView().findViewById(R.id.char_statstouse)).setTextColor(Color.WHITE);
						resetDrawables(v);
					}
				}
				((TextView) v.getRootView().findViewById(R.id.char_statstouse))
						.setText(knight.statPointsToUse + " Stat Points Remaining");
			}

		});
		txtDex.setGravity(Gravity.CENTER);
		txtStr.setGravity(Gravity.CENTER);
		txtVit.setGravity(Gravity.CENTER);
		txtEnergy.setGravity(Gravity.CENTER);
		txtDex.setText(knight.dexterity + " Dexterity" + System.getProperty("line.separator")
				+ String.valueOf((float) (1F - ((knight.frameLengthInMilliseconds - (knight.dexterity / 5) * 1F)
						/ knight.frameLengthInMilliseconds)) * 100F)
				+ " Percent Faster Attack Speed");
		txtStr.setText(knight.strength + " Strength" + System.getProperty("line.separator") + "Current Damage: "
				+ ((int) (knight.BASE_DMG_MIN) + (int) ((double) knight.strength * 0.5F)) + " - "
				+ ((int) (knight.BASE_DMG_MAX) + (int) ((double) knight.strength * 0.5F)));
		txtVit.setText(knight.vitality + " Vitality" + System.getProperty("line.separator") + "Health: " + knight.life);
		txtEnergy.setText(knight.energy + " Energy" + System.getProperty("line.separator") + "Mana: " + knight.mana);
		txtDex.getLayoutParams().width = viewWidth / 3;
		txtDex.getLayoutParams().height = (viewHeight - 140) / 4;
		txtStr.getLayoutParams().width = viewWidth / 3;
		txtStr.getLayoutParams().height = (viewHeight - 140) / 4;
		txtVit.getLayoutParams().width = viewWidth / 3;
		txtVit.getLayoutParams().height = (viewHeight - 140) / 4;
		txtEnergy.getLayoutParams().width = viewWidth / 3;
		txtEnergy.getLayoutParams().height = (viewHeight - 140) / 4;
		txtDex.setLayoutParams(txtDex.getLayoutParams());
		txtStr.setLayoutParams(txtDex.getLayoutParams());
		txtVit.setLayoutParams(txtDex.getLayoutParams());
		txtEnergy.setLayoutParams(txtDex.getLayoutParams());
		activity.skillStat.addView(rootView);
		return rootView;
	}

	private static void resetDrawables(View v) {
		((TextView) v.getRootView().findViewById(R.id.char_dex)).setCompoundDrawables(null, null, null, null);
		((TextView) v.getRootView().findViewById(R.id.char_str)).setCompoundDrawables(null, null, null, null);
		((TextView) v.getRootView().findViewById(R.id.char_vit)).setCompoundDrawables(null, null, null, null);
		((TextView) v.getRootView().findViewById(R.id.char_energy)).setCompoundDrawables(null, null, null, null);

	}
}
