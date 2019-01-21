package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.pets.Folder;
import com.example.android.pets.R;
import com.example.android.pets.data.PetContract.PetEntry;
import com.example.android.pets.data.WordContract.WordEntry;
import com.example.android.pets.data.DeckContract.DeckEntry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * {@link ContentProvider} for Pets app.
 */
public class PetProvider extends ContentProvider implements SharedPreferences {

    /** URI matcher code for the content URI for the pets table */
    private static final int PETS = 100;

    /** URI matcher code for the content URI for a single pet in the pets table */
    private static final int PET_ID = 101;


    private static final int WORDS = 200;
    private static final int WORD_ID = 201;

    private static final int DECKS = 300;
    private static final int DECK_ID = 301;

    private static final int FOLDER_TO_REPEAT = 202;

//    private static final int DECK = 300;

    SharedPreferences mSettings;
    private Integer mSettingWordsAtTime;

    ArrayList<Folder> mTreeArray;


    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID);

        sUriMatcher.addURI(WordContract.CONTENT_AUTHORITY, WordContract.PATH_WORDS, WORDS);
        sUriMatcher.addURI(WordContract.CONTENT_AUTHORITY, WordContract.PATH_WORDS + "/#", WORD_ID);

        sUriMatcher.addURI(WordContract.CONTENT_AUTHORITY, WordContract.PATH_REPEAT_COUNT, FOLDER_TO_REPEAT);

        sUriMatcher.addURI(DeckContract.CONTENT_AUTHORITY, DeckContract.PATH_DECKS, DECKS);
        sUriMatcher.addURI(DeckContract.CONTENT_AUTHORITY, DeckContract.PATH_DECKS + "/#", DECK_ID);

