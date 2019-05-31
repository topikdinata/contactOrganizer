package com.gaoxingliang.contactorganizer;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // https://stackoverflow.com/questions/3465841/how-to-change-visibility-of-layout-  study the invisible or visible layout
    final String defaultImage = "android.resource://com.gaoxingliang.contactorganizer/drawable/no_user_image.png";
    private static final int EDIT = 0, DELETE = 1;


    ImageView ContactImageImgView;
    static List<Contact> Contacts = new ArrayList<Contact>();
    DatabaseHandler dbHandler;
    ListView contactListView;
    Uri ImageURI = Uri.parse(defaultImage);
    public static int clickIndex;

    EditText editNameTxt, editPhoneTxt, editEmailTxt, editAddressTxt;
    ImageView EditContactImageImgView;
    EditText nameTxt, phoneTxt, emailTxt, addressTxt;

    int longClickedItemIndex;
    static ArrayAdapter<Contact> contactAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView refreshContactList = (TextView) findViewById(R.id.lblContactList);
        ContactImageImgView = (ImageView) findViewById(R.id.imgViewContactImage);
        nameTxt = (EditText) findViewById(R.id.txtName);
        phoneTxt = (EditText) findViewById(R.id.txtPhone);
        emailTxt = (EditText) findViewById(R.id.txtEmail);
        addressTxt = (EditText) findViewById(R.id.txtAddress);

        contactListView = (ListView) findViewById(R.id.listView);
        dbHandler = new DatabaseHandler(getApplicationContext());

        final Button addBtn = (Button) findViewById(R.id.btnAdd);
        final Button backupBtn = (Button) findViewById(R.id.btnBackUp);
        final Button restoreBtn = (Button) findViewById(R.id.btnRestore);

        registerForContextMenu(contactListView);

        //tabs setup
        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);

        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("creator");
        tabSpec.setContent(R.id.tabCreator);
        tabSpec.setIndicator("Creator");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("list");
        tabSpec.setContent(R.id.tabContactList);
        tabSpec.setIndicator("List");
        tabHost.addTab(tabSpec);

        //add button click action
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //addContact(nameTxt.getText().toString(), phoneTxt.getText().toString(), emailTxt.getText().toString(), addressTxt.getText().toString(), ImageURI);

                // tocreate the contact ID
                Contact contact = new Contact(dbHandler.getContactsCount(),String.valueOf(nameTxt.getText()),String.valueOf(phoneTxt.getText()),String.valueOf(emailTxt.getText()),String.valueOf(addressTxt.getText()), ImageURI);

                // code optimization using the contactExist
                if (!contactExist(contact)){
                    dbHandler.createContact(contact);
                    Contacts.add(contact);
                    contactAdapter.notifyDataSetChanged();
                    //populateList();
                    Toast.makeText(getApplicationContext(), String.valueOf(nameTxt.getText()) +" is successfully added to Contact List!", Toast.LENGTH_SHORT).show();
                    nameTxt.setText("");
                    phoneTxt.setText("");
                    addressTxt.setText("");
                    emailTxt.setText("");

                    Uri defaultImgView = Uri.parse(defaultImage);
                    ContactImageImgView.setImageURI(defaultImgView);
                    return;
                }
                Toast.makeText(getApplicationContext(), String.valueOf(nameTxt.getText()) + " is already exist in the Contact List!", Toast.LENGTH_SHORT).show();
            }


        });

        backupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backUp();
            }
        });

        restoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restore();
            }
        });

        refreshContactList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contactAdapter.clear();
                Contacts.addAll(dbHandler.getAllContacts());
                populateList();
            }
        });

        contactListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                longClickedItemIndex = position;
                clickIndex = longClickedItemIndex;
                return false;
            }
        });

        nameTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addBtn.setEnabled(!nameTxt.getText().toString().trim().isEmpty()); //dont enable button if name is null
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //contact image click action by using Intent instance
        // LEARN FROM https://developer.android.com/guide/topics/providers/document-provider
        ContactImageImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*"); //this is choosing the type of file
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT); //this is to start the action  FIXED: ACTION_GET_CONTENT is no longer used, ACTION_OPEN_DOCUMENT
                startActivityForResult(Intent.createChooser(intent, "SELECT IMAGE"),1); //the req code is for confirm in the result method

            }
        });

        if(dbHandler.getContactsCount() != 0)
            Contacts.addAll(dbHandler.getAllContacts());

        populateList();
    }

        public void backUp() {
        //https://stackoverflow.com/questions/13502223/backup-restore-sqlite-db-in-android/13504743#13504743

            final String inFileName = "/data/data/com.gaoxingliang.contactorganizer/databases/contactManager";
            File dbFile = new File(inFileName);

            String outFileName = Environment.getExternalStorageDirectory()+"/database_copy.db";
            // String outFileName = "/Copy.db";
             try{


                    FileInputStream fis = new FileInputStream(dbFile);
                    // Open the empty db as the output stream
                    OutputStream output = new FileOutputStream(outFileName);
                    //Toast.makeText(getApplicationContext(), "good", Toast.LENGTH_SHORT).show();


                 // Transfer bytes from the inputfile to the outputfile
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer))>0){
                    output.write(buffer, 0, length);

                    }
                 Toast.makeText(getApplicationContext(), "good", Toast.LENGTH_SHORT).show();
                    // close streams
                    output.flush();
                    output.close();
                    fis.close();
             }

            catch(Exception e) {
                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
             }
        }


        public void restore(){
                try {
                File sd = Environment.getExternalStorageDirectory();
                File data = Environment.getDataDirectory();

                if (sd.canWrite()) {
                    String currentDBPath = "/data/com.gaoxingliang.contactorganizer/databases/contactManager";
                    String backupDBPath = "database_copy.db";
                    File currentDB = new File(data, currentDBPath);
                    File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(backupDB).getChannel();
                    FileChannel dst = new FileOutputStream(currentDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Toast.makeText(getApplicationContext(), "Database Restored successfully", Toast.LENGTH_SHORT).show();
                }
            }
    }           catch (Exception e) {
                Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                }
    }

    //found my icon from http://www.iconarchive.com/search?q=edit
    public void onCreateContextMenu (ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, view, menuInfo);

        menu.setHeaderIcon(R.drawable.edit_icon);
        menu.setHeaderTitle("Options");
        // menu lists
        menu.add(menu.NONE, EDIT, menu.NONE, "Edit Contact");
        menu.add(menu.NONE, DELETE, menu.NONE, "Delete Contact");
    }

    public boolean onContextItemSelected(MenuItem item){
        switch (item.getItemId()){
            case EDIT:
                Intent i = new Intent(MainActivity.this, com.gaoxingliang.contactorganizer.EditContact.class);
                startActivityForResult(i,3);
                break;
            case DELETE:
                //delete
                dbHandler.deleteContact(Contacts.get(longClickedItemIndex));
                Contacts.remove(longClickedItemIndex);
                onResume();
                break;
        }
        return super.onContextItemSelected(item);
    }

    public boolean contactExist(Contact contact) {
        String name = contact.getName();
        int contactCount = Contacts.size();

        for(int index = 0; index < contactCount; index++) {
            if(name.compareToIgnoreCase(Contacts.get(index).getName()) == 0) //return 0 if exist
                return true;
        }
        return false;
    }

    public void onActivityResult(int reqCode, int resCode, Intent data) {
        if (resCode == RESULT_OK) { //confirmation ok or not
            switch (reqCode) {
                case 1:
                    ImageURI = data.getData();
                    ContactImageImgView.setImageURI(data.getData());
                    break;
                case 3:
                    //do nothing
                    break;
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        contactAdapter.clear();
        Contacts.addAll(dbHandler.getAllContacts());
        populateList();
    }

    //after assigned the listView
    public void populateList() {
        contactAdapter = new contactListAdapter(); //make new adapter
        contactListView.setAdapter(contactAdapter); //NOTE: NOT getAdapter, it is setAdapter
    }

    //make new array adaptors
    private class contactListAdapter extends ArrayAdapter<Contact>{
        public contactListAdapter(){
           super(MainActivity.this, R.layout.listview_item, Contacts);
        }

        @Override //put the data in a layout
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null)
                view = getLayoutInflater().inflate(R.layout.listview_item, parent, false);  //if the view is null, get the layout to inflate into the view

            Contact currentContact = Contacts.get(position);

            TextView name = (TextView) view.findViewById(R.id.contactName); //connect the code with the layout
            name.setText(currentContact.getName()); //use the getter from Contact.java

            TextView phone = (TextView) view.findViewById(R.id.phoneNumber);
            phone.setText(currentContact.getPhone());

            TextView email = (TextView) view.findViewById(R.id.emailAddress);
            email.setText(currentContact.getEmail());

            TextView address = (TextView) view.findViewById(R.id.cAddress);
            address.setText(currentContact.getAddress());

           //addition
            ImageView contactImageIV = (ImageView) view.findViewById(R.id.ivContactImage);
            contactImageIV.setImageURI(currentContact.getImage());

           return view;
        }
    }
}

//https://stackoverflow.com/questions/18370219/how-to-use-adb-in-android-studio-to-view-an-sqlite-db
