package com.comp486.knightsrush;

import java.util.ArrayList;

import com.comp486.knightsrush.GameAct.Quest.Reward;
import com.comp486.knightsrush.Potion.Size;
import com.comp486.knightsrush.dummy.ItemBonus;
import com.comp486.knightsrush.dummy.Items.Item;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

public class KnightSprite extends Sprite {
	/*
	 * http://gamedev.stackexchange.com/questions/55151/rpg-logarithmic-leveling
	 * -formula log equation constants Used graphing calculator @
	 * http://www.desmos.com/calculator To refine constants to slow levelling as
	 * player level increases If the level gap rises too fast for your taste
	 * increase constA, decrease constB if you want the initial level gap to be
	 * higher, and finally set constC ~= exp((1-constB)/constA), in order to
	 * properly start at level 1. Formula: Level = constA * log( XP + constC ) +
	 * constB Formula: Experience = (Euler's constant) raised to the power of
	 * ((Level - constB)/constA) - constC)
	 */
	private static final double constA = 8.7;
	private static final double constB = -70;
	private static final double constC = 3501.28;
	// Base Character Statistics
	protected int BASE_DMG_MIN = 5;
	protected int BASE_DMG_MAX = 10;
	protected int BASE_DEX = 25;
	protected int BASE_STR = 25;
	protected int BASE_VIT = 10;
	protected int BASE_HP = 50;
	protected int BASE_ENERGY = 10;
	protected int BASE_MANA = 50;
	private int BASE_DEFENSE = 0;
	private float BASE_ML = 0;
	// Base mana recovery per second
	protected int BASE_MANA_RECOVERY = 2;
	protected float BASE_LL = 0;
	protected int BASE_MOVEX = 800;
	protected int BASE_MOVEY = -900;
	// Ground level at 40 pixels
	protected int GROUND = 40;
	// Player actions
	private boolean isAttacking;
	private boolean isJumping;
	private boolean isRunning;
	private boolean isMoving;
	private boolean isFalling;
	private boolean isDead;
	protected boolean isOnGround;

	// settings
	private boolean isInvincible = false;
	private boolean isHardcore = false;

	protected boolean hasLL;
	protected boolean hasML;
	public boolean isHit;
	// default level 1
	protected double level = 1;
	// jump direction and angle
	private int jumpY;
	protected double jumpAngle;
	// jump variables for jump math
	protected float jumpHeight = 500;
	protected float jumpSpeedLimit = 25;
	protected float jumpSpeed = jumpSpeedLimit;

	protected int skillPointsToUse = 0;
	protected int statPointsToUse = 0;
	protected double experience;
	private int currentHealth;
	private int currentMana;
	protected int addedStr = 0;
	protected int addedDex = 0;
	protected int addedVit = 0;
	protected int addedEnergy = 0;
	protected int strength;
	protected int dexterity;
	protected int energy;
	protected int vitality;
	protected int velocityY;
	protected int velocityX;
	// This is bad to carry around the view
	// TODO: find better way to access bitmaps currently used by view
	public GameView mGv;
	// Unused - hitbox adjustment based on attack
	// protected Rect hitbox = new Rect(0, 0, 0, 0);
	// protected Rect currentHitBox = new Rect(0, 0, 0, 0);
	// list of acquired rewards
	protected ArrayList<Reward> rewards = new ArrayList<Reward>();

	// list of equipped items
	protected ArrayList<Item> equipped = new ArrayList<Item>();

	// frameTodraw used to determine place on spritesheet to draw
	protected Rect frameToDraw = new Rect(0, 0, 0, 0);

	// whereToDraw determines place on screen to draw frame
	protected RectF whereToDraw = new RectF(0, 0, 0, 0);

	// collision bounds
	protected Rect bounds = new Rect(0, 0, 0, 0);

	// current frame
	protected int currentFrame;

	// comparing last frame change against frame length in milliseconds
	protected long lastFrameChangeTime;
	protected long lastRegenTime;
	protected long frameLengthInMilliseconds = 50;

	// does the current animation loop or not - only death animation does not
	// loop
	protected boolean loop;

	// Timing stuff to determine respawn and availability of jump command
	private long lastDeathTime = 0;
	protected long lastJumpTime = 0;
	protected long lastSpellTime = 0;

	// Sprite sheets
	public Bitmap bitmapknight, bitmapknightLeft, knightDeath, knightDeathLeft;
	public int addedZeal, addedFB, addedLS;

	public ArrayList<Spell> spells = new ArrayList<Spell>(3);
	private boolean isZealing;
	public int lastSpellCasted;

	public KnightSprite(int x, int y, int rows, int framecount, boolean vis, int exp, Bitmap spritesheet,
			ContextParameters params, GameView gv, ArrayList<Reward> rewards, ArrayList<Item> equipped,
			boolean _isInvincible, boolean _isHardcore) {
		super(x, y, rows, framecount, vis, null, params);
		mGv = gv;
		skillPointsToUse = mGv.prefs.getInt(PreferenceConstants.PREFERENCE_SKILLSTOUSE, 0);
		statPointsToUse = mGv.prefs.getInt(PreferenceConstants.PREFERENCE_STATSTOUSE, 0);
		addedDex = mGv.prefs.getInt(PreferenceConstants.PREFERENCE_ADDEDDEX, 0);
		addedStr = mGv.prefs.getInt(PreferenceConstants.PREFERENCE_ADDEDSTR, 0);
		addedVit = mGv.prefs.getInt(PreferenceConstants.PREFERENCE_ADDEDVIT, 0);
		addedEnergy = mGv.prefs.getInt(PreferenceConstants.PREFERENCE_ADDEDENERGY, 0);
		addedZeal = mGv.prefs.getInt(PreferenceConstants.PREFERENCE_ADDEDZEAL, 0);
		addedFB = mGv.prefs.getInt(PreferenceConstants.PREFERENCE_ADDEDFB, 0);
		addedLS = mGv.prefs.getInt(PreferenceConstants.PREFERENCE_ADDEDLS, 0);
		setResources();
		isInvincible = _isInvincible;
		isHardcore = _isHardcore;
		experience = exp;
		this.rewards = rewards;
		if (rewards != null) {
			assignRewards();
		}
		this.equipped = equipped;
		if (equipped != null) {
			assignItemBonus();
		}
		spritesheet = bitmapknight;
		reset(spritesheet, rows);
	}

