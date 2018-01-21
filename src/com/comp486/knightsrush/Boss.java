package com.comp486.knightsrush;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Boss extends EnemySprite {
	public static final int BOSS_VIKING = 0;
	public static final int BOSS_TROLL_GREEN = 1;

	public Boss(int bossid, int x, int y, int framecount, int rows, ContextParameters ctx, GameView gv) {
		super(bossid, x, y, framecount, rows, ctx, gv);
		facingDirection.set(-1, 0);
		mId = bossid;
		getPosition().set(x, y);
		getResources(bossid, ctx.context.getResources());
	}

	/**
	 * @description Adjust viking stats based on difficulty
	 * 
	 */
	private void resetViking() {
		switch (mParams.difficulty) {
		case DifficultyConstants.NORMAL:
			level = 15;
			BASE_DMG_MIN = 20;
			BASE_DMG_MAX = 35;
			BASE_DEX = 65;
			BASE_STR = 65;
			BASE_VIT = 50;
			BASE_HP = 250;
			BASE_MOVEX = 460;
			BASE_MOVEY = -600;
			frameLengthInMilliseconds = 140;
			break;
		case DifficultyConstants.NIGHTMARE:
			level = 34;
			BASE_DMG_MIN = 35;
			BASE_DMG_MAX = 45;
			BASE_DEX = 75;
			BASE_STR = 90;
			BASE_VIT = 75;
			BASE_HP = 300;
			BASE_MOVEX = 480;
			BASE_MOVEY = -600;
			frameLengthInMilliseconds = 130;
			break;
		case DifficultyConstants.HELL:
			level = 55;
			BASE_DMG_MIN = 55;
			BASE_DMG_MAX = 75;
			BASE_DEX = 95;
			BASE_STR = 115;
			BASE_VIT = 100;
			BASE_HP = 350;
			BASE_MOVEX = 500;
			BASE_MOVEY = -700;
			frameLengthInMilliseconds = 120;
			break;
		}
		reset(spritesheet, rows);
		strength = (int) (BASE_STR + (level / 2));
		dexterity = (int) (BASE_DEX + (level / 2));
		vitality = (int) (BASE_VIT + level);
		life = BASE_HP + (vitality * 3);
		setStartingHealth(life);
		velocityX = BASE_MOVEX;
		velocityY = BASE_MOVEY;
		loop = true;
		setJumpY(0);
		mCurrentAction = ActionType.IDLE;

	}

	/**
	 * @description Adjust viking stats based on difficulty
	 * 
	 */
	private void resetTrollGreen() {
		switch (mParams.difficulty) {
		case DifficultyConstants.NORMAL:
			level = 25;
			BASE_DMG_MIN = 35;
			BASE_DMG_MAX = 45;
			BASE_DEX = 65;
			BASE_STR = 45;
			BASE_VIT = 45;
			BASE_HP = 300;
			BASE_MOVEX = 500;
			BASE_MOVEY = -600;
			frameLengthInMilliseconds = 140;
			break;
		case DifficultyConstants.NIGHTMARE:
			level = 34;
			BASE_DMG_MIN = 55;
			BASE_DMG_MAX = 75;
			BASE_DEX = 85;
			BASE_STR = 85;
			BASE_VIT = 65;
			BASE_HP = 450;
			BASE_MOVEX = 550;
			BASE_MOVEY = -600;
			frameLengthInMilliseconds = 130;
			break;
		case DifficultyConstants.HELL:
			level = 55;
			BASE_DMG_MIN = 65;
			BASE_DMG_MAX = 95;
			BASE_DEX = 110;
			BASE_STR = 95;
			BASE_VIT = 85;
			BASE_HP = 450;
			BASE_MOVEX = 600;
			BASE_MOVEY = -650;
			frameLengthInMilliseconds = 120;
			break;
		}
		reset(spritesheet, rows);
		strength = (int) (BASE_STR + (level / 2));
		dexterity = (int) (BASE_DEX + (level / 2));
		vitality = (int) (BASE_VIT + level);
		life = BASE_HP + (vitality * 3);
		setStartingHealth(life);
		velocityX = BASE_MOVEX;
		velocityY = BASE_MOVEY;
		loop = true;
		setJumpY(0);
		mCurrentAction = ActionType.IDLE;

	}

	private void getResources(int bossid, Resources res) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 1;
		switch (bossid) {
		case BOSS_VIKING:
			idle = BitmapFactory.decodeResource(res, R.drawable.zaul_idle, options);
			idle = InventoryMenu.resizeImage(idle, (int) (idle.getWidth() * 0.8F));
			walk = BitmapFactory.decodeResource(res, R.drawable.zaul_walk, options);
			walk = InventoryMenu.resizeImage(walk, (int) (walk.getWidth() * 0.8F));
			melee = BitmapFactory.decodeResource(res, R.drawable.zaul_swing, options);
			melee = InventoryMenu.resizeImage(melee, (int) (melee.getWidth() * 0.8F));

			idleL = BitmapFactory.decodeResource(res, R.drawable.zaul_idle_left, options);
			idleL = InventoryMenu.resizeImage(idleL, (int) (idleL.getWidth() * 0.8F));
			walkL = BitmapFactory.decodeResource(res, R.drawable.zaul_walk_left, options);
			walkL = InventoryMenu.resizeImage(walkL, (int) (walkL.getWidth() * 0.8F));
			meleeL = BitmapFactory.decodeResource(res, R.drawable.zaul_swing_left, options);
			meleeL = InventoryMenu.resizeImage(meleeL, (int) (meleeL.getWidth() * 0.8F));

			spritesheet = idleL;
			resetViking();
			break;
		case BOSS_TROLL_GREEN:
			idle = BitmapFactory.decodeResource(res, R.drawable.troll_king_idle, options);
			idle = InventoryMenu.resizeImage(idle, (int) (idle.getWidth() * 0.75F));
			walk = BitmapFactory.decodeResource(res, R.drawable.troll_king_walk, options);
			walk = InventoryMenu.resizeImage(walk, (int) (walk.getWidth() * 0.75F));
			melee = BitmapFactory.decodeResource(res, R.drawable.troll_king_attack, options);
			melee = InventoryMenu.resizeImage(melee, (int) (melee.getWidth() * 0.75F));
			deathRight = BitmapFactory.decodeResource(res, R.drawable.troll_king_death,  options);
			deathRight = InventoryMenu.resizeImage(deathRight, (int) (deathRight.getWidth() * 0.75F));

			idleL = BitmapFactory.decodeResource(res, R.drawable.troll_king_idle_left, options);
			idleL = InventoryMenu.resizeImage(idleL, (int) (idleL.getWidth() * 0.75F));
			walkL = BitmapFactory.decodeResource(res, R.drawable.troll_king_walk_left, options);
			walkL = InventoryMenu.resizeImage(walkL, (int) (walkL.getWidth() * 0.75F));
			meleeL = BitmapFactory.decodeResource(res, R.drawable.troll_king_attack_left, options);
			meleeL = InventoryMenu.resizeImage(meleeL, (int) (meleeL.getWidth() * 0.75F));
			deathLeft = BitmapFactory.decodeResource(res, R.drawable.troll_king_death_left, options);
			deathLeft = InventoryMenu.resizeImage(deathLeft, (int) (deathLeft.getWidth() * 0.75F));

			spritesheet = idleL;
			resetTrollGreen();
			break;
		}
	}

	public void recycleBmps() {
		idle.recycle();
		idleL.recycle();
		walk.recycle();
		walkL.recycle();
		// run.recycle();
		// runL.recycle();
		if (deathRight != null) {
			deathRight.recycle();
			deathLeft.recycle();
		}
		melee.recycle();
		meleeL.recycle();
	}

	@Override
	public void sheetDirectionTestLeft() {
		// enemy and knight aren't looking same direction
		if (facingDirection.x == -1) {
			// flip sheet and set facingDirection
			if (mId == BOSS_VIKING) {
				switch (mCurrentAction) {
				case IDLE:
					framecount = 11;
					setSheet(idle, 1);
					break;
				case MOVE:
					framecount = 9;
					setSheet(walk, 1);
					break;
				case ATTACK:
					framecount = 10;
					setSheet(melee, 1);
					break;
				case JUMP:
					framecount = 9;
					setSheet(walk, 1);
					break;
				case DEATH:
					framecount = 11;
					setSheet(idle, 1);
					break;
				default:
					framecount = 9;
					setSheet(walk, 1);
					break;

				}
			} else if (mId == BOSS_TROLL_GREEN) {
				switch (mCurrentAction) {
				case IDLE:
					framecount = 6;
					setSheet(idle, 1);
					break;
				case MOVE:
					framecount = 8;
					setSheet(walk, 1);
					break;
				case ATTACK:
					framecount = 9;
					setSheet(melee, 1);
					break;
				case JUMP:
					framecount = 8;
					setSheet(walk, 1);
					break;
				case DEATH:
					if (deathRight != null) {
						framecount = 10;
						setSheet(deathRight,  1);
					}
					break;
				default:
					framecount = 9;
					setSheet(walk, 1);
					break;

				}

			}
			facingDirection.set(1, 0);
		}
	}

	@Override
	public void setFrameWidth(int i) {
		width = i / framecount;

	}

	@Override
	public void testSheet() {
		if (facingDirection.x == -1) {
			if (mId == BOSS_VIKING) {
				switch (mCurrentAction) {
				case IDLE:
					framecount = 11;
					setSheet(idleL, 1);
					break;
				case MOVE:
					framecount = 9;
					setSheet(walkL, 1);
					break;
				case ATTACK:
					framecount = 10;
					setSheet(meleeL, 1);
					break;
				case JUMP:
					framecount = 9;
					setSheet(walkL, 1);
					break;
				case DEATH:
					framecount = 11;
					setSheet(idleL, 1);
					break;
				default:
					framecount = 9;
					setSheet(walkL, 1);
					break;

				}
			} else if (mId == BOSS_TROLL_GREEN) {
				switch (mCurrentAction) {
				case IDLE:
					framecount = 6;
					setSheet(idleL, 1);
					break;
				case MOVE:
					framecount = 8;
					setSheet(walkL, 1);
					break;
				case ATTACK:
					framecount = 9;
					setSheet(meleeL, 1);
					break;
				case JUMP:
					framecount = 8;
					setSheet(walkL, 1);
					break;
				case DEATH:
					if (deathRight != null) {
						framecount = 10;
						setSheet(deathLeft, 1);
					}
					break;
				default:
					framecount = 8;
					setSheet(walkL, 1);
					break;
				}
			}
		} else {
			if (mId == BOSS_VIKING) {
				switch (mCurrentAction) {
				case IDLE:
					framecount = 11;
					setSheet(idle, 1);
					break;
				case MOVE:
					framecount = 9;
					setSheet(walk, 1);
					break;
				case ATTACK:
					framecount = 10;
					setSheet(melee, 1);
					break;
				case JUMP:
					framecount = 9;
					setSheet(walk, 1);
					break;
				case DEATH:
					framecount = 11;
					setSheet(idle, 1);
					break;
				default:
					framecount = 9;
					setSheet(walk, 1);
					break;

				}
			} else if (mId == BOSS_TROLL_GREEN) {
				switch (mCurrentAction) {
				case IDLE:
					framecount = 6;
					setSheet(idle, 1);
					break;
				case MOVE:
					framecount = 8;
					setSheet(walk, 1);
					break;
				case ATTACK:
					framecount = 9;
					setSheet(melee, 1);
					break;
				case JUMP:
					framecount = 8;
					setSheet(walk, 1);
					break;
				case DEATH:
					if (deathRight != null) {
						framecount = 10;
						setSheet(deathRight,  1);
					}
					break;
				default:
					framecount = 8;
					setSheet(walk, 1);
					break;

				}

			}
		}
	}

	@Override
	public void sheetDirectionTestRight() {
		// enemy and knight aren't looking same direction
		if (facingDirection.x == 1) {
			if (mId == BOSS_VIKING) {
				switch (mCurrentAction) {
				case IDLE:
					framecount = 11;
					setSheet(idle, 1);
					break;
				case MOVE:
					framecount = 9;
					setSheet(walk, 1);
					break;
				case ATTACK:
					framecount = 10;
					setSheet(melee, 1);
					break;
				case DEATH:
					framecount = 11;
					setSheet(idle, 1);
				default:
					framecount = 9;
					setSheet(walk, 1);
					break;

				}
			} else if (mId == BOSS_TROLL_GREEN) {
				switch (mCurrentAction) {
				case IDLE:
					framecount = 6;
					setSheet(idle, 1);
					break;
				case MOVE:
					framecount = 8;
					setSheet(walk, 1);
					break;
				case ATTACK:
					framecount = 9;
					setSheet(melee, 1);
					break;
				case DEATH:
					if (deathRight != null) {
						framecount = 10;
						setSheet(deathRight,  1);
					}
					break;
				case JUMP:
					framecount = 8;
					setSheet(walk, 1);
					break;
				default:
					framecount = 8;
					setSheet(walk, 1);
					break;

				}

			}
			facingDirection.set(-1, 0);
		}

	}

	@Override
	public void setAnimation(ActionType animation) {

		setCurrentAction(animation);
		final int frameHeight = getFrameHeight();
		int top;
		int bottom;
		top = 0;
		bottom = frameHeight;/*
								 * switch (animation) { case ATTACK: framecount
								 * = 10; break; case MOVE: framecount = 9;
								 * break; case RUN: framecount = 12; break; case
								 * JUMP: framecount = 10; break; case JATTACK:
								 * framecount = 10; break; case IDLE: framecount
								 * = 11; break; case DEATH: framecount = 10;
								 * default: framecount = 10; break; }
								 */
		frameToDraw.top = top;
		frameToDraw.bottom = bottom;
		if (spritesheet != null) {
			width = spritesheet.getWidth() / framecount;
			setAnimationLeftRight();
		}
	}

}
