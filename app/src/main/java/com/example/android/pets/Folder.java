package com.example.android.pets;

import java.util.ArrayList;

public class Folder {
    private Integer id;
    private String name;
    private String image;
    private ArrayList<Integer> children;
    private Integer marked;

    public Folder(int id, String name, String image, int marked, ArrayList<Integer> children) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.marked = marked;
        this.children = children;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public boolean isMarked() {
        if (this.marked == 1)
            return true;
        return false;
    }

    public ArrayList<Integer> getChildren() {
        return children;
    }

}
