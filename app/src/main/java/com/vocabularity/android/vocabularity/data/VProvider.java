package com.vocabularity.android.vocabularity.data;

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

import com.vocabularity.android.vocabularity.Folder;
import com.vocabularity.android.vocabularity.data.FolderContract.FolderEntry;
import com.vocabularity.android.vocabularity.data.WordContract.WordEntry;
import com.vocabularity.android.vocabularity.data.DeckContract.DeckEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;


public class VProvider extends ContentProvider implements SharedPreferences {

    /** URI matcher code for the content URI for the folders table */
    private static final int FOLDERS = 100;

    /** URI matcher code for the content URI for a single folder in the folders table */
    private static final int FOLDER_ID = 101;


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

        sUriMatcher.addURI(FolderContract.CONTENT_AUTHORITY, FolderContract.PATH_FOLDERS, FOLDERS);
        sUriMatcher.addURI(FolderContract.CONTENT_AUTHORITY, FolderContract.PATH_FOLDERS + "/#", FOLDER_ID);

        sUriMatcher.addURI(WordContract.CONTENT_AUTHORITY, WordContract.PATH_WORDS, WORDS);
        sUriMatcher.addURI(WordContract.CONTENT_AUTHORITY, WordContract.PATH_WORDS + "/#", WORD_ID);

        sUriMatcher.addURI(WordContract.CONTENT_AUTHORITY, WordContract.PATH_REPEAT_COUNT, FOLDER_TO_REPEAT);