	public KnightSprite() {

	}

	public KnightSprite(int x, int y, int rows, int framecount, boolean vis, Bitmap spritesheet,
			ContextParameters params) {
		super(x, y, rows, framecount, vis, spritesheet, params);
	}

	/*
	 * when character levels up add stat points and update preferences
	 */
	public void levelUp() {
		statPointsToUse += 5;
		skillPointsToUse += 1;
		if (mGv.editor == null) {
			mGv.editor = mGv.prefs.edit();
		}
		mGv.editor.putInt(PreferenceConstants.PREFERENCE_STATSTOUSE, statPointsToUse);
		mGv.editor.putInt(PreferenceConstants.PREFERENCE_SKILLSTOUSE, skillPointsToUse);
		mGv.editor.commit();
		mGv.editor = null;
	}

	public void updateStatPoints() {
		statPointsToUse -= 1;
		if (mGv.editor == null) {
			mGv.editor = mGv.prefs.edit();
		}
		mGv.editor.putInt(PreferenceConstants.PREFERENCE_STATSTOUSE, statPointsToUse);
		mGv.editor.commit();
		mGv.editor = null;
	}

	public void addStrPoints() {
		addedStr += 1;
		strength += 1;
		updateStatPoints();
		if (mGv.editor == null) {
			mGv.editor = mGv.prefs.edit();
		}
		mGv.editor.putInt(PreferenceConstants.PREFERENCE_ADDEDSTR, addedStr);
		mGv.editor.commit();
		mGv.editor = null;
	}

	public void addVitPoints() {
		addedVit += 1;
		vitality += 1;
		life = getBaseHealth();
		updateStatPoints();
		if (mGv.editor == null) {
			mGv.editor = mGv.prefs.edit();
		}
		mGv.editor.putInt(PreferenceConstants.PREFERENCE_ADDEDVIT, addedVit);
		mGv.editor.commit();
		mGv.editor = null;
	}

	public void addEnergyPoints() {
		addedEnergy += 1;
		energy += 1;
		mana = getBaseMana();
		updateStatPoints();
		if (mGv.editor == null) {
			mGv.editor = mGv.prefs.edit();
		}
		mGv.editor.putInt(PreferenceConstants.PREFERENCE_ADDEDENERGY, addedEnergy);
		mGv.editor.commit();
		mGv.editor = null;
	}

	public void addDexPoints() {
		addedDex += 1;
		dexterity += 1;
		updateStatPoints();
		if (mGv.editor == null) {
			mGv.editor = mGv.prefs.edit();
		}
		mGv.editor.putInt(PreferenceConstants.PREFERENCE_ADDEDDEX, addedDex);
		mGv.editor.commit();
		mGv.editor = null;
	}

	public void updateSkillPoints() {
		skillPointsToUse -= 1;
		if (mGv.editor == null) {
			mGv.editor = mGv.prefs.edit();
		}
		mGv.editor.putInt(PreferenceConstants.PREFERENCE_SKILLSTOUSE, skillPointsToUse);
		mGv.editor.commit();
		mGv.editor = null;
	}

	public void setResources() {
		final Resources res = mGv.getResources();
		Runtime.getRuntime().gc();
		bitmapknight = BitmapFactory.decodeResource(res, R.drawable.sprites_small);
		bitmapknightLeft = BitmapFactory.decodeResource(res, R.drawable.sprites_small_left);
		knightDeath = BitmapFactory.decodeResource(res, R.drawable.knight_dead);
		knightDeathLeft = BitmapFactory.decodeResource(res, R.drawable.knight_dead_left);
	}

	public void addZealPoints() {
		addedZeal += 1;
		updateSkillPoints();
		if (mGv.editor == null) {
			mGv.editor = mGv.prefs.edit();
		}
		mGv.editor.putInt(PreferenceConstants.PREFERENCE_ADDEDZEAL, addedZeal);
		mGv.editor.commit();
		mGv.editor = null;

	}

	public void addFirePoints() {
		addedFB += 1;
		updateSkillPoints();
		if (mGv.editor == null) {
			mGv.editor = mGv.prefs.edit();
		}
		mGv.editor.putInt(PreferenceConstants.PREFERENCE_ADDEDFB, addedFB);
		mGv.editor.commit();
		mGv.editor = null;

	}

	public void addLightPoints() {
		addedLS += 1;
		updateSkillPoints();
		if (mGv.editor == null) {
			mGv.editor = mGv.prefs.edit();
		}
		mGv.editor.putInt(PreferenceConstants.PREFERENCE_ADDEDLS, addedLS);
		mGv.editor.commit();
		mGv.editor = null;

	}

	public int getBaseHealth() {
		return BASE_HP + vitality;
	}

	public int getBaseMana() {
		return BASE_MANA + energy;
	}

	/**
	 * @description Get invincibility setting
	 */
	public boolean isInvincible() {
		return isInvincible;
	}

	/**
	 * @description Get hardcore setting
	 */
	public boolean isHardcore() {
		return isHardcore;
	}

	/**
	 * reset all actions
	 */
	public void resetActionStatus() {
		isAttacking = false;
		// isJumping = false;
		isMoving = false;
		isRunning = false;
		isOnGround = false;
		isFalling = false;
	}

