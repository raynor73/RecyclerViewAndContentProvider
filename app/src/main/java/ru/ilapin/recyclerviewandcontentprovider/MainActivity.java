package ru.ilapin.recyclerviewandcontentprovider;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {

	private ContactsAdapter mContactsAdapter;
	private LinearLayoutManager mLayoutManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
		mLayoutManager = new LinearLayoutManager(this);
		recyclerView.setLayoutManager(mLayoutManager);

        final Cursor contactsCursor = getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                new String[]{
                        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
                },
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " ASC"
        );
        contactsCursor.moveToFirst();

		mContactsAdapter = new ContactsAdapter(this, contactsCursor);
		recyclerView.setAdapter(mContactsAdapter);
	}

	private static class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

        private final Cursor mCursor;
        private final Context mContext;

        private ContactsAdapter(MainActivity context, Cursor cursor) {
            this.mCursor = cursor;
            this.mContext = context;
        }

        @Override
		public ContactsAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
			return new ViewHolder(LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_1, parent, false));
		}

		@Override
		public void onBindViewHolder(final ContactsAdapter.ViewHolder holder, final int position) {
            mCursor.moveToPosition(position);

            holder.textView.setText(mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)));
		}

		@Override
		public int getItemCount() {
			return mCursor.getCount();
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
