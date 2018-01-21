package com.comp486.knightsrush;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * @author Nick Custom Sprite class used for GameObject that requires animation
 *         Extending GameObject x, y : Poaition on screen rows : number of
 *         animation on current spritesheet framecount : number of frames per
 *         animation vis : visible? spritesheet: current spritesheet to use
 *         params : commonly used parameters
 */
public class Sprite extends GameObject {
	protected int x;
	protected int y;
	protected int width;
	protected int height;
	private int speedX = 0;
	private int speedY = 0;
	protected int framecount;
	protected int rows;
	private boolean vis;
	protected Bitmap spritesheet = null;
	protected ContextParameters mParams;

	public Sprite(int x, int y, int rows, int framecount, boolean vis, Bitmap spritesheet, ContextParameters params) {
		getPosition().set(x,y);
		this.x = x;
		this.y = y;
		this.rows = rows;
		this.framecount = framecount;
		if (spritesheet != null) {
			this.width = spritesheet.getWidth();
			this.height = spritesheet.getHeight();
		}
		this.vis = vis;
		this.spritesheet = spritesheet;
		mParams = params;
	}

	public Sprite() {

	}

	public void reset() {
		x = 0;
		y = -150;
		rows = 1;
		framecount = 1;
		width = 0;
		height = 0;
		speedX = 0;
		vis = false;

	}

	public void onDraw(Canvas canvas) {

	}

	public int getSpeedX() {
		return speedX;
	}

	public void setSpeedX(int x) {
		this.speedX = x;
	}

	public int getSpeedY() {
		return speedY;
	}

	public void setSpeedY(int y) {
		this.speedY = y;
	}

	public Bitmap getSprites() {
		return this.spritesheet;
	}

	public Bitmap setBitmap(Bitmap bmp) {
		spritesheet = bmp;
		return bmp;
	}

	public int getFrameWidth() {
		return this.width / framecount;
	}

	public int getFrameHeight() {
		return this.height / rows;
	}

	public int getBitmapHeight() {
		return this.spritesheet.getHeight();
	}

	public int getBitmapWidth() {
		return this.spritesheet.getWidth();
	}

	public boolean isVisible() {
		return this.vis;
	}

	public void setVisible(Boolean visible) {
		this.vis = visible;
	}
}
