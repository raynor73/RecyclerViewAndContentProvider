package ru.ilapin.recyclerviewandcontentprovider.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import ru.ilapin.recyclerviewandcontentprovider.R;
import ru.ilapin.recyclerviewandcontentprovider.providers.CitiesContract;

/**
 * Created by Raynor on 01.02.2015.
 */
public class CustomProviderFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

	private static final String TAG = "CustomProviderFragment";

	private final static int CITIES_LOADER_ID = 0;
	private final static int CAPITALS_LOADER_ID = 1;

	private Activity mActivity;
	private TextView mCityIdTextView;
	private EditText mCityName;
	private CheckBox mCapitalCheckbox;
	private Button mSaveButton;
	private RecyclerView mCapitalsRecyclerView;
	private Button mReloadButton;
	private RecyclerView mCitiesRecyclerView;
	private CitiesAdapter mCitiesAdapter;
	private CitiesAdapter mCapitalsAdapter;

	@Override
	public void onAttach(Activity activity) {
		Log.d(TAG, "onAttach");
		super.onAttach(activity);

		mActivity = activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		return inflater.inflate(R.layout.fragment_custom_provider, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		Log.d(TAG, "onViewCreated");
		super.onViewCreated(view, savedInstanceState);

		mCityIdTextView = (TextView) view.findViewById(R.id.city_id);
		mCityName = (EditText) view.findViewById(R.id.city_name);
		mCapitalCheckbox = (CheckBox) view.findViewById(R.id.city_is_capital);
		mSaveButton = (Button) view.findViewById(R.id.save_city);
		mCapitalsRecyclerView = (RecyclerView) view.findViewById(R.id.capitals_list);
		mCitiesRecyclerView = (RecyclerView) view.findViewById(R.id.cities_list);

		mCapitalsRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
		mCitiesRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		Log.d(TAG, "onActivityCreated");
		super.onActivityCreated(savedInstanceState);

		mCitiesAdapter = new CitiesAdapter(mActivity);
		mCitiesRecyclerView.setAdapter(mCitiesAdapter);

		mCapitalsAdapter = new CitiesAdapter(mActivity);
		mCapitalsRecyclerView.setAdapter(mCapitalsAdapter);

		LoaderManager loaderManager = getLoaderManager();
		loaderManager.initLoader(CITIES_LOADER_ID, null, this);
		loaderManager.initLoader(CAPITALS_LOADER_ID, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
		Log.d(TAG, "onCreateLoader: " + id);

		switch (id) {
			case CITIES_LOADER_ID:
				return new CursorLoader(
						mActivity,
						CitiesContract.Cities.CONTENT_URI,
						new String[]{
								CitiesContract.Cities._ID,
								CitiesContract.Cities.NAME
						},
						null,
						null,
						CitiesContract.Cities.NAME + " ASC"
				);

			case CAPITALS_LOADER_ID:
				return new CursorLoader(
						mActivity,
						CitiesContract.Cities.CAPITALS_CONTENT_URI,
						new String[]{
								CitiesContract.Cities._ID,
								CitiesContract.Cities.NAME
						},
						null,
						null,
						CitiesContract.Cities.NAME + " ASC"
				);

			default:
				throw new IllegalArgumentException("Unknown loader id: " + id);
		}
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
		Log.d(TAG, "onLoadFinished: " + loader.getId());

		switch (loader.getId()) {
			case CITIES_LOADER_ID:
				mCitiesAdapter.setCursor(cursor);
				break;

			case CAPITALS_LOADER_ID:
				mCapitalsAdapter.setCursor(cursor);
				break;

			default:
				throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
		}
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader) {
		Log.d(TAG, "onLoaderReset: " + loader.getId());

		switch (loader.getId()) {
			case CITIES_LOADER_ID:
				mCitiesAdapter.setCursor(null);
				break;

			case CAPITALS_LOADER_ID:
				mCapitalsAdapter.setCursor(null);
				break;

			default:
				throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
		}
	}

	private static class CitiesAdapter extends RecyclerView.Adapter<CitiesAdapter.ViewHolder> {

		private Cursor mCursor;
		private final Context mContext;

		private CitiesAdapter(Context context) {
			this.mContext = context;
		}

		public void setCursor(Cursor cursor) {
			this.mCursor = cursor;
			notifyDataSetChanged();
		}

		@Override
		public CitiesAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
			return new ViewHolder(LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_1, parent, false));
		}

		@Override
		public void onBindViewHolder(final CitiesAdapter.ViewHolder holder, final int position) {
			if (mCursor != null) {
				mCursor.moveToPosition(position);
				final String nameAndId = String.format(
						"#%d %s",
						mCursor.getLong(mCursor.getColumnIndex(CitiesContract.Cities._ID)),
						mCursor.getString(mCursor.getColumnIndex(CitiesContract.Cities.NAME))
				);
				holder.textView.setText(nameAndId);
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
