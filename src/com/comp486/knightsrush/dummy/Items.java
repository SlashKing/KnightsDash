package com.comp486.knightsrush.dummy;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.comp486.knightsrush.DifficultyConstants;
import com.comp486.knightsrush.EnemySprite;
import com.comp486.knightsrush.GameAct;
import com.comp486.knightsrush.KnightSprite;
import com.comp486.knightsrush.Potion;
import com.comp486.knightsrush.R;
import com.comp486.knightsrush.Sprite;
import com.comp486.knightsrush.GameAct.AreaGroup;
import com.comp486.knightsrush.GameAct.DialogEntry;
import com.comp486.knightsrush.GameAct.GameArea;
import com.comp486.knightsrush.GameAct.Quest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 */
/**
 * @author Nick
 *
 */
public class Items {

	/**
	 * An array of sample items.
	 */
	public static List<Item> ITEMS = new ArrayList<Item>();
	public static List<Item> NORMAL_ITEMS = new ArrayList<Item>();
	public static List<Item> MEDIEVAL_ITEMS = new ArrayList<Item>();
	public static List<Item> LEGENDARY_ITEMS = new ArrayList<Item>();

	// Type of item constants
	public static final int HELMET_TYPE = 0;
	public static final int ARMOR_TYPE = 1;
	public static final int WEAPON_TYPE = 2;
	public static final int BOOT_TYPE = 3;
	public static final int GLOVE_TYPE = 4;
	public static final int RING_TYPE = 5;
	public static final int AMULET_TYPE = 6;
	public static final int BELT_TYPE = 7;
	public static final int SHIELD_TYPE = 8;

	// Normal, Magic, Rare, or Unique constants
	public static final int MAGIC = 0;
	public static final int NORMAL = 1;
	public static final int RARE = 2;
	public static final int UNIQUE = 3;

	// Normal, medieval, or legendary item class
	public static final int ITEM_CLASS_NORMAL = 0;
	public static final int ITEM_CLASS_MEDIEVAL = 1;
	public static final int ITEM_CLASS_LEGENDARY = 2;

	// database
	public static DaoUtils du;

	/**
	 * A map of sample items, by ID.
	 */
	public static Map<Integer, Item> ITEM_MAP = new HashMap<Integer, Item>();

	private static boolean mLoaded = false;
	private static int mLoadedResource = -1;

	static {
		// Add items from xml file.
	}

	private static void addItem(Item item, int iClass) {
		ITEMS.add(item);
		switch (iClass) {
		case ITEM_CLASS_NORMAL:
			NORMAL_ITEMS.add(item);
			break;
		case ITEM_CLASS_MEDIEVAL:
			MEDIEVAL_ITEMS.add(item);
			break;
		case ITEM_CLASS_LEGENDARY:
			LEGENDARY_ITEMS.add(item);
			break;
		}
		ITEM_MAP.put(item.id, item);
	}

