package com.android.contacts_master.activity;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.android.contacts_master.R;
import com.android.contacts_master.list.ContactsFragment;

public class PeopleActivity extends AppCompatActivity implements OnQueryTextListener,SearchView.OnCloseListener,ContactsFragment.BackListener  {


    Listener  mListener;

    private String mQueryString;
    private  SearchView mSearchView;

    @Override
    public void onUpBackPressed() {
        onBackPressed();
    }


    public interface Listener {

        public boolean onQueryTextChange(String queryString) ;

        public boolean onQueryTextSubmit(String query) ;

        public boolean onClose();



    }

    public void setListener(Listener listener){
        this.mListener=listener;
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
            ActionBar actionBar = getSupportActionBar();

            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

            actionBar.addTab(actionBar.newTab()
                    .setText("")
                    .setTabListener(new ActionBar.TabListener() {
                        @Override
                        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                            fragmentTransaction.replace(android.R.id.content, new ContactsFragment());
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


        MenuItem item = menu.add("Search");
        item.setIcon(R.drawable.ic_search);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
                | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        setupSearchView();
           item.setActionView(mSearchView);

        getMenuInflater().inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();

    }

    private void setupSearchView(){

       mSearchView = new SearchView(this);
        if(mSearchView!=null) {
            mSearchView.setQueryHint(getString(R.string.search_contacts));
            mSearchView.setOnQueryTextListener(this);
            mSearchView.setOnCloseListener(this);
            mSearchView.setIconifiedByDefault(true);

            // 设置该SearchView显示搜索按钮
            mSearchView.setSubmitButtonEnabled(false);

            mSearchView.setQuery(mQueryString, false);
            int id = mSearchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);

            TextView textView = (TextView) mSearchView.findViewById(id);
            textView.setTextColor(Color.WHITE);
            textView.setHintTextColor(Color.parseColor("#CCCCCC"));

            //        Class<?> argClass=mSearchView.getClass();
//           //指定某个私有属性
//           Field mSearchHintIconField = null;
//           try {
//               mSearchHintIconField = argClass.getDeclaredField("mSearchHintIcon");
//
//           mSearchHintIconField.setAccessible(true);
//           View mSearchHintIcon = (View)mSearchHintIconField.get(mSearchView);
//
//
//           //注意mSearchPlate的背景是stateListDrawable(不同状态不同的图片)  所以不能用BitmapDrawable
//           Field ownField = argClass.getDeclaredField("mSearchPlate");
//           //setAccessible 它是用来设置是否有权限访问反射类中的私有属性的，只有设置为true时才可以访问，默认为false
//           ownField.setAccessible(true);
//           View mView = (View) ownField.get(mSearchView);
//           mView.setBackground(getResources().getDrawable(R.drawable.ic_search));
//
//           } catch (NoSuchFieldException e) {
//               e.printStackTrace();
//           }
//           catch (IllegalAccessException e) {
//               e.printStackTrace();
//           }
        }

    }



    @Override
    public boolean onQueryTextChange(String queryString) {

       if (mListener != null) {
                    mListener.onQueryTextChange(queryString);

                }



        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        if (mListener != null) {
            mListener.onQueryTextSubmit(query);
        }
        return true;
    }
    @Override
    public boolean onClose() {
        if (mListener != null) {
            mListener.onClose();
        }
        return true;
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.about:{


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


}