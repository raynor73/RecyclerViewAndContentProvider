package ru.ilapin.recyclerviewandcontentprovider.ui.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import ru.ilapin.recyclerviewandcontentprovider.R;
import ru.ilapin.recyclerviewandcontentprovider.ui.fragments.ContactsLoaderFragment;
import ru.ilapin.recyclerviewandcontentprovider.ui.fragments.CustomProviderFragment;


public class MainActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		FragmentManager fragmentManager = getSupportFragmentManager();
		if (fragmentManager.findFragmentById(R.id.container) == null) {
			fragmentManager
					.beginTransaction()
					.add(R.id.container, new ContactsLoaderFragment())
					.commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		switch (item.getItemId()) {
			case R.id.menu_item_contacts_content_provider:
				fragmentManager
						.beginTransaction()
						.add(R.id.container, new ContactsLoaderFragment())
						.addToBackStack(null)
						.commit();
				return true;

			case R.id.menu_item_custom_provider:
				fragmentManager
						.beginTransaction()
						.add(R.id.container, new CustomProviderFragment())
						.addToBackStack(null)
						.commit();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
