package com.android.contacts_master.activity;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import android.widget.LinearLayout;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.android.contacts_master.R;
import com.android.contacts_master.list.ContactsFragment;

public class PeopleActivity extends AppCompatActivity {


    Listener mListener;


    private EditText mSearchView;
    /**
     * The view that represents tabs when we are in portrait mode
     **/
    private View mSearchContainer;
    /**
     * The view that represents tabs when we are in landscape mode
     **/
    private boolean mSearchMode;
    private String mQueryString;
    private ActionBar actionBar;
    private ContactsFragment  mContactsFragment;

    public interface Listener {

        public boolean onQueryTextChange(String queryString);

        public boolean onQueryTextSubmit(String query);




    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActionBar();
    }


    @SuppressWarnings("deprecation")
    private void initActionBar() {
        if (getSupportActionBar() != null) {
            actionBar = getSupportActionBar();
            mContactsFragment= new ContactsFragment();
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            setupSearchView();
            actionBar.addTab(actionBar.newTab()
                    .setText("")
                    .setTabListener(new ActionBar.TabListener() {
                        @Override
                        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                            fragmentTransaction.replace(android.R.id.content,mContactsFragment);
                        }

                        @Override
                        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                        }

                        @Override
                        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                        }
                    }));

        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        getMenuInflater().inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {


            case R.id.action_search: {
                setSearchMode(true);

                return true;
            }

            case R.id.about: {


                TextView content = (TextView) getLayoutInflater().inflate(R.layout.about_view, null);
                content.setMovementMethod(LinkMovementMethod.getInstance());
                content.setText(Html.fromHtml(getString(R.string.about_body)));
                new AlertDialog.Builder(this)
                        .setTitle(R.string.about)
                        .setView(content)
                        .setInverseBackgroundForced(true)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
                return true;

            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (isSearchMode()) {
            setSearchMode(false);
        } else {
            super.onBackPressed();
        }

    }

    private void setupSearchView() {
        final LayoutInflater inflater = (LayoutInflater) getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        mSearchContainer = inflater.inflate(R.layout.search_bar_expanded,
                new LinearLayout(this), false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(mSearchContainer);
        mSearchContainer.setBackgroundColor(getResources().getColor(
                R.color.searchbox_background_color));
        mSearchView = (EditText) mSearchContainer.findViewById(R.id.search_view);
        mSearchView.setHint(getString(R.string.hint_findContacts));
        /// M: FOR CT NEW COMMON FEATURE from CT ,IME should keep english whole key when search
        mSearchView.setInputType(EditorInfo.TYPE_CLASS_TEXT
                | EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        mSearchView.addTextChangedListener(new SearchTextWatcher());
        mSearchContainer.findViewById(R.id.search_close_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setQueryString(null);
                    }
                });
        mSearchContainer.findViewById(R.id.search_back_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });
    }

    private class SearchTextWatcher implements TextWatcher {

        @Override
        public void onTextChanged(CharSequence queryString, int start, int before, int count) {
            if (queryString.equals(mQueryString)) {
                return;
            }
            mQueryString = queryString.toString();
            if (!mSearchMode) {
                if (!TextUtils.isEmpty(queryString)) {
                    setSearchMode(true);
                }
            } else if (mListener != null) {
               mListener.onQueryTextChange(mQueryString);

            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
    }

    /**
     * @return Whether in search mode, i.e. if the search view is visible/expanded.
     * <p/>
     * Note even if the action bar is in search mode, if the query is empty, the search fragment
     * will not be in search mode.
     */
    public boolean isSearchMode() {
        return mSearchMode;
    }

    public void setSearchMode(boolean SearchMode) {

        if (mSearchView == null) {
            return;
        }
        if (SearchMode) {
            mSearchContainer.setVisibility(View.VISIBLE);
            mSearchView.setEnabled(true);
            setFocusOnSearchView();
            mSearchMode = true;
        } else {
            // Disable search view, so that it doesn't keep the IME visible.
            mSearchView.setEnabled(false);
            setQueryString(null);
            mSearchMode = false;
            mSearchContainer.setVisibility(View.GONE);
        }

    }


    public void setFocusOnSearchView() {
        mSearchView.requestFocus();
        showInputMethod(mSearchView); // Workaround for the "IME not popping up" issue.
    }

    private void showInputMethod(View view) {
        final InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, 0);
        }
    }


    public String getQueryString() {
        return mSearchMode ? mQueryString : null;
    }

    public void setQueryString(String query) {
        mQueryString = query;
        if (mSearchView != null) {
            mSearchView.setText(query);
            // When programmatically entering text into the search view, the most reasonable
            // place for the cursor is after all the text.
            mSearchView.setSelection(mSearchView.getText() == null ?
                    0 : mSearchView.getText().length());
        }
    }




}