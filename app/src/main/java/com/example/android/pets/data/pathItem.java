package com.example.android.pets.data;

import android.os.Parcel;
import android.os.Parcelable;

public class pathItem implements Parcelable {

    Long id;

    public pathItem(Long id) {
        this.id = id;
    }

    protected pathItem(Parcel in) {
        id = in.readLong();
    }

    public static final Creator<pathItem> CREATOR = new Creator<pathItem>() {
        @Override
        public pathItem createFromParcel(Parcel in) {
            return new pathItem(in);
        }

        @Override
        public pathItem[] newArray(int size) {
            return new pathItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
    }

    @Override
    public String toString() {
        return id.toString();
    }

    public Long getId() {
        return id;
    }
}
