package ru.ilapin.recyclerviewandcontentprovider;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.PilotCards.Common.*;
import com.PilotCards.Database.BrandDao;
import com.PilotCards.Database.HelperFactory;
import com.PilotCards.Database.PosDao;
import com.PilotCards.EventManager.EventListener;
import com.PilotCards.EventManager.EventType;
import com.PilotCards.ImageProcessor.ImageProcessor;
import com.PilotCards.ImageProcessor.RecyclingImageView;
import com.PilotCards.Model.*;
import com.PilotCards.R;
import com.PilotCards.Views.AddCardScreen.NewPhoto.TakeCardFrontPhoto;
import com.PilotCards.Views.MainScreen.MainActivity;
import com.PilotCards.Views.MainScreen.OffersAndCards.CardListCursorAdapter;
import com.PilotCards.Views.MainScreen.OffersAndCards.OfferListCursorAdapter;
import com.PilotCards.Views.Widgets.ActionBar.ActionBar;
import com.PilotCards.Views.Widgets.ActionBar.ActionBarButton;
import com.PilotCards.Views.Widgets.CustomButton;
import com.PilotCards.Views.Widgets.CustomImageButton;
import com.PilotCards.Views.Widgets.TabBar.UberTabBar;
import com.PilotCards.Views.Widgets.TabBar.UberTabView;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.List;

/**
 * @author Scalar
 *         Class PosActivity description
 */
public class PosActivity extends HelpOverlayActivity {
	public static final String POS_ID = "POS_ID";
	public static final String BRAND_SERVER_ID = "BRAND_SERVER_ID";

	public static final float ACTIONS_TAB_WIDTH_FACTOR = 0.3314814814814815f;
	public static final float CARDS_TAB_WIDTH_FACTOR = 0.3342592592592593f;
	public static final float NEARBY_TAB_WIDTH_FACTOR = 0.3342592592592593f;

	private static final int HELP_BUTTON_ID = 0x00000001;
	private static final int BACK_BUTTON_ID = 0x00000002;
	private static final int CARD_LIST_LOADER = 111;
	private static final int OFFER_LIST_LOADER = 222;
	private static final String TAG = "PosActivity(!@#)";
	private int id;
	private int brandServerId;

	private LinearLayout layout;
	private LinearLayout tabsContainer;

	private UberTabBar tabBar;
	private UberTabView offersTab, contactsTab, cardsTab;
	private ViewGroup offersContent, cardsContent;
	private ScrollView contactsContent;
	private LinearLayout contactsLayout;
	private CustomButton feedbackButton;
	private CustomImageButton workTimeButton;
	private CustomImageButton mapButton;
	private View workTimeLayout;
	private TableLayout workTimeTable;
	private ListView offerList, cardList;
	private RecyclingImageView brandImageView;

	private Pos pos;

	private boolean workTimeListVisible;

	private EventListener offersEventListener = new EventListener() {
		@Override
		public void onEventFired(EventType type, Object data) {
			getSupportLoaderManager().restartLoader(OFFER_LIST_LOADER, null, offerListLoaderCallbacks);
		}
	};

	private EventListener cardsEventListener = new EventListener() {
		@Override
		public void onEventFired(EventType type, Object data) {
			getSupportLoaderManager().restartLoader(CARD_LIST_LOADER, null, cardListLoaderCallbacks);
		}
	};