	public void setDead(Bitmap spritesheet, int rows) {
		resetActionStatus();
		isDead = true;
		// lose ten percent of experience when you die but don't go below
		// experience required for current level
		// TODO: more sophisticated experience loss based on current difficulty
		// and player level
		if (experience - (experience * 0.1) > getExpByLevel((int) getCurrentLevel())) {
			experience -= experience * 0.1;
		} else {
			experience = getExpByLevel((int) getCurrentLevel());
		}
		currentHealth = 0;
		if (loop) {
			loop = false;
		}
		currentFrame = 0;
		framecount = 10;
		lastFrameChangeTime = 0;
		lastRegenTime = 0;

		setSheet(spritesheet, rows);

	}

	public final float getCenteredPositionX() {
		return mPosition.x + (width / 2.0f);
	}

	public final float getCenteredPositionY() {
		return mPosition.y + (height / 2.0f);
	}

	public void setJumpSpeed(float mJumpSpeed) {
		jumpSpeed = mJumpSpeed;
	}

	/**
	 * This method will be called when items are replaced/changed on the
	 * character cia the inventory screen
	 */
	public void resetBaseStats() {

		BASE_DMG_MIN = 5;
		BASE_DMG_MAX = 10;
		BASE_DEX = 25;
		BASE_STR = 25;
		BASE_VIT = 10;
		BASE_HP = 50;
		BASE_ENERGY = 10;
		BASE_MANA = 50;
		BASE_DEFENSE = 0;
		BASE_ML = 0;
		// Base mana recovery per second
		BASE_MANA_RECOVERY = 2;
		BASE_LL = 0;

		if (rewards != null) {
			assignRewards();
		}

		if (equipped != null) {
			assignItemBonus();
		}
		strength = BASE_STR + addedStr;
		dexterity = BASE_DEX + addedDex;
		vitality = BASE_VIT + addedVit;
		energy = BASE_ENERGY + addedEnergy;
		life = getBaseHealth();
		mana = getBaseMana();
		currentMana = mana;
		currentHealth = life;
	}

	public void reset(Bitmap sheet, int r) {
		resetActionStatus();
		isDead = false;
		mPosition.set(x, y);
		jumpAngle = 1;
		level = experience <= 0 ? 1 : getCurrentLevel();
		if (this instanceof KnightSprite) {
			strength = BASE_STR + addedStr + (int) (level / 2);
			dexterity = BASE_DEX + addedDex + ((int) level / 2);
			vitality = BASE_VIT + addedVit + (int) level;
			energy = BASE_ENERGY + addedEnergy + (int) level;
			team = Team.PLAYER;
		} else {
			team = Team.ENEMY;
		}
		life = getBaseHealth();
		mana = getBaseMana();
		currentMana = mana;
		currentHealth = life;
		velocityX = BASE_MOVEX;
		velocityY = BASE_MOVEY;
		jumpY = 0;
		loop = true;
		spritesheet = sheet;
		rows = r;
		currentFrame = 0;
		lastFrameChangeTime = 0;
		lastRegenTime = 0;
		mCurrentAction = ActionType.IDLE;
		setFrameWidth(spritesheet.getWidth());
		setFrameHeight(spritesheet.getHeight());
	}

	public boolean canWeWear(int requiredStr, int requiredDex) {

		if (strength >= requiredStr && dexterity >= requiredDex) {
			return true;
		} else {
			return false;
		}
	}

	public void update(long timedelta) {

		int platSpeed = getXVelocity() / 100;
		int groundSpeed = getXVelocity() / 60;
		if (isHardcore() && lastDeathTime > 0) {
			// wait two seconds after death
			if (System.currentTimeMillis() - lastDeathTime > 2000) {

				// restart game by finishing current task and starting new main
				// menu activity, while clearing the old main menu, passing the
				// newGame intent extra to wupe away preferences before setting
				// up the splash screen
				mGv.main.finish();
				Intent i = new Intent(mGv.main, MainMenuActivity.class);
				i.putExtra("newGame", true);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
				mGv.main.startActivity(i);
			}
		} else if (!isDead()) {
			if (jumpSpeed > 0) {
				// Only need to check floor collisions when falling
				checkFloorCollisions(mGv.plat);
			}

			// set bitmap according to facingDirection
			if (facingDirection.x == -1) {
				setSheet(bitmapknightLeft, 6);
			} else {
				setSheet(bitmapknight, 6);
			}

			handleMovementX(timedelta, groundSpeed, platSpeed);
			handleJump(platSpeed, groundSpeed);
			// handle knight damage on the enemy
			// player does damage on the seventh frame of attack animation

		} else {

			// we died! set death animation and lastDeathTime
			final long time = System.currentTimeMillis();
			mGv.currentExperience = (int) experience;
			if (getLastDeathTime() == 0) {
				if (facingDirection.x == 1) {
					setDead(knightDeath, 1);
				} else {
					setDead(knightDeathLeft, 1);
				}
				setLastDeathTime(time);
			}
			// respawn after 5 seconds
			if (time - lastDeathTime >= 5000 && lastDeathTime != 0) {

				// respawn enemies and potions
				mGv.enemies.clear();
				mGv.potions.clear();

				reset(bitmapknight, 6);
				setLastDeathTime(0);
				setDead(false);

				// if quest is active reset kill count and update distance
				// walked
				if (mGv.currentQuest != null && mGv.currentQuest.inProcess) {
					mGv.currentKillCount = 0;
					for (Item item : mGv.droppedItems) {
						item.update(
								mGv.distanceWalked - (mGv.currentQuest.distanceWalked - mGv.currentQuest.disactRange));
					}
					mGv.distanceWalked = mGv.currentQuest.distanceWalked - mGv.currentQuest.disactRange;
				}

				if (mGv.boss == null) {
					// spawn number of enemies based on difficulty
					switch (mGv.mParams.difficulty) {
					case 0:
						mGv.createEnemies(10);
						break;
					case DifficultyConstants.NORMAL:
						mGv.createEnemies(10);
						break;
					case DifficultyConstants.NIGHTMARE:
						mGv.createEnemies(15);
						break;
					case DifficultyConstants.HELL:
						mGv.createEnemies(25);
						break;
					}
				}
			} else {
				if (jumpSpeed > 0) {
					checkFloorCollisionsDeath();
				}
				handleMovementX(timedelta, groundSpeed, platSpeed);
				handleJump(platSpeed, groundSpeed);
			}
		}
		setBounds();
		frameToDraw = getCurrentFrame();
	}