	/**
	 * @description load all the items into memory so we can access them and
	 *              generate random items from them
	 * @param context
	 * @param difficulty
	 */
	public static final void loadItems(Context context, int resourceId)
			throws XmlPullParserException, FileNotFoundException {
		if (ITEMS.size() > 0 && mLoadedResource == resourceId) {
			// already loaded
			return;

		}

		if (!Items.isLoaded(resourceId)) {

			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();

			// parse xml from raw folder
			parser.setInput(context.getResources().openRawResource(resourceId), "UTF-8");
			ITEMS.clear();
			NORMAL_ITEMS.clear();
			MEDIEVAL_ITEMS.clear();
			LEGENDARY_ITEMS.clear();
			du = new DaoUtils();
			du.createDB(context);
			Item item = null;

			/**
			 * this while statement wrapped in a try/catch block iterates
			 * through the document starting at the very first tag and running
			 * till it finds the end of the file.
			 */

			try {
				int eventType = parser.getEventType();
				while (eventType != XmlPullParser.END_DOCUMENT) {
					if (eventType == XmlPullParser.START_TAG) {
						if (parser.getName().equals("item")) {
							item = new Item();
							for (int i = 0; i < parser.getAttributeCount(); i++) {
								final String value = parser.getAttributeValue(i);
								if (value != null) {
									if (parser.getAttributeName(i).equals("id")) {
										item.id = GameAct.getResourceId(context,
												value.substring(value.indexOf("/") + 1, value.length()),
												value.substring(1, value.indexOf("/")));
									} else if (parser.getAttributeName(i).equals("dropChance")) {
										item.dropChance = Integer.parseInt(value);
									} else if (parser.getAttributeName(i).equals("type")) {
										item.type = Integer.parseInt(value);
									} else if (parser.getAttributeName(i).equals("iLvl")) {
										item.iLvl = Integer.parseInt(value);
									} else if (parser.getAttributeName(i).equals("iClass")) {
										item.iClass = Integer.parseInt(value);
									} else if (parser.getAttributeName(i).equals("maxStat")) {
										item.maxStat = Integer.parseInt(value);
									} else if (parser.getAttributeName(i).equals("minStat")) {
										item.minStat = Integer.parseInt(value);
									} else if (parser.getAttributeName(i).equals("content")) {
										item.content = value;
									} else if (parser.getAttributeName(i).equals("name")) {
										item.name = value;
									} else if (parser.getAttributeName(i).equals("slots")) {
										item.slots = Integer.parseInt(value);
									} else if (parser.getAttributeName(i).equals("requiredStr")) {
										item.requiredStr = Integer.parseInt(value);
									} else if (parser.getAttributeName(i).equals("requiredDex")) {
										item.requiredDex = Integer.parseInt(value);
									}

								}

							}

							addItem(item, item.iClass);
							item = null;
						}
					}
					eventType = parser.next();
				}

			} catch (Exception e) {
				Log.e("Loading Items: ", e.getStackTrace().toString());
			} finally {
				mLoadedResource = resourceId;
				mLoaded = true;
			}
		}
	}

	private static Item itemsBasedonArea(Item item, int aLvl, Random random) {
		if (aLvl <= 25) {
			item = NORMAL_ITEMS.get(random.nextInt(NORMAL_ITEMS.size()));
		} else if (aLvl > 25 && aLvl < 55) {
			item = MEDIEVAL_ITEMS.get(random.nextInt(MEDIEVAL_ITEMS.size()));
		} else {
			item = LEGENDARY_ITEMS.get(random.nextInt(LEGENDARY_ITEMS.size()));
		}
		return item;
	}

