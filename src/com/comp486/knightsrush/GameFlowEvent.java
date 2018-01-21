/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.comp486.knightsrush;

import android.content.Context;
import android.util.Log;

/**
 * @author Nick GameFlowEvent manages the gameFlowThread from gameView
 */
public class GameFlowEvent implements Runnable {

	// constants to determine what type of event
	public static final int EVENT_INVALID = -1;
	public static final int EVENT_RESTART_LEVEL = 0;
	public static final int EVENT_END_GAME = 1;
	public static final int EVENT_BEAT_QUEST = 2;
	public static final int EVENT_BEAT_AREA = 3;
	public static final int EVENT_BEAT_ACT = 4;
	public static final int EVENT_LEVELLED = 5;
	public static final int EVENT_START_AREA_BOSS = 6;
	public static final int EVENT_FAILED_QUEST = 7;
	public static final int EVENT_BEAT_QUEST_UI = 8;

	// dialog events
	public static final int EVENT_SHOW_DIALOG_NPC = 9;
	public static final int EVENT_SHOW_DIALOG_PLAYER = 10;
	public static final int EVENT_GET_NEXT_AREA = 11;
	public static final int EVENT_PAUSED= 12;
	public static final int EVENT_RESUMED= 13;
	public static final int EVENT_SKILLSTAT_TREE= 14;

	// event code
	private int mEventCode;

	// index of object
	private int mDataIndex;

	// time to run thread operation
	// used to display UI dialog (Quest Completed, etc.)
	private long mTimeToRun = 0;
	private long startTime = 0;

	// is the thread running boolean
	private volatile boolean mRunning;

	// current activity
	private MainActivity mMainActivity;

	// thread
	private Thread mThread;

	/**
	 * This method starts the onGameFlowEvent thread
	 * 
	 * @param event
	 * @param index
	 * @param context
	 * @param thread
	 * @param timeToRun
	 **/
	public void post(int event, int index, Context context, Thread t, int timeToRun) {
		if (context instanceof MainActivity) {
			Log.d("GameFlowEvent", "Post Game Flow Event: " + event + ", " + index);
			mEventCode = event;
			mDataIndex = index;
			mMainActivity = (MainActivity) context;
			mTimeToRun = timeToRun;
			mRunning = true;
			Log.d("STATE BEFORE: ", t.getState().toString());
			// if thread hasn't been started yet, start
			if (!t.isAlive()) {
				if (t.getState() == Thread.State.NEW) {
					t.start();

				} else if (t.getState() == Thread.State.TERMINATED) {
					// Thread terminates after starting three times for some
					// reason so when the thread is terminated we join the
					// current thread assign a new one, update the gameView's
					// flowThread and start it
					try {
						t.join();

					} catch (InterruptedException ie) {
						Log.e("InterruptExc: GameFlowEvent(post()", "Interrupted Exception");
					} finally {
						t = new Thread(this);
						mMainActivity.gameView.flowThread = t;
						t.start();
						Log.d("STATE: ", t.getState().toString());
					}

				}
			}

			mThread = t;
		}

	}

	/*
	 * This method runs on the UI thread
	 */

	/**
	 * @param event
	 * @param index
	 * @param context
	 */
	public void postImmediate(int event, int index, Context context) {
		if (context instanceof MainActivity) {
			Log.d("GameFlowEvent", "Execute Immediate Game Flow Event: " + event + ", " + index);
			mEventCode = event;
			mDataIndex = index;
			mMainActivity = (MainActivity) context;
			mMainActivity.runOnUiThread(this);
			mMainActivity.gameView.onGameFlowEvent(mEventCode, mDataIndex);
		}
	}

	public void run() {
		while (mRunning) {
			// if time to run has been set
			if (mTimeToRun > 0) {
				long time = System.currentTimeMillis();
				if (startTime == 0) {
					startTime = time;
				}
				// determine whether the time has elapsed
				if (time - startTime > mTimeToRun) {
					// reset quest completed boolean on game view
					mMainActivity.gameView.setQuestCompleted(false);

					// reset for reuse
					mMainActivity = null;
					startTime = 0;
					mTimeToRun = 0;
					mRunning = false;
				}
			} else {
				// if timeToRun is not set run onGameFlowEvent from gameView
				if (mMainActivity != null) {
					Log.d("GameFlowEvent", "Execute Game Flow Event: " + mEventCode + ", " + mDataIndex);
					mMainActivity.gameView.onGameFlowEvent(mEventCode, mDataIndex);
					Log.d("GameFlowEvent", "Game Flow Event: " + mEventCode + ", " + mDataIndex + " : Executed");
				}
				mMainActivity = null;
				Log.d("GameFlowEvent", "Game Flow Event: " + mEventCode + ", " + mDataIndex + " : Finished");
				mRunning = false;
			}
		}
	}

	public boolean getRunning() {
		return mRunning;
	}

	public void setRunning(boolean running) {
		mRunning = running;
	}
}
