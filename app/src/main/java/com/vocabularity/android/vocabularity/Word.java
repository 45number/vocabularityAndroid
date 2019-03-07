package com.vocabularity.android.vocabularity;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class Word implements Comparable, Parcelable {

    private int id;
    private String word;
    private String translation;
    private int toRepeatMem;
    private int toRepeatSpell;

    public Word(int id,String word, String translation, int toRepeatMem, int toRepeatSpell){
        this.id=id;
        this.word = word;
        this.translation = translation;
        this.toRepeatMem = toRepeatMem;
        this.toRepeatSpell = toRepeatSpell;
    }

    protected Word(Parcel in) {
        id = in.readInt();
        word = in.readString();
        translation = in.readString();
        toRepeatMem = in.readInt();
        toRepeatSpell = in.readInt();
    }

    public static final Creator<Word> CREATOR = new Creator<Word>() {
        @Override
        public Word createFromParcel(Parcel in) {
            return new Word(in);
        }

        @Override
        public Word[] newArray(int size) {
            return new Word[size];
        }
    };

    public String getWord() {
        return word;
    }
    public void setWord(String word) {
        this.word = word;
    }
    public String getTranslation() {
        return translation;
    }
    public void setTranslation(String translation) {
        this.translation = translation;
    }
    public int getToRepeatMem() {
        return toRepeatMem;
    }
    public void setToRepeatMem(int toRepeatMem) {
        this.toRepeatMem = toRepeatMem;
    }

    public int getToRepeatSpell() {
        return toRepeatSpell;
    }
    public void setToRepeatSpell(int toRepeatSpell) {
        this.toRepeatSpell = toRepeatSpell;
    }

    public int getId() {
        return id;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        int compareId=((Word)o).getId();
        return this.id-compareId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
    }
//    @Override
//    public String toString() {
//        return "Demo [message=" + message + ", time=" + time
//                + ", count=" + count + ", version=" + version + "]";
//    }

}