	public static Item getItemToDrop(Random random, int aLvl) {
		// get item based on area lvl
		Item item = null;
		item = itemsBasedonArea(item, aLvl, random);
		int number = random.nextInt(1000) + 1;
		while (number > item.dropChance) {
			number = random.nextInt(1000) + 1;
			item = itemsBasedonArea(item, aLvl, random);
		}
		Item newItem = new Item();
		newItem.id = item.id;
		newItem.dropChance = item.dropChance;
		newItem.iClass = item.iClass;
		newItem.iLvl = item.iLvl;
		newItem.type = item.type;
		newItem.maxStat = item.maxStat;
		newItem.minStat = item.minStat;
		newItem.content = item.content;
		newItem.name = item.name;
		newItem.requiredDex = item.requiredDex;
		newItem.requiredStr = item.requiredStr;
		int bonusCount = 0;
		int currentRandom = 0;
		float bonusRandom = 0;
		int typeRandom = 0;
		switch (item.iClass) {
		case Items.ITEM_CLASS_NORMAL:
			bonusCount = 2;
			break;
		case Items.ITEM_CLASS_MEDIEVAL:
			bonusCount = 3;
			break;
		case Items.ITEM_CLASS_LEGENDARY:
			bonusCount = 4;
			break;
		default:
			break;

		}
		currentRandom = random.nextInt(bonusCount) + 1;
		ArrayList<Integer> numbers = new ArrayList<Integer>();
		while (numbers.size() < currentRandom) {
			Integer next = random.nextInt(ItemBonus.PHYSICAL_DAMAGE_MAXMIN + 1);
			// As we're adding to a set, this will automatically do a
			// containment check to 
			if (!numbers.contains(Integer.valueOf(next))) {
				numbers.add(next);
			}
		}

		for (int d = 0; d < currentRandom; d++) {
			typeRandom = numbers.get(d);
			switch (item.iClass) {
			case Items.ITEM_CLASS_NORMAL:
				if (item.iLvl <= 11) {
					bonusRandom = random.nextInt(3) + 1;
				} else if (item.iLvl > 11 && item.iLvl < 21) {
					bonusRandom = random.nextInt(4) + 1;
				} else {
					bonusRandom = random.nextInt(5) + 1;
				}
				break;
			case Items.ITEM_CLASS_MEDIEVAL:
				if (item.iLvl <= 30) {
					bonusRandom = random.nextInt(13 - 7) + 7;
				} else if (item.iLvl > 30 && item.iLvl < 45) {
					bonusRandom = random.nextInt(17 - 7) + 7;
				} else {
					bonusRandom = random.nextInt(20 - 7) + 7;
				}
				break;
			case Items.ITEM_CLASS_LEGENDARY:
				if (item.iLvl <= 55) {
					bonusRandom = random.nextInt(20 - 12) + 12;
				} else if (item.iLvl > 55 && item.iLvl < 75) {
					bonusRandom = random.nextInt(25 - 12) + 15;
				} else {
					bonusRandom = random.nextInt(40 - 20) + 20;
				}
				break;
			default:
				break;

			}
			// life leech is Over-Powered (OP) at the moment so reduce the
			// amount available to
			// the player
			if (typeRandom == ItemBonus.LIFELEECH_PERCENT || typeRandom == ItemBonus.MANALEECH_PERCENT) {
				bonusRandom = random.nextInt(2) + 1;
			}
			if (item.type != Items.WEAPON_TYPE
					&& (typeRandom == ItemBonus.LIFELEECH_PERCENT|| typeRandom == ItemBonus.MANALEECH_PERCENT)) {
				// don't add life or mana leech stats to anything but weapons
			} else {
				newItem.itemBonuses.add(new ItemBonus(typeRandom, bonusRandom));

			}
		}
		return newItem;
	}

	/**
	 * @description Is the resource loaded
	 * @param resource
	 * @return mLoaded && mLoadedResource == resource
	 */
	public static final boolean isLoaded(int resource) {
		return mLoaded && mLoadedResource == resource;
	}

	/**
	 * An item
	 */
	public static class Item extends Sprite {
		public long autoid = -1; // database id
		public int id;// unique id - based on bitmap resource id
		public String name;
		public String content;// general description
		public int type;// helmet,armor,boot,glove,ring,belt. or amulet?
		public float dropChance;// likelihood to drop
		public int iLvl;// item level
		public int iClass;// item class
		public int requiredStr;
		public int requiredDex;
		public int minStat;// minimum stat (defense, damage, etc...)
		public int maxStat;// maximum stat (defense, damage, etc...)
		public boolean hasBeenLookedAt = false;
		public boolean isEquipped = false;
		public boolean inStash = false;
		public int slots;
		public ArrayList<ItemBonus> itemBonuses = new ArrayList<ItemBonus>();
		private Rect bounds = new Rect(0, 0, 0, 0);

		public Item(int id, String content, String name, int type, float dropChance, int iLvl, int minStat, int maxStat,
				int iClass, int requiredStr, int requiredDex) {
			super(0, 0, 1, 1, true, null, null);
			this.id = id;
			this.content = content;
			this.type = type;
			this.dropChance = dropChance;
			this.iLvl = iLvl;
			this.iClass = iClass;
			this.minStat = minStat;
			this.maxStat = maxStat;
			this.requiredStr = requiredStr;
			this.requiredDex = requiredDex;
			this.name = name;
			framecount = 1;
			rows = 1;
		}

