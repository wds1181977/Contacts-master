package com.android.contacts_master.list;

import android.annotation.SuppressLint;
import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.android.contacts_master.Constants;
import com.android.contacts_master.R;
import com.android.contacts_master.activity.ContactEditorActivity;
import com.android.contacts_master.activity.ContactInfoActivity;
import com.android.contacts_master.activity.PeopleActivity;
import com.android.contacts_master.provider.ContactsProjection;
import com.android.contacts_master.util.ImageLoader;
import com.melnykov.fab.FloatingActionButton;
import com.melnykov.fab.ScrollDirectionListener;

/**
 * Created by wds on 16-2-22.
 */
public class ContactsFragment extends Fragment {
    private  BackListener  mBackListener;
    public interface BackListener {
        void onUpBackPressed();
    }
    public void setBackListener(BackListener listener) {
        mBackListener = listener;
    }

    private static final int QUERY_TOKEN = 303;
    private ListView mContactsListView;
    private EditText mSearchTextView;
    //    private ContactAlphaView mContactAlphaView;
    private TextView mOverlayView;
    private View mEmptyView;
    private View mQueryProgressView;
    private TextView mEmptyTextView;
    private ImageButton mSearchClearButton;
    private static final int QUERY_COMPLETE = 0;
    private static final int QUERY_NO_CONTACTS = 1;
    private static final int QUERY_NO_RESULTS = 2;
    private WindowManager windowManager;
    private OverlayThread overlayThread;
    private boolean isHasCreated=false;
    protected ContactsAdapter mContactsAdapter;
    private QueryContactsHandler mQueryContactsHandler;
    private  String mQueryString;

    private static final String PERSONAL_QUERY_SELECTION =
            ContactsContract.Contacts.DISPLAY_NAME   + " LIKE ? || '%'" + " OR " +
                    ContactsContract.Contacts.DISPLAY_NAME   + " LIKE '%' || ' ' || ? || '%'" + " OR " +
                    ContactsContract.Contacts.DISPLAY_NAME   + " LIKE '+' || ? || '%'" + " OR " +
                    ContactsContract.Contacts.DISPLAY_NAME   + " LIKE '(' || ? || '%'";
    private static final String SORT_KEY_ORDER = "sort_key COLLATE LOCALIZED ASC";



    private Handler mStopLoadHandler = new Handler() {

        public void handleMessage(Message msg) {
            switch(msg.what){
                case QUERY_COMPLETE:
//                        mEmptyView.setVisibility(View.GONE);
//                        mQueryProgressView.setVisibility(View.GONE);
//                        mEmptyTextView.setVisibility(View.GONE);
                    break;
                case QUERY_NO_CONTACTS:
//                        mEmptyView.setVisibility(View.VISIBLE);
//                        mQueryProgressView.setVisibility(View.GONE);
//                        mEmptyTextView.setVisibility(View.VISIBLE);
//                        mEmptyTextView.setText(R.string.no_contacts);
                    break;
                case QUERY_NO_RESULTS:
//                        mEmptyView.setVisibility(View.VISIBLE);
//                        mQueryProgressView.setVisibility(View.GONE);
//                        mEmptyTextView.setVisibility(View.VISIBLE);
//                        mEmptyTextView.setText(R.string.no_results_found);
                    break;
            }
        }

    };



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PeopleActivity activity=(PeopleActivity) getActivity();

        activity.setListener(new PeopleActivity.Listener() {


            @Override
            public boolean onQueryTextChange(String queryString) {
                String newFilter = !TextUtils.isEmpty(queryString) ? queryString : null;
                /*
                Don't do anything if the filter hasn't actually changed.
                Prevents restarting the loader when restoring state.
                */
                if (mQueryString == null && newFilter == null) {
                    return true;
                }
                if (mQueryString != null && mQueryString.equals(newFilter)) {
                    return true;
                }

                mQueryString = queryString;


                startQuery();
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onClose() {

                return true;
            }
        });


    }



    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_listview, container, false);
        mContactsListView = (ListView) root.findViewById(R.id.contacts_listview);
        buildLayout(root);
        mQueryContactsHandler = new QueryContactsHandler(getActivity());
        mContactsAdapter = new ContactsAdapter(getActivity());
        mContactsListView.setAdapter(mContactsAdapter);

        FloatingActionButton fab = (FloatingActionButton) root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), ContactEditorActivity.class);
                startActivity(intent);