	public void handleMovementX(long timedelta, int groundSpeed, int platSpeed) {
		// If knight is moving (the player is touching the screen)
		// then move him to the right based on his target speed and the
		if (isMoving() || isJumping()) {
			if (facingDirection.x == 1) {
				platSpeed = -platSpeed;
				groundSpeed = -groundSpeed;
			}
			if (!isAttacking()) {
				mGv.back.update((int) Math.floor((platSpeed - 2) * jumpAngle));
				mGv.plat.update((int) Math.floor(platSpeed * jumpAngle));
				mGv.bg.update((int) Math.floor(groundSpeed * jumpAngle));
				movePotions((int) Math.floor(groundSpeed * jumpAngle), (int) Math.floor(platSpeed * jumpAngle));
				moveDroppedItems((int) Math.floor(groundSpeed * jumpAngle), (int) Math.floor(platSpeed * jumpAngle));
				moveEnemies((int) Math.floor(groundSpeed * jumpAngle), (int) Math.floor(platSpeed * jumpAngle));
				// moveSpells((int) Math.floor(groundSpeed * jumpAngle), (int)
				// Math.floor(platSpeed * jumpAngle));
				// Update Distance Walked in Either Direction
				// will be used to spawn monsters randomly
				// and to determine various checkpoints
				mGv.distanceWalked += -groundSpeed * jumpAngle;
			}
		}
	}

	/**
	 * Move lastSpellCasted based on character movement and position of the
	 * spell and only runs if the current spell is visible
	 * 
	 * @param gs
	 * @param ps
	 */
	private void moveSpells(int gs, int ps) {
		if (lastSpellCasted > 0) {
			final Spell spell = spells.get(lastSpellCasted);
			if (spell.isVisible()) {
				if (spell.mPosition.y >= mParams.viewHeight - mGv.groundTile2.getHeight() - 40) {
					spell.mPosition.set(mPosition.x + gs, mPosition.y);
				} else {
					spell.mPosition.set(mPosition.x + ps, mPosition.y);
				}
			}
		}
	}

	/**
	 * @description This method determines whether a quest is in process and
	 *              which type of quest is currently active. We currently only
	 *              have one quest type, killing monsters. Additional quest
	 *              types cand be added to the switch statement.
	 */
	public void handleKillQuest() {
		// update kill count for quest of monster killing type
		if (mGv.currentQuest != null && mGv.currentQuest.inProcess) {
			switch (mGv.currentQuest.type) {
			case QuestConstants.QC_TYPE_MONSTER_KILL:
				if (mGv.currentQuest.threshold > mGv.currentKillCount) {
					// update the current kill count which is reset when quest
					// was started
					mGv.currentKillCount += 1;
				} else {
					// run game flow event BEAT QUEST
					mGv.onGameFlowEvent(GameFlowEvent.EVENT_BEAT_QUEST, 0);
				}
				break;

			}
		}
	}

	/**
	 * @description Method to handle experience gain. if the player's level is
	 *              greater after the kill, a game flow event is triggered.
	 * @formula experience += (100*(enemy level/4))
	 * @param enemy
	 */
	public void handleExperience(EnemySprite enemy) {
		final double level = getCurrentLevel();

		// experience gained Formula
		// New Experience = Old Experience + (100 * (1/4 of
		// monster level
		setExperience(getExperience() + (100 * (enemy.getCurrentLevel() * 0.25)));
		// knight.setLevel(level);
		int levelAfter = (int) getCurrentLevel();
		if (levelAfter > (int) level) {
			/**
			 * 
			 * TODO Add UI update when character levels final project will also
			 * handle adding skills and stat points for the player
			 */
			mGv.onGameFlowEvent(GameFlowEvent.EVENT_LEVELLED, levelAfter);
		}
		// update current experience for UI
		mGv.currentExperience = (int) getExperience();
	}

	/**
	 * @description Method that determines damage output and returns an integer.
	 *              get and setter.
	 * @return (int) Random.nextInt(between min and max damage) + strength/2
	 */
	public int getDamage() {
		final float damage = (mGv.cloudRandom.nextInt(BASE_DMG_MAX) + BASE_DMG_MIN) + (int) ((double) strength * 0.5F);
		if (isZealing) {
			return (int) Math.floor(damage * (0.5F + ((float) addedZeal / 100F)));
		} else {
			return (int) Math.floor(damage);
		}
	}

	/**
	 * @description Method that you call to move potions when character moves.
	 * @param gs
	 * @param ps
	 */
	public void movePotions(int gs, int ps) {
		for (Potion potion : mGv.potions) {
			if (potion.isVisible()) {
				if (potion.getPosition().y >= mParams.viewHeight - 40) {
					potion.update(gs, this);
				} else {
					potion.update(ps, this);
				}
			} else {
				potion.mPosition.set(-1000, -1000);
			}
		}
	}

