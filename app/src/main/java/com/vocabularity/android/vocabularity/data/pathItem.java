package com.vocabularity.android.vocabularity.data;

import android.os.Parcel;
import android.os.Parcelable;

public class pathItem implements Parcelable {

    Long id;
    String name;

    public pathItem(Long id, String name) {
        this.id = id;
        this.name = name;
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

    public String getName() { return name; }
}
