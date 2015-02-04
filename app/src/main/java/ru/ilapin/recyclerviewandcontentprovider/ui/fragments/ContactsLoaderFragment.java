package ru.ilapin.recyclerviewandcontentprovider.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.ilapin.recyclerviewandcontentprovider.R;

/**
 * Created by Raynor on 31.01.2015.
 */
public class ContactsLoaderFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

	private static final String TAG = "ContactsLoaderFragment";

	private final static int LOADER_ID = 0;

	private ContactsAdapter mContactsAdapter;
	private RecyclerView mRecyclerView;
	private Activity mActivity;

	@Override
	public void onAttach(Activity activity) {
		Log.d(TAG, "onAttach");
		super.onAttach(activity);
		
		mActivity = activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		return inflater.inflate(R.layout.fragment_contacts_loader, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		Log.d(TAG, "onViewCreated");
		super.onViewCreated(view, savedInstanceState);

		mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		Log.d(TAG, "onActivityCreated");
		super.onActivityCreated(savedInstanceState);

		mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));

		mContactsAdapter = new ContactsAdapter(mActivity);
		mRecyclerView.setAdapter(mContactsAdapter);

		getLoaderManager().initLoader(LOADER_ID, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Log.d(TAG, "onCreateLoader");
		return new CursorLoader(
				mActivity,
				ContactsContract.Contacts.CONTENT_URI,
				new String[]{
						ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
				},
				null,
				null,
				ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " ASC"
		);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		Log.d(TAG, "onLoadFinished");
		mContactsAdapter.setCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> cursorLoader) {
		Log.d(TAG, "onLoaderReset");
		mContactsAdapter.setCursor(null);
	}

	private static class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

		private Cursor mCursor;
		private final Context mContext;

		private ContactsAdapter(Context context) {
			this.mContext = context;
		}

		public void setCursor(Cursor cursor) {
			this.mCursor = cursor;
			notifyDataSetChanged();
		}

		@Override
		public ContactsAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
			return new ViewHolder(LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_1, parent, false));
		}

		@Override
		public void onBindViewHolder(final ContactsAdapter.ViewHolder holder, final int position) {
			if (mCursor != null) {
				mCursor.moveToPosition(position);
				holder.textView.setText(mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)));
			}
		}

		@Override
		public int getItemCount() {
			if (mCursor == null) {
				return 0;
			} else {
				return mCursor.getCount();
			}
		}

		public class ViewHolder extends RecyclerView.ViewHolder {

			public TextView textView;

			public ViewHolder(final View itemView) {
				super(itemView);

				textView = (TextView) itemView.findViewById(android.R.id.text1);
			}
		}
	}
}
