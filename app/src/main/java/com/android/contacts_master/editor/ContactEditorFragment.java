package com.android.contacts_master.editor;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.widget.Toast;
import com.android.contacts_master.Constants;
import com.android.contacts_master.R;
import com.android.contacts_master.activity.ContactDetailActivity;
import com.android.contacts_master.bean.ContactInfo;
import com.android.contacts_master.bean.EmailContact;
import com.android.contacts_master.bean.TaggedContactPhoneNumber;
import com.android.contacts_master.detail.ContactInfoItem;
import com.android.contacts_master.email.EmailSender;
import com.android.contacts_master.logging.DevLog;
import com.android.contacts_master.logging.LogLevel;
import com.android.contacts_master.logging.MarketLog;
import com.android.contacts_master.provider.ContactsProjection;
import com.android.contacts_master.util.BitmapUtils;
import com.android.contacts_master.util.ContactsUtils;
import com.android.contacts_master.util.DialUtils;
import com.android.contacts_master.util.ProgressHandler;
import com.baidu.mobstat.StatService;
import android.database.Cursor;
import java.util.List;
import java.util.ArrayList;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ContactEditorFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ContactEditorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContactEditorFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private ContactInfo mContactInfo;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    public long mContactId;
    String mAction;
    String oldname;

    private EditText mNameEdit,mPhoneEdit,mAddressEdit,mEmailEdit;
    private OnFragmentInteractionListener mListener;
    ProgressHandler mProgressHandler = new ProgressHandler();
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ContactEditorFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ContactEditorFragment newInstance(String param1, String param2) {
        ContactEditorFragment fragment = new ContactEditorFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public ContactEditorFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_contact_editor, container, false);

        mNameEdit=(EditText)root.findViewById(R.id.edit_name);
        mPhoneEdit=(EditText)root.findViewById(R.id.edit_phone);
        mAddressEdit=(EditText)root.findViewById(R.id.edit_address);
        mEmailEdit=(EditText)root.findViewById(R.id.edit_email);



        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (Intent.ACTION_EDIT.equals(mAction)) {
            // Either...
            // 1) orientation change but load never finished.
            // or
            // 2) not an orientation change.  data needs to be loaded for first time.
            bindEditors();
        }

    }

    @Override
    public void onResume(){
        super.onResume();
        StatService.onResume(this);


    }


    public void load(String action, long ContactId ) {
        ///M:fix ALPS01020577,Original Google Code
        ///mAction = action;
        ///mLookupUri = lookupUri;
        //There only has 2 place will call this function,from:onSaveCompleted() or ContactEditorActivity
        //When from ContactEditorActivity:first enter EditorFragment or restart ContactEditorActivity
        //when from onSaveCompleted():when saved complete,the action will always be Intent.ACTION_EDIT
        //So here should check the mAction to avoid reset it to older value
       if (mAction == null || (action == Intent.ACTION_EDIT && mAction != null)) {
            mAction = action;
           mContactId = ContactId;

        }



    }


    private  void   bindEditors(){

        mContactInfo = ContactsUtils.getContactInfoByContactId(getActivity(), mContactId);
        if (mContactInfo == null) {
            if (LogLevel.MARKET) {

            }

            return;
        }
        refreshContact();

    }
    private void refreshContact(){


        mNameEdit.setText(mContactInfo.displayName);
        oldname =mContactInfo.displayName;
        List<TaggedContactPhoneNumber> phoneList = ContactsUtils.getPersonalContactPhoneNumbers(getActivity(), mContactInfo.contactId);
        if(phoneList != null && phoneList.size() > 0){

        } else {
           mPhoneEdit.setText("");
        }

        for(final TaggedContactPhoneNumber phone : phoneList){
            if(phone == null || TextUtils.isEmpty(phone.originalNumber)){
                if(LogLevel.DEV){

                }
                continue;
            }



        //    mPhoneEdit.setTypeText(phone.numberTag);
            mPhoneEdit.setText(phone.originalNumber);

        }

        List<EmailContact> mailList = ContactsUtils.getEmailAddresses(getActivity(), mContactInfo.contactId);
        if(mailList != null && mailList.size() > 0){

        } else {

        }

        for(final EmailContact mail : mailList){
            if(mail == null || TextUtils.isEmpty(mail.emailAddress)){
                if(LogLevel.DEV){

                }
                continue;
            }


          //  item.setTypeText(mail.emailTag);
            mEmailEdit.setText(mail.emailAddress);

        }


    }



    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    mListener = null;


    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }


    public   void setListener(OnFragmentInteractionListener listener){
        if (mListener != null) {
            mListener = listener;
        }
    }



    public void doSaveAction() {
        mProgressHandler.showDialog(getFragmentManager());
       String name= mNameEdit.getText().toString().trim();
       String phone= mPhoneEdit.getText().toString().trim();
        String email=  mEmailEdit.getText().toString().trim();

        if(name.equals("")&&phone.equals("")){
            getActivity().finish();
                       return;
        }
        if (Intent.ACTION_EDIT.equals(mAction)) {

            updateContact(name,phone,email,null,null,null);

        }else{
            insertContacts(name,phone,null,email);
        }




    }


    private void insertContacts(String name,String phone,String adress,String  email){

        ContentValues values = new ContentValues();
// 首先向RawContacts.CONTENT_URI执行一个空值插入，目的是获取系统返回的rawContactId
        Uri rawContactUri = getActivity().getContentResolver()
                .insert(RawContacts.CONTENT_URI, values);
        long rawContactId = ContentUris.parseId(rawContactUri);
// 往data表入姓名数据
        values.clear();
        values.put(Data.RAW_CONTACT_ID, rawContactId);
        values.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);// 内容类型

        values.put(StructuredName.GIVEN_NAME, mNameEdit.getText().toString().trim());
        getActivity()
                .getContentResolver()
                .insert(ContactsContract.Data.CONTENT_URI,
                        values);

