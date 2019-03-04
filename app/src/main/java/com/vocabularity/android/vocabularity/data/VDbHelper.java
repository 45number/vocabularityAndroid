/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vocabularity.android.vocabularity.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.vocabularity.android.vocabularity.Word;
import com.vocabularity.android.vocabularity.data.FolderContract.FolderEntry;
import com.vocabularity.android.vocabularity.data.WordContract.WordEntry;
import com.vocabularity.android.vocabularity.data.DeckContract.DeckEntry;

/**
 * Database helper for Vocabularity app. Manages database creation and version management.
 */
public class VDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = VDbHelper.class.getSimpleName();

    /** Name of the database file */
    private static final String DATABASE_NAME = "vocabularity.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link VDbHelper}.
     *
     * @param context of the app
     */
    public VDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the folders table
        String SQL_CREATE_FOLDERS_TABLE =  "CREATE TABLE " + FolderEntry.TABLE_NAME + " ("
                + FolderContract.FolderEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + FolderEntry.COLUMN_LEARNING_LANGUAGE + " INTEGER NOT NULL, "
                + FolderEntry.COLUMN_USERS_LANGUAGE + " INTEGER NOT NULL DEFAULT 0, "
                + FolderEntry.COLUMN_FOLDER_NAME + " TEXT NOT NULL, "
                + FolderContract.FolderEntry.COLUMN_IMAGE + " TEXT, "
                + FolderEntry.COLUMN_PARENT + " INTEGER,"
                + FolderEntry.COLUMN_MARKED + " INTEGER NOT NULL DEFAULT 0,"
                + " FOREIGN KEY (" + FolderEntry.COLUMN_PARENT + ") REFERENCES "+ FolderEntry.TABLE_NAME +"(" + FolderEntry._ID + ") ON DELETE CASCADE"
                + ");";
//                + ");";

//        NOT NULL DEFAULT 0
        // Execute the SQL statement
        db.execSQL(SQL_CREATE_FOLDERS_TABLE);

        // Create a String that contains the SQL statement to create the words table
        String SQL_CREATE_WORDS_TABLE =  "CREATE TABLE " + WordEntry.TABLE_NAME + " ("
                + WordEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + WordEntry.COLUMN_WORD + " TEXT NOT NULL, "
                + WordEntry.COLUMN_TRANSLATION + " TEXT NOT NULL, "
                + WordEntry.COLUMN_FOLDER + " INTEGER NOT NULL,"
                + WordEntry.COLUMN_LANGUAGE_LEARNING + " INTEGER NOT NULL,"
                + WordEntry.COLUMN_USERS_LANGUAGE + " INTEGER NOT NULL DEFAULT 0,"
                + WordEntry.COLUMN_REPEAT_MEM + " INTEGER NOT NULL DEFAULT 0,"
                + WordEntry.COLUMN_REPEAT_SPELL + " INTEGER NOT NULL DEFAULT 0,"
                + WordEntry.COLUMN_EXAMPLE + " TEXT,"
                + WordEntry.COLUMN_EXAMPLE_TRANSLATION + " TEXT,"
                + WordEntry.COLUMN_IMAGE + " TEXT,"
                + WordEntry.COLUMN_SOUND + " TEXT,"
                + WordEntry.COLUMN_SYNC_ID + " INTEGER,"
                + WordEntry.COLUMN_TRANSCRIPTION + " TEXT,"
                + WordEntry.COLUMN_VIDEO + " TEXT,"
                + " FOREIGN KEY (" + WordEntry.COLUMN_FOLDER + ") REFERENCES "+ FolderEntry.TABLE_NAME +"(" + FolderEntry._ID + ") ON DELETE CASCADE"
                + ");";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_WORDS_TABLE);



        // Create a String that contains the SQL statement to create the decks table
        String SQL_CREATE_DECKS_TABLE =  "CREATE TABLE " + DeckEntry.TABLE_NAME + " ("
                + DeckEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DeckEntry.COLUMN_DECK + " INTEGER NOT NULL, "
                + DeckEntry.COLUMN_FOLDER + " INTEGER NOT NULL,"
                + " FOREIGN KEY (" + DeckEntry.COLUMN_FOLDER + ") REFERENCES "+ FolderContract.FolderEntry.TABLE_NAME +"(" + FolderEntry._ID + ") ON DELETE CASCADE"
                + ");";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_DECKS_TABLE);



        /*String SQL_CREATE_LANGUAGES_TABLE =  "CREATE TABLE " + LanguagesEntry.TABLE_NAME + " ("
                + LanguagesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + LanguagesEntry.COLUMN_LANG_ID + " INTEGER NOT NULL DEFAULT 0"
                + ");";
        db.execSQL(SQL_CREATE_LANGUAGES_TABLE);*/
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }

    /*@Override
    public void onConfigure(SQLiteDatabase db){
        db.setForeignKeyConstraintsEnabled(true);
    }*/


    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }


}