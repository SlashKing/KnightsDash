package com.comp486.knightsrush;

import com.comp486.knightsrush.dummy.Items.Item;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class EnemySprite extends KnightSprite {
	protected int activationRadius = 1000;
	protected int jumpActivationR = 100;

	public enum MonsterType {
		adventureGirl, robot, trollGreen, trollBlue
	}

	int mId = -1;
	Potion potion;
	Item itemToDrop = null;
	public boolean isHitByKnight;
	MonsterType mType;
	private boolean hasPotion;
	protected Bitmap left, right, walk = null, walkL = null, idle = null;
	protected Bitmap idleL = null;
	protected Bitmap jump = null;
	protected Bitmap jumpL = null;
	protected Bitmap melee = null;
	protected Bitmap meleeL = null;
	protected Bitmap deathLeft;
	protected Bitmap deathRight;
	Rect bounds = new Rect(0, 0, 0, 0);

	public EnemySprite(MonsterType type, int id, int x, int y, int rows, int framecount, boolean vis, int lvl,
			Bitmap spritesheet, ContextParameters params, GameView gv) {
		super(x, y, rows, framecount, vis, spritesheet, params);
		// set base stats based on type of enemy
		// only the one enemy at the moment
		mGv = gv;
		switch (type) {
		case adventureGirl:
			mType = MonsterType.adventureGirl;
			resetGirl();
			break;
		case robot:
			mType = MonsterType.robot;
			resetRobot();
			break;
		case trollGreen:
			mType = MonsterType.trollGreen;
			resetTroll();
			break;
		case trollBlue:
			mType = MonsterType.trollBlue;
			resetTrollBlue();
			break;
		default:
			break;
		}
		getResources(type, params.context.getResources());
		if (mType == MonsterType.adventureGirl) {
			this.spritesheet = spritesheet;
			this.rows = rows;
		} else {
			this.spritesheet = idleL;
			this.rows = 1;
		}
		reset(this.spritesheet, this.rows);
		level = lvl;
		strength = BASE_STR + (lvl / 6);
		dexterity = BASE_DEX + (lvl / 4);
		vitality = BASE_VIT + lvl;
		life = BASE_HP + (vitality * 3);
		setStartingHealth(life);
		velocityX = BASE_MOVEX;
		velocityY = BASE_MOVEY;
		loop = true;
		setJumpY(0);
		mId = id;
		potion = null;
		mCurrentAction = ActionType.IDLE;
	}

	public EnemySprite(int bossid, int x, int y, int rows, int framecount, ContextParameters params, GameView gv) {
		super(x, y, rows, framecount, true, null, null);
		mGv = gv;
		mParams = params;
	}

	public void getResources(MonsterType type, Resources res) {
		switch (type) {
		case adventureGirl:
			if (!mGv.isBmp(mGv.bmpEnemyLeft)) {
				mGv.bmpEnemyLeft = BitmapFactory.decodeResource(res, R.drawable.agsprites_small_left);
				mGv.bmpEnemy = BitmapFactory.decodeResource(res, R.drawable.agsprites_small);
				mGv.enemyDeathLeft = BitmapFactory.decodeResource(res, R.drawable.agsprites_small);
				mGv.enemyDeath = BitmapFactory.decodeResource(res, R.drawable.agsprites_small);
			}
			left = mGv.bmpEnemyLeft;
			right = mGv.bmpEnemy;
			deathLeft = mGv.enemyDeathLeft;
			deathRight = mGv.enemyDeath;
			break;
		case robot:
			if (!mGv.isBmp(mGv.idle)) {
				mGv.idle = BitmapFactory.decodeResource(res, R.drawable.robot_idle);
				mGv.idleL = BitmapFactory.decodeResource(res, R.drawable.robot_idle_left);
				mGv.walk = BitmapFactory.decodeResource(res, R.drawable.robot_walk);
				mGv.walkL = BitmapFactory.decodeResource(res, R.drawable.robot_walk_left);
				mGv.jump = BitmapFactory.decodeResource(res, R.drawable.robot_jump);
				mGv.jumpL = BitmapFactory.decodeResource(res, R.drawable.robot_jump_left);
				mGv.melee = BitmapFactory.decodeResource(res, R.drawable.robot_melee);
				mGv.meleeL = BitmapFactory.decodeResource(res, R.drawable.robot_melee_left);
				mGv.enemyDeath = BitmapFactory.decodeResource(res, R.drawable.robot_dead);
				mGv.enemyDeathLeft = BitmapFactory.decodeResource(res, R.drawable.robot_dead_left);
			}
			left = mGv.idleL;
			right = mGv.idle;
			idle = mGv.idle;
			idleL = mGv.idleL;
			walk = mGv.walk;
			walkL = mGv.walkL;
			melee = mGv.melee;
			meleeL = mGv.meleeL;
			jump = mGv.jump;
			jumpL = mGv.jumpL;
			deathLeft = mGv.enemyDeathLeft;
			deathRight = mGv.enemyDeath;
			break;
		case trollGreen:
			if (!mGv.isBmp(mGv.idle)) {
				mGv.idle = BitmapFactory.decodeResource(res, R.drawable.troll_idle);
				mGv.idleL = BitmapFactory.decodeResource(res, R.drawable.troll_idle_left);
				mGv.walk = BitmapFactory.decodeResource(res, R.drawable.troll_walk);
				mGv.walkL = BitmapFactory.decodeResource(res, R.drawable.troll_walk_left);
				mGv.melee = BitmapFactory.decodeResource(res, R.drawable.troll_attack);
				mGv.meleeL = BitmapFactory.decodeResource(res, R.drawable.troll_attack_left);
				mGv.enemyDeath = BitmapFactory.decodeResource(res, R.drawable.troll_death);
				mGv.enemyDeathLeft = BitmapFactory.decodeResource(res, R.drawable.troll_death_left);
			}
			left = mGv.idleL;
			right = mGv.idle;
			idle = mGv.idle;
			idleL = mGv.idleL;
			walk = mGv.walk;
			walkL = mGv.walkL;
			melee = mGv.melee;
			meleeL = mGv.meleeL;
			deathLeft = mGv.enemyDeathLeft;
			deathRight = mGv.enemyDeath;
			//left = deathLeft;
			//right = deathRight;
			break;
		case trollBlue:
			if (!mGv.isBmp(mGv.idle)) {
				mGv.idle = BitmapFactory.decodeResource(res, R.drawable.troll_blue_idle);
				mGv.idleL = BitmapFactory.decodeResource(res, R.drawable.troll_blue_idle_left);
				mGv.walk = BitmapFactory.decodeResource(res, R.drawable.troll_blue_walk);
				mGv.walkL = BitmapFactory.decodeResource(res, R.drawable.troll_blue_walk_left);
				mGv.melee = BitmapFactory.decodeResource(res, R.drawable.troll_blue_attack);
				mGv.meleeL = BitmapFactory.decodeResource(res, R.drawable.troll_blue_attack_left);
				mGv.enemyDeath = BitmapFactory.decodeResource(res, R.drawable.troll_blue_death);
				mGv.enemyDeathLeft = BitmapFactory.decodeResource(res, R.drawable.troll_blue_death_left);
			}
			left = mGv.idleL;
			right = mGv.idle;
			idle = mGv.idle;
			idleL = mGv.idleL;
			walk = mGv.walk;
			walkL = mGv.walkL;
			melee = mGv.melee;
			meleeL = mGv.meleeL;
			deathLeft = mGv.enemyDeathLeft;
			deathRight = mGv.enemyDeath;
			//left = deathLeft;
			//right = deathRight;
			break;
		default:
			break;
		}
	}

	public void resetGirl() {
		switch (mGv.mParams.difficulty) {
		case DifficultyConstants.NORMAL:
			BASE_DMG_MIN = 2;
			BASE_DMG_MAX = 5;
			BASE_DEX = 10;
			BASE_STR = 10;
			BASE_VIT = 10;
			BASE_HP = 45;
			BASE_MOVEX = 450;
			BASE_MOVEY = -700;
			frameLengthInMilliseconds = 120;
			break;
		case DifficultyConstants.NIGHTMARE:
			BASE_DMG_MIN = 15;
			BASE_DMG_MAX = 25;
			BASE_DEX = 55;
			BASE_STR = 50;
			BASE_VIT = 75;
			BASE_HP = 150;
			BASE_MOVEX = 500;
			BASE_MOVEY = -700;
			frameLengthInMilliseconds = 110;
			break;
		case DifficultyConstants.HELL:
			BASE_DMG_MIN = 25;
			BASE_DMG_MAX = 40;
			BASE_DEX = 95;
			BASE_STR = 115;
			BASE_VIT = 100;
			BASE_HP = 250;
			BASE_MOVEX = 550;
			BASE_MOVEY = -700;
			frameLengthInMilliseconds = 90;
			break;
		}
	}

	private void resetRobot() {
		switch (mGv.mParams.difficulty) {
		case DifficultyConstants.NORMAL:
			BASE_DMG_MIN = 7;
			BASE_DMG_MAX = 15;
			BASE_DEX = 25;
			BASE_STR = 25;
			BASE_VIT = 25;
			BASE_HP = 80;
			BASE_MOVEX = 500;
			BASE_MOVEY = -800;
			frameLengthInMilliseconds = 120;
			break;
		case DifficultyConstants.NIGHTMARE:
			BASE_DMG_MIN = 20;
			BASE_DMG_MAX = 30;
			BASE_DEX = 55;
			BASE_STR = 55;
			BASE_VIT = 55;
			BASE_HP = 180;
			BASE_MOVEX = 550;
			BASE_MOVEY = -800;
			frameLengthInMilliseconds = 110;
			break;
		case DifficultyConstants.HELL:
			BASE_DMG_MIN = 40;
			BASE_DMG_MAX = 50;
			BASE_DEX = 95;
			BASE_STR = 115;
			BASE_VIT = 90;
			BASE_HP = 300;
			BASE_MOVEX = 600;
			BASE_MOVEY = -800;
			frameLengthInMilliseconds = 100;
			break;
		}
	}

	private void resetTrollBlue() {
		switch (mGv.mParams.difficulty) {
		case DifficultyConstants.NORMAL:
			BASE_DMG_MIN = 10;
			BASE_DMG_MAX = 20;
			BASE_DEX = 25;
			BASE_STR = 25;
			BASE_VIT = 25;
			BASE_HP = 70;
			BASE_MOVEX = 480;
			BASE_MOVEY = -800;
			frameLengthInMilliseconds = 100;
			break;
		case DifficultyConstants.NIGHTMARE:
			BASE_DMG_MIN = 20;
			BASE_DMG_MAX = 30;
			BASE_DEX = 65;
			BASE_STR = 65;
			BASE_VIT = 75;
			BASE_HP = 150;
			BASE_MOVEX = 550;
			BASE_MOVEY = -800;
			frameLengthInMilliseconds = 90;
			break;
		case DifficultyConstants.HELL:
			BASE_DMG_MIN = 40;
			BASE_DMG_MAX = 50;
			BASE_DEX = 95;
			BASE_STR = 115;
			BASE_VIT = 90;
			BASE_HP = 300;
			BASE_MOVEX = 600;
			BASE_MOVEY = -800;
			frameLengthInMilliseconds = 80;
			break;
		}

	}

	private void resetTroll() {
		switch (mGv.mParams.difficulty) {
		case DifficultyConstants.NORMAL:
			BASE_DMG_MIN = 9;
			BASE_DMG_MAX = 16;
			BASE_DEX = 25;
			BASE_STR = 25;
			BASE_VIT = 25;
			BASE_HP = 80;
			BASE_MOVEX = 460;
			BASE_MOVEY = -800;
			frameLengthInMilliseconds = 125;
			break;
		case DifficultyConstants.NIGHTMARE:
			BASE_DMG_MIN = 20;
			BASE_DMG_MAX = 30;
			BASE_DEX = 55;
			BASE_STR = 55;
			BASE_VIT = 55;
			BASE_HP = 180;
			BASE_MOVEX = 500;
			BASE_MOVEY = -800;
			frameLengthInMilliseconds = 110;
			break;
		case DifficultyConstants.HELL:
			BASE_DMG_MIN = 40;
			BASE_DMG_MAX = 50;
			BASE_DEX = 85;
			BASE_STR = 95;
			BASE_VIT = 90;
			BASE_HP = 300;
			BASE_MOVEX = 540;
			BASE_MOVEY = -800;
			frameLengthInMilliseconds = 100;
			break;
		}

	}

	@Override
	public void draw(long timedelta, Canvas canvas, Paint paint, Paint paintOpac) {
		if (isVisible()) {
			paint.setStrokeWidth(25);
			if (getCurrentHealth() >= 0) {
				final float enemyhb = (float) getCurrentHealth() / (float) life;
				canvas.drawLine(getPosition().x, getPosition().y, (getPosition().x + ((enemyhb) * (width))),
						getPosition().y, paint);
			}
		}

		whereToDraw.set(getPosition().x, getPosition().y - height, getPosition().x + width, getPosition().y);
		if (spritesheet != null) {
			canvas.drawBitmap(spritesheet, frameToDraw, whereToDraw, paint);
		}
	}

	public Potion getPotion() {
		return potion;
	}

	public void setPotion(Potion potion) {
		this.potion = potion;
	}

	public void update(long timedelta, KnightSprite knight) {

		// Initialize common variables
		final Vector2 pos = getPosition();
		final int frameWidth = knight.getFrameWidth();

		// X and Y difference between knight and enemy
		float diff = pos.x - knight.getPosition().x;
		float diffy = pos.y - knight.getPosition().y;

		// using a variable called jumpY to determine whether the user wants to
		// jump up or down
		// helps to determine collision conditions with platform and ground

		/*
		 * ** AI*** Monsters will follow user around Need to set jumpY both ifs
		 * test whether the knight is in range if #1 : If enemy is above if #2 :
		 * If enemy is below
		 */
		if (!isDead() && !knight.isDead()) {
			// Can't bonk when jumping so only detect floor collisions when
			// falling
			if (jumpSpeed > 0) {
				checkFloorCollisions(mGv.plat);
			}
			bounds = setBounds();

			final Spell spell = knight.spells.get(knight.lastSpellCasted);
			if (spell.isMoving()) {
				spell.checkCollisionsWithEnemy(getBounds(), spell.whereToDraw, this, knight);
			}
			// if it's a boss we need to check what the current animation is and
			// set the correct spritesheet
			if (this instanceof Boss) {
				mGv.boss.testSheet();
			}
			if (diffy >= jumpActivationR && !isJumping() && (diff > -activationRadius && diff < activationRadius)) {
				setJumpY(knight.getJumpY());
				setMoving(true);
				setJumping(true);
				setJumpSpeed(-jumpSpeedLimit);
			} else if (diffy <= -jumpActivationR && !isJumping()
					&& (diff > -activationRadius && diff < activationRadius)) {
				setJumpY(knight.getJumpY());
				setMoving(true);
				setJumping(true);
				setJumpSpeed(-jumpSpeedLimit);
			}

			// knight is in range but not close enough to attack
			// and on right side of knight
			if (diff <= activationRadius && diff >= frameWidth / 2) {
				setMoving(true);
				if (isAttacking()) {
					setAttacking(false);
				}
				sheetDirectionTestRight();

			}
			// on left side of knight
			else if (diff >= -1000 && diff <= -frameWidth / 2) {
				setMoving(true);
				if (isAttacking()) {
					setAttacking(false);
				}
				sheetDirectionTestLeft();
			}
			// Out of range
			else if (diff > activationRadius || diff < -activationRadius) {

				// if enemies get too far away reposition them in front
				// of
				// knight
				if (diff < -4000) {
					getPosition().set(mParams.viewWidth * (mGv.cloudRandom.nextInt(3) + 1),
							mParams.viewHeight - GROUND);
				} else {
					resetActionStatus();
					setCurrentAction(ActionType.IDLE);
				}
			}
			// if enemy is within range, attack
			else if (diff < frameWidth / 2 || diff > -frameWidth / 2) {
				if (isMoving() && !isJumping()) {
					setMoving(false);
				}
				if (!isAttacking() && !isJumping()) {
					setAttacking(true);
				}

			}

			// the isMoving property works its magic
			if (isMoving()) {
				// manage left and right movement based on facingDirection
				if (facingDirection.x == -1) {
					pos.set(pos.x - (float) BASE_MOVEX / 60, pos.y);
				} else {
					pos.set(pos.x + (float) BASE_MOVEX / 60, pos.y);
				}
				handleJump();

			}
			if (mType != null) {
				if (mType != MonsterType.adventureGirl) {
					testSheet();
				}
			}
			checkCollisions(getBounds(), knight.getBounds(), knight);
		} else if (knight.isDead() && isDead()) {
			checkFloorCollisionsDeath();
			handleJump();
			bounds = setBounds();
			setCurrentAction(ActionType.DEATH);
			if (mType != null) {
				if (mType != MonsterType.adventureGirl) {
					testSheet();
				}
			}
		} else if (knight.isDead() && !isDead()) {
			checkFloorCollisionsDeath();
			handleJump();
			bounds = setBounds();
			setCurrentAction(ActionType.IDLE);
			if (mType != null) {
				if (mType != MonsterType.adventureGirl) {
					testSheet();
				}
			}
		} else {
			// pseudo-code
			// if(difference since last death > constant
			// && lastDeathTime is set)
			// reset dead enemy
			handleJump();
			checkFloorCollisionsDeath();
			if (System.currentTimeMillis() - getLastDeathTime() >= 4000 && getLastDeathTime() != 0) {
				if (this instanceof Boss) {
					mGv.onGameFlowEvent(GameFlowEvent.EVENT_BEAT_AREA, mGv.areaId);
				} else {
					loop = true;
					setCurrentHealth(getLife());
					resetActionStatus();
					setCurrentAction(ActionType.IDLE);
					setSheet(left, 4);
					getPosition().set((float) mParams.viewWidth * 2F, (float) mParams.viewHeight - (float) GROUND);
					setLastDeathTime(0);
					setDead(false);
				}
			}

		}

		frameToDraw = getCurrentFrame();

	}

	public void setItemToDrop(Item item) {
		itemToDrop = item;
	}

	private void handleJump() {
		if (isJumping()) {
			setCurrentAction(ActionType.JUMP);
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
		}
	}

	public void sheetDirectionTestLeft() {
		// enemy and knight aren't looking same direction
		if (facingDirection.x == -1) {
			// flip sheet and set facingDirection
			if (mType == MonsterType.adventureGirl) {
				setSheet(right, 4);
			} else {
				// enemy and knight aren't looking same direction
				switch (mCurrentAction) {
				case IDLE:
					setSheet(idle, 1);
					break;
				case MOVE:
					setSheet(walk, 1);
					break;
				case JUMP:
					if (jump != null) {
						setSheet(jump, 1);
					} else {
						setSheet(walk, 1);
					}
					break;
				case ATTACK:
					setSheet(melee, 1);
					break;
				default:
					if (jump != null) {
						setSheet(jump, 1);
					} else {
						setSheet(walk, 1);
					}
					break;
				}
			}
			facingDirection.set(1, 0);
		}
	}

	public void sheetDirectionTestRight() {
		// enemy and knight aren't looking same direction
		if (facingDirection.x == 1) {
			if (mType == MonsterType.adventureGirl) {
				setSheet(left, 4);
			} else {
				// enemy and knight aren't looking same direction
				switch (mCurrentAction) {
				case IDLE:
					setSheet(idleL, 1);
					break;
				case MOVE:
					setSheet(walkL, 1);
					break;
				case JUMP:
					if (jumpL != null) {
						setSheet(jumpL, 1);
					} else {
						setSheet(walkL, 1);
					}
					break;
				case ATTACK:
					setSheet(meleeL, 1);
					break;
				default:
					if (jumpL != null) {
						setSheet(jumpL, 1);
					} else {
						setSheet(walkL, 1);
					}
					break;
				}
			}
			facingDirection.set(-1, 0);
		}
	}

	public void setDead(Bitmap spritesheet, int rows) {
		resetActionStatus();
		setDead(true);
		setStartingHealth(0);
		if (loop) {
			loop = false;
		}
		currentFrame = 0;
		framecount = 10;
		lastFrameChangeTime = 0;
		if (spritesheet != null) {
			setSheet(spritesheet, rows);
		}
	}

	public void setPotion(boolean bool) {
		hasPotion = bool;
	}

	public boolean hasPotion() {
		return hasPotion;
	}

	public void testSheet() {
		if (mType != MonsterType.adventureGirl) {
			if (mType == MonsterType.trollGreen || mType == MonsterType.trollBlue) {
				if (facingDirection.x == -1) {
					switch (mCurrentAction) {
					case IDLE:
						framecount = 6;
						setSheet(idleL, 1);
						break;
					case MOVE:
						framecount = 8;
						setSheet(walkL, 1);
						break;
					case JUMP:
						framecount = 8;
						if (jump != null) {
							setSheet(jumpL, 1);
						} else {
							setSheet(walkL, 1);
						}
						break;
					case ATTACK:
						framecount = 9;
						setSheet(meleeL, 1);
						break;
					case DEATH:
						framecount = 10;
						setSheet(deathLeft, 1);
						break;
					default:
						if (jump != null) {
							framecount = 8;
							setSheet(jumpL, 1);
						} else {
							framecount = 6;
							setSheet(idleL, 1);
						}
						break;
					}
				} else {
					// flip sheet and set facingDirection
					switch (mCurrentAction) {
					case IDLE:
						framecount = 6;
						setSheet(idle, 1);
						break;
					case MOVE:
						framecount = 8;
						setSheet(walk, 1);
						break;
					case JUMP:
						framecount = 8;
						if (jump != null) {
							setSheet(jump, 1);
						} else {
							setSheet(walk, 1);
						}
						break;
					case ATTACK:
						framecount = 9;
						setSheet(melee, 1);
						break;
					case DEATH:
						framecount = 10;
						setSheet(deathRight, 1);
						break;
					default:
						if (jump != null) {
							framecount = 8;
							setSheet(jump, 1);
						} else {
							framecount = 6;
							setSheet(idle, 1);
						}
						break;

					}
				}
			} else if (mType == MonsterType.robot) {
				if (facingDirection.x == -1) {
					switch (mCurrentAction) {
					case IDLE:
						framecount = 10;
						setSheet(idleL, 1);
						break;
					case MOVE:
						framecount = 8;
						setSheet(walkL, 1);
						break;
					case JUMP:
						if (jumpL != null) {
							framecount = 10;
							setSheet(jumpL, 1);
						}
						break;
					case ATTACK:
						framecount = 8;
						setSheet(meleeL, 1);
						break;
					case DEATH:
						framecount = 10;
						setSheet(deathLeft, 1);
						break;
					default:
						framecount = 10;
						if (isJumping()) {
							setSheet(jumpL, 1);
						} else {
							setSheet(idleL, 1);
						}
						break;
					}
				} else {
					// flip sheet and set facingDirection
					switch (mCurrentAction) {
					case IDLE:
						framecount = 10;
						setSheet(idle, 1);
						break;
					case MOVE:
						framecount =8;
						setSheet(walk, 1);
						break;
					case JUMP:
						if (jump != null) {
							framecount = 10;
							setSheet(jump, 1);
						} else {
							framecount = 8;
							setSheet(walk, 1);
						}
						break;
					case ATTACK:
						framecount = 8;
						setSheet(melee, 1);
						break;
					case DEATH:
						framecount = 10;
						setSheet(deathRight, 1);
						break;
					default:
						if (jump != null) {
							framecount = 8;
							setSheet(jump, 1);
						} else {
							framecount = 10;
							setSheet(idle, 1);
						}
						break;

					}
				}

			}
		}
	}

	@Override
	public void setAnimation(ActionType animation) {

		setCurrentAction(animation);
		int top;
		int bottom;
		if (mType != MonsterType.adventureGirl) {
			top = 0;
			bottom = getFrameHeight();
			if (mType == MonsterType.trollGreen || mType == MonsterType.trollBlue) {

				switch (animation) {
				case ATTACK:
					framecount = 9;
					break;
				case MOVE:
					framecount = 8;
					break;
				case JUMP:
					framecount = 8;
					break;
				case JATTACK:
					framecount = 8;
					break;
				case IDLE:
					framecount = 6;
					break;
				case DEATH:
					framecount = 10;
				default:
					framecount = 8;
					break;
				}
			} else if (mType == MonsterType.robot) {
				switch (animation) {
				case ATTACK:
					framecount = 8;
					break;
				case MOVE:
					framecount = 8;
					break;
				case JUMP:
					framecount = 10;
					break;
				case IDLE:
					framecount = 10;
					break;
				case DEATH:
					framecount = 10;
				default:
					framecount = 8;
					break;
				}
			}
		} else {
			switch (animation) {
			case ATTACK:
				framecount = 7;
				top = getFrameHeight();
				bottom = getFrameHeight() * 2;
				break;
			case MOVE:
				framecount = 8;
				top = 0;
				bottom = getFrameHeight();
				break;
			case RUN:
				framecount = 8;
				top = 0;
				bottom = getFrameHeight();
				break;
			case JUMP:
				framecount = 10;
				top = getFrameHeight() * 3;
				bottom = getFrameHeight() * 4;
				break;
			case JATTACK:
				framecount = 10;
				top = getFrameHeight() * 3;
				bottom = getFrameHeight() * 4;
				break;
			case IDLE:
				framecount = 8;
				top = getFrameHeight() * 2;
				bottom = getFrameHeight() * 3;
				break;
			case DEATH:
				framecount = 10;
				top = 0;
				bottom = getFrameHeight();
			default:
				framecount = 10;
				top = 0;
				bottom = getFrameHeight();
				break;
			}
			width = spritesheet.getWidth() / 10;
		}
		frameToDraw.top = top;
		frameToDraw.bottom = bottom;
		setAnimationLeftRight();
	}

	public int getHalfFrame() {
		return width / 2;
	}

	public int getId() {
		return mId;
	}

	public void setId(int i) {
		mId = i;
	}

	public void checkCollisions(Rect a, Rect b, KnightSprite knight) {

		// if the bounding boxes of our sprites are intersecting
		if (Rect.intersects(a, b)) {

			// Enemy attacking player
			// damage is inflicted on the fifth frame
			if (isAttacking() && getFrameInt() == 5 && !isHit()) {

				// Every sprite has this isHit boolean
				// should really be called isHitting()
				// the knight isHit value is not updated till the next frame so
				// it runs more than once if trying to change the knight isHit
				// value
				// set to true on this frame and next frame will be set to false
				// allows for the damage to only occur one time per attack
				// sequence

				// inflict damage to player
				if (!knight.isInvincible()) {

					if (!knight.isHit()) {
						knight.setHit(true);
					}
					setHit(true);
					knight.setCurrentHealth(-getDamage());
					// Log.d("EnemySprite: getDamage(): ",
					// String.valueOf(getDamage())); // Testing
					// Log.d("EnemySprite: isHit(): ", String.valueOf(isHit()));
					// // Testing

					// if health goes beyond zero activate death animation
					if (knight.getCurrentHealth() <= 0) {
						knight.setCurrentAction(ActionType.DEATH);
						knight.setDead(true);
					}
				}

			} else if (getFrameInt() == 6 && isHit()) {
				if (isHit()) {
					setHit(false);
					// Log.d("EnemySprite: removingHit(): ",
					// String.valueOf(isHit())); // Testing

				}
			}
			if (knight.isAttacking() && knight.getFrameInt() == 7) {

				// same concept as above in the enemy collision test
				// each enemy need the isHitByKnight boolean so the damage only
				// occurs once
				if (!isHitByKnight) {
					// inflict damage
					setCurrentHealth(-knight.getDamage());
					isHitByKnight = true;
				}
				// Log.d("KnightSprite: getDamage(): ",
				// String.valueOf(knight.getDamage())); // Testing
				// Log.d("KnightSprite: isHit(): ",
				// String.valueOf(isHitByKnight)); // Testing

				// life leech
				// increase life of knight by current health the percentage
				// of Life leech the player has
				if (knight.getHasLL()) {
					knight.setCurrentHealth((int) (knight.getCurrentHealth() * knight.getLL()));
				}
				if (knight.getHasML()) {
					knight.setCurrentMana((int) (knight.getCurrentMana() * knight.getML()));
				}
				handleDeath(knight);

			} else if (knight.getFrameInt() == 8 && isHitByKnight) {
				// same concept as enemy hit sequenced, explained above
				isHitByKnight = false;
				// Log.d("KnightSprite: removingHitByKnight(): ",
				// String.valueOf(isHitByKnight)); // Testing
			}

		}

	}

	public void handleDeath(KnightSprite knight) {
		// handle enemy death
		if (getCurrentHealth() <= 0) {
			if (!isDead()) {
				resetActionStatus();
				// drop potion if enemy has one to drop
				if (hasPotion) {
					Potion.dropPotion(this);
				}
				if (itemToDrop != null) {
					mGv.droppedItems.add(itemToDrop);
					Item.dropItem(itemToDrop, this);
					// manage the amoutn of dropped items
					if (mGv.droppedItems.size() >= 6) {
						// get rid of first item that dropped
						mGv.droppedItems.get(0).destroy();
						mGv.droppedItems.remove(0);
					}
					mGv.assignItemToDrop(this);
				} else {
					mGv.assignItemToDrop(this);
				}
				// set death animation animation and status
				setCurrentAction(ActionType.DEATH);
				setDead(true);

				// set death time, enemies reset after being dead a
				// certain amount of time
				if (getLastDeathTime() == 0) {

					/*
					 * set death animation based on which direction the enemy is
					 * facing. Using two bitmaps and swapping between them
					 * produced less lag then flipping the bitmap horizontally
					 * perhaps if I used individual bitmaps instead of
					 * spritesheets I still am uncertain of the most effective
					 * and memory efficient way to do animations for Android
					 * games. Using OpenGL drawTexture would obviously be ideal,
					 * but it was a much more involved process that I believe
					 * was outside of the scope of this class
					 */
					if (this instanceof Boss) {
						mGv.onGameFlowEvent(GameFlowEvent.EVENT_BEAT_AREA, mGv.areaId);
					} else {
						if (facingDirection.x == 1) {
							setDead(deathRight, 1);
						} else {
							setDead(deathLeft, 1);
						}
						setLastDeathTime(System.currentTimeMillis());
						// Log.d("EXPERIENCE: ", knight.getExperience() + "");
						frameToDraw.top = 0;
						frameToDraw.bottom = height;
					}
				}
				knight.handleKillQuest();
				// update global kill count - stat tracking
				mGv.totalKills += 1;

				// handle experience gain for knight
				knight.handleExperience(this);
			}

		}
	}

	/**
	 * @descriptionMonsters don't need experience calculation to get level, so
	 *                      just return level
	 * @return level enemy level
	 */
	@Override
	public double getCurrentLevel() {
		return level;
	}

	@Override
	public void onDraw(Canvas canvas) {

	}

	public void setYVelocity(int velocity) {
		velocityY = velocity;
	}

	public void setXVelocity(int velocity) {
		velocityX = velocity;
	}

	public int getXVelocity() {
		return velocityX;
	}

}
