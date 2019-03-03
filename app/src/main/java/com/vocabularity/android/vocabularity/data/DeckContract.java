package com.vocabularity.android.vocabularity.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class DeckContract {

    private DeckContract() {}

    public static final String CONTENT_AUTHORITY = "com.example.android.pets";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_DECKS = "decks";

    public static final String PATH_REPEAT_COUNT = "repeat_count";

    public static final class DeckEntry implements BaseColumns {

        /** Name of database table for pets */
        public final static String TABLE_NAME = "decks";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_DECKS);

        public static final Uri TO_REP_COUNT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_REPEAT_COUNT);


        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of pets.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DECKS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single pet.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DECKS;


        public final static String _ID = BaseColumns._ID;

        public final static String COLUMN_DECK ="deck";

        public final static String COLUMN_FOLDER = "folder_id";

    }

}
