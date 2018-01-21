package com.comp486.knightsrush;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Random;

import com.comp486.knightsrush.EnemySprite.MonsterType;
import com.comp486.knightsrush.GameAct.GameArea;
import com.comp486.knightsrush.GameAct.Quest;
import com.comp486.knightsrush.GameAct.Quest.Reward;
import com.comp486.knightsrush.dummy.DaoUtils;
import com.comp486.knightsrush.dummy.ItemBonus;
import com.comp486.knightsrush.dummy.Items;
import com.comp486.knightsrush.dummy.Items.Item;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class GameView extends SurfaceView
		implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, SurfaceHolder.Callback {

	// This is our two threads
	// one for gameFlowEvent's (ie. finish quests)
	Thread flowThread = null;
	// other for main game logic
	MainThread gameThread = null;

	MainActivity main = ((MainActivity) getContext());
	private GestureDetectorCompat mGestureDetector;

	// We need a SurfaceHolder
	// When we use Paint and Canvas in a thread
	// We will see it in action in the draw method soon.
	SurfaceHolder ourHolder;

	// A boolean which we will set and unset
	// when the game is running- or not.
	volatile boolean playing;

	// A Canvas and a Paint object
	Canvas canvas;
	// added second paint object to lower opacity of player when hit
	Paint paint, paintOpac;

	// Declare knight and other display items
	// Future versions will require a more robust and dynamic way to
	// set and access these bitmaps based on the currently active level
	// for this assignment, just needed something that works and provides access
	// to the bitmaps from the KnightSprite and EnemySprite classes
	public Bitmap groundTile2, groundTile, background;
	public Bitmap bmpEnemy, bmpEnemyLeft, enemyDeath, enemyDeathLeft, walk, walkL, idle, idleL, jump, jumpL, melee,
			meleeL;
	protected Bitmap healthBar, addButton, pause, gameOver, questComplete, respawn, emptyInventory, zealButton, tome,
			fireButton, lightButton, spells, inv;
	protected Sprite zeal = new Sprite(), fire = new Sprite(), light = new Sprite();
	float knightYPosition;
	float knightXPosition;

	// jump state, used to track which direction the user wishes to jump
	int jumpState = 1;

	private long lastClickTime;
	public int distanceWalked;
	public int currentKillCount, totalKills, actId, areaId, questId = -1, difficulty;

	// A rect that defines an area of the screen
	// on which to draw
	RectF whereToDraw = new RectF(0, 0, 0, 0);
	RectF whereToDraw2 = new RectF(0, 0, 0, 0);

	// the player
	private KnightSprite knightSprite;

	// enemies
	protected ArrayList<EnemySprite> enemies = new ArrayList<EnemySprite>();

	// potions
	ArrayList<Potion> potions = new ArrayList<Potion>();

	// ground and platform
	public Background plat, bg, back;

	// Random object used throughout the application
	public Random cloudRandom = new Random();

	// current experience
	public int currentExperience = 0;

	// Shared Preferences and editor to save game progress
	protected SharedPreferences prefs;
	protected SharedPreferences.Editor editor;

	//settings preference manager

	SharedPreferences pref = PreferenceManager
			.getDefaultSharedPreferences(this.getContext().getApplicationContext());
	
	// current area
	GameArea currentArea;

	// completed quest in area
	ArrayList<Quest> completedQuestsArea = new ArrayList<Quest>();

	// current active quest
	GameAct.Quest currentQuest = null;

	//
	GameFlowEvent mGameFlowEvent = new GameFlowEvent();
	public ContextParameters mParams;

	public float mTotalGameTime, mTotalRealTime;
	public Bitmap potionSm, potionLg;
	protected NPC currentNPC;
	private boolean questStarted, questCompleted, difficultyCompleted, pastAreaLimit;
	private ArrayList<Reward> rewardsToAssign = new ArrayList<Reward>();
	private ArrayList<Item> equipped = new ArrayList<Item>();
	protected Item currentWeapon = null;
	protected Item currentHelmet = null;
	protected Item currentShield = null;
	protected int currentSkillInt = 0;
	protected Bitmap weaponBitmap, helmetBitmap, shieldBitmap;
	protected Sprite weapon, helmet, shield;

	// Inventory
	InventoryMenu inventory;

	// Stats Screen
	StatsScreen stats;

	// Current item display view
	View currentItem = null;
	Item thisItem = null;
	// dropped items
	public ArrayList<Item> droppedItems = new ArrayList<Item>();
	protected boolean inventoryScreen = false;

	// boss can be null
	Boss boss = null;
	protected boolean statsScreen = false, skillsScreen = false;
	protected Bitmap quest_active;

	// When the we initialize (call new()) on gameView
	// This special constructor method runs
	public GameView(Context context, ContextParameters params) {
		// The next line of code asks the
		// SurfaceView class to set up our object.
		// How kind.
		super(context);
		// use this variable for monster kill quest
		prefs = context.getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, 0);
		currentKillCount = 0;

		this.mGestureDetector = new GestureDetectorCompat(context, this);
		mGestureDetector.setOnDoubleTapListener(this);
		mGestureDetector.setIsLongpressEnabled(true);

		// Initialize ourHolder and paint objects
		ourHolder = getHolder();
		paint = new Paint();
		paintOpac = new Paint();

		// required for SurfaceView
		ourHolder.addCallback(this);

		// set mParams
		mParams = params;
		// operate GameFlowEvent's on separate thread as they are blocking tasks
		flowThread = new Thread(mGameFlowEvent);

		emptyInventory = BitmapFactory.decodeResource(getResources(), R.drawable.empty);
		// if user desired a new game upon launch
		if (mParams.intent.getBooleanExtra("newGame", false) == true) {
			currentArea = GameAct.get(0, 0);
			newGame();
			if (GameAct.isLoaded()) {
				if (currentArea.showDialog) {
					currentArea.showDialog = false;
					if (editor == null) {
						editor = prefs.edit();
					}
					editor.putInt("A_" + actId + "_A" + areaId + "_dialog_" + mParams.difficulty, 1);
					editor.commit();
					editor.apply();
					onGameFlowEvent(GameFlowEvent.EVENT_SHOW_DIALOG_NPC, 0);

				}
			}
		} else {
			if (GameAct.isLoaded()) {
				// boolean is true if the difficulty completed in the saved
				// parameters
				// is greater or equal to the current difficulty
				difficultyCompleted = mParams.difficulty <= mParams.difficultyCompleted ? true : false;

				// Using shared preferences get and set current area, as well as
				// act
				// area and quest id
				if (mParams.intent.getBooleanExtra("takeArea", false) == false) {
					mParams.area = prefs.getInt(LevelPreferences.PREFERENCE_AREA, 0);
				}else{
					mParams.area = mParams.intent.getIntExtra(LevelPreferences.PREFERENCE_AREA, -1);
				}
				currentArea = GameAct.get(mParams.act, mParams.area);
				actId = prefs.getInt(LevelPreferences.PREFERENCE_ACT, 0);
				areaId = mParams.area;
				if (mParams.area == mParams.intent.getIntExtra(LevelPreferences.PREFERENCE_AREA, -1)) {
					questId = prefs.getInt(
							LevelPreferences.PREFERENCE_QUEST + "_" + actId + "_" + areaId + "_" + mParams.difficulty,
							-1);
				}
				if (currentArea.showDialog) {
					currentArea.showDialog = false;
					if (editor == null) {
						editor = prefs.edit();
					}
					editor.putInt("A_" + actId + "_A" + areaId + "_dialog_" + mParams.difficulty, 1);
					editor.commit();
					editor.apply();
					onGameFlowEvent(GameFlowEvent.EVENT_SHOW_DIALOG_NPC, 0);
				} else {
					// Inventory
					inventory = new InventoryMenu(this.getContext(), mParams, emptyInventory);
					equipKnight();
					// set character inventory item bitmaps and position for
					// pause
					// screen
					if (currentWeapon != null) {
						currentWeapon.spritesheet = weaponBitmap;
						currentWeapon.mPosition.set(mParams.viewWidth - weaponBitmap.getWidth(),
								mParams.viewHeight / 2 - weaponBitmap.getHeight() / 2);
					}
					if (currentShield != null) {
						currentShield.spritesheet = shieldBitmap;
						currentShield.mPosition.set(0, mParams.viewHeight / 2 - shieldBitmap.getHeight() / 2);
					}
					if (currentHelmet != null) {
						currentHelmet.spritesheet = helmetBitmap;
						currentHelmet.mPosition.set(mParams.viewWidth / 2 - helmetBitmap.getWidth() / 2, 80);
					}
					// if the dialog for the start of the area has already been
					// shown,
					// don't
					// bother showing it

					// let's loop through all acts to find out which rewards we
					// have
					// then apply the rewards to the player
					for (int z = 0; z < 3; z++) { // 3 difficulties
						for (int i = 0; i < GameAct.levels.size(); i++) { // acts
							for (int j = 0; j < GameAct.get(i).levels.size(); j++) { // areas
								for (GameAct.Quest quest : GameAct.get(i, j).quests) { // quests
									if (prefs.getInt("A" + i + "_A" + j + "_Q" + quest.id + "_" + z, 0) == 1) {
										rewardsToAssign.add(quest.reward);
									}
								}
							}
						}
					}
					//
					totalKills = prefs.getInt(PreferenceConstants.PREFERENCE_TOTAL_KILLS, 0);
					currentExperience = prefs.getInt(PreferenceConstants.PREFERENCE_EXP, 0);
					if (mParams.intent.getBooleanExtra("resetDistance", false)) {
						distanceWalked = 0;
						currentArea.startBossDialog = true;
						currentArea.endBossDialog = true;
					} else {
						distanceWalked = prefs.getInt(PreferenceConstants.PREFERENCE_DISTANCEWALKED, 0);
					} // distanceWalked = 99500;
					int complete;
					for (int i = 0; i < currentArea.quests.size(); i++) {
						complete = prefs.getInt("A" + actId + "_A" + areaId + "_Q" + i + "_" + mParams.difficulty, 0);
						Quest quest = GameAct.get(actId, areaId, i);
						quest.completed = complete == 1 ? true : false;
						if (complete == 1) {
							completedQuestsArea.add(quest);
						}
					}

					// set innitial knight x and y position
					knightXPosition = mParams.viewWidth / 2;
					knightYPosition = mParams.viewHeight - 40;

					getResources(getResources());

					mTotalGameTime = prefs.getFloat("totalGameTime", 0.0f);
					currentQuest = questId > -1 ? currentArea.quests.get(questId) : null;

					createBackground();
					createKnight();

					// Stats
					stats = new StatsScreen();
					if (currentArea.bossId == -1
							|| (currentArea.bossId != -1 && currentArea.startBossDialog && currentArea.mId != 2)) {

						// set number of enemies based on difficulty
						switch (mParams.difficulty) {
						case DifficultyConstants.NORMAL:
							createEnemies(8);
							break;
						case DifficultyConstants.NIGHTMARE:
							createEnemies(8);
							break;
						case DifficultyConstants.HELL:
							createEnemies(8);
							break;
						default:
							createEnemies(10);
							break;
						}
					}

					// Placeholder - updated dynamically based on distance
					// walked,
					// and
					// quest
					// currentNPC = new NPC(mParams.viewWidth,
					// mParams.viewHeight -
					// knightSprite.GROUND, 10, bmpNPC, mParams, this, 0);

					gameThread = gameThread == null ? new MainThread(ourHolder, this) : gameThread;
				}
			}

		}
	}

	/**
	 * Checks whether player has an item equipped, then adds item bonuses where
	 * applicable
	 */
	private void equipKnight() {

		String[] key = new String[1];
		String[] ibkey = new String[1];
		String[] equippedkey = new String[1];
		String[] var = new String[1];
		key[0] = "auto_id";
		equippedkey[0] = "equipped";
		ibkey[0] = "item_id";
		var[0] = "1";
		Cursor c = Items.du.execSQL(Items.du.TBL_ITEMS, equippedkey, var);
		while (c.moveToNext()) {
			Cursor c2;
			var[0] = String.valueOf(c.getInt(0));
			switch (c.getInt(3)) {
			case (Items.WEAPON_TYPE):
				currentWeapon = new Item(c.getInt(1), c.getString(11), c.getString(12), c.getInt(3), c.getFloat(2),
						c.getInt(4), c.getInt(6), c.getInt(7), c.getInt(5), c.getInt(9), c.getInt(10));
				weaponBitmap = InventoryMenu.resizeImage(BitmapFactory.decodeResource(getResources(), currentWeapon.id),
						emptyInventory.getWidth());
				currentWeapon.autoid = c.getInt(0);
				c2 = Items.du.execSQL(Items.du.TBL_ITEMBONUS, ibkey, var);
				while (c2.moveToNext()) {
					currentWeapon.itemBonuses.add(new ItemBonus(c.getInt(1), c2.getInt(2), c2.getFloat(3)));
				}
				c2.close();
				equipped.add(currentWeapon);
				break;
			case Items.HELMET_TYPE:
				currentHelmet = new Item(c.getInt(1), c.getString(11), c.getString(12), c.getInt(3), c.getFloat(2),
						c.getInt(4), c.getInt(6), c.getInt(7), c.getInt(5), c.getInt(9), c.getInt(10));
				helmetBitmap = InventoryMenu.resizeImage(BitmapFactory.decodeResource(getResources(), currentHelmet.id),
						emptyInventory.getWidth());
				currentHelmet.autoid = c.getInt(0);
				c2 = Items.du.execSQL(Items.du.TBL_ITEMBONUS, ibkey, var);
				while (c2.moveToNext()) {
					currentHelmet.itemBonuses.add(new ItemBonus(c.getInt(1), c2.getInt(2), c2.getFloat(3)));
				}
				c2.close();

				equipped.add(currentHelmet);
				break;
			case Items.SHIELD_TYPE:
				currentShield = new Item(c.getInt(1), c.getString(11), c.getString(12), c.getInt(3), c.getFloat(2),
						c.getInt(4), c.getInt(6), c.getInt(7), c.getInt(5), c.getInt(9), c.getInt(10));
				shieldBitmap = InventoryMenu.resizeImage(BitmapFactory.decodeResource(getResources(), currentShield.id),
						emptyInventory.getWidth());
				currentShield.autoid = c.getInt(0);
				c2 = Items.du.execSQL(Items.du.TBL_ITEMBONUS, ibkey, var);
				while (c2.moveToNext()) {
					currentShield.itemBonuses.add(new ItemBonus(c.getInt(1), c2.getInt(2), c2.getFloat(3)));
				}
				c2.close();

				equipped.add(currentShield);
				break;
			}

		}
		c.close();
	}

	// Everything that needs to be updated goes in here
	public void update(long timedelta) {
		// cloudbg1.update();
		if (playing) {
			for (Spell spell : knightSprite.spells) {
				spell.update(knightSprite, knightSprite.getXVelocity() / 60, knightSprite.getXVelocity() / 100);
			}
			knightSprite.update(timedelta);
			if (boss != null) {
				boss.update(timedelta, knightSprite);
			} else {
				if (completedQuestsArea.size() >= currentArea.quests.size() || currentArea.quests.size() == 0) {
					if (distanceWalked > currentArea.mDistanceEnd) {
						// if we have already completed difficulty, the
						// quest component is virtually inactive so player
						// can level in area.
						if (!pastAreaLimit) {
							pastAreaLimit = true;
							mGameFlowEvent.post(GameFlowEvent.EVENT_BEAT_AREA, 0, mParams.context, flowThread, 0);
						}
					}
				}
				if (!pastAreaLimit) {
					for (int i = 0; i < droppedItems.size(); i++) {
						final Item item = droppedItems.get(i);
						// destroy item when you walk away from item
						if (knightSprite.mPosition.x > item.mPosition.x + 5000) {
							item.itemBonuses.clear();
							item.destroy();
							droppedItems.remove(item);
							continue;
						}
					}
					for (GameAct.Quest quest : currentArea.quests) {
						// if we haven't completed quest
						if (!quest.completed) {
							// if we activate the quest and haven't gone out
							// of
							// range
							if (distanceWalked >= quest.distanceWalked - quest.disactRange
									&& distanceWalked - quest.distanceWalked <= quest.disactRange) {
								// starting out fresh no quest started
								if (!quest.inProcess && !questStarted) {
									switch (quest.type) {
									case QuestConstants.QC_TYPE_MONSTER_KILL:
										currentKillCount = currentKillCount >= 0 ? 0 : currentKillCount;
										break;
									default:
										break;
									}
									currentQuest = quest;
									questId = quest.id;
									quest.inProcess = true;
									distanceWalked = quest.distanceWalked - quest.disactRange;
									saveGame();
									onGameFlowEvent(GameFlowEvent.EVENT_SHOW_DIALOG_NPC, 0);
								} else {
									// quest was started and activity was
									// refreshed
									// update currentQuest, questStarted,
									// questId,
									// NPC id,position, and visibility
									if (!questStarted) {
										questStarted = true;
										currentQuest = quest;
										questId = currentQuest.id;
									}
								}
							} // left quest range
							else if (distanceWalked - quest.distanceWalked - quest.disactRange > 0
									|| distanceWalked < quest.distanceWalked - quest.disactRange) {
								// there's an active quest that is not
								// completed
								if (!quest.completed && quest.inProcess) {
									// reset relevant quest variables
									setQuestCompleted(false);
									setQuestStarted(false);
									quest.inProcess = false;
									currentQuest = null;
								}
							}
						}
						continue;

					}
					for (EnemySprite enemy : enemies) {
						enemy.update(timedelta, knightSprite);
					}
				}

			}
		} else

		{ // Paused

		}
	}

	// Draw the newly updated scene
	public void draw(Canvas canvas, long timedelta) {
		// Make sure our drawing surface is valid or we crash
		// Draw the background color
		if (canvas != null) {
			canvas.drawColor(Color.rgb(26, 128, 182));
			if (knightSprite.isHardcore() && knightSprite.getLastDeathTime() > 0
					&& System.currentTimeMillis() - knightSprite.getLastDeathTime() > 1950) {

			} else {
				// Choose the brush color for drawing
				paint.setColor(Color.argb(255, 159, 0, 0));

				paintOpac.setAlpha(175);
				// canvas.drawText("Total Game Time:" + mTotalGameTime, 20, 300,
				// paint);
				if (playing) {
					back.draw(canvas, paint);
					plat.draw(canvas, paint);
					bg.draw(canvas, paint);
					if (currentQuest != null) {
						canvas.drawBitmap(quest_active, healthBar.getWidth(), 0, paint);
					}
					if (boss != null) {
						boss.draw(timedelta, canvas, paint, paintOpac);
					} else {

						for (Item item : droppedItems) {
							item.draw(canvas, paint);
						}
						for (Potion potion : potions) {
							potion.draw(canvas, paint);
						}
						for (EnemySprite enemy : enemies) {
							enemy.draw(timedelta, canvas, paint, paintOpac);
						}
					}
					knightSprite.draw(timedelta, canvas, paint, paintOpac);

					for (Spell spell : knightSprite.spells) {
						switch (spell.type) {
						case Spell.ZEAL:
							if (knightSprite.addedZeal > 0) {
								zeal.mPosition.set(currentSkillInt * zealButton.getWidth(),
										mParams.viewHeight - zealButton.getHeight());
								canvas.drawBitmap(zealButton, zeal.mPosition.x, zeal.mPosition.y, paint);
								currentSkillInt++;
							}
							break;
						case Spell.FIREBALL:
							if (knightSprite.addedFB > 0) {
								fire.mPosition.set(currentSkillInt * zealButton.getWidth(),
										mParams.viewHeight - zealButton.getHeight());
								canvas.drawBitmap(fireButton, currentSkillInt * zealButton.getWidth(),
										mParams.viewHeight - zealButton.getHeight(), paint);
								currentSkillInt++;
							}
							break;
						case Spell.LIGHTSTORM:
							if (knightSprite.addedLS > 0) {
								light.mPosition.set(currentSkillInt * zealButton.getWidth(),
										mParams.viewHeight - zealButton.getHeight());
								canvas.drawBitmap(lightButton, currentSkillInt * zealButton.getWidth(),
										mParams.viewHeight - zealButton.getHeight(), paint);
								currentSkillInt++;
							}
							break;
						}
					}
					currentSkillInt = 0;
					for (Spell spell : knightSprite.spells) {
						spell.draw(canvas, paint);
						switch (spell.type) {
						case Spell.ZEAL:
							if (knightSprite.addedZeal > 0) {
								zeal.mPosition.set(currentSkillInt * zealButton.getWidth(),
										mParams.viewHeight - zealButton.getHeight());
								canvas.drawBitmap(zealButton, zeal.mPosition.x, zeal.mPosition.y, paint);
								currentSkillInt++;
							}
							break;
						case Spell.FIREBALL:
							if (knightSprite.addedFB > 0) {
								fire.mPosition.set(currentSkillInt * zealButton.getWidth(),
										mParams.viewHeight - zealButton.getHeight());
								canvas.drawBitmap(fireButton, currentSkillInt * zealButton.getWidth(),
										mParams.viewHeight - zealButton.getHeight(), paint);
								currentSkillInt++;
							}
							break;
						case Spell.LIGHTSTORM:
							if (knightSprite.addedLS > 0) {
								light.mPosition.set(currentSkillInt * zealButton.getWidth(),
										mParams.viewHeight - zealButton.getHeight());
								canvas.drawBitmap(lightButton, currentSkillInt * zealButton.getWidth(),
										mParams.viewHeight - zealButton.getHeight(), paint);
								currentSkillInt++;
							}
							break;
						}
					}
					currentSkillInt = 0;

				}
				// canvas.drawBitmap(addButton, mParams.viewWidth -
				// (inventory.empty.getWidth() * 3), 0, paint);
				// canvas.drawBitmap(spells, mParams.viewWidth -
				// (inventory.empty.getWidth() * 2), 0, paint);
				// canvas.drawBitmap(inv, mParams.viewWidth -
				// (inventory.empty.getWidth() * 1), 0, paint);
				// currentNPC.draw(timedelta, canvas, paint);
				if (!playing) {
					int slots = (mParams.viewHeight / inventory.empty.getHeight()) + 1;
					for (int i = 0; i < slots; i++) {
						canvas.drawBitmap(inventory.empty, 1000, ((i) * inventory.empty.getHeight()), null);

					}
					for (Item i : equipped) {
						switch (i.type) {
						case Items.WEAPON_TYPE:
							currentWeapon.getPosition().set(1000, 0);
							canvas.drawBitmap(weaponBitmap, currentWeapon.mPosition.x, currentWeapon.mPosition.y, null);
							break;
						case Items.HELMET_TYPE:
							currentHelmet.getPosition().set(1000, inventory.empty.getHeight());
							canvas.drawBitmap(helmetBitmap, currentHelmet.mPosition.x, currentHelmet.mPosition.y, null);
							break;
						case Items.SHIELD_TYPE:
							currentShield.getPosition().set(1000, inventory.empty.getHeight() * 2);
							canvas.drawBitmap(shieldBitmap, currentShield.mPosition.x, currentShield.mPosition.y, null);

							break;
						default:
							break;
						}
					}
				} else if (inventoryScreen) {
					inventory.render(canvas, mParams);
				}

				if (questCompleted) {
					canvas.drawBitmap(questComplete, (mParams.viewWidth / 2) - (questComplete.getWidth() / 2),
							(mParams.viewHeight / 2) - (questComplete.getHeight() / 2), paint);
				}
			}

		}
	}

	public void onGameFlowEvent(int eventCode, int index) {
		switch (eventCode) {
		case GameFlowEvent.EVENT_END_GAME:
			pause();
			main.finish();
			break;
		case GameFlowEvent.EVENT_RESTART_LEVEL:
			restartLevel();
			break;
		case GameFlowEvent.EVENT_BEAT_ACT:
			final GameAct.AreaGroup currentAct = GameAct.get(prefs.getInt(LevelPreferences.PREFERENCE_ACT, 0));
			final int count = currentAct.levels.size();
			boolean groupCompleted = true;

			for (int x = 0; x < count; x++) {
				if (currentAct.levels.get(x).completed == false) {
					// We haven't completed the group yet
					groupCompleted = false;
					break;
				}
			}

			if (currentAct.mId < GameAct.levels.size() - 1 && groupCompleted) {
				final GameAct.GameArea currentarea = GameAct.get(actId, areaId);
				currentAct.completed = true;
				saveGame();
				pauseGame();
				if (editor == null) {
					editor = prefs.edit();
				}
				editor.putInt(
						LevelPreferences.PREFERENCE_AREA_COMPLETED + "_" + actId + "_" + areaId + "_" + difficulty,
						actId);
				editor.putInt(LevelPreferences.PREFERENCE_ACT_COMPLETED + "_" + actId + "_" + difficulty, actId);
				editor.putInt(LevelPreferences.PREFERENCE_ACT, actId + 1);
				editor.apply();
				editor.commit();
				// go to the level select. if more areas in this act NOT
				// IMPLEMENTED IGNORE *****
				Intent i = new Intent(mParams.context, LevelSelectActivity.class);
				main.startActivityForResult(i, 1);
				if (UIConstants.mOverridePendingTransition != null) {
					try {
						UIConstants.mOverridePendingTransition.invoke(main, R.anim.activity_fade_in,
								R.anim.activity_fade_out);
					} catch (InvocationTargetException ite) {
						Log.d("Activity Transition", "Invocation Target Exception");
					} catch (IllegalAccessException ie) {
						Log.d("Activity Transition", "Illegal Access Exception");
					}
				}

			} else {
				// We beat the game for this difficulty!
				actId = 0;
				areaId = 0;
				questId = 0;
				difficultyCompleted = true;
				currentArea.completed = true;
				GameAct.get(actId).completed = true;
				mParams.difficultyCompleted += 1;
				distanceWalked = 0;
				saveGame();
				// Intent i = new Intent(mParams.context,
				// MainMenuActivity.class);
				pauseGame();
				main.finish();
				// ((MainActivity) mParams.context).startActivity(i);
			}

			break;
		case GameFlowEvent.EVENT_BEAT_AREA:
			// if we've completed the last area in the act, we beat this
			// difficulty
			if (currentArea.bossId != -1 && boss == null) {
				if (currentArea.startBossDialog) {
					startedAreaBoss();
				} else if (!currentArea.startBossDialog && currentArea.endBossDialog) {
					endedAreaBoss();
				} else {
					loadNextLevel();

				}
			} else {
				// beat the area boss
				if (currentArea.bossId != -1) {
					if (currentArea.endBossDialog) {
						currentArea.endBossDialog = false;
						onGameFlowEvent(GameFlowEvent.EVENT_SHOW_DIALOG_NPC, areaId);
					}
				} else {
					loadNextLevel();
				}

			}

			break;

		case GameFlowEvent.EVENT_BEAT_QUEST:
			currentQuest.inProcess = false;
			currentQuest.completed = true;
			knightSprite.applyReward(currentQuest.reward);
			setQuestStarted(false);
			setQuestCompleted(true);
			currentExperience = (int) knightSprite.getExperience();
			completedQuestsArea.add(currentQuest);
			saveGame();
			// register a timed event to run on flowThread to display quest
			// completed image
			mGameFlowEvent.post(GameFlowEvent.EVENT_BEAT_QUEST_UI, 0, mParams.context, flowThread, 4000);
			break;

		case GameFlowEvent.EVENT_BEAT_QUEST_UI:

			setQuestCompleted(false);
			break;
		case GameFlowEvent.EVENT_START_AREA_BOSS:
			// this event will occur when all quests in the area are completed
			// player must defeat boss to continue to the next area
			if (currentArea.endBossDialog) {
				// recycle bitmaps to make room for boss sprites in memory
				recycleEnemyBmps();
				// get rid of enemies
				enemies.clear();

				for (int i = 0; i < droppedItems.size(); i++) {
					final Item item = droppedItems.get(i);
					item.itemBonuses.clear();
					item.destroy();
					droppedItems.remove(item);
					continue;
				}
				Runtime.getRuntime().gc();
				System.runFinalization();
				// add boss to game
				boss = new Boss(currentArea.bossId, mParams.viewWidth, mParams.viewHeight - 40, 1, 11, mParams, this);
			} else {
				onGameFlowEvent(GameFlowEvent.EVENT_BEAT_AREA, areaId);
			}
			break;

		case GameFlowEvent.EVENT_SHOW_DIALOG_NPC:
			saveGame();
			// pauseGame();
			if (knightSprite != null) {
				recycleEnemyBmps();
				knightSprite.destroy();
				groundTile.recycle();
				groundTile2.recycle();
				potionSm.recycle();
				potionLg.recycle();
				enemies.clear();
				potions.clear();
				healthBar.recycle();
				fireButton.recycle();
				lightButton.recycle();
				zealButton.recycle();
				questComplete.recycle();
				quest_active.recycle();
				tome.recycle();
				background.recycle();
			}
			if (boss != null) {
				boss.recycleBmps();
			}
			// gameView.bmpNPC.recycle();
			// gameView.bmpNPCLeft.recycle();

			if (weaponBitmap != null) {
				weaponBitmap.recycle();
			}
			if (shieldBitmap != null) {
				shieldBitmap.recycle();
			}
			if (helmetBitmap != null) {
				helmetBitmap.recycle();
			}

			Intent i = new Intent(main, ConversationDialogActivity.class);
			i.putExtra("actId", actId);
			i.putExtra("areaId", areaId);
			i.putExtra("questId", questId);
			if (currentArea.bossId != -1 && pastAreaLimit) {
				i.putExtra("bossId", currentArea.bossId);
				if (!currentArea.startBossDialog && currentArea.endBossDialog) {
					i.putExtra("index", 1);
				} else if (!currentArea.startBossDialog && !currentArea.endBossDialog) {
					i.putExtra("index", 2);
				}
			}
			i.putExtra("character", 1);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			main.finish();
			main.startActivity(i);
			break;
		case GameFlowEvent.EVENT_GET_NEXT_AREA:
			break;
		case GameFlowEvent.EVENT_PAUSED:

			break;
		case GameFlowEvent.EVENT_RESUMED:

			break;
		case GameFlowEvent.EVENT_LEVELLED:
			knightSprite.levelUp();
			break;
		// case GameFlowEvent.EVENT_SHOW_DIALOG_CHARACTER2:
		// i = new Intent(this, ConversationDialogActivity.class);
		// i.putExtra("levelRow", mLevelRow);
		// i.putExtra("levelIndex", mLevelIndex);
		// i.putExtra("index", index);
		// i.putExtra("character", 2);
		// startActivity(i);
		// break;

		}
	}

	private void startedAreaBoss() {
		currentArea.startBossDialog = false;
		onGameFlowEvent(GameFlowEvent.EVENT_SHOW_DIALOG_NPC, areaId);

	}

	private void endedAreaBoss() {
		onGameFlowEvent(GameFlowEvent.EVENT_START_AREA_BOSS, areaId);
	}

	private void loadNextLevel() {
		if (currentArea.mId >= GameAct.get(actId).levels.size() - 1) {
			onGameFlowEvent(GameFlowEvent.EVENT_BEAT_ACT, actId);
		} else {
			currentArea.completed = true;
			if (currentArea.bossId != -1) {
				currentArea.startBossDialog = true;
				currentArea.endBossDialog = true;
			}
			saveGame();
			/// load level select activity
			main.finish();
			Intent i = new Intent(main, LevelSelectActivity.class);
			i.putExtra(LevelPreferences.PREFERENCE_DIFFICULTY_COMPLETED, difficultyCompleted);
			i.putExtra("difficulty", mParams.difficulty);
			main.startActivity(i);
		}
	}

	/**
	 * Method is called very frequently to update the shared preferences for
	 * persistence sake. Initializes the editor is it isn't already and updates
	 * various preferences accordingly.
	 */
	protected void saveGame() {
		initEditor();
		editor.putInt(LevelPreferences.PREFERENCE_AREA,
				currentArea.mId);
		editor.putInt(LevelPreferences.PREFERENCE_AREA + "_" + actId + "_" + areaId + "_" + mParams.difficulty,
				currentArea.mId);
		if (currentQuest != null) {
			if (currentQuest.completed) {
				editor.putInt("A" + actId + "_A" + areaId + "_Q" + questId + "_" + mParams.difficulty, 1);
				editor.putInt(LevelPreferences.PREFERENCE_QUEST + "_" + actId + "_" + areaId + "_" + mParams.difficulty,
						-1);
				currentQuest = null;
				questId = -1;

			} else {
				editor.putInt(LevelPreferences.PREFERENCE_QUEST + "_" + actId + "_" + areaId + "_" + mParams.difficulty,
						currentQuest.id);
			}
		} else {
			editor.putInt(LevelPreferences.PREFERENCE_QUEST + "_" + actId + "_" + areaId + "_" + mParams.difficulty,
					-1);
		}
		editor.putInt(
				LevelPreferences.PREFERENCE_AREA_COMPLETED + "_" + actId + "_" + areaId + "_" + mParams.difficulty,
				currentArea.completed == false ? 0 : 1);
		// editor.putFloat(PreferenceConstants.PREFERENCE_TOTAL_GAME_TIME,
		// mTotalGameTime);
		editor.putInt(PreferenceConstants.PREFERENCE_TOTAL_KILLS, totalKills + currentKillCount);

		if (currentQuest != null) {
			distanceWalked = currentQuest.distanceWalked - currentQuest.disactRange;

		}
		editor.putInt(PreferenceConstants.PREFERENCE_DISTANCEWALKED, distanceWalked);
		if (knightSprite != null) {
			editor.putInt(PreferenceConstants.PREFERENCE_EXP, (int) knightSprite.getExperience());
		}
		if (difficultyCompleted) {
			editor.putInt(LevelPreferences.PREFERENCE_DIFFICULTY_COMPLETED, mParams.difficultyCompleted);
			difficultyCompleted = false;
		}
		editor.putInt(LevelPreferences.PREFERENCE_DIFFICULTY, mParams.difficulty);
		editor.commit();
		editor.apply();

		editor = null;

	}

	/**
	 * @description If the editor is null, we initialize it
	 */
	private void initEditor() {
		editor = prefs.edit();

	}

	private Item handleItemBonuses(Item item, int typeRandom, int currentRandom, int bonusCount, float bonusRandom) {

		currentRandom = cloudRandom.nextInt(bonusCount + 1);
		for (int d = 0; d < currentRandom; d++) {
			typeRandom = cloudRandom.nextInt(ItemBonus.PHYSICAL_DAMAGE_MAXMIN + 1);

			// fail-safe
			// test current ItemBonuses to make sure the bonus type has
			// not already been assigned to the item
			// if it has, fetch new random number
			for (ItemBonus ib2 : item.itemBonuses) {
				if (ib2.mType == typeRandom) {
					typeRandom = cloudRandom.nextInt(ItemBonus.PHYSICAL_DAMAGE_MAXMIN + 1);
					break;
				}
			}
			bonusRandom = cloudRandom.nextInt(3) + 1;
			// life leech is Over-Powered (OP) at the moment so reduce the
			// amount available to
			// the player
			if (typeRandom == ItemBonus.LIFELEECH_PERCENT || typeRandom == ItemBonus.MANALEECH_PERCENT) {
				bonusRandom = cloudRandom.nextInt(2) + 1;
			}
			if (item.type != Items.WEAPON_TYPE
					&& (typeRandom == ItemBonus.LIFELEECH_PERCENT || typeRandom == ItemBonus.MANALEECH_PERCENT)) {
				// don't add life or mana leech stats to anything but
				// weapons
			} else {
				item.itemBonuses.add(new ItemBonus((int) item.autoid, typeRandom, bonusRandom));
			}

		}
		return item;
	}

	private void addItemBonusesCharacterStart() {
		Item item = null;
		int bonusCount = 0;
		int currentRandom = 0;
		float bonusRandom = 0;
		int typeRandom = 0;

		if (prefs.getInt(PreferenceConstants.PREFERENCE_CURRENTWEAPON, -1) == -1) {
			item = Items.ITEM_MAP.get(R.drawable.weapon_standard);

			item = new Item(item.id, item.content, item.name, item.type, item.dropChance, item.iLvl, item.minStat,
					item.maxStat, item.iClass, item.requiredStr, item.requiredDex, 0, 0, true, -1);

			editor.putInt(PreferenceConstants.PREFERENCE_CURRENTWEAPON, (int) item.autoid);
			weaponBitmap = InventoryMenu.resizeImage(BitmapFactory.decodeResource(getResources(), item.id),
					emptyInventory.getWidth());
			item.spritesheet = weaponBitmap;
			bonusCount = 2;
			item = handleItemBonuses(item, typeRandom, currentRandom, bonusCount, bonusRandom);
			currentWeapon = item;
			equipped.add(item);

		}
		if (prefs.getInt(PreferenceConstants.PREFERENCE_CURRENTHELMET, -1) == -1) {
			item = Items.ITEM_MAP.get(R.drawable.helmet_standard);
			item = new Item(item.id, item.content, item.name, item.type, item.dropChance, item.iLvl, item.minStat,
					item.maxStat, item.iClass, item.requiredStr, item.requiredDex, 0, 0, true, -1);
			editor.putInt(PreferenceConstants.PREFERENCE_CURRENTHELMET, (int) item.autoid);
			helmetBitmap = InventoryMenu.resizeImage(BitmapFactory.decodeResource(getResources(), (int) item.id),
					emptyInventory.getWidth());
			item.spritesheet = helmetBitmap;
			bonusCount = 2;

			item = handleItemBonuses(item, typeRandom, currentRandom, bonusCount, bonusRandom);
			currentHelmet = item;
			equipped.add(item);

		}
		if (prefs.getInt(PreferenceConstants.PREFERENCE_CURRENTSHIELD, -1) == -1) {
			item = Items.ITEM_MAP.get(R.drawable.shield_standard);
			item = new Item(item.id, item.content, item.name, item.type, item.dropChance, item.iLvl, item.minStat,
					item.maxStat, item.iClass, item.requiredStr, item.requiredDex, 0, 0, true, -1);
			editor.putInt(PreferenceConstants.PREFERENCE_CURRENTSHIELD, (int) item.autoid);
			shieldBitmap = InventoryMenu.resizeImage(BitmapFactory.decodeResource(getResources(), item.id),
					emptyInventory.getWidth());
			item.spritesheet = shieldBitmap;
			bonusCount = 2;
			item = handleItemBonuses(item, typeRandom, currentRandom, bonusCount, bonusRandom);
			currentShield = item;
			equipped.add(item);
		}
	}

	/**
	 * @description Wipe away shared prefereneces to start over
	 */
	protected void newGame() {
		initEditor();
		editor.clear();
		editor.commit();
		editor.apply();
		addItemBonusesCharacterStart();
		editor = null;
	}

	// temporary pause to show overlays
	// used to stop updating screen and not lose position of characters
	public void pauseScreen() {
		if (playing) {
			main.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (main.overlay.getChildCount() > 0) {
						main.overlay.removeAllViews();
					}
					if (inventoryScreen) {
						inventoryScreen = false;
					}
					if (!statsScreen) {
						statsScreen = true;
					}
					if (skillsScreen) {
						statsScreen = false;
					}
					currentItem = ItemDetailFragment.pauseScreenView(main, currentWeapon, main.getLayoutInflater());
					if (statsScreen) {
						setStatsScreen();
					} else if (skillsScreen) {
						setSpellScreen();
					}

				}
			});

			playing = false;
		}
	}

	public void replaceScreen() {
		main.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (main.overlay.getChildCount() > 0) {
					main.overlay.removeAllViews();
				}
				if (inventoryScreen) {
					inventoryScreen = false;
				}
				if (!statsScreen) {
					statsScreen = true;
				}
				if (skillsScreen) {
					statsScreen = false;
				}
				currentItem = ItemDetailFragment.pauseScreenView(main, currentWeapon, main.getLayoutInflater());
				if (statsScreen) {
					setStatsScreen();
				} else if (skillsScreen) {
					setSpellScreen();
				}

			}
		});

	}

	public void setPlaying(boolean play) {
		playing = play;
	}

	// resume from temp pause
	public void resumeScreen() {
		if (!playing) {
			if (main.overlay.getChildCount() > 0) {
				main.overlay.removeAllViews();
				main.menubar.removeAllViews();
			}
			currentItem = null;
			if (statsScreen) {
				statsScreen = false;
			}
			if (skillsScreen) {
				skillsScreen = false;
			}
			main.skillStat.removeAllViews();
			playing = true;
		}
	}

	public void setStatsScreen() {
		StatsScreen.getStatsScreen(mParams.viewWidth, mParams.viewHeight, main, knightSprite, main.getLayoutInflater());

	}

	public void setSpellScreen() {
		SpellScreen.getSpellScreen(mParams.viewWidth, mParams.viewHeight, main, knightSprite, main.getLayoutInflater());

	}

	/**
	 * @description If Main Activity is paused/stopped shutdown our thread.
	 */
	public void pause() {
		if (gameThread != null) {
			if (gameThread.isRunning()) {
				playing = false;
				gameThread.setRunning(false);
			}
			try {
				gameThread.join();
				flowThread.join();
			} catch (InterruptedException e) {
				Log.e("Error:", "joining thread");
			}
		}
	}

	/**
	 * @description If Main Activity is paused/stopped shutdown our thread.
	 */
	public void pauseGame() {
		if (gameThread != null) {
			if (gameThread.isRunning()) {
				playing = false;
				gameThread.setRunning(false);
			}
			try {
				gameThread.join();
			} catch (InterruptedException e) {
				Log.e("Error:", "joining thread");
			}
		}
	}

	/**
	 * @description If MainActivity is started then start our thread.
	 */

	public void resume() {
		if (!gameThread.isRunning()) {
			playing = true;
			if (!gameThread.isAlive() && !gameThread.isInterrupted()) {
				if (gameThread.getState() == Thread.State.NEW) {
					gameThread.start();
				}
			}
			gameThread.setRunning(true);
		}
	}

	/**
	 * @description The SurfaceView class implements onTouchListener So we can
	 *              override this method and detect screen touches.
	 */
	private int mActivePointerId;

	@Override
	public boolean onTouchEvent(MotionEvent motionEvent) {
		final long time = System.currentTimeMillis();
		lastClickTime = time;
		if (knightSprite != null) {
			if (!knightSprite.isDead()) {

				// Send all input to gesture detector
				mGestureDetector.onTouchEvent(motionEvent);
				switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
				// Player has touched the screen
				case MotionEvent.ACTION_DOWN:
					mActivePointerId = motionEvent.getPointerId(0);
					break;
				case MotionEvent.ACTION_POINTER_DOWN:
					if (knightSprite.lastSpellTime == 0) {
						// Use the pointer ID to find the index of the active
						// pointer
						// and fetch its position
						final int pointerIndex = motionEvent.findPointerIndex(mActivePointerId);
						// Get the pointer's current position
						final float x = motionEvent.getX(pointerIndex);
						final float y = motionEvent.getY(pointerIndex);
						mActivePointerId = motionEvent.getPointerId(1);
						final float newX = motionEvent.getX(mActivePointerId);
						final float newY = motionEvent.getY(mActivePointerId);
						for (Spell spell : knightSprite.spells) {
							if (knightSprite.getCurrentMana() > spell.getBaseManaCost(knightSprite)) {

								switch (spell.type) {
								case Spell.ZEAL:
									if (knightSprite.addedZeal > 0 && newX > zeal.mPosition.x
											&& newX < zealButton.getWidth()
											&& newY > mParams.viewHeight - zealButton.getHeight()) {
										knightSprite.lastSpellTime = time;
										knightSprite.setZeal(true);

										break;
									}
									break;
								case Spell.FIREBALL:
									if (knightSprite.addedFB > 0 && newX > fire.mPosition.x
											&& newX < fire.mPosition.x + zealButton.getWidth()
											&& newY > mParams.viewHeight - zealButton.getHeight()) {
										knightSprite.lastSpellTime = time;
										knightSprite.lastSpellCasted = Spell.FIREBALL;
										knightSprite.setCurrentMana(-spell.getBaseManaCost(knightSprite));
										spell.setMoving(true);
										spell.setVisible(true);
										break;
									}
									break;
								case Spell.LIGHTSTORM:
									if (knightSprite.addedLS > 0 && newX > light.mPosition.x
											&& newX < light.mPosition.x + zealButton.getWidth()
											&& newY > mParams.viewHeight - zealButton.getHeight()) {
										knightSprite.lastSpellTime = time;
										spell.mPosition.set(x, spell.mPosition.y);
										knightSprite.lastSpellCasted = Spell.LIGHTSTORM;
										knightSprite.setCurrentMana(-spell.getBaseManaCost(knightSprite));
										spell.setMoving(true);
										spell.setVisible(true);
										break;
									}
									break;
								}
							}
						}
					}
					if (knightSprite.isMoving() || knightSprite.isJumping()) {
						knightSprite.setMoving(false);
						knightSprite.setAttacking(true);
					} else {
						knightSprite.setAttacking(true);
					}
					break;
				// second pointer removed from screen, stop attacking
				case MotionEvent.ACTION_POINTER_UP:
					if (knightSprite.isAttacking()) {
						knightSprite.setAttacking(false);
					}
					if (knightSprite.getZeal()) {
						knightSprite.setZeal(false);
						knightSprite.lastSpellTime = 0;
					}
					break;

				case MotionEvent.ACTION_UP:
					// Player has removed finger from screen
					// Stop walk and run constants
					// reset walk speed if running
					if (knightSprite.isMoving()) {
						knightSprite.setMoving(false);
					}
					if (knightSprite.isRunning()) {
						knightSprite.setXVelocity(800);
						knightSprite.setRunning(false);
					}
					break;
				}
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	@Override
	public boolean performClick() {
		// Necessary for SurfaceView, not being used
		super.performClick();
		return false;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		// Log.d("onSingleTapConfirmed", "onSingleTapConfirmed");
		return false;
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		if (!inventoryScreen) {
			for (Item item : droppedItems) {

				if (e.getRawX() > item.mPosition.x && e.getRawX() < item.mPosition.x + item.spritesheet.getWidth()
						&& e.getRawY() > item.mPosition.y
						&& e.getRawY() < item.mPosition.y + item.spritesheet.getHeight()) {

					currentItem = ItemDetailFragment.itemPickUpView(main, item, main.getLayoutInflater());
					thisItem = item;
					inventoryScreen = true;
					break;
				}
			}
			if (currentItem != null) {
				if (e.getRawX() < +currentItem.getWidth()) {
					if (inventory.inv.size() < inventory.COLUMNS * inventory.ROWS) {
						currentItem = null;
						main.overlay.removeAllViews();
						if (thisItem != null) {
							// remove from droppedItems array
							if (droppedItems.contains(thisItem)) {
								droppedItems.remove(thisItem);
							}
							// picked up item add to inventory
							if (!inventory.inv.contains(thisItem)) {
								inventory.inv.add(thisItem);
							}
							thisItem.insertIntoDB(false);
							for (ItemBonus ib : thisItem.itemBonuses) {
								ib.insertIntoDB((int) thisItem.autoid, ib.mType, ib.mChosen);
							}
						}

					}
				}
			}
		} else {
			// destroy item when double clicked on in the inventory
			for (Item item : inventory.inv) {
				if (e.getRawX() > item.mPosition.x && e.getRawX() < item.mPosition.x + item.spritesheet.getWidth()
						&& e.getRawY() > item.mPosition.y
						&& e.getRawY() < item.mPosition.y + item.spritesheet.getHeight()) {
					inventory.inv.remove(item);
					try {
						Items.du.delete(Items.du.TBL_ITEMBONUS, "item_id = " + item.autoid);
						Items.du.delete(Items.du.TBL_ITEMS, "auto_id = " + item.autoid);
						item.spritesheet.recycle();
						item = null;
						currentItem = null;
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					main.overlay.removeAllViews();
					break;
				}

			}
			if (currentItem != null) {
				if (e.getRawX() < +currentItem.getWidth()) {
					if (inventory.inv.size() < inventory.COLUMNS * inventory.ROWS) {
						if (thisItem != null) {
							// remove from droppedItems array
							if (droppedItems.contains(thisItem)) {
								if (thisItem != null) {
									thisItem.insertIntoDB(false);
									for (ItemBonus ib : thisItem.itemBonuses) {
										ib.insertIntoDB((int) thisItem.autoid, ib.mType, ib.mChosen);
									}
								}
								droppedItems.remove(thisItem);
							}
							// picked up item add to inventory
							if (!inventory.inv.contains(thisItem)) {
								inventory.inv.add(thisItem);
							} else {
								// otherwise we double-clicked to wear it
								// current item
								int strToRem = 0;
								int dexToRem = 0;
								Item itemToEval = null;
								if (thisItem.type == Items.WEAPON_TYPE) {
									if (currentWeapon != null) {
										itemToEval = currentWeapon;
									}
								} else if (thisItem.type == Items.SHIELD_TYPE) {
									if (currentShield != null) {
										itemToEval = currentShield;
									}
								} else if (thisItem.type == Items.HELMET_TYPE) {
									if (currentHelmet != null) {
										itemToEval = currentHelmet;
									}
								}
								for (ItemBonus ib : itemToEval.itemBonuses) {
									if (ib.mType == ItemBonus.DEXTERITY) {
										dexToRem += ib.mChosen;
									} else if (ib.mType == ItemBonus.STRENGTH) {
										strToRem += ib.mChosen;
									}
									continue;
								}
								if (knightSprite.canWeWear(thisItem.requiredStr + strToRem,
										thisItem.requiredDex + dexToRem)) {
									inventory.inv.remove(thisItem);

									// we either just added the item to the
									// inventory or
									// are trying to equip the item
									// if player has required stats to wear item

									switch (thisItem.type) {
									case Items.WEAPON_TYPE:
										if (currentWeapon != null) {
											Items.du.updateEquipped(false, String.valueOf(currentWeapon.autoid));
											equipped.remove(currentWeapon);
											inventory.inv.add(currentWeapon);
										}
										currentWeapon = thisItem;
										weaponBitmap = currentWeapon.spritesheet;
										break;
									case Items.HELMET_TYPE:
										if (currentHelmet != null) {
											Items.du.updateEquipped(false, String.valueOf(currentHelmet.autoid));
											equipped.remove(currentHelmet);
											inventory.inv.add(currentHelmet);
										}
										currentHelmet = thisItem;
										helmetBitmap = currentHelmet.spritesheet;
										break;
									case Items.SHIELD_TYPE:
										if (currentShield != null) {
											Items.du.updateEquipped(false, String.valueOf(currentShield.autoid));
											equipped.remove(currentShield);
											inventory.inv.add(currentShield);
										}
										currentShield = thisItem;
										shieldBitmap = currentShield.spritesheet;
										break;
									}
									Items.du.updateEquipped(true, String.valueOf(thisItem.autoid));
									equipped.add(thisItem);
									knightSprite.equipped = equipped;
									knightSprite.resetBaseStats();
									currentItem = null;
									main.overlay.removeAllViews();
								}
							}

						}

					}
				}

			} else {
				if (main.overlay.getChildCount() > 0) {
					main.overlay.removeAllViews();
				} else {
					if (e.getRawX() < mParams.viewWidth - inventory.empty.getWidth() * inventory.COLUMNS) {
						inventoryScreen = false;
					}
				}

			}
		}

		return true;

	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		// Log.d("onDoubleTapEvent", "onDoubleTapEvent");
		return false;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		Log.d("onDown", "onDown");
		final float x = e.getRawX();
		final float y = e.getRawY();
		// Set isMoving so Knight is moved in the update method
		// Disable down click when jumping
		if (!knightSprite.isJumping() && playing && !inventoryScreen) {
			knightSprite.setMoving(true);
			if (x > knightSprite.getPosition().x + knightSprite.getFrameWidth()) {
				// Already facing right
				if (knightSprite.facingDirection.x != 1) {
					knightSprite.facingDirection.set(1, 0);
				}
			} else if (x < knightSprite.getPosition().x) {
				// Already facing left
				if (knightSprite.facingDirection.x != -1) {
					knightSprite.facingDirection.set(-1, 0);
				}
			}
			return true;
		} else if (!playing) {
			if (inventoryScreen) {
				if (main.overlay.getChildCount() > 0) {
					main.overlay.removeAllViews();
				}
				inventoryScreen = false;

			}
			// clicked weapon
			if (x > currentWeapon.mPosition.x && x < currentWeapon.mPosition.x + currentWeapon.spritesheet.getWidth()
					&& y > currentWeapon.mPosition.y
					&& y < currentWeapon.mPosition.y + currentWeapon.spritesheet.getHeight()) {
				if (currentItem != null) {
					main.overlay.removeAllViews();
					currentItem = null;
					currentItem = ItemDetailFragment.pauseScreenView(main, currentWeapon, main.getLayoutInflater());
				} else {
					currentItem = ItemDetailFragment.pauseScreenView(main, currentWeapon, main.getLayoutInflater());

				}

			} else if (x > currentHelmet.mPosition.x
					&& x < currentHelmet.mPosition.x + currentHelmet.spritesheet.getWidth()
					&& y > currentHelmet.mPosition.y
					&& y < currentHelmet.mPosition.y + currentHelmet.spritesheet.getHeight()) {
				if (currentItem != null) {
					main.overlay.removeAllViews();
					currentItem = null;
					currentItem = ItemDetailFragment.pauseScreenView(main, currentHelmet, main.getLayoutInflater());
				} else {
					currentItem = ItemDetailFragment.pauseScreenView(main, currentHelmet, main.getLayoutInflater());
				}

			} else if (x > currentShield.mPosition.x
					&& x < currentShield.mPosition.x + currentShield.spritesheet.getWidth()
					&& y > currentShield.mPosition.y
					&& y < currentShield.mPosition.y + currentShield.spritesheet.getHeight()) {
				if (currentItem != null) {
					main.overlay.removeAllViews();
					currentItem = null;
					currentItem = ItemDetailFragment.pauseScreenView(main, currentShield, main.getLayoutInflater());
				} else {
					currentItem = ItemDetailFragment.pauseScreenView(main, currentShield, main.getLayoutInflater());
				}

			}
		} else if (inventoryScreen) {
			for (Item item : inventory.inv) {
				if (e.getRawX() > item.mPosition.x && e.getRawX() < item.mPosition.x + item.spritesheet.getWidth()
						&& e.getRawY() > item.mPosition.y
						&& e.getRawY() < item.mPosition.y + item.spritesheet.getHeight()) {
					if (currentItem != null) {
						main.overlay.removeAllViews();
						currentItem = null;
						currentItem = ItemDetailFragment.itemPickUpView(main, item, main.getLayoutInflater());
					} else {
						currentItem = ItemDetailFragment.itemPickUpView(main, item, main.getLayoutInflater());
					}
					thisItem = item;
				}
			}

			return true;
		} else {
			return false;
		}
		return false;
	}

	/**
	 * @description Run when one finger is held down
	 */
	@Override
	public void onLongPress(MotionEvent e) {
		// Log.d("onLongPress", "onLongPress");

		knightSprite.setXVelocity(1200);
		knightSprite.setRunning(true);

	}

	/**
	 * @description jump on fling
	 */
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		// Log.d("onFling", "onFling" + velocityX + " " + velocityY);
		if (knightSprite.isJumping() || knightSprite.isAttacking() || !playing) {
			return false;
		} else {
			if (!inventoryScreen) {
				// use tan(theta) = a/b to calculate angle of jump
				final long time = System.currentTimeMillis();
				float diffx = e1.getRawX() - e2.getRawX();
				float diffy = e1.getRawY() - e2.getRawY();
				diffx = Math.abs(diffx);
				diffy = Math.abs(diffy);
				float a = diffx * diffx;
				float b = diffy * diffy;
				double angle;

				// formula requires smaller side as numerator
				angle = (float) (a <= b ? Math.tan((double) a / (double) b)
						: (float) Math.tan((double) b / (double) a));
				// setting fling sensitivity
				if (velocityY <= -7000) {
					// if jumping down
					knightSprite.setMoving(true);
					knightSprite.setJumping(true);
					knightSprite.setJumpSpeed(-knightSprite.jumpSpeedLimit);
					knightSprite.jumpAngle = angle;
					knightSprite.setLastJumpTime(time);
					knightSprite.setJumpY(0);
				} else if (velocityY >= 7000) {
					// if jumping up
					knightSprite.setMoving(true);
					knightSprite.setJumping(true);
					knightSprite.setJumpSpeed(-knightSprite.jumpSpeedLimit);
					knightSprite.jumpAngle = angle;
					knightSprite.setLastJumpTime(time);
					knightSprite.setJumpY(1);
				}
				if (main.overlay.getChildCount() > 0) {
					currentItem = null;
					main.overlay.removeAllViews();
				}
			} else {
				currentItem = null;
				main.overlay.removeAllViews();
			}
			return true;

		}

	}

	@Override
	public void onShowPress(MotionEvent e) {
		// Log.d("onShowPress", "onShowPress");
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// Log.d("onSingleTapUp", "onSingleTapUp");
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		// Log.d("onScroll", "onScroll");
		return false;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d("GameView", "surfaceCreated");
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

		Log.d("GameView", "surfaceChanged");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

		Log.d("GameView", "surfaceDestroyed");

	}

	/**
	 * @description Creates potion if the number of potions in the array is less
	 *              than or equal to 3. Potion is assigned to enemy 30 percent
	 *              of the time. If in Nightmare or Hell, use big potion.
	 * 
	 * @param maxPotions
	 * @param enemy
	 */
	private void createPotions(int maxPotions, EnemySprite enemy) {
		if (potions.size() < 3) {
			// Generate potions for random enemies
			// 30 percent chance
			if (cloudRandom.nextInt(100) + 1 <= 30) {
				Potion potion;
				if (difficulty > DifficultyConstants.NORMAL) {
					potion = new Potion(mParams.viewWidth, (int) knightYPosition, 0, 1, true, potionSm, mParams, this,
							enemy.mId);

				} else {
					potion = new Potion(mParams.viewWidth, (int) knightYPosition, 0, 1, true, potionLg, mParams, this,
							enemy.mId);
				}
				potion.setAssigned(true);
				potions.add(potion);
				enemy.setPotion(true);
				enemy.setPotion(potion);
				Log.d("createPotions", "added potion");// Testing
			}
		}

	}

	public void assignItemToDrop(EnemySprite enemy) {

		enemy.setItemToDrop(Items.getItemToDrop(cloudRandom, (int) enemy.level));

		if (enemy.itemToDrop != null) {

			enemy.itemToDrop.setBitmap(InventoryMenu.resizeImage(
					BitmapFactory.decodeResource(getResources(), enemy.itemToDrop.id), inventory.empty.getHeight()));
			Log.d("assignItemToDrop", "added item");// Testing
		}

	}

	/**
	 * @description Creating multiple enemies. Runs when the game is launched,
	 *              quest has started, or anytime the player leaves the gameView
	 *              and returns.
	 * 
	 * @param maxEnemies
	 */
	public void createEnemies(int maxEnemies) {
		MonsterType type = MonsterType.adventureGirl;
		switch (currentArea.monsterType) {
		case 0:
			type = MonsterType.adventureGirl;
			break;
		case 1:
			type = MonsterType.robot;
			break;
		case 2:
			type = MonsterType.trollGreen;
			break;
		case 3:
			type = MonsterType.trollBlue;
			break;
		}
		// for loop up to the amount of max enemies parameter
		for (int i = 0; i < maxEnemies; i++) {
			// space out enemies based on screen width and the place in the loop
			int enemyPosX = mParams.viewWidth;
			enemyPosX = (i > 0) ? enemyPosX + cloudRandom.nextInt((mParams.viewWidth + 1) + 1) * i : enemyPosX;
			EnemySprite enemy;
			if (knightSprite.getCurrentLevel() >= 5) {
				enemy = new EnemySprite(type, i, enemyPosX, (int) knightYPosition, 4, 10, true,
						currentArea.monsterLevel, bmpEnemyLeft, mParams, this);
			} else {
				enemy = new EnemySprite(type, i, enemyPosX, (int) knightYPosition, 4, 10, true,
						(int) knightSprite.getCurrentLevel(), bmpEnemyLeft, mParams, this);
			}
			createPotions(3, enemy);
			assignItemToDrop(enemy);
			enemies.add(enemy);
		}
	}

	/**
	 * @description Creating a single enemy and add to array. If the player
	 *              level is greater than 5, use the monster level of the
	 *              current area, else use the player's level as the benchmark
	 * @param index
	 *            TODO: switch statement determining monsterType from XML file
	 */
	public void createEnemy(int index) {
		int enemyPosX = mParams.viewWidth;

		// place enemy randomly away from player when spawning based on the
		// index and width of the screen
		enemyPosX = enemyPosX + cloudRandom.nextInt((mParams.viewWidth + 1) + 1) * index;
		EnemySprite enemy;
		if (knightSprite.getCurrentLevel() >= 5) {
			enemy = new EnemySprite(MonsterType.adventureGirl, index, enemyPosX, (int) knightYPosition, 4, 10, true,
					(int) currentArea.monsterLevel, bmpEnemy, mParams, this);
		} else {
			enemy = new EnemySprite(MonsterType.adventureGirl, index, enemyPosX, (int) knightYPosition, 4, 10, true,
					(int) knightSprite.getCurrentLevel(), bmpEnemy, mParams, this);
		}
		enemies.add(enemy);
	}

	public boolean isBmp(Object bmp) {
		if (bmp != null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @description load bitmaps into memory
	 * 
	 * @TODO: certain enemy, NPC, and tile bitmaps should be assigned to area
	 *        through leveltree XML file and appropriate resource should be
	 *        retrieved
	 */
	private void getResources(Resources res) {
		background = BitmapFactory.decodeResource(res, currentArea.mBg);
		background = Bitmap.createScaledBitmap(background, mParams.viewWidth, mParams.viewHeight, true);
		groundTile = BitmapFactory.decodeResource(res, currentArea.mFloor);
		groundTile = Bitmap.createScaledBitmap(groundTile, mParams.viewWidth, groundTile.getHeight(), true);
		groundTile2 = BitmapFactory.decodeResource(res, currentArea.mPlat);
		groundTile2 = Bitmap.createScaledBitmap(groundTile2, mParams.viewWidth, groundTile2.getHeight(), true);
		questComplete = BitmapFactory.decodeResource(res, R.drawable.questcomplete);
		quest_active = BitmapFactory.decodeResource(res, R.drawable.quest_active);
		tome = BitmapFactory.decodeResource(res, R.drawable.tome);
		// bmpNPC = BitmapFactory.decodeResource(res,
		// R.drawable.zephyr_smaller);
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 1;
		if (currentArea.bossId == -1 || (currentArea.bossId != -1 && currentArea.mId != 2)) {
			switch (currentArea.monsterType) {
			case 0:// adventureGirl
				bmpEnemy = BitmapFactory.decodeResource(res, R.drawable.agsprites_small, options);
				bmpEnemyLeft = BitmapFactory.decodeResource(res, R.drawable.agsprites_small_left, options);
				enemyDeath = BitmapFactory.decodeResource(res, R.drawable.agdeath_small, options);
				enemyDeathLeft = BitmapFactory.decodeResource(res, R.drawable.agdeath_small_left, options);
				break;
			}
		}
		healthBar = BitmapFactory.decodeResource(res, R.drawable.healthmanaportrait);
		addButton = BitmapFactory.decodeResource(res, R.drawable.add_button);
		inv = BitmapFactory.decodeResource(res, R.drawable.inv);
		spells = BitmapFactory.decodeResource(res, R.drawable.fire);
		potionSm = BitmapFactory.decodeResource(res, R.drawable.potion_sm_red);
		potionLg = BitmapFactory.decodeResource(res, R.drawable.potion_bg_red);
		emptyInventory = BitmapFactory.decodeResource(getResources(), R.drawable.pause);
		zealButton = BitmapFactory.decodeResource(getResources(), R.drawable.img_zeal);
		fireButton = BitmapFactory.decodeResource(getResources(), R.drawable.img_fire);
		lightButton = BitmapFactory.decodeResource(getResources(), R.drawable.img_light);
	}

	/**
	 * @description Initialize knight
	 */
	public void createKnight() {

		// Initialize knightSprite. Location is updated onDraw
		knightSprite = new KnightSprite((int) (knightXPosition - (knightXPosition / 4)), (int) knightYPosition, 6, 10,
				true, currentExperience, null, mParams, this, rewardsToAssign, equipped,
				pref.getBoolean("pref_inv", false), pref.getBoolean("pref_hardcore", false));

		Spell.loadSpells(knightSprite, mParams);
		// test invincibility setting
		// Log.d("pref: ", String.valueOf(pref.getBoolean("pref_inv", false)));

		// scaling bitmap for consistency across devices
		// Not sure this is necessary since the bitmap is being resized to its
		// size in pixelss t
	}

	/**
	 * @description Initialize platform and ground tile
	 */
	public void createBackground() {
		back = new Background(mParams, background, 0, 0);
		plat = new Background(mParams, groundTile2, 0, mParams.viewHeight - groundTile2.getHeight() - 55);
		bg = new Background(mParams, groundTile, 0, mParams.viewHeight - 55);
	}

	public void restartLevel() {
		Bundle options = new Bundle();
		Intent intent = new Intent();

		getContext().startActivity(intent, options);

	}

	public KnightSprite getKnight() {
		return knightSprite;
	}

	public int getKillCount() {
		return currentKillCount;
	}

	public boolean isQuestCompleted() {
		return questCompleted;
	}

	public void setQuestCompleted(boolean questCompleted) {
		this.questCompleted = questCompleted;
	}

	public void setQuestStarted(boolean b) {
		this.questStarted = b;
	}

	public boolean isQuestStarted() {
		return questStarted;
	}

	public void recycleEnemyBmps() {
		if (isBmp(bmpEnemy)) {
			bmpEnemy.recycle();
		}
		if (isBmp(bmpEnemyLeft)) {
			bmpEnemyLeft.recycle();
		}
		if (isBmp(enemyDeathLeft)) {
			enemyDeathLeft.recycle();
		}
		if (isBmp(enemyDeath)) {
			enemyDeath.recycle();
		}
		if (isBmp(idle)) {
			idle.recycle();
		}
		if (isBmp(idleL)) {
			idleL.recycle();
		}
		if (isBmp(jump)) {
			jump.recycle();
		}
		if (isBmp(jumpL)) {
			jumpL.recycle();
		}
		if (isBmp(walk)) {
			walk.recycle();
		}
		if (isBmp(walkL)) {
			walkL.recycle();
		}

	}
}
