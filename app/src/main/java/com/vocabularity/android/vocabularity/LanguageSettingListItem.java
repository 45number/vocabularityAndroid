package com.vocabularity.android.vocabularity;

public class LanguageSettingListItem {
    private String name;
    private Boolean isLearning;

    public LanguageSettingListItem(String name, Boolean isLearning) {
        this.name = name;
        this.isLearning = isLearning;
    }

    public String getName() {
        return name;
    }

    public Boolean getIsLearning() {
        return isLearning;
    }
}