	/**
	 * @description Method that you call to move droped items when character
	 *              moves.
	 * @param gs
	 * @param ps
	 */
	public void moveDroppedItems(int gs, int ps) {

		for (Item item : mGv.droppedItems) {
			if (item.getPosition().y >= mParams.viewHeight - mGv.groundTile2.getHeight() - 40) {
				item.update(gs, this);
			} else {
				item.update(ps, this);
			}
		}
	}

	/**
	 * @description player jump handler.
	 * @param platSpeed
	 * @param groundSpeed
	 */
	private void handleJump(int platSpeed, int groundSpeed) {
		if (isJumping()) {
			if (facingDirection.x == 1) {
				platSpeed = -platSpeed;
				groundSpeed = -groundSpeed;
			}
			// Log.d("JumpSpeed", String.valueOf(jumpSpeed));
			if (jumpSpeed < 0) {
				jumpSpeed *= 1F - jumpSpeedLimit / jumpHeight;
				if (jumpSpeed > -jumpSpeedLimit / 5F) {
					jumpSpeed *= -1F;
				}
			}
			if (jumpSpeed > 0 && jumpSpeed <= jumpSpeedLimit) {
				jumpSpeed *= 1F + jumpSpeedLimit / 50F;
			}
			// handleJumpAngle(groundSpeed, platSpeed);
			mPosition.y += jumpSpeed;
			setOnGround(false);
			// setFalling(true);
			// }
		}

	}

	/**
	 * @description move enemies when player is moving
	 * @param gs
	 *            groundspeed
	 * @param ps
	 *            platformspeed
	 */
	public void moveEnemies(int gs, int ps) {
		for (EnemySprite enemy : mGv.enemies) {
			// move enemies
			if (enemy.getPosition().y >= mParams.viewHeight - enemy.height - mGv.groundTile2.getHeight() - 40) {
				enemy.getPosition().set(enemy.getPosition().x + gs, enemy.getPosition().y);
			} else {
				enemy.getPosition().set(enemy.getPosition().x + ps, enemy.getPosition().y);
			}
		}
		if (mGv.boss != null) {
			if (mGv.boss.getPosition().y + mGv.boss.getFrameHeight() >= mParams.viewHeight - mGv.groundTile2.getHeight()
					- 40) {
				mGv.boss.getPosition().set(mGv.boss.getPosition().x + gs, mGv.boss.getPosition().y);
			} else {
				mGv.boss.getPosition().set(mGv.boss.getPosition().x + ps, mGv.boss.getPosition().y);
			}
		}
	}

	/**
	 * @description Attack Speed method, we reduce time to complete frame by
	 *              dexterity/5 5 dexterity points increased attack speed by 1
	 *              millisecond/frame
	 * 
	 * @return player attack speed
	 */
	public long getAttackSpeed() {
		// Log.d("attackspeed", String.valueOf((float)
		// (frameLengthInMilliseconds - (dexterity / 5) *
		// 1F)/frameLengthInMilliseconds));
		if (isZealing) {
			return (long) (frameLengthInMilliseconds / 2F);
		} else {
			return (long) (frameLengthInMilliseconds - (((float) dexterity / 20F) * 1F));
		}
	}

	public void setJumpY(int i) {
		jumpY = i;

	}

	public int getJumpY() {
		return jumpY;

	}

	public int getYVelocity() {

		return velocityY;
	}

	public int getCurrentMana() {
		return currentMana;
	}

	public void setStartingMana(int mana) {
		currentMana = mana;
	}

	/**
	 * @description reduce or add to the player's current mana will determine if
	 *              the addition is more than the max mana of player
	 * @param mana
	 */
	public void setCurrentMana(int mana) {
		if (mana > 0) {
			currentMana = currentMana + mana >= this.mana ? this.mana : currentMana + mana;
		} else {
			currentMana = currentMana + mana < 0 ? 0 : currentMana + mana;
		}
	}

	public int getCurrentHealth() {
		return currentHealth;
	}

	public void setStartingHealth(int health) {
		currentHealth = health;
	}

	/**
	 * @description reduce or add to the player's current health will determine
	 *              if the addition is more than the max health of player
	 * @param health
	 */
	public void setCurrentHealth(int health) {
		currentHealth = currentHealth + health >= life ? life : currentHealth + health;
	}

	/**
	 * 
	 * @return life/max health
	 */
	public int getLife() {
		return life;
	}

	/**
	 * @description set max health to the input
	 * @param health
	 */
	public void setLife(int health) {
		life = health;
	}

