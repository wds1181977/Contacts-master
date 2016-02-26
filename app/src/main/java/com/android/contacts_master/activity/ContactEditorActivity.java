package com.android.contacts_master.activity;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.android.contacts_master.R;
import com.android.contacts_master.editor.ContactEditorFragment;

public class ContactEditorActivity extends AppCompatActivity {
    View mSaveMenuItem;
    private ContactEditorFragment mFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_editor);

        if (getSupportActionBar() != null) {
            ActionBar actionBar = getSupportActionBar();
            // Inflate a custom action bar that contains the "done" button for saving changes
            // to the contact
            LayoutInflater inflater = (LayoutInflater) getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            View customActionBarView = inflater.inflate(R.layout.editor_custom_action_bar, null);
            mSaveMenuItem = customActionBarView.findViewById(R.id.save_menu_item);
            mSaveMenuItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                //    mFragment.doSaveAction();
                    /** M: Add for ALPS01566939 @ {***/

                    /** @ }**/
                }
            });
            TextView title = (TextView) customActionBarView.findViewById(R.id.title);
//            if (Intent.ACTION_EDIT.equals(action)) {
//                title.setText(getResources().getString(
//                        R.string.contact_editor_title_existing_contact));
//            } else {
//                title.setText(getResources().getString(
//                        R.string.contact_editor_title_new_contact));
//            }
            // Show the custom action bar but hide the home icon and title
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME |
                            ActionBar.DISPLAY_SHOW_TITLE);
            actionBar.setCustomView(customActionBarView);
        }

//        mFragment = (ContactEditorFragment) getFragmentManager().findFragmentById(
//                R.id.contact_editor_fragment);
//        mFragment.setListener(mFragmentListener);


    }

}
