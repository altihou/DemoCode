package com.iflytek.aiuidemosample.repository;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.ContactsContract;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by PR on 2017/8/28.
 */

public class ContactRepository {
    public static final String LAST_VERSION_KEY = "contact_last_version";
    private Activity mActivity;
    private String mLastVersion;
    private String mCurrentVersion;

    public ContactRepository(Activity activity){
        mActivity = activity;

        mLastVersion = mActivity.getPreferences(Context.MODE_PRIVATE).
                getString(LAST_VERSION_KEY, "0");


    }

    /**
     * 返回自上次查询后通讯录是否有变化（仅可调用一次）
     * @return
     */
    public boolean hasChanged(){
        mCurrentVersion = calculateContactVersion();
        boolean changed = !mCurrentVersion.equals(mLastVersion);
        SharedPreferences.Editor editor = mActivity.getPreferences(Context.MODE_PRIVATE).edit();
        editor.putString(LAST_VERSION_KEY, mCurrentVersion);
        editor.commit();
        mLastVersion = mCurrentVersion;

        return changed;
    }

    /**
     * 返回通讯录中记录，格式如下::
     *  姓名$$电话号码
     * @return
     */
    public List<String> getContacts(){
        List<String> contacts = new ArrayList<>();

        ContentResolver cr = mActivity.getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if(cursor != null) {
            while (cursor.moveToNext()) {
                int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
                String personName = cursor.getString(nameFieldColumnIndex);
                String ContactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                Cursor phone = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + ContactId, null, null);

                //只取第一个联系电话
                while (phone.moveToNext()) {
                    String phoneNumber = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    phoneNumber = phoneNumber.replace("-", "");
                    phoneNumber = phoneNumber.replace(" ", "");
                    contacts.add(personName + "$$" + phoneNumber);
                    break;
                }
            }

            cursor.close();
        }



        return contacts;
    }


    private String calculateContactVersion(){
        return stringToMd5(join(getContacts(), "&&"));
    }

    /**
     * 将字符串version转换成MD5格式的
     *
     * @param s
     * @return
     */
    private String stringToMd5(String s) {
        byte[] value = s.getBytes();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(value);
            byte[] temp = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : temp) {
                sb.append(Integer.toHexString(b & 0xff));
            }
            String md5Version = sb.toString();

            return md5Version;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String join(List<String> source, String separator){
        StringBuilder builder = new StringBuilder();
        for(int index=0;index<source.size();index++){
            builder.append(source.get(index));
            if(index != source.size()-1){
                builder.append(separator);
            }
        }

        return builder.toString();
    }




}
