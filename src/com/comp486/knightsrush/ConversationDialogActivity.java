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

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.comp486.knightsrush.ConversationUtils.Conversation;
import com.comp486.knightsrush.ConversationUtils.ConversationPage;

/**
 * @description Modifications to original made by Nick LeBlanc for COMP486
 *              assignment Inspired by Replica Island open source Android Game
 *              Project by Chris Pruett
 */
public class ConversationDialogActivity extends Activity {

	private final static float TEXT_CHARACTER_DELAY = 0.1f;
	private final static int TEXT_CHARACTER_DELAY_MS = (int) (TEXT_CHARACTER_DELAY * 1000);
	private ConversationUtils.Conversation mConversation;
	private ArrayList<ConversationUtils.ConversationPage> mPages;
	private int mCurrentPage;

	private ImageView mOkArrow;
	private AnimationDrawable mOkAnimation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.conversation_dialog);

		mOkArrow = (ImageView) findViewById(R.id.ok);
		mOkArrow.setBackgroundResource(R.anim.ui_button);
		mOkAnimation = (AnimationDrawable) mOkArrow.getBackground();
		mOkArrow.setVisibility(View.INVISIBLE);

		// intent has act, area, and quest id to find conversation pages
		final Intent callingIntent = getIntent();
		final int act = callingIntent.getIntExtra("actId", -1);
		final int area = callingIntent.getIntExtra("areaId", -1);
		final int quest = callingIntent.getIntExtra("questId", -1);
		final int boss = callingIntent.getIntExtra("bossId", -1);
		int index = callingIntent.getIntExtra("index", -1);
		final int character = callingIntent.getIntExtra("character", 1);

		mPages = null;
		TypewriterTextView tv = (TypewriterTextView) findViewById(R.id.typewritertext);

		tv.setParentActivity(this);

		// Hide back stop and resume buttons
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			tv.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			tv.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
		}

		if (act != -1 && area != -1 && quest != -1 && index != -1) {
			if (character == 1) {
				mConversation = GameAct.get(act, area).quests.get(quest).dialogResources.npcConvo.get(index);
			} else {
				mConversation = GameAct.get(act, area).quests.get(quest).dialogResources.playerConvo.get(index);
			}
		} else if (act != -1 && area != -1 && quest != -1 && index == -1) {
			if (character == 1) {
				mConversation = GameAct.get(act, area).quests.get(quest).dialogResources.npcConvo.get(0);
			} else {
				mConversation = GameAct.get(act, area).quests.get(quest).dialogResources.playerConvo.get(0);
			}
		} else if (quest == -1) {
			mConversation = GameAct.get(act, area).dialogResources.npcConvo.get(index = index > 0 ? index : 0);
		} else if (boss != -1 && quest == -1) {
			mConversation = GameAct.get(act, area).dialogResources.npcConvo.get(index);
		} else {

			// bail
			finish();
		}
	}

	/**
	 * @description Written by Chris Pruett. Taken from Android Source Open
	 *              Source Project -Replica Island
	 * @param conversation
	 * @param textView
	 */
	private void formatPages(Conversation conversation, TextView textView) {
		Paint paint = new Paint();
		final int maxWidth = textView.getWidth();
		final int maxHeight = textView.getHeight();
		paint.setTextSize(textView.getTextSize());
		paint.setTypeface(textView.getTypeface());

		for (int page = conversation.pages.size() - 1; page >= 0; page--) {
			ConversationPage currentPage = conversation.pages.get(page);
			CharSequence text = currentPage.text;
			// Iterate line by line through the text. Add \n if it gets too
			// wide,
			// and split into a new page if it gets too long.
			int currentOffset = 0;
			int textLength = text.length();
			SpannableStringBuilder spannedText = new SpannableStringBuilder(text);
			int lineCount = 0;
			final float fontHeight = -paint.ascent() + paint.descent();
			final int maxLinesPerPage = (int) (maxHeight / fontHeight);
			CharSequence newline = "\n";
			int addedPages = 0;
			int lastPageStart = 0;
			do {
				int fittingChars = paint.breakText(text, currentOffset, textLength, true, maxWidth, null);

				if (currentOffset + fittingChars < textLength) {
					fittingChars -= 2;
					// Text doesn't fit on the line. Insert a return after the
					// last space.
					int lastSpace = TextUtils.lastIndexOf(text, ' ', currentOffset + fittingChars - 1);
					if (lastSpace == -1) {
						// No spaces, just split at the last character.
						lastSpace = currentOffset + fittingChars - 1;
					}
					spannedText.replace(lastSpace, lastSpace + 1, newline, 0, 1);
					lineCount++;
					currentOffset = lastSpace + 1;
				} else {
					lineCount++;
					currentOffset = textLength;
				}

				if (lineCount >= maxLinesPerPage || currentOffset >= textLength) {
					lineCount = 0;
					if (addedPages == 0) {
						// overwrite the original page
						currentPage.text = spannedText.subSequence(lastPageStart, currentOffset);
					} else {
						// split into a new page
						ConversationUtils.ConversationPage newPage = new ConversationUtils.ConversationPage();
						newPage.imageResource = currentPage.imageResource;
						newPage.text = spannedText.subSequence(lastPageStart, currentOffset);
						newPage.title = currentPage.title;
						conversation.pages.add(page + addedPages, newPage);
					}
					lastPageStart = currentOffset;
					addedPages++;
				}
			} while (currentOffset < textLength);

		}

		// We did a lot of allocation there.
		Runtime.getRuntime().gc();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			TypewriterTextView tv = (TypewriterTextView) findViewById(R.id.typewritertext);

			if (tv.getRemainingTime() > 0) {
				tv.snapToEnd();
			} else {
				mCurrentPage++;
				// if more pages to show, get them, otherwise fire up the most
				// recent difficulty
				if (mCurrentPage < mPages.size()) {
					showPage(mPages.get(mCurrentPage));
				} else {
					finish();
					Intent i = new Intent(this, MainActivity.class);
					SharedPreferences prefs = getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
					i.putExtra(LevelPreferences.PREFERENCE_DIFFICULTY,
							prefs.getInt(LevelPreferences.PREFERENCE_DIFFICULTY, DifficultyConstants.NORMAL));
					i.putExtra(LevelPreferences.PREFERENCE_DIFFICULTY_COMPLETED,
							prefs.getInt(LevelPreferences.PREFERENCE_DIFFICULTY_COMPLETED, 0));
					startActivity(i);
				}
			}
		}
		// Sleep so that the main thread doesn't get flooded with UI events.
		try {
			Thread.sleep(32);
		} catch (InterruptedException e) {
			// No big deal if this sleep is interrupted.
		}
		return true;
	}

	protected void showPage(ConversationUtils.ConversationPage page) {
		TypewriterTextView tv = (TypewriterTextView) findViewById(R.id.typewritertext);
		tv.setTypewriterText(page.text);

		mOkArrow.setVisibility(View.INVISIBLE);
		mOkAnimation.start();

		tv.setOkArrow(mOkArrow);

		ImageView image = (ImageView) findViewById(R.id.speaker);
		if (page.imageResource != 0) {
			image.setImageResource(page.imageResource);
			image.setVisibility(View.VISIBLE);
		} else {
			image.setVisibility(View.GONE);
		}

		TextView title = (TextView) findViewById(R.id.speakername);
		if (page.title != null) {
			title.setText(page.title);
			title.setVisibility(View.VISIBLE);
		} else {
			title.setVisibility(View.GONE);
		}

	}

	public void processText() {
		if (!mConversation.splittingComplete) {
			TextView textView = (TextView) findViewById(R.id.typewritertext);
			formatPages(mConversation, textView);
			mConversation.splittingComplete = true;
		}

		if (mPages == null) {
			mPages = mConversation.pages;
			showPage(mPages.get(0));

			mCurrentPage = 0;
		}
	}

	public static class TypewriterTextView extends TextView {
		private int mCurrentCharacter;
		private long mLastTime;
		private CharSequence mText;
		private View mOkArrow;
		private ConversationDialogActivity mParentActivity;

		public TypewriterTextView(Context context) {
			super(context);
		}

		public TypewriterTextView(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		public TypewriterTextView(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
		}

		public void setParentActivity(ConversationDialogActivity parent) {
			mParentActivity = parent;
		}

		public void setTypewriterText(CharSequence text) {
			mText = text;
			mCurrentCharacter = 0;
			mLastTime = 0;
			postInvalidate();
		}

		public long getRemainingTime() {
			return (mText.length() - mCurrentCharacter) * TEXT_CHARACTER_DELAY_MS;
		}

		public void snapToEnd() {
			mCurrentCharacter = mText.length() - 1;
		}

		public void setOkArrow(View arrow) {
			mOkArrow = arrow;
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			// We need to wait until layout has occurred before we can setup the
			// text page. Ugh. Bidirectional dependency!
			if (mParentActivity != null) {
				mParentActivity.processText();
			}
			super.onSizeChanged(w, h, oldw, oldh);
		}

		@Override
		public void onDraw(Canvas canvas) {
			final long time = SystemClock.uptimeMillis();
			final long delta = time - mLastTime;
			if (delta > TEXT_CHARACTER_DELAY_MS) {
				if (mText != null) {
					if (mCurrentCharacter <= mText.length()) {
						CharSequence subtext = mText.subSequence(0, mCurrentCharacter);
						setText(subtext, TextView.BufferType.SPANNABLE);
						mCurrentCharacter++;
						postInvalidateDelayed(TEXT_CHARACTER_DELAY_MS);
					} else {
						if (mOkArrow != null) {
							mOkArrow.setVisibility(View.VISIBLE);
						}
					}
				}
			}
			super.onDraw(canvas);
		}
	}

}
