package com.comp486.knightsrush;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class Potion extends Sprite {
	private static GameView mGv;
	private int mId = -1;
	private boolean isAssigned;
	private Rect bounds = new Rect(0, 0, 0, 0);

	public enum Size {
		SMALL, LARGE
	};

	public Size size;

	public Potion(int x, int y, int rows, int framecount, boolean vis, Bitmap spritesheet, ContextParameters params,
			GameView gv, int id) {
		super(x, y, rows, framecount, vis, spritesheet, params);
		mGv = gv;
		isAssigned = false;
		setVisible(false);
		team = Team.NONE;
		size = Size.SMALL;
		mId = id;
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
			canvas.drawBitmap(mGv.potionSm, getPosition().x, getPosition().y, paint);
		}
	}

	public static void dropPotion(EnemySprite enemy) {
		Potion potion = enemy.potion;
		potion.setVisible(true);
		if (!enemy.isJumping()) {
			potion.getPosition().set(enemy.getPosition().x + 90, enemy.getPosition().y-potion.spritesheet.getHeight());
		} else {

			if (enemy.getPosition().y <= enemy.mGv.plat.getPosition().y) {
				potion.getPosition().set(enemy.getPosition().x + 90,
						enemy.mGv.plat.getPosition().y - potion.spritesheet.getHeight());
			}else{
				potion.getPosition().set(enemy.getPosition().x + 90,
						enemy.mGv.bg.getPosition().y  -potion.spritesheet.getHeight());
			}
		}
	}

	public void setId(int i) {
		mId = i;
	}

	public int getId() {
		return mId;
	}

	public void setAssigned(boolean bool) {
		isAssigned = bool;
	}

	public boolean isAssigned() {
		return isAssigned;
	}

	public Rect setBounds() {
		bounds.set((int) getPosition().x, (int) getPosition().y, (int) getPosition().x + width,
				(int) getPosition().y - height);
		return bounds;
	}

	public Rect getBounds() {
		return bounds;
	}
}
