package com.comp486.knightsrush;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.comp486.knightsrush.ConversationUtils;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.support.v4.app.ActionBarDrawerToggle;
import android.util.Log;

/**
 * @author Nick LeBlanc. Thank you Chris Pruett from Google for the inspiration
 * 
 * @description Inspired by the LevelTree class in Android Open Source Project
 *              Each act consists of an AreaGroup
 * 
 */
public final class GameAct {
	/**
	 * 
	 * @description Each AreaGroup has an array of GameAreas
	 *
	 */
	public static class AreaGroup {
		public int mId;
		public int difficultyCompleted = 0;
		public boolean completed = false;
		public ArrayList<GameArea> levels = new ArrayList<GameArea>();
	}

	/**
	 * 
	 * @description Nick
	 *
	 */
	public static class GameArea {
		public int mFloor;
		public int mPlat;
		public int mBg;
		public int mDistanceStart = 0;
		public int mDistanceEnd;
		public int monsterLevel = 1;
		public int monsterType = 0;
		public boolean showDialog = true;
		public int mId;
		public ArrayList<Quest> quests = new ArrayList<Quest>();
		public String name;
		public int bossId = -1;
		public boolean completed;
		public DialogEntry dialogResources;
		public boolean startBossDialog = true;
		public boolean endBossDialog = true;

		/**
		 * @description GameArea initialization
		 * @param id
		 * @param floor:
		 *            resource
		 * @param platform:
		 *            resource
		 * @param bg:
		 *            resource
		 * @param dialogs:
		 *            DialogEntry NPC and player dialog
		 * @param title:
		 *            name of GameArea
		 */
		public GameArea(int id, int floor, int plat, int bg, DialogEntry dialogs, String title, int _monsterLevel,
				int distanceEnd) {
			mFloor = floor;
			mBg = bg;
			mPlat = plat;
			dialogResources = dialogs;
			mId = id;
			monsterLevel = _monsterLevel;
			name = title;
			mDistanceEnd = distanceEnd;
			completed = false;
		}

	}

	/**
	 * @description Quests Get NPC by id from xml file.
	 * 
	 *              Each Quest has a DialogEntry Each DialogEnty can have an
	 *              array of Conversation items for both NPC and player
	 */
	public static class Quest {
		public int id = -1;
		public int npc = 0;
		public int threshold = 0;
		public boolean completed, inProcess = false;
		public int distanceWalked = 0;
		public int disactRange = 0;
		public DialogEntry dialogResources;
		public int type = QuestConstants.QC_TYPE_MONSTER_KILL;
		public Reward reward = null;

		public Quest() {
		}

		/**
		 * @description Quest initialization
		 * @param mId
		 * @param mType:
		 *            Quest type - use QuestConstants
		 * @param NPC
		 * @param mDistanceWalked
		 * @param mDisactRange
		 * @param dialog
		 * @param mThreshold
		 * @param mCompleted
		 */
		public Quest(int mId, int mType, int NPC, int mDistanceWalked, int mDisactRange, DialogEntry dialog,
				int mThreshold, boolean mCompleted) {
			id = mId;
			type = mType;
			npc = NPC;
			distanceWalked = mDistanceWalked;
			disactRange = mDisactRange;
			dialogResources = dialog;
			completed = mCompleted;
			inProcess = false;
			threshold = mThreshold;
		}

		public static class Reward {
			public int mType = RewardConstants.R_TYPE_HEALTH;
			public int mIsInt = RewardConstants.R_BASE;
			public float mBonus = 0F;

			public Reward() {
			}

			/**
			 * 
			 * @param type:
			 *            Reward type - user RewardConstants
			 * @param isInt:
			 *            determine whether to apply reward using an integer or
			 *            float value
			 * @param bonus:
			 *            Integer representing either a total or percentage (ex.
			 *            when not isInt, 5 turns into 0.05F)
			 */
			public Reward(int type, int isInt, float bonus) {
				mType = type;
				mIsInt = isInt;
				mBonus = bonus;
			}
		}
	}

	public static class DialogEntry {
		public int npcEntry = 0;
		public int playerEntry = 0;
		public ArrayList<ConversationUtils.Conversation> npcConvo;
		public ArrayList<ConversationUtils.Conversation> playerConvo;
	}

	public final static ArrayList<AreaGroup> levels = new ArrayList<AreaGroup>();
	private static boolean mLoaded = false;
	private static int mLoadedResource = -1;

	/**
	 * @description Get act by id 0 is act 1
	 * @param act
	 * @return AreaGroup/Act
	 */
	public static final AreaGroup get(int act) {
		return GameAct.levels.get(act);
	}

