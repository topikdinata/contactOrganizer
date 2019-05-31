package com.gaoxingliang.contactorganizer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;


public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "contactManager",
                                TABLE_CONTACTS = "contacts",
                                KEY_ID = "id",
                                KEY_NAME = "name",
                                KEY_PHONE = "phone",
                                KEY_EMAIL = "email",
                                KEY_ADDRESS = "address",
                                KEY_IMAGEURI = "image";

    public DatabaseHandler (Context context ) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_CONTACTS + "("   + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                                                            + KEY_NAME + " TEXT, "
                                                            + KEY_PHONE + " TEXT, "
                                                            + KEY_EMAIL + " TEXT, "
                                                            + KEY_ADDRESS + " TEXT, "
                                                            + KEY_IMAGEURI + " TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);

        onCreate(db);
    }

    public void createContact(Contact contact){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_NAME, contact.getName());
        values.put(KEY_PHONE, contact.getPhone());
        values.put(KEY_EMAIL, contact.getEmail());
        values.put(KEY_ADDRESS, contact.getAddress());
        values.put(KEY_IMAGEURI, contact.getImage().toString());

        db.insert(TABLE_CONTACTS, null, values);
        db.close();
    }

    // was going to be used for search, mmaybe later update
    /*
    public Contact getContact(int id){

        SQLiteDatabase db = getReadableDatabase();

        //cursor to go to each data inside the database
        Cursor cursor = db.query(TABLE_CONTACTS, new String[] {KEY_ID, KEY_NAME, KEY_PHONE, KEY_EMAIL, KEY_ADDRESS, KEY_IMAGEURI}, KEY_ID + "=?", new String[] {String.valueOf(KEY_ID)}, null, null, null, null );

        if(cursor != null)
            cursor.moveToFirst();

        Contact contact = new Contact(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), Uri.parse(cursor.getString(5)));
        cursor.close();
        db.close();
        return contact;
    }
    */

    public void deleteContact(Contact contact){
        SQLiteDatabase db = getWritableDatabase();

        db.delete(TABLE_CONTACTS, KEY_ID + "=?", new String[] { String.valueOf(contact.getID()) });

        db.close();
    }

    public int getContactsCount(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CONTACTS, null);

        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }

    public boolean updateContact(Contact contact)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, contact.getID());
        values.put(KEY_NAME, contact.getName());
        values.put(KEY_PHONE, contact.getPhone());
        values.put(KEY_EMAIL, contact.getEmail());
        values.put(KEY_ADDRESS, contact.getAddress());
        values.put(KEY_IMAGEURI, String.valueOf(contact.getImage()));

        db.update(TABLE_CONTACTS, values, KEY_ID + " = " + contact.getID() , null);
        db.close();
        return true;

    }

    public List<Contact> getAllContacts(){
        List<Contact> contacts = new ArrayList<Contact>();

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CONTACTS, null);

        if(cursor.moveToFirst()){
            do{
                contacts.add(new Contact(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), Uri.parse(cursor.getString(5))));
            }
            while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return contacts;
    }
}