		public Item(int id, String content, String name, int type, float dropChance, int iLvl, int minStat, int maxStat,
				int iClass, int requiredStr, int requiredDex, int x, int y, boolean isEquipped, int autoid) {
			super(x, y, 1, 1, true, null, null);
			mPosition.set(x, y);
			this.x = x;
			this.y = y;
			framecount = 1;
			rows = 1;
			this.id = id;
			this.content = content;
			this.type = type;
			this.dropChance = dropChance;
			this.iLvl = iLvl;
			this.iClass = iClass;
			this.minStat = minStat;
			this.maxStat = maxStat;
			this.requiredStr = requiredStr;
			this.requiredDex = requiredDex;
			this.name = name;
			this.isEquipped = isEquipped;
			this.autoid = autoid;
			// DaoUtils dao = new DaoUtils();
			insertIntoDB(isEquipped);

		}

		public void insertIntoDB(boolean equipped) {
			if (du.sql != null) {

				du.attrs = new String[26];
				du.attrs[0] = "id";
				du.attrs[1] = String.valueOf(id);
				du.attrs[2] = "dropChance";
				du.attrs[3] = String.valueOf(dropChance);
				du.attrs[4] = "type";
				du.attrs[5] = String.valueOf(type);
				du.attrs[6] = "iLvl";
				du.attrs[7] = String.valueOf(iLvl);
				du.attrs[8] = "iClass";
				du.attrs[9] = String.valueOf(iClass);
				du.attrs[10] = "minStat";
				du.attrs[11] = String.valueOf(minStat);
				du.attrs[12] = "maxStat";
				du.attrs[13] = String.valueOf(maxStat);
				du.attrs[14] = "slots";
				du.attrs[15] = String.valueOf(slots);
				du.attrs[16] = "requiredStr";

				du.attrs[17] = String.valueOf(requiredStr);
				du.attrs[18] = "requiredDex";

				du.attrs[19] = String.valueOf(requiredDex);
				du.attrs[20] = "content";

				du.attrs[21] = content;
				du.attrs[22] = "name";

				du.attrs[23] = name;
				du.attrs[24] = "equipped";
				du.attrs[25] = equipped ? String.valueOf(1) : String.valueOf(0);
				long rowId = Items.du.insert(du.TBL_ITEMS, du.attrs);
				this.autoid = rowId;
			}
		}

		public Item() {

		}

		public void update(int moveX) {
			getPosition().x += moveX;
		}

		public void update(int moveX, KnightSprite knight) {
			getPosition().x += moveX;
			setBounds();
			knight.checkCollisions(getBounds(), knight.getBounds(), this);
		}

		public void draw(Canvas canvas, Paint paint) {
			if (isVisible()) {
				if (spritesheet != null) {
					if (!spritesheet.isRecycled()) {
						canvas.drawBitmap(spritesheet, getPosition().x, getPosition().y, paint);
					}
				}
			}
		}

		public static void dropItem(Item item, EnemySprite enemy) {
			item.setVisible(true);
			if (!enemy.isJumping()) {
				item.getPosition().set(enemy.getPosition().x + 90,
						enemy.getPosition().y - item.spritesheet.getHeight());
			} else {
				if (enemy.getPosition().y <= enemy.mGv.plat.getPosition().y) {
					item.getPosition().set(enemy.getPosition().x + 90,
							enemy.mGv.plat.getPosition().y - item.spritesheet.getHeight());
				} else {
					item.getPosition().set(enemy.getPosition().x + 90,
							enemy.mGv.bg.getPosition().y - item.spritesheet.getHeight());
				}
			}
		}

		public Rect setBounds() {
			bounds.set((int) getPosition().x, (int) getPosition().y, (int) getPosition().x + width,
					(int) getPosition().y - height);
			return bounds;
		}

		public Rect getBounds() {
			return bounds;
		}

		@Override
		public String toString() {
			return name + ": " + content;
		}

		public void destroy() {
			mPosition.set(0, 4000);
			setVisible(false);
			if (spritesheet != null && !spritesheet.isRecycled()) {
				spritesheet.recycle();
			}
		}
	}
}
