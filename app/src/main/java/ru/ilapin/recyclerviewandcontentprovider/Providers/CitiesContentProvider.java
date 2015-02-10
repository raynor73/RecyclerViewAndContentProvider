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
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import ru.ilapin.recyclerviewandcontentprovider.database.DatabaseHelper;

public class CitiesContentProvider extends ContentProvider {

	private static final String TAG = "CitiesContentProvider";

	private static final int CITIES = 0;
	private static final int CAPITALS = 1;
	private static final int CITY = 2;
	private static final int IMAGE = 3;

	private static final String IMAGE_FILENAME = "image.jpg";

	private DatabaseHelper mDatabaseHelper;
	private final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

	@Override
	public boolean onCreate() {
		Log.d(TAG, "onCreate");

		mDatabaseHelper = new DatabaseHelper(getContext());
		mUriMatcher.addURI(CitiesContract.AUTHORITY, "cities", CITIES);
		mUriMatcher.addURI(CitiesContract.AUTHORITY, "capitals", CAPITALS);
		mUriMatcher.addURI(CitiesContract.AUTHORITY, "cities/#", CITY);
		mUriMatcher.addURI(CitiesContract.AUTHORITY, "image", IMAGE);

		return true;
	}

	@Override
	public Cursor query(final Uri uri, final String[] projection, final String selection, final String[] selectionArgs, final String sortOrder) {
		Log.d(TAG, "query: " + uri);

		switch (mUriMatcher.match(uri)) {
			case CITIES: {
				Log.d(TAG, "cities URI match");
				final Cursor cursor = mDatabaseHelper.getReadableDatabase().query(
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

			case CITY: {
				Log.d(TAG, "city URI match");
				final Cursor cursor = mDatabaseHelper.getReadableDatabase().query(
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
			}

			case IMAGE: {
				Log.d(TAG, "image URI match");
				final MatrixCursor cursor = new MatrixCursor(new String[]{
					OpenableColumns.DISPLAY_NAME,
					OpenableColumns.SIZE
				}, 1);

				try {
					final AssetFileDescriptor fileDescriptor = getContext().getAssets().openFd(IMAGE_FILENAME);
					cursor.addRow(new String[]{
							IMAGE_FILENAME,
							String.valueOf(fileDescriptor.getLength())
					});
					fileDescriptor.close();
				} catch (final IOException e) {
					throw new RuntimeException(e);
				}

				return cursor;
			}

			default:
				Log.d(TAG, "no URI match");
				return null;
		}
	}

	@Override
	public String getType(final Uri uri) {
		Log.d(TAG, "getType: " + uri);

		switch (mUriMatcher.match(uri)) {
			case IMAGE:
				return "image/jpeg";

			case CITY:
				return CitiesContract.Cities.CONTENT_ITEM_TYPE;

			default:
				return CitiesContract.Cities.CONTENT_TYPE;
		}
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

		final int affectedRows = mDatabaseHelper.getWritableDatabase().update("City", values, selection, selectionArgs);

		getContext().getContentResolver().notifyChange(uri, null);

		return affectedRows;
	}

	@Override
	public AssetFileDescriptor openAssetFile(final Uri uri, final String mode) throws FileNotFoundException {
		Log.d(TAG, "openFile: " + uri);

		if (mUriMatcher.match(uri) != IMAGE) {
			throw new FileNotFoundException(uri.toString());
		}

		try {
			return getContext().getAssets().openFd(IMAGE_FILENAME);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
}
