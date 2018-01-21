package com.comp486.knightsrush.dummy;

import java.util.Random;

import android.database.Cursor;

/**
 * ItemBonus class
 * 
 * @description Items will be assigned random bonuses NORMAL items can have a
 *              max of 2 bonuses MEDIEVAL items can have up to 3 bonuses
 *              LEGENDARY items can have up to 4 bonuses
 * @author Nick LeBlanc
 *
 */
public class ItemBonus {
	// BONUS TYPES
	public static final int DEXTERITY = 0;
	public static final int STRENGTH = 1;
	public static final int ENERGY = 2;
	public static final int VITALITY = 3;
	public static final int LIFELEECH_PERCENT = 4;
	public static final int MANALEECH_PERCENT = 5;
	public static final int PHYSICAL_DAMAGE_MAX = 6;
	public static final int PHYSICAL_DAMAGE_MIN = 7;
	public static final int PHYSICAL_DAMAGE_MAXMIN = 8;

	public int mType;
	public int mMin;
	public int mMax;
	public float mChosen;

	public ItemBonus() {

	}

	public ItemBonus(int type, float chosen) {
		mType = type;
		mChosen = chosen;
	}

	public ItemBonus(int type, int min, int max, float chosen) {
		mType = type;
		mChosen = chosen;
	}

	public ItemBonus(int itemid, int type, float chosen) {
		mType = type;
		mChosen = chosen;
		insertIntoDB(itemid,type,chosen);

	}
	public void insertIntoDB(int itemid, int type, float chosen){
		if (Items.du.sql != null) {
			if (!Items.du.doesItemBonusExist(itemid, type)) {
				Items.du.attrs = new String[6];
				Items.du.attrs[0] = "item_id";
				Items.du.attrs[1] = String.valueOf(itemid);
				Items.du.attrs[2] = "type";
				Items.du.attrs[3] = String.valueOf(type);
				Items.du.attrs[4] = "chosen";
				Items.du.attrs[5] = String.valueOf(chosen);
				Items.du.insert(Items.du.TBL_ITEMBONUS, Items.du.attrs);
			}
		}
	}
}
