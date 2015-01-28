package ru.ilapin.recyclerviewandcontentprovider;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.AsyncTaskLoader;

/**
 * @author Raynor
 *         Class SqlCursorLoader description
 */
public class SqlCursorLoader extends AsyncTaskLoader<Cursor> {
	protected SQLiteDatabase db;
	protected String sql;
	protected String[] selectionArgs;
	protected Cursor cursor;

	public SqlCursorLoader(Context context, SQLiteDatabase db, String sql, String[] selectionArgs) {
		super(context);

		this.db = db;
		this.sql = sql;
		this.selectionArgs = selectionArgs;
	}

	@Override
	public Cursor loadInBackground() {
		return db.rawQuery(sql, selectionArgs);
	}

	@Override
	public void deliverResult(Cursor newCursor) {
		if (isReset()) {
			newCursor.close();
			releaseResources();
			return;
		}

		Cursor oldCursor = cursor;
		cursor = newCursor;

		if (isStarted()) {
			super.deliverResult(newCursor);
		}

		if (oldCursor != null && oldCursor != newCursor) {
			oldCursor.close();
		}
	}

	@Override
	protected void onStartLoading() {
		if (cursor != null) {
			deliverResult(cursor);
		} else {
			forceLoad();
		}
	}

	@Override
	protected void onReset() {
		releaseResources();
	}

	@Override
	public void onCanceled(Cursor cursor) {
		super.onCanceled(cursor);

		if (cursor != null) {
			cursor.close();
		}
	}

	private void releaseResources() {
		if (cursor != null) {
			cursor.close();
			cursor = null;
		}
	}
}
