package com.comp486.knightsrush;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LevelSelectActivity extends ListActivity {
	private final static int UNLOCK_ALL_LEVELS_ID = 0;
	private final static int UNLOCK_NEXT_LEVEL_ID = 1;
	private final static LevelDataComparator sLevelComparator = new LevelDataComparator();
	private ArrayList<LevelMetaData> mLevelData;
	private Animation mButtonFlickerAnimation;
	private boolean mLevelSelected;
	private int currentArea;

	private class LevelMetaData {
		public GameAct.AreaGroup act;
		public GameAct.GameArea area;
		public int x;
		public int y;
		boolean enabled;

		@Override
		public String toString() {
			return area.name;
		}
	}

	private class DisableItemArrayAdapter<T> extends ArrayAdapter<T> {
		private static final int TYPE_ENABLED = 0;
		private static final int TYPE_DISABLED = 1;
		private static final int TYPE_COMPLETED = 2;
		private static final int TYPE_COUNT = 3;

		private int mRowResource;
		private int mDisabledRowResource;
		private int mCompletedRowResource;
		private Context mContext;
		private int mTextViewResource;
		private int mTextViewResource2;

		public DisableItemArrayAdapter(Context context, int resource, int disabledResource, int completedResource,
				int textViewResourceId, int textViewResourceId2, List<T> objects) {
			super(context, resource, textViewResourceId, objects);
			mRowResource = resource;
			mDisabledRowResource = disabledResource;
			mCompletedRowResource = completedResource;
			mContext = context;
			mTextViewResource = textViewResourceId;
			mTextViewResource2 = textViewResourceId2;
		}

		@Override
		public boolean isEnabled(int position) {
			// TODO: do we have separators in this list?
			return mLevelData.get(position).enabled;
		}

		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}

		@Override
		public int getItemViewType(int position) {
			int type = TYPE_ENABLED;
			LevelMetaData level = mLevelData.get(position);
			if (level != null) {
				if (!level.area.completed) {
					type = TYPE_DISABLED;
				} else {
					type = TYPE_COMPLETED;
				}
			}
			return type;
		}

		@Override
		public int getViewTypeCount() {
			return TYPE_COUNT;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public boolean isEmpty() {
			return mLevelData.size() > 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View sourceView = null;
			if (mLevelData.get(position).enabled && !mLevelData.get(position).area.completed) {
				if (convertView != null && convertView.getId() == mRowResource) {
					sourceView = convertView;
				} else {
					sourceView = LayoutInflater.from(mContext).inflate(mRowResource, parent, false);
				}
			} else if (mLevelData.get(position).enabled && mLevelData.get(position).area.completed) {
				if (convertView != null && convertView.getId() == mCompletedRowResource) {
					sourceView = convertView;
				} else {
					sourceView = LayoutInflater.from(mContext).inflate(mCompletedRowResource, parent, false);
				}
			} else {
				if (convertView != null && convertView.getId() == mDisabledRowResource) {
					sourceView = convertView;
				} else {
					sourceView = LayoutInflater.from(mContext).inflate(mDisabledRowResource, parent, false);
				}
			}
			TextView view = (TextView) sourceView.findViewById(mTextViewResource);
			if (view != null) {
				view.setText(mLevelData.get(position).area.name);
			}

			// TextView view2 = (TextView)
			// sourceView.findViewById(mTextViewResource2);
			// if (view2 != null) {
			// view2.setText(mLevelData.get(position).area.);
			// }
			return sourceView;
		}

	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.level_select);
		mLevelData = new ArrayList<LevelMetaData>();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			getListView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			getListView().setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
		SharedPreferences prefs2 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences prefs = getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, Context.MODE_PRIVATE);
		generateLevelList(prefs.getInt(LevelPreferences.PREFERENCE_ACT, 0), prefs2);
		currentArea = prefs.getInt(LevelPreferences.PREFERENCE_AREA, 0);
		DisableItemArrayAdapter<LevelMetaData> adapter = new DisableItemArrayAdapter<LevelMetaData>(this,
				R.layout.level_select_row, R.layout.level_select_disabled_row, R.layout.level_select_completed_row,
				R.id.title, R.id.time, mLevelData);

		adapter.sort(sLevelComparator);

		setListAdapter(adapter);

		mButtonFlickerAnimation = AnimationUtils.loadAnimation(this, R.anim.button_flicker);

		mLevelSelected = false;

		// Keep the volume control type consistent across all activities.
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}

	protected void generateLevelList(int actId, SharedPreferences prefs) {
		final int count = GameAct.levels.size();
		boolean oneBranchUnlocked = false;

		boolean anyUnlocksThisBranch = false;
		final GameAct.AreaGroup group = GameAct.levels.get(actId);
		int nextId = -1;
		for (int y = 0; y < group.levels.size(); y++) {
			GameAct.GameArea level = group.levels.get(y);
			boolean enabled = false;
			Log.d("unlock levels", String.valueOf(prefs.getBoolean("pref_unlock", false)));
			if (prefs.getBoolean("pref_unlock", false)) {
				enabled = true;
			} else {
				if (level.mId == 0 && !level.completed) {
					enabled = true;
					anyUnlocksThisBranch = true;
				} else if (level.mId == 0 && level.completed) {
					enabled = true;
					nextId = level.mId;
					anyUnlocksThisBranch = true;
				}
				if (level.completed) {
					enabled = true;
					nextId = level.mId;
				} else if (level.mId == nextId + 1 && !enabled) {
					enabled = true;
				}
			}
			addItem(level, actId, y, enabled);

		}
		if (anyUnlocksThisBranch) {
			oneBranchUnlocked = true;
		}

	}

	protected void unlockNext() {
		final int count = GameAct.levels.size();
		for (int x = 0; x < count; x++) {
			final GameAct.AreaGroup group = GameAct.levels.get(x);
			for (int y = 0; y < group.levels.size(); y++) {
				GameAct.GameArea level = group.levels.get(y);
				if (!level.completed) {
					// level.completed = true;
					return;
				}
			}

		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (!mLevelSelected) {
			super.onListItemClick(l, v, position, id);
			LevelMetaData selectedLevel = mLevelData.get(position);
			if (selectedLevel.enabled) {
				mLevelSelected = true;
				final int diff = getIntent().getIntExtra("difficulty", 1);
				Intent intent = new Intent(this, MainActivity.class);

				intent.putExtra(LevelPreferences.PREFERENCE_DIFFICULTY_COMPLETED,
						getIntent().getIntExtra(LevelPreferences.PREFERENCE_DIFFICULTY_COMPLETED, 0));
				intent.putExtra("difficulty", diff);
				intent.putExtra(LevelPreferences.PREFERENCE_AREA, position);
				boolean doWeResetDistance = position != currentArea ? true : false;
				intent.putExtra("resetDistance", doWeResetDistance);
				intent.putExtra("takeArea", true);
				intent.putExtra(LevelPreferences.PREFERENCE_ACT, 0);
				TextView text = (TextView) v.findViewById(R.id.title);
				if (text != null) {
					text.startAnimation(mButtonFlickerAnimation);
					mButtonFlickerAnimation.setAnimationListener(new EndActivityAfterAnimation(intent));
				} else {
					setResult(RESULT_OK, intent);
					finish();
					if (UIConstants.mOverridePendingTransition != null) {
						try {
							UIConstants.mOverridePendingTransition.invoke(LevelSelectActivity.class,
									R.anim.activity_fade_in, R.anim.activity_fade_out);
						} catch (InvocationTargetException ite) {
							Log.d("Activity Transition", "Invocation Target Exception");
						} catch (IllegalAccessException ie) {
							Log.d("Activity Transition", "Illegal Access Exception");
						}
					}
				}
			}
		}
	}

	private void addItem(GameAct.GameArea area, int x, int y, boolean enabled) {
		LevelMetaData data = new LevelMetaData();
		data.area = area;
		data.x = x;
		data.y = y;
		data.enabled = enabled;
		mLevelData.add(data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		return false;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case UNLOCK_NEXT_LEVEL_ID:
			unlockNext();
			mLevelData.clear();
			// generateLevelList(false);
			((ArrayAdapter) getListAdapter()).sort(sLevelComparator);
			((ArrayAdapter) getListAdapter()).notifyDataSetChanged();

			return true;

		case UNLOCK_ALL_LEVELS_ID:
			// Regenerate the level list to remove the past-only filter.
			mLevelData.clear();
			// generateLevelList(false);
			for (LevelMetaData level : mLevelData) {
				level.enabled = true;
			}

			((ArrayAdapter) getListAdapter()).sort(sLevelComparator);
			((ArrayAdapter) getListAdapter()).notifyDataSetChanged();
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean result = false;
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			result = true;
		}
		return result;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean result = false;
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			result = true;
			finish();
		}
		return result;
	}

	/** Comparator for level meta data. */
	private final static class LevelDataComparator implements Comparator<LevelMetaData> {
		public int compare(final LevelMetaData object1, final LevelMetaData object2) {
			int result = 0;
			if (object1 == null && object2 != null) {
				result = 1;
			} else if (object1 != null && object2 == null) {
				result = -1;
			} else if (object1 != null && object2 != null) {
				// result =
				// object1.area.timeStamp.compareTo(object2.level.timeStamp);
			}
			return result;
		}
	}

	protected class EndActivityAfterAnimation implements Animation.AnimationListener {
		private Intent mIntent;

		EndActivityAfterAnimation(Intent intent) {
			mIntent = intent;
		}

		public void onAnimationEnd(Animation animation) {
			setResult(RESULT_OK, mIntent);
			finish();
			startActivity(mIntent);
		}

		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub

		}

		public void onAnimationStart(Animation animation) {
			// TODO Auto-generated method stub

		}

	}
}
