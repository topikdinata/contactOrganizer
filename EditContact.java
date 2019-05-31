package com.gaoxingliang.contactorganizer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.net.URI;

public class EditContact extends MainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.edit_contact);
        editNameTxt = (EditText) findViewById(R.id.txtEditName);
        editPhoneTxt = (EditText) findViewById(R.id.txtEditPhone);
        editEmailTxt = (EditText) findViewById(R.id.txtEditEmail);
        editAddressTxt = (EditText) findViewById(R.id.txtEditAddress);
        final Button editBtn = (Button) findViewById(R.id.btnEdit);
        final Button cancelBtn = (Button) findViewById(R.id.btnCancel);
        EditContactImageImgView = (ImageView) findViewById(R.id.imgViewEditContactImage);

        String oldName = Contacts.get(MainActivity.clickIndex).getName();
        String oldPhone = Contacts.get(MainActivity.clickIndex).getPhone();
        String oldEmail = Contacts.get(MainActivity.clickIndex).getEmail();
        String oldAddress = Contacts.get(MainActivity.clickIndex).getAddress();
        Uri oldImg = Contacts.get(MainActivity.clickIndex).getImage();
        //put current contact
        editNameTxt.setText(oldName);
        editPhoneTxt.setText(oldPhone);
        editEmailTxt.setText(oldEmail);
        editAddressTxt.setText(oldAddress);
        EditContactImageImgView.setImageURI(oldImg);

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Contact editedContact = new Contact(Contacts.get(MainActivity.clickIndex).getID(),String.valueOf(editNameTxt.getText()),String.valueOf(editPhoneTxt.getText()),String.valueOf(editEmailTxt.getText()),String.valueOf(editAddressTxt.getText()),ImageURI);
                String oldName = Contacts.get(MainActivity.clickIndex).getName();
                    boolean verify = dbHandler.updateContact(editedContact);
                    MainActivity.Contacts.set(MainActivity.clickIndex, editedContact);
                    if(verify) {
                        Toast.makeText(getApplicationContext(), oldName + " is successfully updated!" +
                                "\nNEW: " + editNameTxt.getText(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), oldName + "editing contact failed" + editNameTxt.getText(), Toast.LENGTH_SHORT).show();
                    }
            }
        });

        EditContactImageImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent();
                intent2.setType("image/*");
                intent2.setAction(Intent.ACTION_OPEN_DOCUMENT); //this is to start the action  FIXED: ACTION_GET_CONTENT is no longer used, ACTION_OPEN_DOCUMENT
                startActivityForResult(Intent.createChooser(intent2, "EDIT IMAGE"),2); //the req code is for confirm in the result method
            }
        });



        editNameTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editBtn.setEnabled(!editNameTxt.getText().toString().trim().isEmpty());
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void onActivityResult(int reqCode, int resCode, Intent data) {
        if (resCode == RESULT_OK) { //confirmation ok or not
            switch (reqCode) {
                case 2:
                    ImageURI = data.getData();
                    EditContactImageImgView.setImageURI(data.getData());
                    break;
            }
        }
    }
}
