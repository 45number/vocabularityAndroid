package com.vocabularity.android.vocabularity;

public class ThreeViewsListItem {
    private String title;
    private String description;
    private String value;

    public ThreeViewsListItem(String title, String description, String value) {
        this.title = title;
        this.description = description;
        this.value = value;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getValue() {
        return value;
    }
}
