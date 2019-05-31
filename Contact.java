package com.gaoxingliang.contactorganizer;

import android.net.Uri;

public class Contact {

    private String name, phone, email, address;
    private Uri imageURI;
    private int id;
    public Contact(int ID, String Name, String Phone, String Email, String Address, Uri ImageURI){ //import the URI FROM ANDROID PACKAGE NOT JAVA
        id = ID;
        name = Name;
        phone = Phone;
        email = Email;
        address = Address;
        imageURI = ImageURI;

    }

    //getter
    public int getID(){
        return id;
    }
    public String getName(){
        return name;
    }
    public String getPhone(){
        return phone;
    }
    public String getEmail(){
        return email;
    }
    public String getAddress(){
        return address;
    }
    public Uri getImage(){
        return imageURI;
    }
}

