package ru.ilapin.recyclerviewandcontentprovider;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

	private static final String TAG = "MainActivity";
	private static int sCounter;

	private final String[] mData = new String[100];
	private SomeAdapter mSomeAdapter;
	private LooperThread mLooperThread = new LooperThread();
	private Handler mHandler;
	private LinearLayoutManager mLayoutManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
		mLayoutManager = new LinearLayoutManager(this);
		recyclerView.setLayoutManager(mLayoutManager);

 		updateData();
		mSomeAdapter = new SomeAdapter();
		recyclerView.setAdapter(mSomeAdapter);

		/*mLooperThread.start();
		mLooperThread.mHandler.post(mDataUpdateRoutine);*/

		mHandler = new Handler(Looper.getMainLooper());
		mHandler.post(mDataUpdateRoutine);

		Log.d(TAG, Thread.currentThread().toString() + ": " + Thread.currentThread().getId());
		new Thread(new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, Thread.currentThread().toString() + ": " + Thread.currentThread().getId());
			}
		}).start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void updateData() {
		for (int i = 0; i < mData.length; i++) {
			mData[i] = String.format("Item #%02d(%d)", i, sCounter);
		}

		sCounter++;
	}

	private class SomeAdapter extends RecyclerView.Adapter<SomeAdapter.ViewHolder> {

		@Override
		public SomeAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
//			return new ViewHolder(LayoutInflater.from(MainActivity.this).inflate(android.R.layout.simple_list_item_1, parent, false));
			return new ViewHolder(LayoutInflater.from(MainActivity.this).inflate(R.layout.view_list_item, parent, false));
		}

		@Override
		public void onBindViewHolder(final SomeAdapter.ViewHolder holder, final int position) {
			holder.checkBox.setText(mData[position]);

			holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					//notifyDataSetChanged();
				}
			});
		}

		@Override
		public int getItemCount() {
			return mData.length;
		}

		public class ViewHolder extends RecyclerView.ViewHolder {

			public TextView textView;
			public CheckBox checkBox;

			public ViewHolder(final View itemView) {
				super(itemView);

				textView = (TextView) itemView.findViewById(android.R.id.text1);
				checkBox = (CheckBox) itemView.findViewById(R.id.check_box);
			}
		}
	}

	private final Runnable mDataUpdateRoutine = new Runnable() {

		@Override
		public void run() {
			updateData();
			mSomeAdapter.notifyDataSetChanged();
//			mLooperThread.mHandler.postDelayed(this, 1000);
			mHandler.postDelayed(this, 1000);
		}
	};

	private class LooperThread extends Thread {
		public Handler mHandler;

		public void run() {
			Looper.prepare();
			mHandler = new Handler();
			mHandler.post(mDataUpdateRoutine);
			Looper.loop();
		}
	}
}
