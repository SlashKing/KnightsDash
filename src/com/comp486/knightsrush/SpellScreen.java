package com.comp486.knightsrush;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class SpellScreen {

	View rootView;

	public SpellScreen() {

	}

	public static View getSpellScreen(int viewWidth, int viewHeight, MainActivity activity, final KnightSprite knight,
			LayoutInflater inflater) {
		View rootView = inflater.inflate(R.layout.fragment_spell_screen, activity.skillStat, false);
		Drawable myDrawable;
		rootView.setBackgroundColor(Color.GRAY);
		rootView.setBackgroundResource(R.drawable.bgspells);
		ImageView imgZeal = ((ImageView) rootView.findViewById(R.id.img_zeal));
		ImageView imgFire = ((ImageView) rootView.findViewById(R.id.img_fire));
		ImageView imgLight = ((ImageView) rootView.findViewById(R.id.img_light));
		TextView txtZeal = ((TextView) rootView.findViewById(R.id.char_zeal));
		TextView txtFire = ((TextView) rootView.findViewById(R.id.char_fire));
		TextView txtLight = ((TextView) rootView.findViewById(R.id.char_light));
		TextView txtRemaining = ((TextView) rootView.findViewById(R.id.char_spellstouse));
		txtRemaining.setText(knight.skillPointsToUse + " Skill Points Remaining");
		txtRemaining.setGravity(Gravity.CENTER_HORIZONTAL);
		imgZeal.setPadding(0, 140, 0, 0);
		imgFire.setPadding(0, 140, 0, 0);
		imgLight.setPadding(0, 140, 0, 0);
		int res;
		if (knight.skillPointsToUse > 0) {
			txtRemaining.setTextColor(Color.RED);
		} else {
		}
		imgZeal.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (knight.skillPointsToUse > 0 && knight.addedZeal < 30) {
					final Spell spell = knight.spells.get(0);
					knight.addZealPoints();
					((TextView) v.getRootView().findViewById(R.id.char_zeal)).setText("ZEAL"
							+ System.getProperty("line.separator") + "LVL " + knight.addedZeal
							+ System.getProperty("line.separator")
							+ (int) Math.floor((float) (knight.BASE_DMG_MIN + (knight.strength * 0.5F))
									* (0.5F + ((float) knight.addedZeal / 100F)))
							+ " - "
							+ +(int) Math.floor((float) (knight.BASE_DMG_MAX + (knight.strength * 0.5F))
									* (0.5F + ((float) knight.addedZeal / 100F)))
							+ System.getProperty("line.separator") + "NEXT LVL" + System.getProperty("line.separator")
							+ (int) Math.floor((float) (knight.BASE_DMG_MIN + (knight.strength * 0.5F))
									* (0.5F + ((float) (knight.addedZeal + 1) / 100F)))
							+ " - " + (int) Math.floor((float) (knight.BASE_DMG_MAX + (knight.strength * 0.5F))
									* (0.5F + ((float) (knight.addedZeal + 1) / 100F))));
					if (knight.skillPointsToUse == 0) {
						((TextView) v.getRootView().findViewById(R.id.char_spellstouse)).setTextColor(Color.BLACK);
					}
				}
				((TextView) v.getRootView().findViewById(R.id.char_spellstouse))
						.setText(knight.skillPointsToUse + " Skill Points Remaining");

			}

		});
		imgFire.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (knight.skillPointsToUse > 0 && knight.addedFB < 30) {
					final Spell spell = knight.spells.get(1);
					knight.addFirePoints();
					spell.getBaseDamage(knight);
					((TextView) v.getRootView().findViewById(R.id.char_fire))
							.setText("FIRE BALL" + System.getProperty("line.separator") + "LVL " + knight.addedFB
									+ System.getProperty("line.separator") + spell.min + " - " + spell.max
									+ System.getProperty("line.separator") + "NEXT LVL "
									+ System.getProperty("line.separator") + (spell.min + 2) + " - " + (spell.max + 3));
					if (knight.skillPointsToUse == 0) {
						((TextView) v.getRootView().findViewById(R.id.char_spellstouse)).setTextColor(Color.BLACK);
					}
				}
				((TextView) v.getRootView().findViewById(R.id.char_spellstouse))
						.setText(knight.skillPointsToUse + " Skill Points Remaining");
			}

		});
		imgLight.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (knight.skillPointsToUse > 0 && knight.addedLS < 30) {
					final Spell spell = knight.spells.get(2);
					knight.addLightPoints();
					spell.getBaseDamage(knight);
					((TextView) v.getRootView().findViewById(R.id.char_light))
							.setText(" Light Storm" + System.getProperty("line.separator") + "LVL " + knight.addedLS
									+ System.getProperty("line.separator") + spell.min + " - " + spell.max
									+ System.getProperty("line.separator") + "Next LVL "
									+ System.getProperty("line.separator") + spell.min + " - " + (spell.max + 8));
					if (knight.skillPointsToUse == 0) {
						((TextView) v.getRootView().findViewById(R.id.char_spellstouse)).setTextColor(Color.BLACK);
					}
				}
				((TextView) v.getRootView().findViewById(R.id.char_spellstouse))
						.setText(knight.skillPointsToUse + " Skill Points Remaining");

			}

		});
		setZealText(knight, txtZeal);
		setFBText(knight, txtFire);
		setLSText(knight, txtLight);
		txtZeal.setGravity(Gravity.CENTER);
		txtFire.setGravity(Gravity.CENTER);
		txtLight.setGravity(Gravity.CENTER);
		activity.skillStat.addView(rootView);
		return rootView;
	}

	private static void setZealText(KnightSprite knight, TextView txtZeal) {
		if (knight.addedZeal > 0) {
			txtZeal.setText("ZEAL" + System.getProperty("line.separator") + "LVL " + knight.addedZeal
					+ System.getProperty("line.separator")
					+ (int) Math.floor((float) (knight.BASE_DMG_MIN + (knight.strength * 0.5F))
							* (0.5F + ((float) knight.addedZeal / 100F)))
					+ " - "
					+ +(int) Math.floor((float) (knight.BASE_DMG_MAX + (knight.strength * 0.5F))
							* (0.5F + ((float) knight.addedZeal / 100F)))
					+ System.getProperty("line.separator") + "NEXT LVL" + System.getProperty("line.separator")
					+ (int) Math.floor((float) (knight.BASE_DMG_MIN + (knight.strength * 0.5F))
							* (0.5F + ((float) (knight.addedZeal + 1) / 100F)))
					+ " - " + (int) Math.floor((float) (knight.BASE_DMG_MAX + (knight.strength * 0.5F))
							* (0.5F + ((float) (knight.addedZeal + 1) / 100F))));
		} else {
			txtZeal.setText(
					"ZEAL" + System.getProperty("line.separator") + "NEXT LVL" + System.getProperty("line.separator")
							+ (int) Math.floor((float) (knight.BASE_DMG_MIN + (knight.strength * 0.5F))
									* (0.5F + ((float) (knight.addedZeal + 1) / 100F)))
							+ " - " + (int) Math.floor((float) (knight.BASE_DMG_MAX + (knight.strength * 0.5F))
									* (0.5F + ((float) (knight.addedZeal + 1) / 100F))));

		}

	}

	private static void setFBText(KnightSprite knight, TextView txtFire) {
		final Spell fire = knight.spells.get(1);
		if (knight.addedFB > 0) {
			txtFire.setText("FIRE BALL" + System.getProperty("line.separator") + "LVL " + knight.addedFB
					+ System.getProperty("line.separator") + fire.min + " - " + fire.max
					+ System.getProperty("line.separator") + "NEXT LVL " + System.getProperty("line.separator")
					+ (fire.min + 2) + " - " + (fire.max + 3));
		} else {
			txtFire.setText("FIRE BALL" + System.getProperty("line.separator") + "NEXT LVL "
					+ System.getProperty("line.separator") + (fire.min + 2) + " - " + (fire.max + 3));

		}

	}

	private static void setLSText(KnightSprite knight, TextView txtLight) {
		final Spell light = knight.spells.get(2);
		if (knight.addedLS > 0) {
			txtLight.setText(" Light Storm" + System.getProperty("line.separator") + "LVL " + knight.addedLS
					+ System.getProperty("line.separator") + light.min + " - " + light.max
					+ System.getProperty("line.separator") + "NEXT LVL " + System.getProperty("line.separator")
					+ light.min + " - " + (light.max + 8));
		} else {
			txtLight.setText(" Light Storm" + System.getProperty("line.separator") + "NEXT LVL "
					+ System.getProperty("line.separator") + light.min + " - " + (light.max + 8));
		}

	}
}
