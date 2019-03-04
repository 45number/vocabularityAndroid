package com.vocabularity.android.vocabularity.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for the Vocabularity app.
 */
public final class FolderContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private FolderContract() {}


    public static final String CONTENT_AUTHORITY = "com.vocabularity.android.vocabularity";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_FOLDERS = "folders";

    /**
     * Inner class that defines constant values for the Folders database table.
     * Each entry in the table represents a single folder.
     */
    public static final class FolderEntry implements BaseColumns {

        /** Name of database table for folders */
        public final static String TABLE_NAME = "voca";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_FOLDERS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of folders.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FOLDERS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single folder.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FOLDERS;


        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_LEARNING_LANGUAGE ="learning_language";
        public final static String COLUMN_USERS_LANGUAGE = "users_language";
        public final static String COLUMN_STATISTICS = "statistics";
        public final static String COLUMN_FOLDER_NAME ="name";
        public final static String COLUMN_IMAGE = "image";
        public final static String COLUMN_PARENT = "parent_id";
        public final static String COLUMN_MARKED ="marked";


    }

}

