package com.android.contacts_master.editor;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.ContactsContract;
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

import com.android.contacts_master.R;
import com.android.contacts_master.util.ProgressHandler;

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

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.edit_contact, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        final MenuItem doneMenu = menu.findItem(R.id.menu_done);


        // Set visibility of menus
        doneMenu.setVisible(true);
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


    public  void doSaveAction(){
        doInsert();

            getActivity().finish();

    }


    public void doInsert() {
        if(mNameEdit.getText().toString().trim().equals("")&&mPhoneEdit.getText().toString().trim().equals("")){
                       return;
        }
        mProgressHandler.showDialog(getFragmentManager());
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
                .insert(android.provider.ContactsContract.Data.CONTENT_URI,
                        values);

// 往data表入电话数据
        values.clear();
        values.put(Data.RAW_CONTACT_ID, rawContactId);
        values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);// 内容类型
        values.put(Phone.NUMBER, mPhoneEdit.getText().toString());
        values.put(Phone.TYPE, Phone.TYPE_MOBILE);
        getActivity()
                .getContentResolver()
                .insert(android.provider.ContactsContract.Data.CONTENT_URI,
                        values);
// 往data表入Email数据
        values.clear();
        values.put(Data.RAW_CONTACT_ID, rawContactId);
        values.put(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);// 内容类型
        values.put(Email.DATA, mEmailEdit.getText().toString());
        values.put(Email.TYPE, Email.TYPE_WORK);
        getActivity()
                .getContentResolver()
                .insert(android.provider.ContactsContract.Data.CONTENT_URI,
                        values);

    }




}
