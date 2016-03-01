package com.android.contacts_master.activity;


import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.contacts_master.Constants;
import com.android.contacts_master.R;
import com.android.contacts_master.editor.ContactEditorFragment;
import com.android.contacts_master.util.ContactsUtils;


public class ContactEditorActivity extends AppCompatActivity implements ContactEditorFragment.OnFragmentInteractionListener {
    View mSaveMenuItem;
    private ContactEditorFragment mFragment;
    boolean isEditMode=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

      setContentView(R.layout.activity_contact_editor);

        final Intent intent = getIntent();
        final String action = intent.getAction();
        if (getSupportActionBar() != null) {
            ActionBar actionBar = getSupportActionBar();

            LayoutInflater inflater = (LayoutInflater) getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            View customActionBarView = inflater.inflate(R.layout.editor_custom_action_bar, null);
            mSaveMenuItem = customActionBarView.findViewById(R.id.save_menu_item);
            mSaveMenuItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFragment.doSaveAction();
                    /** M: Add for ALPS01566939 @ {***/

                    /** @ }**/
                }
            });
            TextView title = (TextView) customActionBarView.findViewById(R.id.title);
            if (Intent.ACTION_EDIT.equals(action)) {
                title.setText(getResources().getString(
                        R.string.edit));

            } else {

                title.setText(getResources().getString(
                        R.string.menu_done));
            }
            // Show the custom action bar but hide the home icon and title
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME |
                            ActionBar.DISPLAY_SHOW_TITLE);
            actionBar.setCustomView(customActionBarView);
        }

       mFragment = (ContactEditorFragment) getFragmentManager().findFragmentById(
                R.id.contact_editor_fragment);
        mFragment.setListener(this);
        long  ContactId = Intent.ACTION_EDIT.equals(action) ? getIntent().getLongExtra(Constants.EXTRA_CONTACT_PERSON_ID, -1) : -1;
        mFragment.load(action, ContactId);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        getMenuInflater().inflate(R.menu.edit_contact, menu);

        return super.onCreateOptionsMenu(menu);


    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_diss:{
                 finish();
            }

        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        mFragment.doSaveAction();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        onBackPressed();
    }
}