	/**
	 * @description Get GameArea by act and area id
	 * @param act
	 * @param area
	 * @return GameArea
	 */
	public static final GameArea get(int act, int area) {
		return levels.get(act).levels.get(area);
	}

	/**
	 * @description Get Quest by act, area, and quest id
	 * @param act
	 *            id
	 * @param area
	 *            id
	 * @param quest
	 *            id
	 * @return Quest
	 */
	public static final Quest get(int act, int area, int quest) {
		return levels.get(act).levels.get(area).quests.get(quest);
	}

	/**
	 * @description Is the resource loaded
	 * @param resource
	 * @return mLoaded && mLoadedResource == resource
	 */
	public static final boolean isLoaded(int resource) {
		return mLoaded && mLoadedResource == resource;
	}

	public static final boolean isLoaded() {
		return mLoaded;
	}

	// Not implemented
	// tried so many variations to get this to work and had no luck so used an
	// alternative method for loading levelTree.xml
	// http://stackoverflow.com/questions/7388373/copying-xml-file-from-res-xml-folder-to-device-storage
	public static final void saveLevelTree(InputStream input, Context context) {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try {
			docFactory.setNamespaceAware(true);
			docBuilder = docFactory.newDocumentBuilder();
			org.w3c.dom.Document doc = docBuilder.parse(input);

			// write the content into xml file
			File file = new File(context.getFilesDir() + "/level_tree.xml");
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			StreamResult result = new StreamResult(file);
			DOMSource source = new DOMSource(doc);
			transformer.transform(source, result);
			// try {
			//
			// OutputStreamWriter osw = new
			// OutputStreamWriter(context.openFileOutput("level_tree.xml",0));
			// osw.write(writer.toString());
			// int len = writer.toString().length();
			// osw.flush();
			// osw.close();
			// } catch (IOException ioe) {
			// ioe.printStackTrace();
			// }
			// System.out.println("XML IN String format is: \n" +
			// writer.toString());
		} catch (

		UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (NotFoundException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}

	}

	public static boolean fileExists(Context context, String filename) {
		File file = context.getFileStreamPath(filename);
		// saveLevelTree(, context);
		return true;
	}

	/**
	 * @description load level tree based on difficulty
	 * @param context
	 * @param difficulty
	 */
	public static final void loadLevelTree(Context context, int diff, boolean reset) {
		// Set Default
		int levelTreeResource = R.raw.level_tree;
		switch (diff) {
		// Normal
		case DifficultyConstants.NORMAL:
			levelTreeResource = R.raw.level_tree;
			break;
		// Nightmare
		case DifficultyConstants.NIGHTMARE:
			levelTreeResource = R.raw.level_tree_nm;
			break;
		// Hell
		case DifficultyConstants.HELL:
			levelTreeResource = R.raw.level_tree_hell;
			break;
		}
		if (!GameAct.isLoaded(levelTreeResource) || reset) {
			try {
				GameAct.loadLevelTree(levelTreeResource, context, reset, diff);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			} finally {
				GameAct.loadAllDialogAct(context, diff);
			}
		}
	}

	/**
	 * @description Pass levelTree XML resource id and use XMLPullParser Factory
	 *              to iterate through acts, areas, and quests and store them in
	 *              memory for the current difficulty
	 * @param resource
	 * @param context
	 * @throws XmlPullParserException
	 * @throws FileNotFoundException
	 */
	public static final void loadLevelTree(int resource, Context context, boolean reset, int diff)
			throws XmlPullParserException, FileNotFoundException {
		if (levels.size() > 0 && mLoadedResource == resource && !reset) {
			// already loaded
			return;

		} else {
			mLoaded = false;
			mLoadedResource = -1;
		}

		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser parser = factory.newPullParser();

		// parse xml from raw folder
		parser.setInput(context.getResources().openRawResource(resource), "UTF-8");
		levels.clear();

		//
		AreaGroup currentGroup = null;
		GameArea currentLevel = null;
		Quest currentQuest = null;
		DialogEntry currentDialog = null;
		DialogEntry startDialog = null;
		Quest.Reward currentReward = null;

		/**
		 * this while statement wrapped in a try/catch block iterates through
		 * the document starting at the very first tag and running till it finds
		 * the end of the file Schema : Act-> attributes[difficultyCompleted]
		 * Area-> attributes[platResource, groundResource,title,id]
		 * Quest->Reward->attributes[
		 */

		SharedPreferences prefs = context.getSharedPreferences(PreferenceConstants.PREFERENCE_NAME,
				Context.MODE_PRIVATE);
		try {
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					if (parser.getName().equals("act")) {
						currentGroup = new AreaGroup();
						for (int i = 0; i < parser.getAttributeCount(); i++) {
							final String value = parser.getAttributeValue(i);
							if (value != null) {
								// if
								// (parser.getAttributeName(i).equals("diffComplete"))
								// {
								// currentGroup.difficultyCompleted =
								// Integer.parseInt(value);
								// }

							}
						}
						currentGroup.mId = levels.size();// id = size-1
						currentGroup.completed = prefs.getInt(LevelPreferences.PREFERENCE_DIFFICULTY_COMPLETED,
								0) >= diff ? true : false;
						levels.add(currentGroup);
						currentDialog = null;
						currentLevel = null;
					}

					if (parser.getName().equals("area") && currentGroup != null) {
						int levelResource = 0;
						int floorResource = 0;
						int platResource = 0;
						int id = 0;
						String titleString = null;
						int monsterLevel = 0;
						int monsterType = -1;
						int bossId = -1;
						int distanceEnd = 0;
						for (int i = 0; i < parser.getAttributeCount(); i++) {
							final String value = parser.getAttributeValue(i);
							if (value != null) {
								if (parser.getAttributeName(i).equals("bgRes")) {
									levelResource = getResourceId(context,
											value.substring(value.indexOf("/") + 1, value.length()),
											value.substring(1, value.indexOf("/")));
								} else if (parser.getAttributeName(i).equals("floorRes")) {
									floorResource = getResourceId(context,
											value.substring(value.indexOf("/") + 1, value.length()),
											value.substring(1, value.indexOf("/")));
								} else if (parser.getAttributeName(i).equals("platRes")) {
									platResource = getResourceId(context,
											value.substring(value.indexOf("/") + 1, value.length()),
											value.substring(1, value.indexOf("/")));
								} else if (parser.getAttributeName(i).equals("title")) {
									titleString = context.getString(getResourceId(context,
											value.substring(value.indexOf("/") + 1, value.length()),
											value.substring(1, value.indexOf("/"))));
								} else if (parser.getAttributeName(i).equals("id")) {
									id = Integer.parseInt(value);
								} else if (parser.getAttributeName(i).equals("monsterLevel")) {
									monsterLevel = Integer.parseInt(value);
								} else if (parser.getAttributeName(i).equals("monsterType")) {
									monsterType = Integer.parseInt(value);
								} else if (parser.getAttributeName(i).equals("bossId")) {
									bossId = Integer.parseInt(value);
								} else if (parser.getAttributeName(i).equals("end")) {
									distanceEnd = Integer.parseInt(value);
								}

							}

						}
						currentLevel = new GameArea(id, floorResource, platResource, levelResource, null, titleString,
								monsterLevel, distanceEnd);
						currentLevel.monsterType = monsterType;
						currentLevel.bossId = bossId;
						currentLevel.completed = prefs.getInt(LevelPreferences.PREFERENCE_AREA_COMPLETED + "_"
								+ currentGroup.mId + "_" + currentLevel.mId + "_" + diff, 0) == 1
								|| currentGroup.completed ? true : false;
						currentGroup.levels.add(currentLevel);
					}

					if (parser.getName().equals("start")) {
						startDialog = new DialogEntry();
						currentLevel.dialogResources = startDialog;

						for (int i = 0; i < parser.getAttributeCount(); i++) {
							final String value = parser.getAttributeValue(i);
							if (value != null) {
								if (parser.getAttributeName(i).equals("resource")) {
									currentLevel.dialogResources.npcEntry = getResourceId(context,
											value.substring(value.indexOf("/") + 1, value.length()),
											value.substring(1, value.indexOf("/")));
								}
							}
						}
						startDialog = null;
					}
					if (parser.getName().equals("quest") && currentLevel != null) {
						currentQuest = new Quest();
						currentReward = new Quest.Reward();
						for (int i = 0; i < parser.getAttributeCount(); i++) {
							final String value = parser.getAttributeValue(i);
							if (value != null) {
								if (parser.getAttributeName(i).equals("disact")) {
									currentQuest.disactRange = Integer.parseInt(value);
								} else if (parser.getAttributeName(i).equals("distanceWalked")) {
									currentQuest.distanceWalked = Integer.parseInt(value);
								} else if (parser.getAttributeName(i).equals("id")) {
									currentQuest.id = Integer.parseInt(value);
								} else if (parser.getAttributeName(i).equals("type")) {
									currentQuest.type = Integer.parseInt(value);
								} else if (parser.getAttributeName(i).equals("reward")) {
									currentReward.mType = Integer.parseInt(value);
								} else if (parser.getAttributeName(i).equals("bonus")) {
									currentReward.mBonus = Integer.parseInt(value);
								} else if (parser.getAttributeName(i).equals("isInt")) {
									currentReward.mIsInt = Integer.parseInt(value);
								} else if (parser.getAttributeName(i).equals("threshold")) {
									currentQuest.threshold = Integer.parseInt(value);
								}
							}
						}
					}
					if (currentLevel != null && currentQuest != null) {

						if (currentReward != null) {
							currentQuest.reward = currentReward;
						}

						if (parser.getName().equals("npc")) {
							currentDialog = new DialogEntry();
							currentQuest.dialogResources = currentDialog;

							for (int i = 0; i < parser.getAttributeCount(); i++) {
								final String value = parser.getAttributeValue(i);
								if (value != null) {
									if (parser.getAttributeName(i).equals("resource")) {
										currentQuest.dialogResources.npcEntry = getResourceId(context,
												value.substring(value.indexOf("/") + 1, value.length()),
												value.substring(1, value.indexOf("/")));
									} else if (parser.getAttributeName(i).equals("id")) {
										currentQuest.npc = Integer.parseInt(value);
									}
								}
							}
						}

						if (parser.getName().equals("player")) {

							for (int i = 0; i < parser.getAttributeCount(); i++) {
								final String value = parser.getAttributeValue(i);
								if (value != null) {
									if (parser.getAttributeName(i).equals("resource")) {
										currentQuest.dialogResources.playerEntry = getResourceId(context,
												value.substring(value.indexOf("/") + 1, value.length()),
												value.substring(1, value.indexOf("/")));
									}

								}
							}
						}

						if (currentDialog != null) {
							currentLevel.quests.add(currentQuest);
							currentQuest = null;
							currentDialog = null;
						}
					}
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mLoadedResource = resource;
			mLoaded = true;
		}
	}

	/**
	 * http://stackoverflow.com/questions/4427608/android-getting-resource-id-from-string
	 * 
	 * @param c
	 * @param pVariableName
	 * @param pResourcename
	 * @return (int) resourceId or -1
	 */
	public static int getResourceId(Context c, String pVariableName, String pResourcename) {
		try {
			return c.getResources().getIdentifier(pVariableName, pResourcename, c.getPackageName());
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * @description Load all conversation items for NPC and player Also, setting
	 *              the completed state of acts, areas, and quests for this
	 *              difficulty
	 * @param context
	 */
	public final static void loadAllDialogAct(Context context, int difficulty) {
		final int levelGroupCount = levels.size();
		SharedPreferences prefs = context.getSharedPreferences(PreferenceConstants.PREFERENCE_NAME,
				Context.MODE_PRIVATE);
		for (int x = 0; x < levelGroupCount; x++) {
			AreaGroup act = levels.get(x);
			if (prefs.getInt(LevelPreferences.PREFERENCE_ACT_COMPLETED + "_" + act.mId + "_" + difficulty, 0) == 1) {
				act.completed = true;
			}
			final ArrayList<GameArea> row = act.levels;
			final int levelCount = row.size();
			for (int y = 0; y < levelCount; y++) {
				final GameArea level = row.get(y);
				if (level != null) {
					if (level.dialogResources != null) {
						DialogEntry d = level.dialogResources;
						if (d.npcEntry != 0) {
							d.npcConvo = ConversationUtils.loadDialog(d.npcEntry, context);
						}
					}
					if (level.bossId == -1) {
						level.endBossDialog = false;
						level.startBossDialog = false;
					}
					if (prefs.getInt(LevelPreferences.PREFERENCE_AREA_COMPLETED + "_" + act.mId + "_" + level.mId + "_"
							+ difficulty, 0) == 1 || act.completed) {
						level.completed = true;
					}
					if (act.completed) {
						level.showDialog = false;
						// level.startBossDialog = false;
						// level.endBossDialog = false;
					} else {
						if (prefs.getInt("A_" + act.mId + "_A" + level.mId + "_boss_start_dialog", 0) == 1) {
							level.startBossDialog = false;
						}
						if (prefs.getInt("A_" + act.mId + "_A" + level.mId + "_boss_end_dialog", 0) == 1) {
							level.endBossDialog = false;
						}
						if (prefs.getInt("A_" + act.mId + "_A" + level.mId + "_dialog_" + (difficulty), 0) == 1) {
							level.showDialog = false;
						}
					}
					if (level.quests != null) {
						for (int i = 0; i < level.quests.size(); i++) {
							Quest quest = level.quests.get(i);
							DialogEntry dialog = quest.dialogResources;
							if (prefs.getInt("A" + act.mId + "_A" + level.mId + "_Q" + i + "_" + difficulty, 0) == 1
									|| level.completed) {
								quest.completed = true;
							}
							if (dialog.npcEntry != 0) {
								dialog.npcConvo = ConversationUtils.loadDialog(dialog.npcEntry, context);
							}

							if (dialog.playerEntry != 0) {
								dialog.playerConvo = ConversationUtils.loadDialog(dialog.playerEntry, context);
							}
						}
					}
				}
			}
		}
	}

}