	/**
	 * @description draw the knight UI with our paint objects and draw the
	 *              knight. All the logic is taken care of in the update()
	 *              method to determine where to place the player.
	 * @param timedelta
	 * @param canvas
	 * @param paint
	 * @param paintOpac
	 */
	public void draw(long timedelta, Canvas canvas, Paint paint, Paint paintOpac) {
		// TODO: Calculate image into inches based on DPI and convert to pixels
		// for consistent UI display across devices
		// FORMULA Inches = pixels / DPI --> Pixels = Inches * DPI
		// Make the text a bit bigger
		if (mGv.playing && spritesheet != null && !spritesheet.isRecycled()) {
			paint.setTextSize(40);
			// use stroke of particular color for health and experience bar
			paint.setStrokeWidth(50);
			final float liferatio = (float) currentHealth / (float) life;
			final float manaratio = (float) currentMana / (float) mana;
			final double expToNextLvl = getExpByLevel((int) (getCurrentLevel() + 1));
			final double expToLvl = getExpByLevel((int) (getCurrentLevel()));

			// Ugly magic numbers. this could be cleaned up
			canvas.drawLine(265, 50, 265 + ((liferatio) * 460), 50, paint);
			// blue
			paint.setColor(Color.rgb(0, 0, 153));
			canvas.drawLine(265, 105, 265 + ((manaratio) * 460), 105, paint);
			// brown/yellow
			paint.setColor(Color.rgb(199, 195, 142));
			canvas.drawLine(265, 155F,
					(float) (725 - (((expToNextLvl - experience) / (expToNextLvl - expToLvl)) * (460))), 155F, paint);

			paint.setColor(Color.WHITE);
			paint.setFakeBoldText(true);
			canvas.drawBitmap(mGv.healthBar, 0, 0, paint);
			canvas.drawText((int) getCurrentLevel() + "", 118, mGv.healthBar.getHeight() - 40, paint);
			paint.setColor(Color.BLACK);
			canvas.drawText("Distance Walked:" + mGv.distanceWalked, 20, 340, paint);
			canvas.drawText((int) getExperience() + " / " + (int) expToNextLvl, 300, 160, paint);
			paint.setColor(Color.rgb(128, 0, 0));
			if (skillPointsToUse > 0 || statPointsToUse > 0) {
				canvas.drawBitmap(mGv.tome, 0, 335, paint);
			}
			if (skillPointsToUse > 0) {
				canvas.drawText("You have " + skillPointsToUse + " Skill Points Available", mGv.tome.getWidth(), 410,
						paint);
			}
			if (statPointsToUse > 0) {
				canvas.drawText("You have " + statPointsToUse + " Stat Points Available", mGv.tome.getWidth(), 460,
						paint);
			}
		}
		whereToDraw.set(getPosition().x, getPosition().y - height, getPosition().x + width, getPosition().y);
		paint.setStrokeWidth(1);

		/*
		 * // hit box test lines
		 * 
		 * canvas.drawLine(bounds.left, bounds.top, bounds.right, bounds.top,
		 * paint); canvas.drawLine(bounds.left, bounds.bottom, bounds.right,
		 * bounds.top, paint);
		 */
		// Draw knight
		if (spritesheet != null && !spritesheet.isRecycled()) {
			if (!isHit) {
				canvas.drawBitmap(spritesheet, frameToDraw, whereToDraw, paint);
			} else {
				canvas.drawBitmap(spritesheet, frameToDraw, whereToDraw, paintOpac);
				setHit(false);
			}
		}
	}

	public void setSheet(Bitmap sheet, int rows) {
		spritesheet = sheet;
		this.rows = rows;
		setFrameWidth(sheet.getWidth());
		setFrameHeight(sheet.getHeight());
	}

	protected void setFrameHeight(int i) {
		height = i / rows;

	}

	public void setFrameWidth(int i) {
		width = i / framecount;

	}

	@Override
	public int getFrameHeight() {
		return height;
	}

	@Override
	public int getFrameWidth() {
		return width;
	}

	/**
	 * @description Rearranged levelling formula to get experience for the
	 *              current player level
	 * @formula e (Euler's constant) raised to the power of ((Level -
	 *          constB)/constA) - constC)
	 */

	public static double getExpByLevel(int lvl) {
		return (Math.exp((lvl - constB) / constA) - constC);
	}

	/**
	 * 
	 * @description Use levelling formula to get current player level based on
	 *              experience Math.max is used to default to level 1 if below
	 *              level 1 experience
	 * @formula Math.floor(constA * log(experience + constC) + constB)
	 */
	public double getCurrentLevel() {
		return Math.max(Math.floor(constA * Math.log((double) experience + constC) + constB), 1);
	}

	/*
	 * public double solveQuadratic(int experience) {
	 * 
	 * final double root1, root2; root1 = (-360 + Math.sqrt(Math.pow(360, 2) - 4
	 * * 40 * -experience)) / (2 * 360); root2 = (-360 - Math.sqrt(Math.pow(360,
	 * 2) - 4 * 40 * -experience)) / (2 * 360); return Math.max(root1, root2); }
	 */
	public double getExperience() {
		return experience;
	}

	public void setExperience(double exp) {
		experience = exp;
	}

	/**
	 * Gets current frame by integer
	 * 
	 * @return (int) currentFrame
	 */
	public int getFrameInt() {
		return currentFrame;
	}

	/**
	 * @description Gets the Rectangle location of the frame we want to draw.
	 *              This method can be overriden to accompany different
	 *              spreadsheets
	 * @return (Rect) frameToDraw
	 */
	public Rect getCurrentFrame() {
		final ActionType currAnimation;
		long time = System.currentTimeMillis();
		// frame rate is calculated based on attack speed
		if (time > lastFrameChangeTime + getAttackSpeed()) {

			// update current frame and lastFrameCahngeTime
			lastFrameChangeTime = time;
			currentFrame++;

			// if looping restart animation
			if (currentFrame >= framecount && loop) {
				currentFrame = 0;
			}

			// if looping is false stop at last frame
			if (currentFrame >= framecount & !loop) {
				currentFrame = framecount - 1;
			}
		}
		// recover mana every second
		if (time >= lastRegenTime + 1000) {
			lastRegenTime = time;
			setCurrentMana(BASE_MANA_RECOVERY);
			if (isZealing) {
				setCurrentMana(-spells.get(0).manaCost);
			}
		}
		// Determine animation based on character's current state
		// Run/Walk
		if (isMoving() && !isJumping()) {
			if (isRunning()) {

				// set run speed
				setXVelocity(900);
				currAnimation = ActionType.RUN;
			} else {
				currAnimation = ActionType.MOVE;
			}
		} // Jump&&Attack
		else if (isJumping() && isAttacking()) {
			currAnimation = ActionType.JATTACK;
		} // Hack for enemies to show Jump animation because they move both x
			// and y when jumping
		else if (isMoving() && isJumping()) {
			currAnimation = ActionType.JUMP;

		} // Attack
		else if (isAttacking()) {
			currAnimation = ActionType.ATTACK;
		} // Jump
		else if (isJumping() && !isDead()) {
			currAnimation = ActionType.JUMP;

		} // if not any of the other options must be idle or death
		else {
			if (isDead()) {
				currAnimation = ActionType.DEATH;
			} else {
				currAnimation = ActionType.IDLE;
			}
		}
		// set current top bottom of frame
		setAnimation(currAnimation);

		return frameToDraw;
	}

