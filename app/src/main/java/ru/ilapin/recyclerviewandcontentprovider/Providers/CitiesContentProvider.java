/*
 * @file CitiesContentProvider.java
 * @author ilapin
 *
 * Copyright (c) 2004-2015. Parallels IP Holdings GmbH. All rights reserved.
 * http://www.parallels.com
 */
package ru.ilapin.recyclerviewandcontentprovider.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.Arrays;

import ru.ilapin.recyclerviewandcontentprovider.database.DatabaseHelper;

public class CitiesContentProvider extends ContentProvider {

	private static final String TAG = "CitiesContentProvider";

	private static final int CITIES = 0;
	private static final int CAPITALS = 1;
	private static final int CITY = 2;

	private DatabaseHelper mDatabaseHelper;
	private final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

	@Override
	public boolean onCreate() {
		Log.d(TAG, "onCreate");

		mDatabaseHelper = new DatabaseHelper(getContext());
		mUriMatcher.addURI(CitiesContract.AUTHORITY, "cities", CITIES);
		mUriMatcher.addURI(CitiesContract.AUTHORITY, "capitals", CAPITALS);
		mUriMatcher.addURI(CitiesContract.AUTHORITY, "cities/#", CITY);

		return true;
	}

	@Override
	public Cursor query(final Uri uri, final String[] projection, final String selection, final String[] selectionArgs, final String sortOrder) {
		Log.d(TAG, "query: " + uri);

		switch (mUriMatcher.match(uri)) {
			case CITIES: {
				Log.d(TAG, "cities URI match");
				Cursor cursor = mDatabaseHelper.getReadableDatabase().query(
						"City",
						projection,
						selection,
						selectionArgs,
						null,
						null,
						sortOrder
				);

				cursor.setNotificationUri(getContext().getContentResolver(), CitiesContract.Cities.CONTENT_URI);

				return cursor;
			}

			case CAPITALS:
				Log.d(TAG, "capitals URI match");
				final String[] capitalsSelectionArgs;
				if (selectionArgs == null) {
					capitalsSelectionArgs = new String[]{"1"};
				} else {
					capitalsSelectionArgs = Arrays.copyOf(selectionArgs, selectionArgs.length + 1);
					capitalsSelectionArgs[selectionArgs.length] = "1";
				}

				return mDatabaseHelper.getReadableDatabase().query(
						"City",
						projection,
						"(" + (selection == null ? "1" : selection) + ") AND " + CitiesContract.Cities.CAPITAL + " = ?",
						capitalsSelectionArgs,
						null,
						null,
						sortOrder
				);

			case CITY:
				Log.d(TAG, "city URI match");
				Cursor cursor = mDatabaseHelper.getReadableDatabase().query(
						"City",
						projection,
						CitiesContract.Cities._ID + " = ?",
						new String[]{uri.getLastPathSegment()},
						null,
						null,
						null
				);
				cursor.moveToFirst();
				return cursor;

			default:
				Log.d(TAG, "no URI match");
				return null;
		}
	}

	@Override
	public String getType(final Uri uri) {
		Log.d(TAG, "getType: " + uri);

		return mUriMatcher.match(uri) == CITY ? CitiesContract.Cities.CONTENT_ITEM_TYPE : CitiesContract.Cities.CONTENT_TYPE;
	}

	@Override
	public Uri insert(final Uri uri, final ContentValues values) {
		Log.d(TAG, "insert: " + uri);

		return null;
	}

	@Override
	public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
		Log.d(TAG, "delete: " + uri);

		return 0;
	}

	@Override
	public int update(final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs) {
		Log.d(TAG, "update: " + uri);

		int affectedRows = mDatabaseHelper.getWritableDatabase().update("City", values, selection, selectionArgs);

		getContext().getContentResolver().notifyChange(uri, null);

		return affectedRows;
	}
}
