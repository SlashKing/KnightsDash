package com.comp486.knightsrush;

import com.comp486.knightsrush.GameView;

import android.graphics.Canvas;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 * @author Nick
 *
 */
public class MainThread extends Thread {
	private long mLastTime;
	private SurfaceHolder surfaceHolder;
	private GameView gameView;
	private boolean running;
	public static Canvas canvas;
	private int mProfileFrames;
	private long mProfileTime;

	private static final float PROFILE_REPORT_DELAY = 3.0f;

	public MainThread(SurfaceHolder surfaceHolder, GameView gameView) {
		super();
		this.surfaceHolder = surfaceHolder;
		this.gameView = gameView;
	}

	@Override
	public void run() {
		while (running) {
			try {
				final long time = SystemClock.uptimeMillis();
				final long timeDelta = time - mLastTime;
				long finalDelta = timeDelta;
				if (timeDelta > 12) {
					float secondsDelta = (time - mLastTime) * 0.001f;
					if (secondsDelta > 0.1f) {
						secondsDelta = 0.1f;
					}
					mLastTime = time;
					if (surfaceHolder.getSurface().isValid()) {
						// try locking the canvas for pixel editing
						canvas = surfaceHolder.lockCanvas();
						try {
							synchronized (surfaceHolder) {
								this.gameView.update(timeDelta);
								this.gameView.draw(canvas, timeDelta);
							}
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							if (canvas != null) {
								surfaceHolder.unlockCanvasAndPost(canvas);
							}
						}
					}
					final long endTime = SystemClock.uptimeMillis();

					finalDelta = endTime - time;

					mProfileTime += finalDelta;
					mProfileFrames++;
					if (mProfileTime > PROFILE_REPORT_DELAY * 1000) {
						final long averageFrameTime = mProfileTime / mProfileFrames;
	//					Log.d("Game Profile", "Average: " + averageFrameTime);
						mProfileTime = 0;
						mProfileFrames = 0;
					}
				}
				// If the game logic completed in less than 16ms, that means
				// it's
				// running faster than 60fps, which is our target frame rate.
				// In that case we should
				// yield to the rendering thread, at least for the remaining
				// frame.

				if (finalDelta < 16) {
					try {
						Thread.sleep(16 - finalDelta);
					} catch (InterruptedException e) {
						// Interruptions here are no big deal.
					}
				}

			} catch (OutOfMemoryError E) {
				gameView.recycleEnemyBmps();
				gameView.enemies.clear();
				//gameView.gameThread = null;
				//gameView.gameThread = new MainThread(surfaceHolder,gameView);
				Log.d("OOM", "Damn out of memory");
			
			}
		}
	}

	public void setRunning(boolean b) {
		running = b;
	}

	public boolean isRunning() {

		return running;
	}
}