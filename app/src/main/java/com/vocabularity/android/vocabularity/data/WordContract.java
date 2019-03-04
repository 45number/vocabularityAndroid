package com.vocabularity.android.vocabularity.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class WordContract {

    private WordContract() {}

    public static final String CONTENT_AUTHORITY = "com.vocabularity.android.vocabularity";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_WORDS = "words";

    public static final String PATH_REPEAT_COUNT = "repeat_count";

    /**
     * Inner class that defines constant values for the pets database table.
     * Each entry in the table represents a single pet.
     */
    public static final class WordEntry implements BaseColumns {

        /** Name of database table for pets */
        public final static String TABLE_NAME = "words";



        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_WORDS);

        public static final Uri TO_REP_COUNT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_REPEAT_COUNT);


        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of pets.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WORDS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single pet.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WORDS;


        public final static String _ID = BaseColumns._ID;

        public final static String COLUMN_WORD ="word";

        public final static String COLUMN_TRANSLATION = "translation";

        public final static String COLUMN_FOLDER = "folder_id";

        public final static String COLUMN_LANGUAGE_LEARNING = "language_learning";

        public final static String COLUMN_REPEAT_MEM = "to_repeat_mem";

        public final static String COLUMN_REPEAT_SPELL = "to_repeat_spell";

    }
}