//        sUriMatcher.addURI(WordContract.CONTENT_AUTHORITY, WordContract.PATH_WORDS + "/#/#", DECK);
    }


    /** Tag for the log messages */
    public static final String LOG_TAG = PetProvider.class.getSimpleName();

    /** Database helper object */
    private PetDbHelper mDbHelper;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new PetDbHelper(getContext());

        mSettings = getContext().getSharedPreferences(SettingsContract.APP_PREFERENCES, getContext().MODE_PRIVATE);

        mTreeArray = new ArrayList<>();

        return true;
    }


    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                cursor = database.query(PetContract.PetEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);

                MatrixCursor matrixCursor1 = new MatrixCursor(new String[] {
                        PetEntry._ID,
                        PetEntry.COLUMN_FOLDER_NAME,
                        PetEntry.COLUMN_IMAGE,
                        PetEntry.COLUMN_MARKED,
                        PetEntry.COLUMN_STATISTICS
                });
                cursor.moveToFirst();
                for (int counter = 0; counter< cursor.getCount(); counter++) {
                    int idColumnIndex = cursor.getColumnIndex(PetEntry._ID);
                    int nameColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_FOLDER_NAME);
                    int imageColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_IMAGE);
                    int markedColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_MARKED);

                    Integer folderId = cursor.getInt(idColumnIndex);
                    String folderName = cursor.getString(nameColumnIndex);
                    String folderImage = cursor.getString(imageColumnIndex);
                    Integer folderMarked = cursor.getInt(markedColumnIndex);

                    Folder folder = createFolder(folderId, folderName, folderImage, folderMarked);
                    ArrayList<Folder> tree = getTreeArray(folder);
                    int childrenAmount = tree.size() - 1;

                    int wordsInFolder = 0;
                    int decksInFolder = 0;
                    for (int c = 0; c<tree.size(); c++) {
                        int wordsQuantity = countWordsInFolder(tree.get(tree.size() - (c+1))).intValue();
                        wordsInFolder += wordsQuantity;
                        decksInFolder += countDecks(wordsQuantity);
                    }

                    String stat = "Empty folder";
                    if (childrenAmount != 0 || decksInFolder != 0 || wordsInFolder != 0)
                        stat = "Folders: " + childrenAmount + " :: Decks: " + decksInFolder + " :: Cards: " + wordsInFolder;


                    matrixCursor1.addRow(new Object[] { folderId, folderName, folderImage, folderMarked, stat});
                    cursor.moveToNext();
                }
                cursor = matrixCursor1;



                if (cursor.getCount() == 0) {

                    String[] select = {selectionArgs[0]};
                    Double wordsInFolder = countWordsInFolder1(select);

                    if(mSettings.contains(SettingsContract.WORDS_AT_TIME)) {
                        mSettingWordsAtTime = mSettings.getInt(SettingsContract.WORDS_AT_TIME, 25);
                    }

                    if (wordsInFolder > 0) {

                        double decksQuantity =  Math.ceil(wordsInFolder / mSettingWordsAtTime);
                        double moduloDouble = wordsInFolder % mSettingWordsAtTime;
                        int modulo = (int) moduloDouble;

                        if (modulo == 0)
                            modulo = mSettingWordsAtTime;

                        MatrixCursor matrixCursor = new MatrixCursor(new String[] {
                                PetEntry._ID,
                                PetEntry.COLUMN_FOLDER_NAME,
                                PetEntry.COLUMN_IMAGE,
                                PetEntry.COLUMN_MARKED,
                                PetEntry.COLUMN_STATISTICS
                        });

                        for (int counter = 0; counter < decksQuantity; counter++) {
                            // Create a MatrixCursor filled with the rows you want to add.
                            int deckNumber = counter + 1;

                            int isDeckMarked = 0;
                            if (isDeckMarked(select, counter)) {
                                isDeckMarked = 1;
                            }


                            String deckStatistics;
                            if (deckNumber == decksQuantity) {
                                deckStatistics = "Cards in deck: " + modulo;
                            } else {
                                deckStatistics = "Cards in deck: " + mSettingWordsAtTime;
                            }
                            matrixCursor.addRow(new Object[] { counter, "Deck " + deckNumber, "image dummy", isDeckMarked, deckStatistics});
                        }

                        // Merge your existing cursor with the matrixCursor you created.
                        MergeCursor mergeCursor = new MergeCursor(new Cursor[] { matrixCursor, cursor });

                        // Use your the mergeCursor as you would use your cursor.
                        cursor = mergeCursor;
                    }
                }
                break;
            case PET_ID:
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[]{ String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(PetContract.PetEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;

            case WORDS:
                cursor = database.query(WordContract.WordEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;

            case DECKS:
                cursor = database.query(DeckContract.DeckEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;

            case FOLDER_TO_REPEAT:
//                Log.e("PetProvider", selectionArgs.toString());
//                selectionArgs = new String[]{ "1", "1" };
//                String[] selectionArgs1 = { "1", "1" };
//                Log.e("uuuuu", selectionArgs[0]);
//                Log.e("uuuuu", selectionArgs[1]);
//                String uu = selectionArgs[1];
//                selectionArgs = new String[] { "1", "1" };

//                Log.e("uuuuu", selectionArgs[1]);
                Long wordsToRepMem = DatabaseUtils.queryNumEntries(database, WordEntry.TABLE_NAME,
                        WordEntry.COLUMN_REPEAT_MEM + "=? AND " + WordEntry.COLUMN_LANGUAGE_LEARNING + " = ?", selectionArgs);

                Long wordsToRepSpell = DatabaseUtils.queryNumEntries(database, WordEntry.TABLE_NAME,
                        WordEntry.COLUMN_REPEAT_SPELL + "=? AND " + WordEntry.COLUMN_LANGUAGE_LEARNING + " = ?", selectionArgs);

                MatrixCursor matrixCursor = new MatrixCursor(new String[] {
                        WordEntry.COLUMN_REPEAT_MEM,
                        WordEntry.COLUMN_REPEAT_SPELL
                });

                matrixCursor.addRow(new Object[] { wordsToRepMem, wordsToRepSpell });

                // Use your the mergeCursor as you would use your cursor.
                cursor = matrixCursor;
                break;

            case WORD_ID:
                selection = WordContract.WordEntry._ID + "=?";
                selectionArgs = new String[]{ String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(PetContract.PetEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;

            case DECK_ID:
                selection = DeckContract.DeckEntry._ID + "=?";
                selectionArgs = new String[]{ String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(DeckContract.DeckEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Cannot query unknown URI" + uri);

        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }



    public Folder getFolder(Integer folderId) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_FOLDER_NAME,
                PetEntry.COLUMN_IMAGE,
                PetEntry.COLUMN_MARKED
        };

        String selection = PetEntry._ID + " = ?";
        String[] childrenSelectionArgs = {folderId.toString()};
        Cursor cursor = database.query(PetContract.PetEntry.TABLE_NAME, projection, selection,
                childrenSelectionArgs, null, null, null);
        cursor.moveToFirst();

//        int idColumnIndex = cursor.getColumnIndex(PetEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_FOLDER_NAME);
        int imageColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_IMAGE);
        int markedColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_IMAGE);

//        Integer folderId = cursor.getInt(idColumnIndex);
        String folderName = cursor.getString(nameColumnIndex);
        String folderImage = cursor.getString(imageColumnIndex);
        int folderMarked = cursor.getInt(markedColumnIndex);

        return createFolder(folderId, folderName, folderImage, folderMarked);
    }

    public Folder createFolder(Integer folderId, String folderName, String folderImage, Integer folderMarked) {
        return new Folder(folderId, folderName, folderImage, folderMarked, getChildrenIds(folderId));
    }

    public ArrayList<Integer> getChildrenIds(Integer folderId) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_FOLDER_NAME,
                PetEntry.COLUMN_IMAGE
        };

        String childrenSelection = PetEntry.COLUMN_PARENT + " = ?";
        String[] childrenSelectionArgs = {folderId.toString()};
        Cursor cursor = database.query(PetContract.PetEntry.TABLE_NAME, projection, childrenSelection,
                childrenSelectionArgs, null, null, null);


        ArrayList<Integer> children = new ArrayList<>();
        cursor.moveToFirst();
        for (int counter = 0; counter< cursor.getCount(); counter++) {
            int idColumnIndex = cursor.getColumnIndex(PetEntry._ID);

            Integer childId = cursor.getInt(idColumnIndex);
            children.add(childId);

            cursor.moveToNext();
        }
        return  children;
    }

    public void buildTreeArray(Folder folder) {
        mTreeArray.add(folder);
        if (folder.getChildren().size() > 0) {
            for (Integer childId : folder.getChildren()) {
                Folder child = getFolder(childId);
                buildTreeArray(child);
            }
        }
    }

    public ArrayList<Folder> getTreeArray(Folder folder) {
        mTreeArray.clear();
        buildTreeArray(folder);
        return mTreeArray;
    }

    public boolean isDeckMarked(String[] select, Integer deck) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        String[] projection = {
                DeckEntry._ID
        };

//        ,
//        DeckEntry.COLUMN_DECK,
//                DeckEntry.COLUMN_FOLDER

        String selection = DeckEntry.COLUMN_FOLDER + " = ? AND " + DeckEntry.COLUMN_DECK + " = ? ";

        String [] selectionArgs = new String[2];
        selectionArgs[0] = select[0];
        selectionArgs[1] = deck.toString();

        Cursor cursor = database.query(DeckContract.DeckEntry.TABLE_NAME, projection, selection,
                selectionArgs, null, null, null);
        if (cursor.getCount() > 0)
            return true;
        return false;
    }

    public Double countWordsInFolder1(String[] select) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Long wordsInFolder1 = DatabaseUtils.queryNumEntries(database, WordEntry.TABLE_NAME,
                WordEntry.COLUMN_FOLDER + "=?", select);
        Double wordsInFolder = wordsInFolder1.doubleValue();
        return wordsInFolder;
    }


    public Double countWordsInFolder(Folder folder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Integer folderId = folder.getId();
        String[] select = {folderId.toString()};
        Long wordsInFolder1 = DatabaseUtils.queryNumEntries(database, WordEntry.TABLE_NAME,
                WordEntry.COLUMN_FOLDER + "=?", select);
        Double wordsInFolder = wordsInFolder1.doubleValue();
        return wordsInFolder;
    }

    public int countDecks(int wordsInFolder) {
        if(mSettings.contains(SettingsContract.WORDS_AT_TIME)) {
            mSettingWordsAtTime = mSettings.getInt(SettingsContract.WORDS_AT_TIME, 25);
        }

        if (wordsInFolder > 0) {
            int decksQuantity =  (int) Math.ceil((double) wordsInFolder / mSettingWordsAtTime);
            return decksQuantity;
        }
        return 0;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return insertPet(uri, contentValues);
            case WORDS:
                return insertWord(uri, contentValues);
            case DECKS:
                return insertDeck(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertPet(Uri uri, ContentValues values) {

        // Check that the name is not null
        String name = values.getAsString(PetEntry.COLUMN_FOLDER_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Pet requires a name");
        }

        // No need to check the breed, any value is valid (including null).

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new pet with the given values
        long id = database.insert(PetEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }


        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);

    }



    private Uri insertWord(Uri uri, ContentValues values) {

        // Check that the name is not null
        String word = values.getAsString(WordEntry.COLUMN_WORD);
        if (word == null) {
            throw new IllegalArgumentException("Word cannot be empty");
        }

        String translation = values.getAsString(WordEntry.COLUMN_TRANSLATION);
        if (translation == null) {
            throw new IllegalArgumentException("Translation cannot be empty");
        }

        String folder_id = values.getAsString(WordEntry.COLUMN_FOLDER);
        if (folder_id == null) {
            throw new IllegalArgumentException("Word should be in a folder");
        }

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Insert the new pet with the given values
        long id = database.insert(WordEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);
        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    private Uri insertDeck(Uri uri, ContentValues values) {

        // Check that the name is not null
        String deck = values.getAsString(DeckEntry.COLUMN_DECK);
        if (deck == null) {
            throw new IllegalArgumentException("Deck cannot be empty");
        }

        String folder_id = values.getAsString(DeckEntry.COLUMN_FOLDER);
        if (folder_id == null) {
            throw new IllegalArgumentException("Deck should be in a folder");
        }

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Insert the new pet with the given values
        long id = database.insert(DeckEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        } else {Log.e(LOG_TAG, "uhuuuuuuuuuuuuuuuuuu " + id);}
        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);
        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }


    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return updatePet(uri, contentValues, selection, selectionArgs);
            case PET_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updatePet(uri, contentValues, selection, selectionArgs);

            case WORDS:
                return updateWord(uri, contentValues, selection, selectionArgs);
            case WORD_ID:
                selection = WordEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
//                Log.e("hhhhhhhhhhhhhh", "HHHHHHHHHHHHHHHH");
                return updateWord(uri, contentValues, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update pets in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more pets).
     * Return the number of rows that were successfully updated.
     */
    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link PetEntry#COLUMN_PET_NAME} key is present,
        // check that the name value is not null.
        /**TODO There is a mistake here - need to fix
         * */
        if (values.containsKey(PetEntry.COLUMN_FOLDER_NAME)) {
            String name = values.getAsString(PetEntry.COLUMN_FOLDER_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Pet requires a name");
            }
        }

      // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(PetEntry.TABLE_NAME, values, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows updated
        return rowsUpdated;
    }





    private int updateWord(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link PetEntry#COLUMN_PET_NAME} key is present,
        // check that the name value is not null.
        /**TODO There is a mistake here - need to fix
         * */
        if (values.containsKey(WordEntry.COLUMN_WORD)) {
            String word = values.getAsString(WordEntry.COLUMN_WORD);
            if (word == null) {
                throw new IllegalArgumentException("word cannot be empty");
            }
        }

        if (values.containsKey(WordEntry.COLUMN_TRANSLATION)) {
            String translation = values.getAsString(WordEntry.COLUMN_TRANSLATION);
            if (translation == null) {
                throw new IllegalArgumentException("translation cannot be empty");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(WordEntry.TABLE_NAME, values, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows updated
        return rowsUpdated;
    }




    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:

                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PET_ID:
                // Delete a single row given by the ID in the URI

                long folderId = ContentUris.parseId(uri);

                String[] projection = {
                        PetEntry._ID,
                        PetEntry.COLUMN_FOLDER_NAME,
                        PetEntry.COLUMN_IMAGE,
                        PetEntry.COLUMN_MARKED
                };

                Cursor cursor = query(uri, projection ,selection, selectionArgs, null);
                cursor.moveToFirst();

                int idColumnIndex = cursor.getColumnIndex(PetEntry._ID);
                int nameColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_FOLDER_NAME);
                int imageColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_IMAGE);
                int markedColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_MARKED);

                Integer id = cursor.getInt(idColumnIndex);
                String name = cursor.getString(nameColumnIndex);
                String image = cursor.getString(imageColumnIndex);
                Integer marked = cursor.getInt(markedColumnIndex);

                Folder folder = createFolder(id, name, image, marked);
                ArrayList<Folder> tree = getTreeArray(folder);

                for (int c = 0; c<tree.size(); c++) {
                    Folder f = tree.get(tree.size() - (c+1));
                    String imageName = f.getImage();

//                    Log.e("pa", "I aaaaaam heeeeeereeeee 2");

                    if (imageName != "" && imageName != null) {
//                        Log.e("pa", imageName);
                        try {
                            ContextWrapper cw = new ContextWrapper(getContext());
                            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
                            File file = new File(directory, imageName);
                            boolean deleted = file.delete();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }

                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(folderId) };
                rowsDeleted = database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
//                rowsDeleted = 0;
                break;

            case WORDS:

//                Log.e("pa", "I aaaaaam heeeeeereeeee 3");

                if(mSettings.contains(SettingsContract.WORDS_AT_TIME)) {
                    mSettingWordsAtTime = mSettings.getInt(SettingsContract.WORDS_AT_TIME, 25);
                }

                String[] args = {selectionArgs[0]};
                int skipInt = Integer.parseInt(selectionArgs[1]) * mSettingWordsAtTime;


                selection = WordEntry._ID + " in (select " + WordEntry._ID + " from "
                        + WordEntry.TABLE_NAME + " where " + WordEntry.COLUMN_FOLDER
                        + " = ? order by " + WordEntry._ID + " LIMIT " + skipInt + "," + mSettingWordsAtTime + ")";

                rowsDeleted = database.delete(WordEntry.TABLE_NAME, selection, args);

                // Delete all rows that match the selection and selection args
//                rowsDeleted = database.delete(WordEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case WORD_ID:

//                Log.e("pa", "I aaaaaam heeeeeereeeee 4");

                // Delete a single row given by the ID in the URI
                selection = WordEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(WordEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case DECKS:
                Log.e("pa", "I aaaaaam heeeeeereeeee 5");
//                selection = DeckEntry._ID + "=?";
//                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(DeckEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case DECK_ID:
//                Log.e("pa", "I aaaaaam heeeeeereeeee 5");
                selection = DeckEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(DeckEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows deleted
        return rowsDeleted;

    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return PetEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetEntry.CONTENT_ITEM_TYPE;

            case WORDS:
                return WordEntry.CONTENT_LIST_TYPE;
            case WORD_ID:
                return WordEntry.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Map<String, ?> getAll() {
        return null;
    }

    @Nullable
    @Override
    public String getString(String s, @Nullable String s1) {
        return null;
    }

    @Nullable
    @Override
    public Set<String> getStringSet(String s, @Nullable Set<String> set) {
        return null;
    }

    @Override
    public int getInt(String s, int i) {
        return 0;
    }

    @Override
    public long getLong(String s, long l) {
        return 0;
    }

    @Override
    public float getFloat(String s, float v) {
        return 0;
    }

    @Override
    public boolean getBoolean(String s, boolean b) {
        return false;
    }

    @Override
    public boolean contains(String s) {
        return false;
    }

    @Override
    public Editor edit() {
        return null;
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {

    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {

    }
}