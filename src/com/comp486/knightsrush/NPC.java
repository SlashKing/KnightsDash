package com.comp486.knightsrush;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class NPC extends KnightSprite {
	
	/**
	 * 
	 * Add NPC types here
	 *
	 */
	public enum NPCType {
		Zephyr, Galadriel
	}

	NPCType npcType;

	public NPC(int x, int y, int framecount, Bitmap spritesheet, ContextParameters params, GameView gv, int npc) {
		super(x, y, 1, framecount, false, 0, spritesheet, params, gv, null, null, false, false);
		team = Team.NONE;
		frameLengthInMilliseconds = 180;
		setNPCbyId(npc);
		spritesheet = setBitmap(spritesheet);
	}

	/**
	 * Update position of NPC based on player movement
	 * 
	 * @param timedelta
	 * @param knight
	 */
	public void update(long timedelta, KnightSprite knight) {
		if (isVisible()) {
			int platSpeed = knight.getXVelocity() / 100;
			int groundSpeed = knight.getXVelocity() / 60;
			platSpeed = -(int) knight.facingDirection.x * platSpeed;
			groundSpeed = -(int) knight.facingDirection.x * groundSpeed;
			if (knight.isMoving()) {
				if (knight.getPosition().y <= mGv.mParams.viewHeight - mGv.groundTile2.getHeight()) {
					getPosition().set(getPosition().x + platSpeed, getPosition().y);
				} else {
					getPosition().set(getPosition().x + groundSpeed, getPosition().y);
				}
			}
			if (knight.isJumping()) {
				if (knight.getPosition().y >= mGv.mParams.viewHeight - mGv.groundTile2.getHeight()) {
					getPosition().set(getPosition().x + (int) (platSpeed * knight.jumpAngle), getPosition().y);
				} else {
					getPosition().set(getPosition().x + (int) (groundSpeed * knight.jumpAngle), getPosition().y);
				}
			}
			frameToDraw = getCurrentFrame();
		}
	}

	/**
	 * @description Draw method, if the NPC is visible it is drawn. No fancy
	 *              implementations as of yet
	 * @param timedelta
	 * @param canvas
	 * @param paint
	 */
	public void draw(long timedelta, Canvas canvas, Paint paint) {
		if (isVisible()) {
			whereToDraw.set(getPosition().x, getPosition().y - height, getPosition().x + width, getPosition().y);
			//canvas.drawBitmap(mGv.bmpNPC, frameToDraw, whereToDraw, paint);
		}
	}

	/**
	 * NPC type is taken from xml file based on the area
	 * 
	 * @param npc
	 */
	public void setNPCbyId(int npc) {
		switch (npc) {
		case 0:
			npcType = NPC.NPCType.Zephyr;
			break;
		}
	}

	@Override
	public void setAnimation(ActionType animation) {

		setCurrentAction(animation);
		final int frameHeight = getFrameHeight();
		int top;
		int bottom;
		switch (animation) {
		case IDLE:
			framecount = 8;
			top = 0;
			bottom = frameHeight;
			break;
		default:
			top = 0;
			bottom = frameHeight;
			break;
		}
		frameToDraw.top = top;
		frameToDraw.bottom = bottom;

		setAnimationLeftRight();
	}
}
