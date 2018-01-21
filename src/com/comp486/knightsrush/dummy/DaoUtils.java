package com.comp486.knightsrush.dummy;

import java.io.File;
/**
 *  Retrieved from http://www.java2s.com/Code/Android/Database/SQLiteDatabaseHelperclass.htm
 */
import java.io.Serializable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

@SuppressWarnings("serial")
public class DaoUtils implements Serializable {

	protected final String DB_NAME = "KnightsTale_DB";
	public String TBL_ITEMS = "items";
	public String TBL_ITEMBONUS = "itemBonuses";
	public SQLiteDatabase sql;
	protected SQLiteOpenHelper helper;
	protected String[] attrs;

	public DaoUtils() {

	}

	public boolean doesItemBonusExist(int itemid, int type) {
		Cursor c;
		String[] key = new String[2];
		String[] value = new String[2];
		key[0] = "item_id";
		value[0] = String.valueOf(itemid);
		key[1] = "type";
		value[1] = String.valueOf(type);
		c = Items.du.execSQL(Items.du.TBL_ITEMBONUS, key, value);
		if (c.moveToNext()) {
			return true;
		} else {
			return false;
		}
	}

	private static boolean doesDatabaseExist(Context context, String dbName) {
		File dbFile = context.getDatabasePath(dbName);
		return dbFile.exists();
	}

	public void deleteDB(Context ctx){
		ctx.deleteDatabase(DB_NAME);
	}
	public SQLiteDatabase createDB(Context ctx) {
		sql = null;
		try {

			// recreate database so when new items are added via a patch,
			// the new items will be available on load
			if (!doesDatabaseExist(ctx, DB_NAME)) {
				sql = ctx.openOrCreateDatabase(DB_NAME,
						SQLiteDatabase.CREATE_IF_NECESSARY | SQLiteDatabase.OPEN_READWRITE, null);

				String str = "create table " + TBL_ITEMS + " (" + "auto_id integer primary key, id integer, "
						+ "dropChance integer, " + "type integer, " + "iLvl integer, " + "iClass integer, "
						+ "minStat integer, " + "maxStat integer, " + "slots integer, " + "requiredStr integer, "
						+ "requiredDex integer, " + "content text, " + "name text, " + "equipped integer default 0);";
				String str2 = "create table " + TBL_ITEMBONUS + " ("
						+ "auto_id integer primary key, item_id integer, type integer, chosen integer);";

				sql.execSQL(str);
				sql.execSQL(str2);
			} else {
				sql = SQLiteDatabase.openDatabase(ctx.getDatabasePath(DB_NAME).getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
			}

		} catch (Exception e) {
			sql.close();
			sql = ctx.openOrCreateDatabase(DB_NAME, SQLiteDatabase.CREATE_IF_NECESSARY, null);

			String str = "create table " + TBL_ITEMS + " (" + "auto_id integer primary key, id integer, "
					+ "dropChance integer, " + "type integer, " + "iLvl integer, " + "iClass integer, "
					+ "minStat integer, " + "maxStat integer, " + "slots integer, " + "requiredStr integer, "
					+ "requiredDex integer, " + "content text, " + "name text, equipped integer default 0);";
			sql.execSQL(str);
		}
		return sql;
	}

	public void close() {
		if (sql != null) {
			sql.close();
		}
	}

	public void delete(String table, String whereClause) throws Exception {
		sql.delete(table, whereClause, null);
	}
	public void updateEquipped(boolean equipped, String itemid){
		ContentValues cv = new ContentValues();
		cv.put("equipped",equipped?1:0);
		sql.update(TBL_ITEMS, cv, "auto_id = " + itemid, null);
	}
	public int getCount(String table) {
		String countQuery = "SELECT  * FROM " + table;
		Cursor cursor = sql.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();
		return count;
	}
	public int getEquippedCount(String table) {
		String countQuery = "SELECT  * FROM " + table + " WHERE equipped = ? VALUES (1);";
		Cursor cursor = sql.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();
		return count;
	}
	public Cursor getInventoryItems(String table) {
		String countQuery = "SELECT  * FROM " + TBL_ITEMS + " WHERE equipped = 0;";
		Cursor cursor = sql.rawQuery(countQuery, null);
		return cursor;
	}

	public long insert(String table, String[] attr) {
		ContentValues initialValues = new ContentValues();
		for (int i = 0; i < attr.length; i += 2) {
			initialValues.put(attr[i], attr[i + 1]);
		}
		long i = -1;
		try {
			i = sql.insert(table, null, initialValues);
		} catch (Exception e) {
			Log.e("Hata", e.toString());
			e.printStackTrace();
		}
		Log.d("createDB: ", String.valueOf(getCount(table)));
		return i;
	}

	public Cursor getAllRows(String table, String[] var) {
		try {
			return sql.query(table, var, null, null, null, null, null);
		} catch (Exception e) {
			System.out.println(e.toString());
			Log.e("Exception on query", e.toString());
			return null;
		}
	}

	public Cursor execSQL(String table, String[] st, String[] var) {
		try {
			String query = table + " WHERE ";
			boolean k = false;
			for (int i = 0; i < var.length; i++) {
				if (k)
					query += " AND ";
				else
					k = true;
				query += st[i] + " = '" + var[i] + "'";
			}
			return sql.query(query, null, null, null, null, null, null);
		} catch (Exception e) {
			System.out.println(e.toString());
			Log.e("Exception on query", e.toString());
			return null;
		}
	}
}
