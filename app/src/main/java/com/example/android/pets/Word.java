package com.example.android.pets;

import android.support.annotation.NonNull;

public class Word implements Comparable {

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
    public String getWord() {
        return word;
    }
    public String getTranslation() {
        return translation;
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
//    @Override
//    public String toString() {
//        return "Demo [message=" + message + ", time=" + time
//                + ", count=" + count + ", version=" + version + "]";
//    }

}
