package com.comp486.knightsrush;

import java.util.ArrayList;

import com.comp486.knightsrush.ContextParameters;
import com.comp486.knightsrush.EnemySprite;
import com.comp486.knightsrush.GameView;
import com.comp486.knightsrush.KnightSprite;
import com.comp486.knightsrush.Sprite;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class Spell extends Sprite {
	public static final int ZEAL = 0;
	public static final int FIREBALL = 1;
	public static final int LIGHTSTORM = 2;
	public static final int SMITE = 3;
	public static final int MAX_LEVEL = 25;

	protected int range = -1;
	protected int type = ZEAL;
	protected int min, max;
	protected int attackDelay = 3300;
	protected long lastFrameChangeTime;
	protected long frameLengthInMilliseconds = 50;
	private int currentFrame;
	private boolean loop;
	private boolean hasExplosionAnim;
	public boolean isHit;
	private boolean isMoving;
	public int manaCost;
	protected Bitmap left, right, leftExplode, rightExplode;
	public Rect whereToDraw = new Rect(0, 0, 0, 0), frameToDraw = new Rect(0, 0, 0, 0);

	Spell(int type, int x, int y, int framecount, ContextParameters params, KnightSprite knight) {
		super(x, y, 1, framecount, false, null, params);
		facingDirection.x = 1;
		this.framecount = framecount;
		this.rows = 1;
		mPosition.set(x, y);
		loop = true;
		this.type = type;
		switch (type) {
		case ZEAL:
			attackDelay = 0;
			manaCost = 5;
			break;
		case FIREBALL:
			min = 1 + (knight.addedFB * 4);
			max = 4 + (knight.addedFB * 7);
			manaCost = 7 + knight.addedFB;
			range = 600 + (knight.addedFB * 20);
			setSpeedX(14);
			setSpeedY(0);
			hasExplosionAnim = true;
			left = BitmapFactory.decodeResource(params.context.getResources(), R.drawable.left_fb);
			right = BitmapFactory.decodeResource(params.context.getResources(), R.drawable.right_fb);
			leftExplode = BitmapFactory.decodeResource(params.context.getResources(), R.drawable.left_explode);
			rightExplode = BitmapFactory.decodeResource(params.context.getResources(), R.drawable.right_explode);
			break;
		case LIGHTSTORM:
			min = 1;
			max = 10 + (knight.addedLS * 8);
			manaCost = 7 + knight.addedLS;
			range = params.viewWidth / 2;
			setSpeedX(8);
			setSpeedY(0);
			hasExplosionAnim = false;
			left = BitmapFactory.decodeResource(params.context.getResources(), R.drawable.left_light);
			right = BitmapFactory.decodeResource(params.context.getResources(), R.drawable.right_light);
			break;
		}
		if (right != null) {
			spritesheet = right;
		}
		if (spritesheet != null) {

			setFrameWidth(spritesheet.getWidth());
			setFrameHeight(spritesheet.getHeight());
		}
	}

	public int getBaseManaCost(KnightSprite knight) {
		switch (type) {
		case ZEAL:
			manaCost = 5 + knight.addedZeal;
			break;
		case FIREBALL:
			manaCost = 5 + knight.addedFB;
			break;
		case LIGHTSTORM:
			manaCost = 5 + knight.addedLS;
			break;
		}
		return manaCost;
	}

	public void getBaseDamage(KnightSprite knight) {
		switch (type) {
		case ZEAL:
			attackDelay = 0;
			break;
		case FIREBALL:
			min = 1 + (knight.addedFB * 2);
			max = 4 + (knight.addedFB * 3);
			range = 600 + (knight.addedFB * 20);
			break;
		case LIGHTSTORM:
			min = 1;
			max = 10 + (knight.addedLS * 8);
			break;
		}
	}

	public static void loadSpells(KnightSprite knight, ContextParameters params) {
		knight.spells.add(new Spell(ZEAL, 0, 0, 0, params, knight));
		knight.spells.add(new Spell(FIREBALL, (int) knight.mPosition.x, (int) knight.mPosition.y, 5, params, knight));
		knight.spells.add(new Spell(LIGHTSTORM, (int) knight.mPosition.x, (int) knight.mPosition.y, 8, params, knight));
	}

	protected void setFrameHeight(int i) {
		height = i / rows;

	}

	public void setFrameWidth(int i) {
		width = i / framecount;

	}

	public void destroy() {
		if (left != null) {
			left.recycle();
		}
		if (right != null) {
			right.recycle();
		}
		if (leftExplode != null) {
			leftExplode.recycle();
		}
		if (rightExplode != null) {
			rightExplode.recycle();
		}
		if (spritesheet != null) {
			if (!spritesheet.isRecycled()) {
				spritesheet.recycle();
			}
		}
	}

	/**
	 * updates the current state of the spell in question (lastSpellCasted from
	 * the knight)
	 * 
	 * @param knight
	 * @param groundspeed
	 * @param platspeed
	 */
	public void update(KnightSprite knight, int groundspeed, int platspeed) {

		final long time = System.currentTimeMillis();
		if (!knight.getZeal()) {
			if (time > knight.lastSpellTime + knight.spells.get(knight.lastSpellCasted).attackDelay
					&& knight.lastSpellTime > 0 && knight.lastSpellCasted == type) {
				if (isHit) {
					isHit = false;
				}
				setVisible(false);
				loop = true;
				if (isMoving()) {
					setMoving(false);
				}
				currentFrame = 0;
				spritesheet = right;
				setFrameWidth(spritesheet.getWidth());
				setFrameHeight(spritesheet.getHeight());
				knight.lastSpellTime = 0;
				mPosition.set(knight.mPosition.x, mPosition.y);
			} else {
				if (isVisible()) {
					if (knight.isMoving()) {
						if (knight.mPosition.y >= mParams.viewHeight - knight.mGv.groundTile2.getHeight() - 40) {
							mPosition.x = knight.facingDirection.x == 1
									? mPosition.x - (int) Math.floor(groundspeed * knight.jumpAngle)
									: mPosition.x + (int) Math.floor(groundspeed * knight.jumpAngle);
						} else {
							mPosition.x = knight.facingDirection.x == 1
									? mPosition.x - (int) Math.floor(platspeed * knight.jumpAngle)
									: mPosition.x + (int) Math.floor(platspeed * knight.jumpAngle);
						}
					}
					if (isMoving() && !isHit) {
						mPosition.x += getSpeedX() * facingDirection.x;
					}
					if (spritesheet != null) {
						frameToDraw = getCurrentFrame(knight);
					}
				} else {
					facingDirection.x = knight.facingDirection.x;
					if (facingDirection.x == 1) {
						mPosition.set(knight.mPosition.x + (knight.width / 2), knight.mPosition.y);
					} else {
						mPosition.set(knight.mPosition.x - (knight.width / 2), knight.mPosition.y);
					}
				}
			}

			whereToDraw.set((int) mPosition.x, (int) mPosition.y - height, (int) mPosition.x + width,
					(int) mPosition.y);
		}
	}

	private void testSheet() {
		if (isMoving() && !isHit) {
			spritesheet = facingDirection.x == -1 ? left : right;
			switch (type) {
			case FIREBALL:
				framecount = 5;
				break;
			case LIGHTSTORM:
				framecount = 8;
				break;
			}
			setFrameWidth(spritesheet.getWidth());
			setFrameHeight(spritesheet.getHeight());
		} else if (isHit && hasExplosionAnim) {
			spritesheet = facingDirection.x == -1 ? leftExplode : rightExplode;
			switch (type) {
			case FIREBALL:
				framecount = 6;
				break;
			}
			setFrameWidth(spritesheet.getWidth());
			setFrameHeight(spritesheet.getHeight());
		} else {

			spritesheet = facingDirection.x == -1 ? left : right;
		}
	}

	public void draw(Canvas canvas, Paint paint) {
		if (isVisible() && type != ZEAL) {
			canvas.drawBitmap(spritesheet, frameToDraw, whereToDraw, paint);
		}
	}

	public Rect getCurrentFrame(KnightSprite knight) {
		long time = System.currentTimeMillis();
		if (time > lastFrameChangeTime + 125) {

			// update current frame and lastFrameCahngeTime
			lastFrameChangeTime = time;
			currentFrame++;

			// if looping restart animation
			if (currentFrame >= framecount && loop) {
				currentFrame = 0;
			}

			// if looping is false stop at last frame
			if (currentFrame >= framecount && !loop) {
				currentFrame = framecount;
				setVisible(false);
			}
		}
		testSheet();
		frameToDraw.top = 0;
		frameToDraw.bottom = height;
		if (facingDirection.x == 1) {
			frameToDraw.left = currentFrame * width;
			frameToDraw.right = frameToDraw.left + width;
		} else {
			if (currentFrame >= 0) {
				frameToDraw.left = spritesheet.getWidth() - width * (currentFrame + 1);
			}
			frameToDraw.right = frameToDraw.left + width;
		}
		return frameToDraw;
	}

	public boolean checkCollisionsWithEnemy(Rect a, Rect b, EnemySprite enemy, KnightSprite knight) {

		if (Rect.intersects(a, b)) {
			// when the spell RECTangle is past the enemy Rectangle
			if ((facingDirection.x == 1 && b.right > a.right) || (b.left < a.left && facingDirection.x == -1)) {
				enemy.setCurrentHealth(-getSpellDamage(knight));
				enemy.handleDeath(knight);
				if (type == FIREBALL) {
					currentFrame = 0;
					loop = false;
					isHit = true;
					setMoving(false);
				} else {
					loop = true;
					isHit = true;
					setMoving(false);
				}

				return true;
			}
		}
		return false;
	}

	public int getSpellDamage(KnightSprite knight) {
		int damage = 0;
		switch (type) {
		case ZEAL:
			damage = (int) Math.floor((float) knight.getDamage() * (0.5F + ((float) knight.addedZeal / 100F)));
			break;
		case LIGHTSTORM:
			damage = (knight.mGv.cloudRandom.nextInt(max - min) + min);
			break;
		case FIREBALL:
			damage = (knight.mGv.cloudRandom.nextInt(max - min) + min);
			break;
		}
		return damage;
	}

	public boolean isMoving() {
		return isMoving;
	}

	public void setMoving(boolean isMoving) {
		this.isMoving = isMoving;
	}
}