//                Uri insertUri = ContactsContract.Contacts.CONTENT_URI;
//                Intent intent = new Intent(Intent.ACTION_INSERT, insertUri);
//                startActivityForResult(intent, 1008);
            }
        });
        fab.attachToListView(mContactsListView, new ScrollDirectionListener() {
            @Override
            public void onScrollDown() {
             //   Log.d("ListViewFragment", "onScrollDown()");

            }

            @Override
            public void onScrollUp() {


            }
        }, new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
          overlayThread = new OverlayThread();

        isHasCreated = true;



        return root;
    }

    @Override
    public void onResume(){

        super.onResume();

        initOverlay();

        if(isHasCreated){
            startQuery();
            isHasCreated = false;
        }

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }



    private void initOverlay() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        mOverlayView = (TextView) inflater.inflate(R.layout.overlay, null);
        mOverlayView.setVisibility(View.INVISIBLE);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
        windowManager = (WindowManager) getActivity()
                .getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(mOverlayView, lp);
    }


    private void buildLayout(View v){
//
//
//            mContactsListView = (ListView) v.findViewById(R.id.contacts_listview);
//
//            mSearchTextView = (EditText) v.findViewById(R.id.search_edittext);
////            mSearchTextView.addTextChangedListener(mSearchTextWatcher);
////
////            mContactAlphaView = (ContactAlphaView) v.findViewById(R.id.contact_alpha_view);
////            mContactAlphaView.setOnAlphaChangedListener(this);
//
//            mEmptyView = v.findViewById(R.id.empty_layout);
//            mQueryProgressView = v.findViewById(R.id.query_proLoading);
//            mEmptyTextView = (TextView) v.findViewById(R.id.empty_textview);
//            mEmptyView.setVisibility(View.VISIBLE);
//            mQueryProgressView.setVisibility(View.VISIBLE);
//            mEmptyTextView.setVisibility(View.GONE);
//
//            mSearchClearButton = (ImageButton) v.findViewById(R.id.button_search_clear);
//            mSearchClearButton.setVisibility(View.GONE);
//            mSearchClearButton.setOnClickListener(new View.OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//                    mSearchTextView.getEditableText().clear();
//                }
//            });
//
////            mContactsListView.setOnTouchListener(mOnTouchListener);
////            mEmptyView.setOnTouchListener(mOnTouchListener);
//
        mContactsListView.setOnItemClickListener(onItemClickListener);
//
    }


    private void startQuery(){
        if(mQueryString!=null){
            startSearchQuery( mQueryString);
        } else {
            startEmptyQuery();
        }
    }

    private void startEmptyQuery(){


        if (mQueryContactsHandler == null) {

            return;
        }
        mQueryContactsHandler.cancelOperation(QUERY_TOKEN);

        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        mQueryContactsHandler.startQuery(QUERY_TOKEN, null, uri,
                ContactsProjection.PersonalContacts.PERSONAL_CONTACTS_SUMMARY_PROJECTION,
                null,
                null,
                SORT_KEY_ORDER);
    }

    private void startSearchQuery(String search){

        if(mContactsAdapter == null){

            return;
        }

        mContactsAdapter.getFilter().filter(search);
        if(mContactsAdapter.getCount() > 0){
            mContactsListView.setSelection(0);
        }
    }

    private void switchContactInfo(long personId){
        Intent intent = new Intent(getActivity(), ContactInfoActivity.class);
        intent.putExtra(Constants.EXTRA_CONTACT_PERSON_ID, personId);
        getActivity().startActivity(intent);

    }

    private class OverlayThread implements Runnable {

        @Override
        public void run() {
            mOverlayView.setVisibility(View.GONE);
        }

    }
    @Override
    public void onPause(){

        super.onPause();
        try {
            windowManager.removeViewImmediate(mOverlayView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy(){

        super.onDestroy();
        if (mQueryContactsHandler != null) {
            mQueryContactsHandler.cancelOperation(QUERY_TOKEN);
            mQueryContactsHandler = null;
        }

    }




    private  OnItemClickListener onItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(mContactsAdapter == null){

                return;
            }

            Cursor cursor = (Cursor) mContactsAdapter.getItem(position);
            if (cursor == null) {
                return;
            }

            long personId = cursor.getLong(ContactsProjection.PersonalContacts.ID_COLUMN_INDEX);

            switchContactInfo(personId);
        }
    };


    private class ContactItemView{
        public TextView sectionView;
        public TextView nameView;
        public ImageView photoView;

    }



    protected class ContactsAdapter extends ResourceCursorAdapter {

        private Context mContext;
            private ImageLoader mImageLoader;

        public ContactsAdapter(Context context) {
            super(context, R.layout.contacts_list_item_layout, null);
            this.mContext = context;
               mImageLoader = new ImageLoader(context, ImageLoader.TYPE_CONTACTS);
        }


        @Override
        public Cursor runQueryOnBackgroundThread(CharSequence constraint) {

            String what = constraint.toString();
            Cursor cursor = null;

            Uri uri = ContactsContract.Contacts.CONTENT_URI;
            cursor = mContext.getContentResolver().query(
                    uri,
                    ContactsProjection.PersonalContacts.PERSONAL_CONTACTS_SUMMARY_PROJECTION,
                    PERSONAL_QUERY_SELECTION,
                    new String[]{what, what, what, what},
                    SORT_KEY_ORDER);

            if(cursor == null || cursor.isClosed() || cursor.getCount() == 0){
                if(TextUtils.isEmpty(what)) {
                    mStopLoadHandler.sendEmptyMessage(QUERY_NO_CONTACTS);
                } else {
                    mStopLoadHandler.sendEmptyMessage(QUERY_NO_RESULTS);
                }
            } else {
                mStopLoadHandler.sendEmptyMessage(QUERY_COMPLETE);
            }
            return cursor;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = super.newView(context, cursor, parent);
            final ContactItemView cache = new ContactItemView();
            cache.sectionView = (TextView) view.findViewById(R.id.first_alpha_text);
            cache.nameView = (TextView) view.findViewById(R.id.name);
            cache.photoView = (ImageView) view.findViewById(R.id.image_view);

            view.setTag(cache);

            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            if(cursor == null || cursor.isClosed()){
                return;
            }
            final ContactItemView cache = (ContactItemView) view.getTag();
            String name = cursor.getString(ContactsProjection.PersonalContacts.DISPLAY_NAME_COLUMN_INDEX);

            cache.nameView.setText(TextUtils.isEmpty(name) ? "" : name);

//            if(isInSearching()){
//                cache.sectionView.setVisibility(View.GONE);
//            } else {
//
//            }                int position = cursor.getPosition();
//            int section = mAlphabetIndexer.getSectionForPosition(position);
//            if (position == mAlphabetIndexer.getPositionForSection(section)) {
//                cache.sectionView.setText(getTitle(name));
//                cache.sectionView.setVisibility(View.VISIBLE);
//                cache.sectionView.setOnClickListener(null);
//            } else {
//                cache.sectionView.setVisibility(View.GONE);
//            }

            long photoId = cursor.getLong(ContactsProjection.PersonalContacts.PHOTO_ID_COLUMN_INDEX);
            if(photoId <= 0){
            cache.photoView.setImageResource(R.drawable.icon_contact_list_default_round_photo);
                   return;
              }

          //  Picasso.with(mContext).load(String.valueOf(photoId)).into(cache.photoView);
                mImageLoader.displayImage(String.valueOf(photoId), cache.photoView);

        }
    }

    private final class QueryContactsHandler extends AsyncQueryHandler {

        public QueryContactsHandler(Context context) {
            super(context.getContentResolver());
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            if (cursor == null || cursor.isClosed() || !cursor.moveToFirst()) {
                mStopLoadHandler.sendEmptyMessage(QUERY_NO_CONTACTS);
                return;
            }

            if (!getActivity().isFinishing()) {
                //         mAlphabetIndexer = new AlphabetIndexer(cursor, PersonalContacts.SORT_KEY_INDEX, ALPHABET_INDEXS);

                if(cursor.getCount() > 0) {
                    mStopLoadHandler.sendEmptyMessage(QUERY_COMPLETE);
                } else {
                    mStopLoadHandler.sendEmptyMessage(QUERY_NO_CONTACTS);
                }

                mContactsAdapter.changeCursor(cursor);
            } else {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }
        }
    }



}


