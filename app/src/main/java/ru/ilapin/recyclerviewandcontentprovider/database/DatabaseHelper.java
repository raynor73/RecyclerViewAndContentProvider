package ru.ilapin.recyclerviewandcontentprovider.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ru.ilapin.recyclerviewandcontentprovider.Providers.CitiesContract;

/**
 * Created by Raynor on 31.01.2015.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "database.sqlite";
	private static final int DATABASE_VERSION = 1;

	private static DatabaseHelper instance;

	public static DatabaseHelper getInstance() {
		if (instance == null) {
			throw new IllegalStateException("DatabaseHelper not initialized");
		}

		return instance;
	}

	public static void init(Context context) {
		instance = new DatabaseHelper(context);
	}

	private DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase sqliteDatabase) {
		sqliteDatabase.execSQL("CREATE TABLE City (" +
				CitiesContract.Cities.NAME + " INTEGER PRIMARY KEY, " +
				CitiesContract.Cities.NAME + " TEXT, " +
				CitiesContract.Cities.CAPITAL + " INTEGER)");

		for (String cityName : DataProvider.cities) {
			ContentValues contentValues = new ContentValues();
			contentValues.put(CitiesContract.Cities.NAME, cityName);
			contentValues.put(CitiesContract.Cities.CAPITAL, 0);

			sqliteDatabase.insert("City", null, contentValues);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqliteDatabase, int i, int i2) {

	}
}