// 往data表入电话数据
        values.clear();
        values.put(Data.RAW_CONTACT_ID, rawContactId);
        values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);// 内容类型
        values.put(Phone.NUMBER, mPhoneEdit.getText().toString());
        values.put(Phone.TYPE, Phone.TYPE_MOBILE);
        getActivity()
                .getContentResolver()
                .insert(ContactsContract.Data.CONTENT_URI,
                        values);
// 往data表入Email数据
        values.clear();
        values.put(Data.RAW_CONTACT_ID, rawContactId);
        values.put(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);// 内容类型
        values.put(Email.DATA, mEmailEdit.getText().toString());
        values.put(Email.TYPE, Email.TYPE_WORK);
        getActivity()
                .getContentResolver()
                .insert(ContactsContract.Data.CONTENT_URI,
                        values);

        Intent mIntent=new Intent(getActivity(),ContactDetailActivity.class);
        mIntent.putExtra(Constants.EXTRA_CONTACT_PERSON_ID,rawContactId);

       getActivity().startActivity(mIntent);
        getActivity().finish();


    }

  private void  updateContacts(String name,String phone,String adress,String  email){

      ContentValues values = new ContentValues();

//      Uri rawContactUri = getActivity().getContentResolver()
//              .update(RawContacts.CONTENT_URI, values);
//      long rawContactId = ContentUris.parseId(rawContactUri);
//      values.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);// 内容类型
//
//      values.put(StructuredName.GIVEN_NAME,name);

      values.put(Phone.NUMBER, phone);
      values.put(Phone.TYPE, Phone.TYPE_MOBILE);
//
//      values.put(Email.DATA, email);
//      values.put(Email.TYPE, Email.TYPE_WORK);
      String where = ContactsContract.Data.RAW_CONTACT_ID + "=? AND "
              + ContactsContract.Data.MIMETYPE + "=?";
      String[] selectionArgs = new String[] { String.valueOf(mContactInfo.contactId),
              Phone.CONTENT_ITEM_TYPE };
      getActivity()
              .getContentResolver().update(ContactsContract.Data.CONTENT_URI, values,
                 where, selectionArgs);
      getActivity().finish();

  }



    // 根据姓名更新联系人
    public void updateContact( String name,
                                 String phone, String email, String website, String organization,
                                 String note) {
        Cursor cursor =   getActivity().getContentResolver().query(Data.CONTENT_URI,
                new String[] { Data.RAW_CONTACT_ID },

                ContactsContract.Contacts.DISPLAY_NAME + "=?",
                new String[] { oldname }, null);
        if (!cursor.moveToFirst()) {
            return ;
        }
        String id = cursor
                .getString(cursor.getColumnIndex(Data.RAW_CONTACT_ID));
        cursor.close();
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ops.add(ContentProviderOperation
                .newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(

                        Data.RAW_CONTACT_ID + "=?" + " AND "
                                + ContactsContract.Data.MIMETYPE + " = ?"
                                + " AND " + Phone.TYPE + "=?",
                        new String[] { String.valueOf(id),
                                Phone.CONTENT_ITEM_TYPE,
                                String.valueOf(Phone.TYPE_HOME) })
                .withValue(Phone.NUMBER, phone).build());

        // 更新email
        ops.add(ContentProviderOperation
                .newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(
                        Data.RAW_CONTACT_ID + "=?" + " AND "
                                + ContactsContract.Data.MIMETYPE + " = ?"
                                + " AND " + Email.TYPE + "=?",
                        new String[] { String.valueOf(id),
                                Email.CONTENT_ITEM_TYPE,
                                String.valueOf(Email.TYPE_HOME) })
                .withValue(Email.DATA, email).build());

        // 更新姓名
        ops.add(ContentProviderOperation
                .newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(
                        Data.RAW_CONTACT_ID + "=?" + " AND "
                                + ContactsContract.Data.MIMETYPE + " = ?",
                        new String[] { String.valueOf(id),
                                StructuredName.CONTENT_ITEM_TYPE })
                .withValue(StructuredName.DISPLAY_NAME, name).build());

        // 更新网站
        ops.add(ContentProviderOperation
                .newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(
                        Data.RAW_CONTACT_ID + "=?" + " AND "
                                + ContactsContract.Data.MIMETYPE + " = ?",
                        new String[] { String.valueOf(id),
                                Website.CONTENT_ITEM_TYPE })
                .withValue(Website.URL, website).build());

        // 更新公司
        ops.add(ContentProviderOperation
                .newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(
                        Data.RAW_CONTACT_ID + "=?" + " AND "
                                + ContactsContract.Data.MIMETYPE + " = ?",
                        new String[] { String.valueOf(id),
                                Organization.CONTENT_ITEM_TYPE })
                .withValue(Organization.COMPANY, organization).build());

        // 更新note
        ops.add(ContentProviderOperation
                .newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(
                        Data.RAW_CONTACT_ID + "=?" + " AND "
                                + ContactsContract.Data.MIMETYPE + " = ?",
                        new String[] { String.valueOf(id),
                                Note.CONTENT_ITEM_TYPE })
                .withValue(Note.NOTE, note).build());

        try {
            getActivity().getContentResolver().applyBatch(ContactsContract.AUTHORITY,
                    ops);

        } catch (Exception e) {

        }
        getActivity().finish();
    }




}
