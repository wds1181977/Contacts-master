/**
 * Copyright (C) 2013, Easiio, Inc.
 * All Rights Reserved.
 */
package com.android.contacts_master.activity;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.contacts_master.Constants;
import com.android.contacts_master.R;
import com.android.contacts_master.bean.ContactInfo;
import com.android.contacts_master.bean.EmailContact;
import com.android.contacts_master.bean.TaggedContactPhoneNumber;
import com.android.contacts_master.detail.ContactInfoItem;
import com.android.contacts_master.email.EmailSender;
import com.android.contacts_master.logging.DevLog;
import com.android.contacts_master.logging.LogLevel;
import com.android.contacts_master.logging.MarketLog;
import com.android.contacts_master.util.BitmapUtils;
import com.android.contacts_master.util.ContactsUtils;
import com.android.contacts_master.util.DialUtils;
import com.baidu.mobstat.StatService;

import java.util.List;

/**
 * ContactInfoActivity
 * @author wds
 */
public class ContactDetailActivity extends AppCompatActivity {

	private static final String TAG = "ContactDetailActivity";
	
	private static final int EDIT_CONTACT_REQUEST = 79;
	
	private long mContactId;
	
	private ContactInfo mContactInfo;
	
	private ImageButton mBackButton;
	private ImageButton mEditButton;
	private TextView mNameView;
	private TextView mCompanyView;
	private ImageView mPhotoView;
	private LinearLayout mContactInfoPhoneTitleView;
	private LinearLayout mContactInfoPhoneLayout;
	private LinearLayout mContactInfoMailTitleView;
	private LinearLayout mContactInfoMailLayout;
	private View mNoContactView;
	
	private PopupWindow mPopupWindow;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		if(LogLevel.DEV){
			DevLog.i(TAG, "onCreate...");
		}
		this.setContentView(R.layout.contact_info_layout);
		
		mContactId = this.getIntent().getLongExtra(Constants.EXTRA_CONTACT_PERSON_ID, -1);
		if(LogLevel.DEV){
			DevLog.i(TAG, "onCreate contactId = " + mContactId);
		}
		
		if(mContactId < 0){
			if(LogLevel.DEV){
				DevLog.e(TAG, "onCreate error : personId < 0.");
			}
			finish();
			return;
		}
		