	/**
	 * @description set the animation by finding the location on spritesheet
	 * @param animation
	 */
	public void setAnimation(ActionType animation) {

		int top;
		int bottom;
		setCurrentAction(animation);
		switch (mCurrentAction) {
		case ATTACK:
			top = 0;
			bottom = height;
			break;
		case JUMP:
			top = height * 4;
			bottom = height * 5;
			break;
		case MOVE:
			top = height;
			bottom = height * 2;
			break;
		case RUN:
			top = height * 2;
			bottom = height * 3;
			break;
		case JATTACK:
			top = height * 3;
			bottom = height * 4;
			break;
		case IDLE:
			top = height * 5;
			bottom = height * 6;
			break;
		case DEATH:
			top = 0;
			bottom = height;
			break;
		default:
			top = 0;
			bottom = height;
			break;
		}
		frameToDraw.top = top;
		frameToDraw.bottom = bottom;

		setAnimationLeftRight();
	}

	/**
	 * @description update the left and right values of the source of the next
	 *              frame on the spritesheet
	 * @return the frame to draw
	 */
	public Rect setAnimationLeftRight() {
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

	/**
	 * @description Getter
	 * @return (Rect) bounds
	 */
	public Rect getBounds() {
		return bounds;
	}

	/**
	 * @description Set and get current rectangular bounds based on the player's
	 *              position
	 * @return (Rect) bounds
	 */
	public Rect setBounds() {
		bounds.set((int) getPosition().x, (int) getPosition().y - height, (int) getPosition().x + width,
				(int) getPosition().y);
		return bounds;
	}

	public void setHit(boolean bool) {
		isHit = bool;
	}

	public boolean isHit() {
		return isHit;
	}

	public void setDead(boolean bool) {
		isDead = bool;
	}

	public boolean isDead() {
		return isDead;
	}

	public void setOnGround(boolean bool) {
		isOnGround = bool;
	}

	public boolean isOnGround() {
		return isOnGround;
	}

	/**
	 * @description Checking player collisions with potions.
	 * @param Rect
	 *            a
	 * @param Rect
	 *            b
	 * @param potion
	 */
	public void checkCollisions(Rect a, Rect b, Potion potion) {
		// should only run once because we set the potions visibility to false
		if (Rect.intersects(a, b) && potion.isVisible()) {

			// adjust added health based on size enum
			// use ternary statement to determine whether the addition is more
			// than max health
			// and add health accordingly
			if (potion.size == Size.SMALL) {
				currentHealth = currentHealth >= life - 50 ? life : currentHealth + 50;
			} else if (potion.size == Size.LARGE) {
				currentHealth = currentHealth >= life - 150 ? life : currentHealth + 150;
			}

			// make potion invisible and reuse on another enemy
			potion.setVisible(false);
			potion.setAssigned(false);

			for (EnemySprite enemy : mGv.enemies) {
				if (!enemy.hasPotion() && !enemy.isDead()) {
					// assign potion to enemy 30 percent of the time
					if (mGv.cloudRandom.nextInt(100) + 1 <= 30) {
						enemy.potion = potion;
						enemy.setPotion(true);
						potion.setAssigned(true);
						break;
					}
				} else {
					continue;
				}

			}

		}
	}

	/**
	 * @description Checking player collisions with dropped items.
	 * @param Recta
	 * @param Rectb
	 * @param item
	 */
	public void checkCollisions(Rect a, Rect b, final Item item) {
		// should only run once because we set the potions visibility to false
		if (Rect.intersects(a, b) && item.isVisible() && !item.hasBeenLookedAt
				&& mGv.pref.getBoolean("pref_item_on_collide", true)) {
			mGv.main.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (mGv.currentItem == null) {
						item.hasBeenLookedAt = true;
						mGv.currentItem = ItemDetailFragment.itemPickUpView(mGv.main, item,
								mGv.main.getLayoutInflater());
						mGv.thisItem = item;
					}
				}
			});