	private OfferListCursorAdapter offerListCursorAdapter;
	private CardListCursorAdapter cardListCursorAdapter;
	private LoaderManager.LoaderCallbacks<Cursor> cardListLoaderCallbacks;
	private LoaderManager.LoaderCallbacks<Cursor> offerListLoaderCallbacks;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pos);

		setupActivity();

		offerListCursorAdapter = new OfferListCursorAdapter(this);
		cardListCursorAdapter = new CardListCursorAdapter(this);

		initActionBar();
		initTabBar();
		tabBar.selectTab(contactsTab, false);

		setupListViews();
		initLoaderCallbacks();
	}

	@Override
	protected void onResume() {
		super.onResume();

		id = getIntent().getIntExtra(POS_ID, 0);
		brandServerId = getIntent().getIntExtra(BRAND_SERVER_ID, 0);

		Brand brand;
		PosDao posDao;
		try {
			posDao = HelperFactory.getHelper().getPosDao();
			BrandDao brandDao = HelperFactory.getHelper().getBrandDao();
			pos = posDao.queryForId(id);
			brand = brandDao.findByServerId(brandServerId);
		} catch (SQLException e) {
			throw new RuntimeException("SQLException: " + e.getMessage());
		}

		if (!checkMainModel(pos, resources.getString(R.string.POS_ACTIVITY_DELETED_MESSAGE))) {
			return;
		}

		Pos.parseRemainingDataIfNecessary(id);

		fillViewsWithData(pos);
		fillViewsWithData(brand);

		ImageCache.Options options =
				new ImageCache.Options(ImageProcessor.Options.ProcessingType.WIDTH_AND_HEIGHT_KNOWN);
		options.width = (int) resources.getDimension(R.dimen.posScreenBrandLogoSize);
		options.height = (int) resources.getDimension(R.dimen.posScreenBrandLogoSize);
		imageCache.showBitmapInImageView(brand.getLogo(), brandImageView, options, null, "PosActivity");

		displayContactsData();
		buildWorkTimeList();

		eventManager.obtain(EventType.OFFERS_CHANGED).addListener(offersEventListener);
		eventManager.obtain(EventType.CARDS_CHANGED).addListener(cardsEventListener);

		getSupportLoaderManager().restartLoader(CARD_LIST_LOADER, null, cardListLoaderCallbacks);
		getSupportLoaderManager().restartLoader(OFFER_LIST_LOADER, null, offerListLoaderCallbacks);
	}

	@Override
	protected void onPause() {
		super.onPause();

		eventManager.obtain(EventType.OFFERS_CHANGED).removeListener(offersEventListener);
		eventManager.obtain(EventType.CARDS_CHANGED).removeListener(cardsEventListener);
	}

	@Override
	protected void findViews() {
		super.findViews();

		layout = (LinearLayout) findViewById(R.id.complex_navigation_layout);
		tabsContainer = (LinearLayout) findViewById(R.id.tabsContainer);

		offersContent = (ViewGroup) findViewById(R.id.offersContent);
		contactsContent = (ScrollView) findViewById(R.id.contactsContent);
		cardsContent = (ViewGroup) findViewById(R.id.cardsContent);

		contactsLayout = (LinearLayout) findViewById(R.id.contactsLayout);

		feedbackButton = (CustomButton) findViewById(R.id.feedbackButton);
		workTimeButton = (CustomImageButton) findViewById(R.id.workTimeButton);
		mapButton = (CustomImageButton) findViewById(R.id.mapButton);

		workTimeLayout = findViewById(R.id.workTimeLayout);
		workTimeTable = (TableLayout) findViewById(R.id.workTimeTable);

		offerList = (ListView) findViewById(R.id.offerList);
		cardList = (ListView) findViewById(R.id.cardList);

		brandImageView = (RecyclingImageView) findViewById(R.id.brandImage);
	}

	@Override
	protected void addViewListeners() {
		feedbackButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(PosActivity.this, FeedbackActivity.class);
				intent.putExtra(FeedbackActivity.BRAND_ID, brandServerId);
				intent.putExtra(FeedbackActivity.POS_ID, id);
				startActivity(intent);
			}
		});

		workTimeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showWorkTimeList();
			}
		});

		workTimeLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				hideWorkTimeList();
			}
		});

		mapButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(PosActivity.this, MainActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				intent.putExtra(MainActivity.SHOW_POS_ON_MAP_ID, id);
				intent.putExtra(MainActivity.POS_CARD_BRAND_SERVER_ID, brandServerId);
				intent.putExtra(MainActivity.RESET_POSES_SHOWN_FLAG, true);
				startActivity(intent);
			}
		});

		offerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
				Intent intent = new Intent(PosActivity.this, OfferActivity.class);
				intent.putExtra(OfferActivity.OFFER_ID, (int) id);
				startActivity(intent);
			}
		});

		cardList.setOnItemClickListener(new OnCardClickListener(this));
	}

	@Override
	public void onBackPressed() {
		if (workTimeListVisible) {
			hideWorkTimeList();
		} else {
			super.onBackPressed();
		}
	}

	private void initLoaderCallbacks() {
		initCardListLoaderCallbacks();
		initOfferListLoaderCallbacks();
	}

	private void initOfferListLoaderCallbacks() {
		offerListLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
			@Override
			public Loader<Cursor> onCreateLoader(int id, Bundle args) {
				return new SqlCursorLoader(
						PosActivity.this,
						HelperFactory.getHelper().getReadableDatabase(),
						"SELECT " +
								Offer.TABLE_NAME + "." + Offer.ID_COLUMN_NAME + ", " +
								Offer.TABLE_NAME + "." + Offer.THUMB_COLUMN_NAME + ", " +
								Offer.TABLE_NAME + "." + Offer.PROMO_TITLE_COLUMN_NAME + ", " +
								Offer.TABLE_NAME + "." + Offer.START_TIME_COLUMN_NAME + ", " +
								Offer.TABLE_NAME + "." + Offer.FINISH_TIME_COLUMN_NAME + ", " +
								Brand.TABLE_NAME + "." + Brand.NAME_COLUMN_NAME +

								" FROM " + Offer.TABLE_NAME +

								" LEFT OUTER JOIN " + Offer2Pos.TABLE_NAME + " ON " +
								Offer2Pos.TABLE_NAME + "." + Offer2Pos.OFFER_ID_COLUMN_NAME + " = " +
								Offer.TABLE_NAME + "." + Offer.ID_COLUMN_NAME +

								" LEFT OUTER JOIN " + Brand.TABLE_NAME + " ON " +
								Brand.TABLE_NAME + "." + Brand.ID_COLUMN_NAME + " = " +
								Offer.TABLE_NAME + "." + Offer.BRAND_ID_COLUMN_NAME +

								" LEFT OUTER JOIN " + Offer2Brand.TABLE_NAME + " ON " +
								Offer2Brand.TABLE_NAME + "." + Offer2Brand.OFFER_ID_COLUMN_NAME + " = " +
								Offer.TABLE_NAME + "." + Offer.ID_COLUMN_NAME +

								" WHERE " +
								Offer2Pos.TABLE_NAME + "." + Offer2Pos.POS_SERVER_ID_COLUMN_NAME + " = ? OR " +
								Offer2Brand.TABLE_NAME + "." + Offer2Brand.BRAND_SERVER_ID_COLUMN_NAME + " = ?",
						new String[]{
								String.valueOf(PosActivity.this.id),
								String.valueOf(PosActivity.this.brandServerId)
						}
				);
			}

			@Override
			public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
				offerListCursorAdapter.swapCursor(cursor);
			}

			@Override
			public void onLoaderReset(Loader<Cursor> loader) {
				offerListCursorAdapter.swapCursor(null);
			}
		};
	}

	private void initCardListLoaderCallbacks() {
		cardListLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
			@Override
			public Loader<Cursor> onCreateLoader(int id, Bundle args) {
				return new SqlCursorLoader(
						PosActivity.this,
						HelperFactory.getHelper().getReadableDatabase(),
						"SELECT " +
								Card.TABLE_NAME + "." + Card.ID_COLUMN_NAME + ", " +
								Card.TABLE_NAME + "." + Card.FRONT_THUMB_COLUMN_NAME + ", " +
								Card.TABLE_NAME + "." + Card.SHARING_STATUS_COLUMN_NAME + ", " +
								Card.TABLE_NAME + "." + Card.BRAND_ID_COLUMN_NAME + ", " +
								Card.TABLE_NAME + "." + Card.FORM_INSISTENT_TYPE_COLUMN_NAME + ", " +
								Card.TABLE_NAME + "." + Card.FORM_FILLED_COLUMN_NAME + ", " +
								Card.TABLE_NAME + "." + Card.OFFERS_COUNT_COLUMN_NAME + ", " +
								Brand.TABLE_NAME + "." + Brand.NAME_COLUMN_NAME + ", " +
								"(CASE WHEN " + Card.TABLE_NAME + "." + Card.LAST_USAGE_COLUMN_NAME + " > 0 THEN STRFTIME('%s', 'NOW') - STRFTIME('%s', lastUsage) ELSE 0 END + " +
								"STRFTIME('%s', 'NOW') - STRFTIME('%s', " + Card.TABLE_NAME + "." + Card.ISSUING_TIME_COLUMN_NAME + ")) / 86400.0 / 7.0 AS sortIndex" + ", " +
								Card.TABLE_NAME + "." + Card.OFFERS_COUNT_COLUMN_NAME + ", "+
								Brand.TABLE_NAME + "." + Brand.SERVER_ID_COLUMN_NAME + " as brandServerId , " +
								Card.TABLE_NAME + "." + Card.SERVER_ID_COLUMN_NAME + " as cardServerId  " +
								" FROM " + Pos2CardActivity.TABLE_NAME +
								" INNER JOIN " + Card.TABLE_NAME + " ON " +
								Card.TABLE_NAME + "." + Card.SERVER_ID_COLUMN_NAME +
								" = " +
								Pos2CardActivity.TABLE_NAME + "." + Pos2CardActivity.CARD_SERVER_ID_COLUMN_NAME +
								" LEFT JOIN " + Brand.TABLE_NAME + " ON " +
								Brand.TABLE_NAME + "." + Brand.ID_COLUMN_NAME + " = " +
								Card.TABLE_NAME + "." + Card.BRAND_ID_COLUMN_NAME +
								" WHERE " +
								Pos2CardActivity.TABLE_NAME + "." + Pos2CardActivity.POS_ID_COLUMN_NAME + " = ?" +
								" ORDER BY sortIndex",
						new String[]{String.valueOf(PosActivity.this.id)});
			}

			@Override
			public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
				cardListCursorAdapter.swapCursor(cursor);
			}

			@Override
			public void onLoaderReset(Loader<Cursor> loader) {
				cardListCursorAdapter.swapCursor(null);
			}
		};
	}

	private void showWorkTimeList() {
		workTimeLayout.setVisibility(View.VISIBLE);
		workTimeListVisible = true;
	}

	private void hideWorkTimeList() {
		workTimeLayout.setVisibility(View.GONE);
		workTimeListVisible = false;
	}

	private void initTabBar() {
		tabBar = new UberTabBar(this);
		int height = (int) Utils.convertDpToPixel(37);

		tabBar.setDefaultTabColor(0xffffffff);
		tabBar.setDefaultTextColor(0xff88bfff);
		tabBar.setSelectedTextColor(0xff7cb6fc);
		tabBar.setTransitionTabColor(0xffdbe9fc);

		offersTab = new UberTabView(this, "Акции", 0, ViewGroup.LayoutParams.WRAP_CONTENT, DeviceInfo.getSizeRelativeToWidth(ACTIONS_TAB_WIDTH_FACTOR));
		contactsTab = new UberTabView(this, "Контакты", 0, ViewGroup.LayoutParams.WRAP_CONTENT, DeviceInfo.getSizeRelativeToWidth(CARDS_TAB_WIDTH_FACTOR));
		cardsTab = new UberTabView(this, "Карты", 0, ViewGroup.LayoutParams.WRAP_CONTENT, DeviceInfo.getSizeRelativeToWidth(NEARBY_TAB_WIDTH_FACTOR));

		offersTab.setTypeface(Fonts.getRegularFont());
		offersTab.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
		offersTab.setHeight(height);

		contactsTab.setTypeface(Fonts.getRegularFont());
		contactsTab.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
		contactsTab.setHeight(height);

		cardsTab.setTypeface(Fonts.getRegularFont());
		cardsTab.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
		cardsTab.setHeight(height);

		tabBar.addTab(offersTab);
		tabBar.addTab(contactsTab);
		tabBar.addTab(cardsTab);

		tabBar.setTabListener(new UberTabBar.TabListener() {
			@Override
			public void onTabSelected(UberTabView tab) {
				updateTabContentVisibility();

				if (tab == offersTab && offerList.getAdapter() == null) {
					offerList.addHeaderView(getLayoutInflater().inflate(R.layout.li_header_subst,null),null,false);
					offerList.setAdapter(offerListCursorAdapter);
				} else if (tab == cardsTab && cardList.getAdapter() == null) {
					cardList.addHeaderView(getLayoutInflater().inflate(R.layout.li_header_subst,null),null,false);
					cardList.addFooterView(getLayoutInflater().inflate(R.layout.li_footer_subst,null), null, false);

					cardList.setAdapter(cardListCursorAdapter);
				}
			}
		});

		tabsContainer.addView(tabBar, 0);
	}

	private void updateTabContentVisibility() {
		offersContent.setVisibility(View.GONE);
		contactsContent.setVisibility(View.GONE);
		cardsContent.setVisibility(View.GONE);

		if (tabBar.getCurrentTab() == offersTab) {
			offersContent.setVisibility(View.VISIBLE);
		} else if (tabBar.getCurrentTab() == contactsTab) {
			contactsContent.setVisibility(View.VISIBLE);
		} else if (tabBar.getCurrentTab() == cardsTab) {
			cardsContent.setVisibility(View.VISIBLE);
		}
	}

	private void initActionBar() {
		ActionBar actionBar = new ActionBar(this);
		actionBar.showShadow();

		initHelp();

		ActionBarButton helpButton = new ActionBarButton(this, HELP_BUTTON_ID, R.drawable.help_button);
		helpButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showHelp();
			}
		});
		actionBar.addRightButton(helpButton);

		ActionBarButton backButton = new ActionBarButton(this, BACK_BUTTON_ID, R.drawable.left_arrow_button);
		backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				PosActivity.this.onBackPressed();
			}
		});
		actionBar.addLeftButton(backButton);

		layout.addView(actionBar, 0);
	}

	private void buildWorkTimeList() {
		try {
			workTimeTable.removeAllViews();

			for (int i = 0; i < 7; i++) {
				PosWorkTimeBand.DayOfWeek currentDay = PosWorkTimeBand.DayOfWeek.valueOf(i);
				List<PosWorkTimeBand> bands = pos.getWorkTimeBandsForDayOfWeek(currentDay);

				if (bands.size() == 0) {
					TableRow row = (TableRow) getLayoutInflater().inflate(R.layout.work_time_row, null);

					TextView dayOfWeekTitleTextView = (TextView) row.findViewById(R.id.dayOfWeekTitle);
					dayOfWeekTitleTextView.setText(resources.getStringArray(R.array.DAY_OF_WEEK)[i]);
					dayOfWeekTitleTextView.setTextColor(0xffff0000);

					Utils.setupFonts(row);

					TextView workTimeTextView = (TextView) row.findViewById(R.id.workTime);
					workTimeTextView.setText(resources.getString(R.string.DAY_OFF));
					workTimeTextView.setTextColor(0xffff0000);

					workTimeTable.addView(row);

					continue;
				}

				for (int j = 0; j < bands.size(); j++) {
					TableRow row = (TableRow) getLayoutInflater().inflate(R.layout.work_time_row, null);
					if (j == 0) {
						((TextView) row.findViewById(R.id.dayOfWeekTitle)).setText(resources.getStringArray(R.array.DAY_OF_WEEK)[i]);
					}

					Utils.setupFonts(row);
					Utils.fillViewsWithData(bands.get(j), row);

					workTimeTable.addView(row);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("SQLException: " + e.getMessage());
		}
	}

	private void displayContactsData() {
		contactsLayout.removeAllViews();

		for (PosContact contact : pos.getContacts()) {
			if (contact.getType() == PosContact.Type.PHONE) {
				displayPhoneContact(contact);
			} else {
				TextView contactTextView = new TextView(this);

				String targetAddress = contact.getValue();

				contactTextView.setTextColor(0xff88bfff);
				String name = contact.getName();
				SpannableString content = new SpannableString(StringUtils.isEmpty(name) ? targetAddress : name);
				content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
				contactTextView.setText(content);

				if (contact.getType() == PosContact.Type.URL) {
					contactTextView.setOnClickListener(new UrlClickListener(this, targetAddress));
				} else {
					contactTextView.setOnClickListener(new EmailClickListener(this, targetAddress));
				}

				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				layoutParams.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics()), 0, 0);
				contactTextView.setLayoutParams(layoutParams);

				contactTextView.setTypeface(Fonts.getBoldFont());
				contactTextView.setTextSize(19);

				contactsLayout.addView(contactTextView);
			}
		}
	}

	private void displayPhoneContact(final PosContact contact) {
		FrameLayout contactLayout =
				(FrameLayout) getLayoutInflater().inflate(R.layout.pos_phone_contact, null);

		Utils.setupFonts(contactLayout);
		Utils.fillViewsWithData(contact, contactLayout);

		contactLayout.findViewById(R.id.makeCall).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(Intent.ACTION_DIAL);
				intent.setData(Uri.parse("tel:" + contact.getValue()));
				startActivity(intent);
			}
		});

		contactsLayout.addView(contactLayout);
	}

	private void setupListViews() {
		offerList.setEmptyView(getEmptyTextView(R.id.empty_pos_list));
		setupEmptyCardList();

		contactsContent.setVerticalFadingEdgeEnabled(false);
		cardList.setVerticalFadingEdgeEnabled(false);
		offerList.setVerticalFadingEdgeEnabled(false);
	}

	private void setupEmptyCardList() {
		LinearLayout emptyListLayout = (LinearLayout) findViewById(R.id.empty_card_list_layout);
		TextView emptyTextView = (TextView) findViewById(R.id.empty_card_list_text);
		emptyTextView.setTypeface(Fonts.getLightFont());
		findViewById(R.id.empty_card_list_add_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Card card = new Card();
				try {
					HelperFactory.getHelper().getCardDao().create(card);
				} catch (SQLException e) {
					throw new RuntimeException("SQLException: " + e.getMessage());
				}

				Intent intent = new Intent(PosActivity.this, TakeCardFrontPhoto.class);
				intent.putExtra(TakeCardFrontPhoto.CARD_ID, card.getId());
				startActivity(intent);
			}
		});
		cardList.setEmptyView(emptyListLayout);
	}

	private TextView getEmptyTextView(int id) {
		TextView emptyTextView = (TextView) findViewById(id);
		emptyTextView.setTypeface(Fonts.getLightFont());
		return emptyTextView;
	}
}