		buildLayout();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.contactinfo, menu);


		return super.onCreateOptionsMenu(menu);
	}



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home: {
				// The home icon on the action bar is pressed

				// "UP" icon press -- should be treated as "back".
				onBackPressed();

				return true;
			}

			case R.id.edit:{
				if(Constants.isgithub) {

					Uri personUri = ContentUris.withAppendedId(ContactsUtils.Uri_People, mContactInfo.contactId);
					editContact(personUri);
				}{

					Intent intent = new Intent(this, ContactEditorActivity.class);
					intent.setAction(Intent.ACTION_EDIT);
					intent.putExtra(Constants.EXTRA_CONTACT_PERSON_ID,mContactInfo.contactId);
					startActivity(intent);
				}
				return true;
			}
			case R.id.delete:{

				new AlertDialog.Builder(this)
						.setTitle(R.string.multichoice_delete_confirm_title)
						.setIconAttribute(android.R.attr.alertDialogIcon)
						.setMessage(R.string.multichoice_delete_confirm_message)
						.setNegativeButton(android.R.string.cancel, null)
						.setInverseBackgroundForced(true)
						.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Uri personUri = ContentUris.withAppendedId(ContactsUtils.Uri_People, mContactInfo.contactId);
								getContentResolver().delete(personUri, null, null);
								dialog.dismiss();
								Toast.makeText(ContactDetailActivity.this, getString(R.string.delete_contacts_ok), Toast.LENGTH_SHORT).show();
								ContactDetailActivity.this.finish();
							}

						}).create().show();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onResume(){
		super.onResume();
		StatService.onResume(this);
		mContactInfo = ContactsUtils.getContactInfoByContactId(this, mContactId);
		if(mContactInfo == null){
			if(LogLevel.MARKET){
				MarketLog.e(TAG, "onResume -> finish, contact info is null.");
			}
			finish();
			return;
		}
		refreshContact();

	}
	
	private void buildLayout(){
		if (getSupportActionBar() != null) {
			ActionBar actionBar = getSupportActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		mBackButton = (ImageButton) this.findViewById(R.id.button_back);
		mBackButton.setVisibility(View.GONE);
		mBackButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		mEditButton = (ImageButton) this.findViewById(R.id.button_little_edit);
		
		mEditButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mPopupWindow != null) {
					if (mPopupWindow.isShowing()) {
						mPopupWindow.dismiss();
					} else {
						mPopupWindow.showAsDropDown(v);
					}
				}
			}
		});
		
		mNameView = (TextView) this.findViewById(R.id.name_view);
		mCompanyView = (TextView) this.findViewById(R.id.company_view);
		mPhotoView = (ImageView) this.findViewById(R.id.photo_view);
		mContactInfoPhoneTitleView = (LinearLayout) this.findViewById(R.id.contact_info_phone_title);
		mContactInfoPhoneLayout = (LinearLayout) this.findViewById(R.id.contact_info_phone_layout);
		mContactInfoMailTitleView = (LinearLayout) this.findViewById(R.id.contact_info_mail_title);
		mContactInfoMailLayout = (LinearLayout) this.findViewById(R.id.contact_info_mail_layout);
		mNoContactView = this.findViewById(R.id.no_contact_info_view);
		mNoContactView.setVisibility(View.GONE);
	}
	


	
	private void refreshContact(){
		
		if(LogLevel.DEV){
			DevLog.d(TAG, "refreshContact() Contact Info:" + mContactInfo.toString());
		}
		
		boolean isPhoneEmpty = false;
		boolean isMailEmpty = false;
		
		mNameView.setText(mContactInfo.displayName);
		
		String company = mContactInfo.jobTitle;
		if(TextUtils.isEmpty(company)){
			company = mContactInfo.companyName;
		} else {
			company = company + ", " + mContactInfo.companyName;
		}
		
		if(!TextUtils.isEmpty(company)){
			mCompanyView.setText(company);
		} else {
			mCompanyView.setText("");
		}
		
		try{
			Bitmap photo = ContactsUtils.getContactInfoPhoto(this, mContactInfo.photoId);
			if (photo != null) {

				photo = BitmapUtils.toRoundBitmap(photo);
				mPhotoView.setImageBitmap(photo);
			} else {
				mPhotoView.setImageResource(R.drawable.icon_contact_list_default_round_photo);
			}
		} catch (Exception e){
			MarketLog.e(TAG, "set photo failed : " + e.toString());
		}
		
		
		mContactInfoPhoneLayout.removeAllViews();
		mContactInfoMailLayout.removeAllViews();
		
		List<TaggedContactPhoneNumber> phoneList = ContactsUtils.getPersonalContactPhoneNumbers(this, mContactInfo.contactId);
		if(phoneList != null && phoneList.size() > 0){
			mContactInfoPhoneLayout.setVisibility(View.VISIBLE);
			mContactInfoPhoneTitleView.setVisibility(View.VISIBLE);
			isPhoneEmpty = false;
		} else {
			mContactInfoPhoneLayout.setVisibility(View.GONE);
			mContactInfoPhoneTitleView.setVisibility(View.GONE);
			isPhoneEmpty = true;
		}
		
		for(final TaggedContactPhoneNumber phone : phoneList){
			if(phone == null || TextUtils.isEmpty(phone.originalNumber)){
				if(LogLevel.DEV){
					DevLog.e(TAG, "refreshContact() phone is null or original number is empty.");
				}
				continue;
			}
			
			ContactInfoItem item = new ContactInfoItem(this, ContactInfoItem.TYPE_PHONE, new ContactInfoItem.OnClickItemButtonListener() {
				
				@Override
				public void onClickMessageButton() {
					if(LogLevel.DEV){
						DevLog.d(TAG, "User tap send message button : number = " + phone.originalNumber);
					}
					DialUtils.sendMessage(ContactDetailActivity.this, phone.originalNumber);
				}
				
				@Override
				public void onClickCallButton() {
					if(LogLevel.DEV){
						DevLog.d(TAG, "User tap call button : number = " + phone.originalNumber);
					}
					DialUtils.callNumber(ContactDetailActivity.this, phone.originalNumber);
				}
			});
			
			item.setTypeText(phone.numberTag);
			item.setContentText(phone.originalNumber);
			mContactInfoPhoneLayout.addView(item);
		}
		
		List<EmailContact> mailList = ContactsUtils.getEmailAddresses(this, mContactInfo.contactId);
		if(mailList != null && mailList.size() > 0){
			mContactInfoMailLayout.setVisibility(View.VISIBLE);
			mContactInfoMailTitleView.setVisibility(View.VISIBLE);
			isMailEmpty = false;
		} else {
			mContactInfoMailLayout.setVisibility(View.GONE);
			mContactInfoMailTitleView.setVisibility(View.GONE);
			isMailEmpty = true;
		}
		
		for(final EmailContact mail : mailList){
			if(mail == null || TextUtils.isEmpty(mail.emailAddress)){
				if(LogLevel.DEV){
					DevLog.e(TAG, "refreshContact() mail is null or mail address is empty.");
				}
				continue;
			}
			
			ContactInfoItem item = new ContactInfoItem(this, ContactInfoItem.TYPE_EMAIL, null);
			item.setTypeText(mail.emailTag);
			item.setContentText(mail.emailAddress);
			item.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					EmailSender.sendEmail(ContactDetailActivity.this, new String[]{mail.emailAddress}, null, null);
				}
			});
			
			mContactInfoMailLayout.addView(item);
		}
		
		if(isPhoneEmpty && isMailEmpty){
			mNoContactView.setVisibility(View.VISIBLE);
		} else {
			mNoContactView.setVisibility(View.GONE);
		}
	}
	
	private void createNewQuickCall(long peopleId, String name){
//		List<TaggedContactPhoneNumber> list = ContactsUtils.getPersonalContactPhoneNumbers(this, peopleId);
//		if(list == null || list.isEmpty()){
//			if(LogLevel.DEV){
//				DevLog.e(TAG, "sendMessage : list is null");
//			}
//			return;
//		} else if (list.size() == 1){
//			TaggedContactPhoneNumber numbers = list.get(0);
//			Intent intent = new Intent(ContactInfoActivity.this, CreateQuickCallActivity.class);
//			intent.putExtra(QuickCallConstants.EXTRA_SELECT_NAME, name);
//			intent.putExtra(QuickCallConstants.EXTRA_SELECT_NUMBER, numbers.originalNumber);
//			intent.putExtra(QuickCallConstants.EXTRA_SELECT_PHOTO_ID, numbers.photo_id);
//			ContactInfoActivity.this.startActivity(intent);
//			return;
//		}
//
//		new PhoneSelectorDialog(this, list, name, PhoneSelectorDialog.TYPE_SELECT_CONTACT).show();
	}
	
	private void editContact(final Uri personUri) {
		Intent intent = new Intent(Intent.ACTION_EDIT, personUri);
		try {
			startActivityForResult(Intent.createChooser(intent, getString(R.string.menu_editContact)), EDIT_CONTACT_REQUEST);
		} catch (ActivityNotFoundException e) {
			if (LogLevel.MARKET) {
				MarketLog.e(TAG, "editItem(): " + e.getMessage());
			}
		}
	}
	

	
	@Override
	public void onPause(){
		super.onPause();
		StatService.onPause(this);
	}
	
	@Override
	public void finish(){
		super.finish();
	//	this.overridePendingTransition(R.anim.back_in_left, R.anim.back_out_right);
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		mContactId=-1;
		if(LogLevel.MARKET){
			MarketLog.d(TAG, "onDestroy...");
		}
		
		//leakCleanUpRootView();
	}
	
}