			// add to inventory

		}
	}

	/**
	 * @description check player collisions with the floor
	 * @param bg
	 */
	public void checkFloorCollisions(Background bg) {

		Rect playerRect = getBounds();
		Rect pRect;
		if (jumpY == 0) {
			pRect = bg.getBounds();
		} else {
			pRect = mGv.bg.getBounds();
		}

		if (Rect.intersects(playerRect, pRect)) {
			hitFloor(pRect, playerRect);
		}

	}

	/**
	 * @description check player collisions with the floor
	 * @param bg
	 */
	public void checkFloorCollisionsDeath() {

		final Rect playerRect = getBounds();
		final Rect pRect;
		pRect = mGv.bg.getBounds();

		if (Rect.intersects(playerRect, pRect)) {
			hitFloorDeath(pRect, playerRect);
		}

	}

	private void hitFloorDeath(Rect pRect, Rect playerRect) {

		if (playerRect.bottom >= pRect.top) {
			if (jumpSpeed > 0) {
				getPosition().set(getPosition().x, pRect.top + 15);
				setJumping(false);
				setLastJumpTime(0);
				setFalling(false);
				setOnGround(true);
				jumpAngle = 1;
			}
		}

	}

	private void hitFloor(Rect pRect, Rect playerRect) {
		if (isJumping) {
			if (playerRect.bottom >= pRect.top) {
				if (jumpSpeed > 0) {
					getPosition().set(getPosition().x, pRect.top + 15);
					setJumping(false);
					setLastJumpTime(0);
					setFalling(false);
					setOnGround(true);
					jumpAngle = 1;
				}
			}

		}

	}

	public void setLevel(double lvl) {
		level = lvl;
	}

	public void setLastDeathTime(long gameTime) {
		lastDeathTime = gameTime;

	}

	public long getLastDeathTime() {
		return lastDeathTime;

	}

	public void setFalling(boolean bool) {
		isFalling = bool;
	}

	public boolean isFalling() {
		return isFalling;
	}

	public void setAttacking(boolean bool) {
		isAttacking = bool;
	}

	public boolean isAttacking() {

		return isAttacking;
	}

	public void setYVelocity(int velocity) {
		velocityY = velocity;
	}

	public void setJumping(boolean bool) {
		isJumping = bool;
	}

	public boolean isJumping() {

		return isJumping;
	}

	public void setXVelocity(int velocity) {
		velocityX = velocity;
	}

	public int getXVelocity() {
		return velocityX;
	}

	public void setRunning(boolean bool) {
		isRunning = bool;
	}

	public boolean isRunning() {

		return isRunning;
	}

	public void setMoving(boolean bool) {
		isMoving = bool;
	}

	public boolean isMoving() {

		return isMoving;
	}

	public long getLastJumpTime() {
		return lastJumpTime;
	}

	public void setLastJumpTime(long time) {
		lastJumpTime = time;
	}

	/**
	 * Apply a reward on the fly
	 * 
	 * @param reward
	 */
	public void applyReward(Reward reward) {
		switch (reward.mType) {
		case RewardConstants.R_TYPE_HEALTH:
			// because this can be added on the fly have to add to base and
			// reset health
			int diff = life - currentHealth;
			BASE_HP += reward.mBonus;
			life = getBaseHealth();
			currentHealth = life - diff;
			break;
		case RewardConstants.R_TYPE_STRENGTH:
			BASE_STR += reward.mBonus; // add to base strength
			break;
		case RewardConstants.R_TYPE_DEXTERITY:
			BASE_DEX += reward.mBonus; // add to base dexterity
			break;
		case RewardConstants.R_TYPE_LIFE_LEECH:
			setLL(reward.mBonus);
			break;
		}
		addReward(reward);
	}

	/**
	 * Assign item bonuses on initialization
	 */
	public void assignItemBonus() {
		for (Item i : equipped) {
			for (ItemBonus ib : i.itemBonuses) {
				switch (ib.mType) {
				case ItemBonus.DEXTERITY:
					BASE_DEX += ib.mChosen;
					break;
				case ItemBonus.STRENGTH:
					BASE_STR += ib.mChosen;
					break;
				case ItemBonus.ENERGY:
					BASE_ENERGY += ib.mChosen;
					break;
				case ItemBonus.LIFELEECH_PERCENT:
					if (!hasLL) {
						hasLL = true;
					}
					setLL(ib.mChosen);
					break;
				case ItemBonus.PHYSICAL_DAMAGE_MAX:
					BASE_DMG_MAX += ib.mChosen;
					break;
				case ItemBonus.PHYSICAL_DAMAGE_MIN:
					BASE_DMG_MIN += ib.mChosen;
					break;
				case ItemBonus.PHYSICAL_DAMAGE_MAXMIN:
					BASE_DMG_MIN += ib.mChosen;
					BASE_DMG_MAX += ib.mChosen;
					break;
				case ItemBonus.VITALITY:
					BASE_VIT += ib.mChosen;
					break;
				default:
					break;

				}
			}
		}
	}

	/**
	 * Assign Rewards on initialization
	 */
	public void assignRewards() {
		for (Reward reward : rewards) {
			switch (reward.mType) {
			case RewardConstants.R_TYPE_HEALTH:
				if (reward.mIsInt == RewardConstants.R_BASE) {
					BASE_HP += reward.mBonus;
				}
				break;
			case RewardConstants.R_TYPE_STRENGTH:
				BASE_STR += reward.mBonus;
				break;
			case RewardConstants.R_TYPE_VITALITY:
				BASE_VIT += reward.mBonus;
				break;
			case RewardConstants.R_TYPE_DEXTERITY:
				BASE_DEX += reward.mBonus;
				break;
			case RewardConstants.R_TYPE_LIFE_LEECH:
				if (!hasLL) {
					hasLL = true;
				}
				setLL(reward.mBonus);
				break;
			default:
				break;

			}
		}
	}

	public void setML(float mlToAdd) {
		BASE_ML += mlToAdd / 100F;
	}

	public float getML() {
		return BASE_ML;
	}

	public void setHasML(boolean hasML) {
		this.hasML = hasML;
	}

	public boolean getHasML() {
		return hasML;
	}

	public void setLL(float llToAdd) {
		BASE_LL += llToAdd / 100F;
	}

	public float getLL() {
		return BASE_LL;
	}

	public void setHasLL(boolean hasLL) {
		this.hasLL = hasLL;
	}

	public boolean getHasLL() {
		return hasLL;
	}

	public ArrayList<Reward> getRewards() {
		return rewards;
	}

	public void addReward(Reward reward) {
		rewards.add(reward);
	}

	public void addRewardArray(ArrayList<Reward> rewards) {
		this.rewards = rewards;
	}

	public void destroy() {
		// housekeeping
		bitmapknight.recycle();
		bitmapknightLeft.recycle();
		knightDeath.recycle();
		knightDeathLeft.recycle();
		for (Spell spell : spells) {
			spell.destroy();
		}
	}

	public boolean getZeal() {
		return isZealing;

	}

	public void setZeal(boolean b) {
		isZealing = b;

	}

}
