package com.comp486.knightsrush;

import java.util.ArrayList;

import com.comp486.knightsrush.dummy.ItemBonus;
import com.comp486.knightsrush.dummy.Items;
import com.comp486.knightsrush.dummy.Items.Item;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

public class InventoryMenu extends Sprite {
	boolean objectSelected = false;
	Item selectedItem = null;

	public final int COLUMNS = 4;
	public final int ROWS = 4;

	Bitmap empty = null;
	ArrayList<Item> inv = new ArrayList<Item>((ROWS + 1) * COLUMNS);

	/**
	 * Initialize inventory Fetches items and item bonuses from the database if
	 * they exist and adds to the inventory array
	 * 
	 * @param ctx
	 * @param cp
	 * @param bmp
	 */
	public InventoryMenu(Context ctx, ContextParameters cp, Bitmap bmp) {

		empty = bmp;
		Cursor c = Items.du.getInventoryItems("");
		final String[] ibkey = new String[1];
		String[] var = new String[1];
		ibkey[0] = "item_id";
		while (c.moveToNext()) {
			Item item = new Item(c.getInt(1), c.getString(11), c.getString(12), c.getInt(3), c.getFloat(2), c.getInt(4),
					c.getInt(6), c.getInt(7), c.getInt(5), c.getInt(9), c.getInt(10));
			item.autoid = c.getInt(0);
			item.spritesheet = resizeImage(BitmapFactory.decodeResource(ctx.getResources(), item.id),
					empty.getHeight());
			Cursor c2;
			var[0] = String.valueOf(c.getInt(0));
			c2 = Items.du.execSQL(Items.du.TBL_ITEMBONUS, ibkey, var);
			while (c2.moveToNext()) {
				item.itemBonuses.add(new ItemBonus(c.getInt(0), c2.getInt(2), c2.getFloat(3)));
			}
			c2.close();
			inv.add(item);

		}
		c.close();
	}

	/**
	 * Resizes a bitmap to the desired max width or height
	 * 
	 * @param bitmap
	 * @param scaleSize
	 * @return
	 */
	public static Bitmap resizeImage(Bitmap bitmap, int scaleSize) {
		Bitmap resizedBitmap = null;
		int originalWidth = bitmap.getWidth();
		int originalHeight = bitmap.getHeight();
		int newWidth = -1;
		int newHeight = -1;
		float multFactor = -1.0F;
		if (originalHeight > originalWidth) {
			newHeight = scaleSize;
			multFactor = (float) originalWidth / (float) originalHeight;
			newWidth = (int) (newHeight * multFactor);
		} else if (originalWidth > originalHeight) {
			newWidth = scaleSize;
			multFactor = (float) originalHeight / (float) originalWidth;
			newHeight = (int) (newWidth * multFactor);
		} else if (originalHeight == originalWidth) {
			newHeight = scaleSize;
			newWidth = scaleSize;
		}
		resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
		return resizedBitmap;
	}

	/**
	 * Generates a bitmap for each inventory slot, then if the database has any
	 * inventory items, they are placed on top of the slot TODO: at a location
	 * field to the Item class so the location in the inventory may persist
	 * 
	 * @param canvas
	 * @param contextParameters
	 */
	public void render(Canvas c, ContextParameters p) {
		final int maxWidth = p.viewWidth - (empty.getWidth() * COLUMNS);
		final int maxHeight = p.viewHeight - (empty.getHeight() * ROWS);
		for (int i = 0; i < COLUMNS * ROWS; i++) {
			int col = i % COLUMNS;
			int row = ROWS - i / COLUMNS;

			c.drawBitmap(empty, maxWidth + (empty.getWidth() * col),
					maxHeight + (empty.getHeight() * row) - empty.getHeight(), null);

			if (!inv.isEmpty()) {
				if (i < inv.size()) {
					if (inv.get(i) != null) {
						Item item = inv.get(i);
						if (!item.isVisible()) {
							item.setVisible(true);
						}
						item.getPosition()
								.set(maxWidth + (empty.getWidth() * col)
										+ ((empty.getWidth() - item.spritesheet.getWidth()) / 2),
										maxHeight + (empty.getHeight() * row) - empty.getHeight()
												+ ((empty.getHeight() - item.spritesheet.getHeight()) / 2));
						item.setBounds();
						item.draw(c, null);
					}
				}
			}
		}
	}
}
