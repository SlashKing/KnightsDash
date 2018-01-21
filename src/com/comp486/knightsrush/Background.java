package com.comp486.knightsrush;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * @description Background class used to create all background objects
 */
public class Background extends GameObject {
	public Bitmap image;
	// position and speed variables
	protected int dx;
	protected int dy;
	protected int width;
	protected int height;
	protected int rows;
	protected int columns;

	// injected surface view
	protected ContextParameters mParams;
	protected Rect bounds = new Rect(0, 0, 0, 0);

	/**
	 * 
	 * @param params
	 *            context parameters
	 * @param res
	 *            - bitmap
	 * @param x
	 *            - position along x-axis
	 * @param y
	 *            - position along y-axis
	 */
	public Background(ContextParameters params, Bitmap res, int x, int y) {
		mPosition.set(x,y);
		mParams = params;
		rows = 1;
		image = res;
		width = image.getWidth();
		height = image.getHeight();
		columns = params.viewWidth / width;
	}

	public void update() {
		setBounds();
		mPosition.x += dx;
		// walking right - screen moves left
		if (mPosition.x < -mParams.viewWidth) {
			mPosition.x = 0;
		}
		// walking right - screen moves right
		if (mPosition.x > mParams.viewWidth) {
			mPosition.x = 0;
		}
	}

	public void update(int vector) {
		setBounds();
		// vector determines whether to move background left or right
		mPosition.x += vector;
		if (mPosition.x < -width) {
			mPosition.x = 0;
		}
		if (mPosition.x > width) {
			mPosition.x = 0;
		}
	}

	public void draw(Canvas canvas, Paint paint) {
		if (!image.isRecycled()) {
			canvas.drawBitmap(image, mPosition.x, mPosition.y, paint);
			// we want our bricks to span the whole screen
			// when moving right, draw bricks to the right of character as they
			// pan left
			if (mPosition.x < 0) {
				canvas.drawBitmap(image, mPosition.x + mParams.viewWidth, mPosition.y, paint);
			}
			// when moving left, draw brick to the left of character as they pan
			// right
			if (mPosition.x > 0) {
				canvas.drawBitmap(image, mPosition.x - mParams.viewWidth, mPosition.y, paint);
			}
		}
	}

	// sets speed of item along x-axis
	public void setVector(int dx) {
		this.dx = dx;
	}

	/**
	 * @description HACK *** Make rectangle wider by the width of the screen in
	 *              both direction to detect collisions. Sets and gets the
	 *              bounds based on the size of the bitmap
	 * @return Rect bounds
	 */
	public Rect setBounds() {
		bounds.set((int)mPosition.x - width, (int)mPosition.y, (int)mPosition.x + width * 2, (int)mPosition.y + height);
		return bounds;
	}

	public Rect getBounds() {
		return bounds;
	}
}
