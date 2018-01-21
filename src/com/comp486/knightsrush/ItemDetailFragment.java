package com.comp486.knightsrush;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Fragment;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.comp486.knightsrush.dummy.ItemBonus;
import com.comp486.knightsrush.dummy.Items;
import com.comp486.knightsrush.dummy.Items.Item;

/**
 * A fragment representing a single Item detail screen. This fragment is either
 * contained in a {@link ItemListActivity} in two-pane mode (on tablets) or a
 * {@link ItemDetailActivity} on handsets.
 */
public class ItemDetailFragment extends Fragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	/**
	 * The content this fragment is presenting.
	 */
	private Items.Item mItem;

	private int id = -1;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ItemDetailFragment() {
	}

	public ItemDetailFragment(int id) {
		this.id = id;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			if (getArguments().containsKey(ARG_ITEM_ID)) {
				// Load the dummy content specified by the fragment
				// arguments. In a real-world scenario, use a Loader
				// to load content from a content provider.
				mItem = Items.ITEM_MAP.get(getArguments().getInt(ARG_ITEM_ID));
			}
		} else {
			if (id != -1) {

				mItem = Items.ITEM_MAP.get(id);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_item_detail, container, false);

		// Show the item
		if (mItem != null) {
			Drawable myDrawable;
			rootView.setMinimumWidth(1200);
			myDrawable = ContextCompat.getDrawable(getActivity(), mItem.id);
			myDrawable.setBounds(0, 0, myDrawable.getMinimumWidth(), myDrawable.getMinimumHeight());
			((TextView) rootView.findViewById(R.id.item_detail)).setCompoundDrawables(null, myDrawable, null, null);
			((TextView) rootView.findViewById(R.id.item_detail)).setText(mItem.name);
			((TextView) rootView.findViewById(R.id.item_content)).setText(mItem.content);
			// ((ImageView)
			// rootView.findViewById(R.id.item_image)).setBackgroundResource(mItem.id);
		}

		return rootView;
	}

	@SuppressLint("NewApi")
	public static View itemPickUpView(MainActivity activity, Item item, LayoutInflater inflater) {
		View rootView = inflater.inflate(R.layout.fragment_item_detail, activity.overlay, false);
		Drawable myDrawable;
		rootView.setMinimumWidth(1000);
		rootView.setMinimumHeight(activity.rl.getHeight());
		rootView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		rootView.setBackgroundColor(Color.BLACK);
		rootView.setAlpha(0.9F);
		TextView text = ((TextView) rootView.findViewById(R.id.item_detail));
		myDrawable = ContextCompat.getDrawable(activity, item.id);
		myDrawable.setBounds(0, 0, myDrawable.getMinimumWidth(), myDrawable.getMinimumHeight());
		text.setCompoundDrawables(null, myDrawable, null, null);
		text.setText(item.name);
		TextView content = ((TextView) rootView.findViewById(R.id.item_content));
		content.setText(item.content);
		setRequiredText(rootView, item, activity);
		setItemBonusText(rootView, item, activity);
		activity.overlay.addView(rootView);
		return rootView;
	}

	@SuppressLint("NewApi")
	public static View pauseScreenView(MainActivity activity, Item item, LayoutInflater inflater) {
		View rootView = inflater.inflate(R.layout.fragment_item_detail, activity.overlay, false);
		View rootView2 = inflater.inflate(R.layout.fragment_item_detail2, activity.rl, false);
		Drawable myDrawable;
		rootView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		rootView2.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		rootView.setMinimumWidth(1000);
		rootView2.setMinimumWidth(activity.gameView.inventory.empty.getWidth());
		rootView.setX(0);
		rootView.setForegroundGravity(Gravity.CENTER_HORIZONTAL);
		rootView2.setMinimumHeight(activity.gameView.mParams.viewHeight);
		rootView2.setMinimumHeight(activity.gameView.mParams.viewHeight);
		rootView.setBackgroundColor(Color.BLACK);
		rootView.setAlpha(0.9F);
		if (item != null) {
			TextView text = ((TextView) rootView.findViewById(R.id.item_detail));
			myDrawable = ContextCompat.getDrawable(activity, item.id);
			myDrawable.setBounds(0, 0, myDrawable.getMinimumWidth(), myDrawable.getMinimumHeight());
			text.setCompoundDrawables(null, myDrawable, null, null);
			text.setText(item.name);
			text.setGravity(Gravity.CENTER);
			TextView content = ((TextView) rootView.findViewById(R.id.item_content));
			content.setGravity(Gravity.CENTER);
			content.setText(item.content);
			setRequiredText(rootView, item, activity);
			setItemBonusText(rootView, item, activity);
			activity.overlay.addView(rootView);
			activity.menubar.addView(rootView2);
		}
		return rootView;
	}

	private static void setRequiredText(View rootView, Item item, Context activity) {
		TextView tvReqStr = ((TextView) rootView.findViewById(R.id.required_str));
		tvReqStr.setText("Str: " + item.requiredStr + "  " + "Dex: " + item.requiredDex);
		tvReqStr.setGravity(Gravity.CENTER);

	}

	private static void setItemBonusText(View rootView, Item item, Context activity) {
		LinearLayout ll = ((LinearLayout) rootView.findViewById(R.id.ll_item_detail));
		TextView tvDamageDefense = ((TextView) rootView.findViewById(R.id.tv_damage_defense));
		int ibMax = 0;
		int ibMin = 0;
		for (ItemBonus ib : item.itemBonuses) {
			TextView ibText = new TextView(activity);
			ibText.setTextColor(Color.WHITE);
			switch (ib.mType) {
			case ItemBonus.DEXTERITY:
				ibText.setText("+ " + ib.mChosen + " added to base dexterity");
				break;
			case ItemBonus.STRENGTH:
				ibText.setText("+ " + ib.mChosen + " added to base strength");
				break;
			case ItemBonus.ENERGY:
				ibText.setText("+ " + ib.mChosen + " added to base energy");
				break;
			case ItemBonus.LIFELEECH_PERCENT:
				ibText.setText("+ " + ib.mChosen + " percent life leeched as a percentage of total health");
				break;
			case ItemBonus.PHYSICAL_DAMAGE_MAX:
				if (item.type == Items.WEAPON_TYPE) {
					ibMax += ib.mChosen;
				}
				ibText.setText("+ " + ib.mChosen + " added to max physical damage");
				break;
			case ItemBonus.PHYSICAL_DAMAGE_MIN:
				if (item.type == Items.WEAPON_TYPE) {
					ibMin += ib.mChosen;
				}
				ibText.setText("+ " + ib.mChosen + " added to min physical damage");
				break;
			case ItemBonus.PHYSICAL_DAMAGE_MAXMIN:
				if (item.type == Items.WEAPON_TYPE) {
					ibMin += ib.mChosen;
					ibMax += ib.mChosen;
				}
				ibText.setText("+ " + ib.mChosen + " added to max/min physical damage");
				break;
			case ItemBonus.VITALITY:
				ibText.setText("+ " + ib.mChosen + " added to base vitality");
				break;
			default:
				break;
			}
			ibText.setGravity(Gravity.CENTER);
			ll.addView(ibText);
		}
		tvDamageDefense.setGravity(Gravity.CENTER);
		if (ibMax > 0 || ibMin > 0) {
			tvDamageDefense.setTextColor(Color.RED);
		}
		switch (item.type) {
		case Items.WEAPON_TYPE:
			tvDamageDefense.setText("Damage: " + (int) (item.minStat + ibMin) + " - " + (int) (item.maxStat + ibMax));
			break;
		case Items.SHIELD_TYPE:
			tvDamageDefense.setText("Defense: " + (int) (item.maxStat + ibMax));
			break;
		case Items.HELMET_TYPE:
			tvDamageDefense.setText("Defense: " + (int) (item.maxStat + ibMax));
			break;
		}
	}
}