        sUriMatcher.addURI(DeckContract.CONTENT_AUTHORITY, DeckContract.PATH_DECKS, DECKS);
        sUriMatcher.addURI(DeckContract.CONTENT_AUTHORITY, DeckContract.PATH_DECKS + "/#", DECK_ID);

//        sUriMatcher.addURI(WordContract.CONTENT_AUTHORITY, WordContract.PATH_WORDS + "/#/#", DECK);
    }


    /** Tag for the log messages */
    public static final String LOG_TAG = VProvider.class.getSimpleName();

    /** Database helper object */
    private VDbHelper mDbHelper;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new VDbHelper(getContext());

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
            case FOLDERS:
                cursor = database.query(FolderEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);

                MatrixCursor matrixCursor1 = new MatrixCursor(new String[] {
                        FolderContract.FolderEntry._ID,
                        FolderEntry.COLUMN_FOLDER_NAME,
                        FolderEntry.COLUMN_IMAGE,
                        FolderContract.FolderEntry.COLUMN_MARKED,
                        FolderEntry.COLUMN_STATISTICS
                });
                cursor.moveToFirst();
                for (int counter = 0; counter< cursor.getCount(); counter++) {
                    int idColumnIndex = cursor.getColumnIndex(FolderEntry._ID);
                    int nameColumnIndex = cursor.getColumnIndex(FolderEntry.COLUMN_FOLDER_NAME);
                    int imageColumnIndex = cursor.getColumnIndex(FolderContract.FolderEntry.COLUMN_IMAGE);
                    int markedColumnIndex = cursor.getColumnIndex(FolderEntry.COLUMN_MARKED);

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

//                if (selection == FolderEntry.COLUMN_PARENT + " is null AND " + FolderEntry.COLUMN_LEARNING_LANGUAGE + " = ?")
//                    Log.e("Case ", "one");
//                else
//                    Log.e("Case ", "two");

//                selection == FolderEntry.COLUMN_PARENT + " = ?"
//                String selection1 =  FolderEntry.COLUMN_PARENT + " is null AND " + FolderEntry.COLUMN_LEARNING_LANGUAGE + " = ?";
                String selection2 = FolderContract.FolderEntry.COLUMN_PARENT + " = ?";
                String selection3 = FolderContract.FolderEntry.COLUMN_PARENT + " = ? AND " + FolderEntry.COLUMN_LEARNING_LANGUAGE + " = ?";


                if (
                        (selection == selection2 && cursor.getCount() == 0) ||
                        (selection == selection3 && cursor.getCount() == 0)
                        ) {

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
                                FolderContract.FolderEntry._ID,
                                FolderContract.FolderEntry.COLUMN_FOLDER_NAME,
                                FolderContract.FolderEntry.COLUMN_IMAGE,
                                FolderEntry.COLUMN_MARKED,
                                FolderContract.FolderEntry.COLUMN_STATISTICS
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
            case FOLDER_ID:
                selection = FolderEntry._ID + "=?";
                selectionArgs = new String[]{ String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(FolderContract.FolderEntry.TABLE_NAME, projection, selection,
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
                cursor = database.query(FolderContract.FolderEntry.TABLE_NAME, projection, selection,
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
                FolderEntry._ID,
                FolderContract.FolderEntry.COLUMN_FOLDER_NAME,
                FolderContract.FolderEntry.COLUMN_IMAGE,
                FolderContract.FolderEntry.COLUMN_MARKED
        };

        String selection = FolderContract.FolderEntry._ID + " = ?";
        String[] childrenSelectionArgs = {folderId.toString()};
        Cursor cursor = database.query(FolderContract.FolderEntry.TABLE_NAME, projection, selection,
                childrenSelectionArgs, null, null, null);
        cursor.moveToFirst();

//        int idColumnIndex = cursor.getColumnIndex(FolderEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(FolderEntry.COLUMN_FOLDER_NAME);
        int imageColumnIndex = cursor.getColumnIndex(FolderEntry.COLUMN_IMAGE);
        int markedColumnIndex = cursor.getColumnIndex(FolderContract.FolderEntry.COLUMN_IMAGE);

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
                FolderEntry._ID,
                FolderEntry.COLUMN_FOLDER_NAME,
                FolderContract.FolderEntry.COLUMN_IMAGE
        };

        String childrenSelection = FolderEntry.COLUMN_PARENT + " = ?";
        String[] childrenSelectionArgs = {folderId.toString()};
        Cursor cursor = database.query(FolderContract.FolderEntry.TABLE_NAME, projection, childrenSelection,
                childrenSelectionArgs, null, null, null);


        ArrayList<Integer> children = new ArrayList<>();
        cursor.moveToFirst();
        for (int counter = 0; counter< cursor.getCount(); counter++) {
            int idColumnIndex = cursor.getColumnIndex(FolderEntry._ID);

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
        /*select = new String[1];
        select[0] = "10";*/
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
            case FOLDERS:
                return insertFolder(uri, contentValues);
            case WORDS:
                return insertWord(uri, contentValues);
            case DECKS:
                return insertDeck(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertFolder(Uri uri, ContentValues values) {

        // Check that the name is not null
        String name = values.getAsString(FolderContract.FolderEntry.COLUMN_FOLDER_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Folder requires a name");
        }

        // No need to check the breed, any value is valid (including null).

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new folder with the given values
        long id = database.insert(FolderEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }


        // Notify all listeners that the data has changed for the folder content URI
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
        // Insert the new folder with the given values
        long id = database.insert(WordEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        // Notify all listeners that the data has changed for the folder content URI
        getContext().getContentResolver().notifyChange(uri, null);
        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    public Uri insertDeck(Uri uri, ContentValues values) {

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
        // Insert the new folder with the given values
        long id = database.insert(DeckEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
//            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
//        else {Log.e(LOG_TAG, "uhuuuuuuuuuuuuuuuuuu " + id);}
        // Notify all listeners that the data has changed for the folder content URI
        getContext().getContentResolver().notifyChange(uri, null);
        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }


    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case FOLDERS:
                return updateFolder(uri, contentValues, selection, selectionArgs);
            case FOLDER_ID:
                // For the FOLDER_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = FolderContract.FolderEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateFolder(uri, contentValues, selection, selectionArgs);

            case WORDS:
                return updateWord(uri, contentValues, selection, selectionArgs);
            case WORD_ID:
                selection = WordEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateWord(uri, contentValues, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateFolder(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // check that the name value is not null.
        /**TODO There is a mistake here - need to fix
         * */
        if (values.containsKey(FolderContract.FolderEntry.COLUMN_FOLDER_NAME)) {
            String name = values.getAsString(FolderEntry.COLUMN_FOLDER_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Folder requires a name");
            }
        }

      // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(FolderContract.FolderEntry.TABLE_NAME, values, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows updated
        return rowsUpdated;
    }





    private int updateWord(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
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
            case FOLDERS:

                String[] projection1 = {
                        FolderContract.FolderEntry._ID,
                        FolderContract.FolderEntry.COLUMN_FOLDER_NAME,
                        FolderEntry.COLUMN_IMAGE,
                        FolderContract.FolderEntry.COLUMN_MARKED
                };
                Cursor cursor1 = query(uri, projection1 ,selection, selectionArgs, null);
                cursor1.moveToFirst();

                for (int c = 0; c < cursor1.getCount(); c++) {

                    int idColumnIndex = cursor1.getColumnIndex(FolderContract.FolderEntry._ID);
                    int nameColumnIndex = cursor1.getColumnIndex(FolderContract.FolderEntry.COLUMN_FOLDER_NAME);
                    int imageColumnIndex = cursor1.getColumnIndex(FolderEntry.COLUMN_IMAGE);
                    int markedColumnIndex = cursor1.getColumnIndex(FolderEntry.COLUMN_MARKED);

                    Integer id = cursor1.getInt(idColumnIndex);
                    String name = cursor1.getString(nameColumnIndex);
                    String image = cursor1.getString(imageColumnIndex);
                    Integer marked = cursor1.getInt(markedColumnIndex);

                    Folder folder = createFolder(id, name, image, marked);
                    ArrayList<Folder> tree = getTreeArray(folder);

                    for (int c1 = 0; c1<tree.size(); c1++) {
                        Folder f = tree.get(tree.size() - (c1+1));
                        String imageName = f.getImage();

                        if (imageName != "" && imageName != null) {
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

                    cursor1.moveToNext();
                }
                cursor1.close();


                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(FolderEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case FOLDER_ID:
                // Delete a single row given by the ID in the URI

                long folderId = ContentUris.parseId(uri);

                String[] projection = {
                        FolderContract.FolderEntry._ID,
                        FolderEntry.COLUMN_FOLDER_NAME,
                        FolderContract.FolderEntry.COLUMN_IMAGE,
                        FolderEntry.COLUMN_MARKED
                };

                Cursor cursor = query(uri, projection ,selection, selectionArgs, null);
                cursor.moveToFirst();

                int idColumnIndex = cursor.getColumnIndex(FolderEntry._ID);
                int nameColumnIndex = cursor.getColumnIndex(FolderEntry.COLUMN_FOLDER_NAME);
                int imageColumnIndex = cursor.getColumnIndex(FolderContract.FolderEntry.COLUMN_IMAGE);
                int markedColumnIndex = cursor.getColumnIndex(FolderContract.FolderEntry.COLUMN_MARKED);

                Integer id = cursor.getInt(idColumnIndex);
                String name = cursor.getString(nameColumnIndex);
                String image = cursor.getString(imageColumnIndex);
                Integer marked = cursor.getInt(markedColumnIndex);

                Folder folder = createFolder(id, name, image, marked);
                ArrayList<Folder> tree = getTreeArray(folder);

                for (int c = 0; c<tree.size(); c++) {
                    Folder f = tree.get(tree.size() - (c+1));
                    String imageName = f.getImage();

                    if (imageName != "" && imageName != null) {
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

                selection = FolderEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(folderId) };
                rowsDeleted = database.delete(FolderEntry.TABLE_NAME, selection, selectionArgs);
//                rowsDeleted = 0;
                break;

            case WORDS:

                if(mSettings.contains(SettingsContract.WORDS_AT_TIME)) {
                    mSettingWordsAtTime = mSettings.getInt(SettingsContract.WORDS_AT_TIME, 25);
                }

                String[] args = {selectionArgs[0]};
                int skipInt = Integer.parseInt(selectionArgs[1]) * mSettingWordsAtTime;

                selection = WordEntry._ID + " in (select " + WordEntry._ID + " from "
                        + WordEntry.TABLE_NAME + " where " + WordEntry.COLUMN_FOLDER
                        + " = ? order by " + WordEntry._ID + " LIMIT " + skipInt + "," + mSettingWordsAtTime + ")";

                rowsDeleted = database.delete(WordEntry.TABLE_NAME, selection, args);

                shiftDecks(selectionArgs);


                /*String selectionDeck = DeckEntry.COLUMN_FOLDER + " = ? AND " + DeckEntry.COLUMN_DECK + " = ?";
                int rowsDeleted1 = database.delete(DeckEntry.TABLE_NAME, selectionDeck, selectionArgs);

                Integer deckNumber = Integer.parseInt(selectionArgs[1]);

                String selectionDeck1 = DeckEntry.COLUMN_FOLDER + " = ? ";
                String[] projectionDeck1 = {
                        DeckEntry._ID,
                        DeckEntry.COLUMN_DECK,
                        DeckEntry.COLUMN_FOLDER
                };
                Cursor cursorDeck1 = database.query(DeckEntry.TABLE_NAME, projectionDeck1, selectionDeck1,
                    args, null, null, DeckEntry.COLUMN_DECK);
                cursorDeck1.moveToFirst();
                for (int c = 0; c < cursorDeck1.getCount(); c++) {
                    int idColumnIndexDeck1 = cursorDeck1.getColumnIndex(DeckEntry._ID);
                    int deckColumnIndexDeck1 = cursorDeck1.getColumnIndex(DeckEntry.COLUMN_DECK);
                    int folderColumnIndexDeck1 = cursorDeck1.getColumnIndex(DeckEntry.COLUMN_FOLDER);

                    Integer deckId = cursorDeck1.getInt(idColumnIndexDeck1);
                    Integer deckName = cursorDeck1.getInt(deckColumnIndexDeck1);
                    Integer folderName = cursorDeck1.getInt(folderColumnIndexDeck1);

                    if (deckName > deckNumber) {

                        String selectionDeck2 = DeckEntry._ID + " = ? ";
                        String[] selectionArgsDeck2 = {deckId.toString()};
                        int rowsDeleted2 = database.delete(DeckEntry.TABLE_NAME, selectionDeck2, selectionArgsDeck2);

                        ContentValues valuesDeck2 = new ContentValues();
                        valuesDeck2.put(DeckEntry.COLUMN_DECK, deckName - 1);
                        valuesDeck2.put(DeckEntry.COLUMN_FOLDER, folderName);
                        insertDeck(DeckEntry.CONTENT_URI, valuesDeck2);

                    }
                    cursorDeck1.moveToNext();
                }*/

                break;

            case WORD_ID:

                uncheckLastDeckIfNeeded(selectionArgs[0]);

                if (selection != null) {
                    shiftDecks(selectionArgs);
                }

                /*if(mSettings.contains(SettingsContract.WORDS_AT_TIME)) {
                    mSettingWordsAtTime = mSettings.getInt(SettingsContract.WORDS_AT_TIME, 25);
                }
                Double wordsInFolder = countWordsInFolder1(new String[] {selectionArgs[0]});
                double decksQuantity =  Math.ceil(wordsInFolder / mSettingWordsAtTime);
                double moduloDouble = wordsInFolder % mSettingWordsAtTime;
                int modulo = (int) moduloDouble;
//                if (modulo == 0)
//                    modulo = mSettingWordsAtTime;
                if (modulo == 1) {
                    selection = DeckEntry._ID + "=?";
                    selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                    database.delete(DeckEntry.TABLE_NAME, selection, selectionArgs);
                }*/

                // Delete a single row given by the ID in the URI
                selection = WordEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(WordEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case DECKS:
//                Log.e("pa", "I aaaaaam heeeeeereeeee 5");
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

    public void uncheckLastDeckIfNeeded(String folderId) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        if(mSettings.contains(SettingsContract.WORDS_AT_TIME)) {
            mSettingWordsAtTime = mSettings.getInt(SettingsContract.WORDS_AT_TIME, 25);
        }
        Double wordsInFolder = countWordsInFolder1(new String[] {folderId});
        double decksQuantity =  Math.ceil(wordsInFolder / mSettingWordsAtTime);
        double moduloDouble = wordsInFolder % mSettingWordsAtTime;
        int modulo = (int) moduloDouble;
        Integer lastDeck = (int) decksQuantity - 1;

        if (modulo == 1) {
            String selection = DeckEntry.COLUMN_FOLDER + " =? AND " + DeckEntry.COLUMN_DECK + " =? ";
            String[] selectionArgs = new String[] { folderId, lastDeck.toString() };
            database.delete(DeckEntry.TABLE_NAME, selection, selectionArgs);
        }
    }

    public void shiftDecks(String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        String selectionDeck = DeckEntry.COLUMN_FOLDER + " = ? AND " + DeckEntry.COLUMN_DECK + " = ?";
        int rowsDeleted1 = database.delete(DeckEntry.TABLE_NAME, selectionDeck, selectionArgs);

        Integer deckNumber = Integer.parseInt(selectionArgs[1]);

        String selectionDeck1 = DeckEntry.COLUMN_FOLDER + " = ? ";
        String[] projectionDeck1 = {
                DeckEntry._ID,
                DeckEntry.COLUMN_DECK,
                DeckEntry.COLUMN_FOLDER
        };

        String[] args = {selectionArgs[0]};

        Cursor cursorDeck1 = database.query(DeckEntry.TABLE_NAME, projectionDeck1, selectionDeck1,
                args, null, null, DeckEntry.COLUMN_DECK);
        cursorDeck1.moveToFirst();
        for (int c = 0; c < cursorDeck1.getCount(); c++) {
            int idColumnIndexDeck1 = cursorDeck1.getColumnIndex(DeckEntry._ID);
            int deckColumnIndexDeck1 = cursorDeck1.getColumnIndex(DeckEntry.COLUMN_DECK);
            int folderColumnIndexDeck1 = cursorDeck1.getColumnIndex(DeckEntry.COLUMN_FOLDER);

            Integer deckId = cursorDeck1.getInt(idColumnIndexDeck1);
            Integer deckName = cursorDeck1.getInt(deckColumnIndexDeck1);
            Integer folderName = cursorDeck1.getInt(folderColumnIndexDeck1);

            if (deckName > deckNumber) {

                String selectionDeck2 = DeckEntry._ID + " = ? ";
                String[] selectionArgsDeck2 = {deckId.toString()};
                int rowsDeleted2 = database.delete(DeckEntry.TABLE_NAME, selectionDeck2, selectionArgsDeck2);

                ContentValues valuesDeck2 = new ContentValues();
                valuesDeck2.put(DeckEntry.COLUMN_DECK, deckName - 1);
                valuesDeck2.put(DeckEntry.COLUMN_FOLDER, folderName);
                insertDeck(DeckEntry.CONTENT_URI, valuesDeck2);

            }
            cursorDeck1.moveToNext();
        }
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case FOLDERS:
                return FolderContract.FolderEntry.CONTENT_LIST_TYPE;
            case FOLDER_ID:
                return FolderContract.FolderEntry.CONTENT_ITEM_TYPE;

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