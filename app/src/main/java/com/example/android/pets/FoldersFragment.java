package com.example.android.pets;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.pets.data.DeckContract;
import com.example.android.pets.data.SettingsContract;
import com.example.android.pets.data.WordContract;
import com.example.android.pets.data.PetContract.PetEntry;
import com.example.android.pets.data.pathItem;

import java.util.ArrayList;
import java.util.Locale;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;


import android.os.Handler;



public class FoldersFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>, OnBackPressed /*, refreshDecksDogNail*/ {

    private static final int PET_LOADER = 0;

    private static final int REPEAT_LOADER = 1;



    PetCursorAdapter mCursorAdapter;
    private int mAdapterNumber;
    ListView petListView;


    SharedPreferences mSettings;

    View rootView;
    FloatingActionButton fab;
    Button addButtonEmpty;
    TextView pathTextView;
    View emptyFolderView;
    View emptyView;

//    LinearLayout markedBadge;
//    FloatingActionButton rootFab;


    TextToSpeech tts;
    private int s;

    private int mMemQuantity = 0;
    private int mSpellQuantity = 0;

    private static final int RESULT_SETTINGS = 3;
    private static final int RESULT_SETTINGS_LANGUAGES = 4;
    private static final int RESULT_FILE_EXPLORER = 5;
    private static final int RESULT_DELETED_DECK = 6;

    private static final String PATH_TREE = "path";

    public static final String PATH_SEPARATOR = "/";


    private ArrayList<pathItem> mTreePath = new ArrayList<>();

    int mFoldersQuantity;

    public FoldersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_folders, container, false);

        mSettings = getContext().getSharedPreferences(SettingsContract.APP_PREFERENCES, getContext().MODE_PRIVATE);

//        rootFab = ((CatalogActivity)getActivity()).getFab();
//        rootFab.hide();


        if(savedInstanceState == null || !savedInstanceState.containsKey(PATH_TREE)) {
            mTreePath.add(new pathItem(0L, getString(R.string.root)));
        } else {
            mTreePath = savedInstanceState.getParcelableArrayList(PATH_TREE);
        }


        pathTextView = rootView.findViewById(R.id.pathTextView);


        // Setup FAB to open EditorActivity
        fab = rootView.findViewById(R.id.fab);
        fab.hide();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseRepeatMode();
            }
        });


        // Find the ListView which will be populated with the pet data
        petListView = rootView.findViewById(R.id.list);
        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
//        emptyView = rootView.findViewById(R.id.progress_bar);
//        petListView.setEmptyView(emptyView);


        emptyFolderView = rootView.findViewById(R.id.empty_view);
        emptyFolderView.setVisibility(View.GONE);


        mCursorAdapter = new PetCursorAdapter(getActivity(), null);
        petListView.setAdapter(mCursorAdapter);


        addButtonEmpty = rootView.findViewById(R.id.addButtonEmpty);
        addButtonEmpty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addOptions();
            }
        });


        registerForContextMenu(petListView);

        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                if (mAdapterNumber == 0) {

                    TextView folderNameTextView = view.findViewById(R.id.name);
                    String folderName = folderNameTextView.getText().toString();

                    mTreePath.add(new pathItem(id, folderName));

                    Bundle args=new Bundle();
                    args.putString("selection", PetEntry.COLUMN_PARENT + " = ?");
                    Long idLong = id;
                    String idString = idLong.toString();
                    String[] selectionArgs = {idString};
                    args.putStringArray("selectionArgs", selectionArgs);
                    getLoaderManager().restartLoader(PET_LOADER, args, FoldersFragment.this);

//                    SharedPreferences.Editor editor = mSettings.edit();
//                    editor.putLong(SettingsContract.LAST_FOLDER, getCurrentFolder().getId());
//                    editor.apply();
//                    Log.e(PATH_TREE + " onClick ", mTreePath.toString());

                } else {
                    chooseMode(id);
                }


            }
        });


        tts=new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                s = status;
                new Thread(new Runnable() {
                    public void run() {
                        if(s != TextToSpeech.ERROR) {
                            tts.setPitch(1.1f); // saw from internet
                            tts.setLanguage(Locale.UK);
                        }
                    }
                }).start();
            }
        });
        ConvertTextToSpeech(" ");

        setHasOptionsMenu(true);
//        folder_name is null or folder_name = ?
//        or " + PetEntry.COLUMN_PARENT + "= ?



        Bundle args=new Bundle();
        args.putString("selection", PetEntry.COLUMN_PARENT + " is null AND " + PetEntry.COLUMN_LEARNING_LANGUAGE + " = ?");
        Integer langLearningInteger = getArguments().getInt("language_learning");
        String langLearning = langLearningInteger.toString();
//        Log.e("Line 159, learn lang is", langLearning);
        String[] selectionArgs = {/*idString, */langLearning};
        args.putStringArray("selectionArgs", selectionArgs);
        getLoaderManager().initLoader(PET_LOADER, args, FoldersFragment.this);
//        getLoaderManager().initLoader(PET_LOADER, null, FoldersFragment.this);



        Bundle repeatArgs = new Bundle();
        Integer repeatLangLearningInteger = getArguments().getInt("language_learning");
        String repeatLangLearning = repeatLangLearningInteger.toString();
        String[] repeatSelectionArgs = new String[]{ "1",  repeatLangLearning};
        repeatArgs.putStringArray("selectionArgs", repeatSelectionArgs);
        getLoaderManager().initLoader(REPEAT_LOADER, repeatArgs, FoldersFragment.this);

//        updatePathTextView();

        return rootView;
    }

    private void ConvertTextToSpeech(final String toSay) {
        new Thread(new Runnable() {
            public void run() {
                tts.speak(toSay, TextToSpeech.QUEUE_FLUSH, null);
            }
        }).start();
    }

    public ArrayList<pathItem> getFoldersPath() {
        return mTreePath;
    }

    public void clearTreePath() {
        mTreePath.clear();
        mTreePath.add(new pathItem(0L, getString(R.string.root)));
    }

/* private void insertPet() {

        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_FOLDER_NAME, "Toto");
        values.put(PetEntry.COLUMN_IMAGE, "");
        values.put(PetEntry.COLUMN_PARENT, mTreePath.get(mTreePath.size() - 1));

        Uri newUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);
    }

    private void deleteAllPets() {
        int rowsDeleted = getContentResolver().delete(PetEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from pet database");
    }*/


    @Override
    public void onResume()
    {
        // After a pause OR at startup
        super.onResume();
        //Refresh your stuff here

        /*clearTreePath();
        ((CatalogActivity)getActivity()).refreshDecks();
        ((CatalogActivity)getActivity()).refreshMemWords();*/

//        ((CatalogActivity)getActivity()).refreshMemWords();

        Bundle repeatArgs = new Bundle();
        Integer repeatLangLearningInteger = getArguments().getInt("language_learning");
        String repeatLangLearning = repeatLangLearningInteger.toString();
        String[] repeatSelectionArgs = new String[]{ "1",  repeatLangLearning};
        repeatArgs.putStringArray("selectionArgs", repeatSelectionArgs);
        getLoaderManager().restartLoader(REPEAT_LOADER, repeatArgs, FoldersFragment.this);

//        clearTreePath();
//        ((CatalogActivity)getActivity()).refreshDecks();

//        Log.e("path", ((CatalogActivity)getActivity()).getFoldersPath().toString());
        updatePathTextView();
//        Log.e("path", ((CatalogActivity)getActivity()).getFoldersPath().toString());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_catalog, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {

            case R.id.action_go_to_settings:
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
//                startActivity(intent);
                startActivityForResult(intent, 3);
                return true;

            // Respond to a click on the "Add" menu option
            case R.id.action_add:
                addOptions();
        }
        return super.onOptionsItemSelected(item);
    }


    private boolean addOptions() {
        if ( mTreePath.size() == 1 || (mFoldersQuantity > 0 &&  mAdapterNumber == 0) ) {
            chooseAddMode(true, false, false);
            return true;
        } else if (mAdapterNumber == 1) {
            chooseAddMode(false, true, true);
            return true;
        } else {
            chooseAddMode(true,true,true);
            return true;
        }
    }


    private void chooseAddMode(boolean folderEnabled, boolean wordsEnabled, boolean excelEnabled) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.add_picker, null));
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        LinearLayout folderOption = alertDialog.findViewById(R.id.folder);
        LinearLayout wordsOption = alertDialog.findViewById(R.id.words);
        LinearLayout excelOption = alertDialog.findViewById(R.id.excel);

        TextView folderTitle = alertDialog.findViewById(R.id.folderTitle);
        TextView wordsTitle = alertDialog.findViewById(R.id.wordsTitle);
        TextView excelTitle = alertDialog.findViewById(R.id.excelTitle);

        ImageView folderImage = alertDialog.findViewById(R.id.folderIcon);
        ImageView wordsImage = alertDialog.findViewById(R.id.wordsIcon);
        ImageView excelImage = alertDialog.findViewById(R.id.excelIcon);

        int disabledItemColor = ContextCompat.getColor(getActivity(), R.color.textColorDisabledItem);

        if (!folderEnabled) {
            folderOption.setEnabled(false);
            folderTitle.setTextColor(disabledItemColor);
            folderImage.setColorFilter(disabledItemColor);
        }
        if (!wordsEnabled) {
            wordsOption.setEnabled(false);
            wordsTitle.setTextColor(disabledItemColor);
            wordsImage.setColorFilter(disabledItemColor);
        }
        if (!excelEnabled) {
            excelOption.setEnabled(false);
            excelTitle.setTextColor(disabledItemColor);
            excelImage.setColorFilter(disabledItemColor);
        }

        folderOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                addFolder();
            }
        });

        wordsOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                addWords();
            }
        });

        excelOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                uploadExcel();
            }
        });

    }

    private void addFolder() {
        Intent intent = new Intent(getActivity(), EditorActivity.class);
//        intent.putExtra("folder_id", mTreePath.get(mTreePath.size() - 1).getId());
        intent.putExtra("folder_id", ((CatalogActivity)getActivity()).getCurrentFolder().getId());
        intent.putExtra("language_learning", getArguments().getInt("language_learning"));
        startActivity(intent);
    }

    private void addWords() {
        Intent intent = new Intent(getActivity(), WordEditorActivity.class);
//        intent.putExtra("folder_id", mTreePath.get(mTreePath.size() - 1).getId());
        intent.putExtra("folder_id", ((CatalogActivity)getActivity()).getCurrentFolder().getId());
        intent.putExtra("language_learning", getArguments().getInt("language_learning"));
        startActivityForResult(intent, 1);
    }

    private void uploadExcel() {
        Intent intent1 = new Intent(getActivity(), FileChooser.class);
//        intent1.putExtra("folder_id", mTreePath.get(mTreePath.size() - 1).getId());
        intent1.putExtra("folder_id", ((CatalogActivity)getActivity()).getCurrentFolder().getId());
        intent1.putExtra("language_learning", getArguments().getInt("language_learning"));
        startActivityForResult(intent1,RESULT_FILE_EXPLORER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case 1:
                ((CatalogActivity)getActivity()).refreshDecks();
//                refreshDecks();
                break;
            case 2:
                break;
            case RESULT_SETTINGS:
                ((CatalogActivity)getActivity()).refreshDecks();
//                refreshDecks();
                break;
            case RESULT_SETTINGS_LANGUAGES:
                ((CatalogActivity)getActivity()).setTabs();
                ((CatalogActivity)getActivity()).updateFolderPageAdapter();
//                refreshDecks();
                break;
            case RESULT_FILE_EXPLORER:
                /*if (resultCode == RESULT_OK) {
                    curFileName = data.getStringExtra("GetFileName");
                    edittext.setText(curFileName);
                }*/
                ((CatalogActivity)getActivity()).refreshDecks();
//                refreshDecks();
                break;
            case RESULT_DELETED_DECK:
                ((CatalogActivity)getActivity()).refreshDecks();

                break;
        }
    }

    public void refreshDecks() {

        Bundle args=new Bundle();

        Integer landId = getArguments().getInt("language_learning");

//        if (mSettings.getLong(SettingsContract.LAST_FOLDER, 0) != 0L) {
        if (((CatalogActivity)getActivity()).getCurrentFolder().getId() != 0L) {
            args.putString("selection", PetEntry.COLUMN_PARENT + " = ? AND " + PetEntry.COLUMN_LEARNING_LANGUAGE + " = ?");

//            Long parentLong = mSettings.getLong(SettingsContract.LAST_FOLDER, 0);
            Long parentLong = ((CatalogActivity)getActivity()).getCurrentFolder().getId();

            String parent = parentLong.toString();

            String[] selectionArgs = {parent, landId.toString()};
            args.putStringArray("selectionArgs", selectionArgs);




//            ((CatalogActivity)getActivity()).getFoldersPath();
//            Log.e("path", ((CatalogActivity)getActivity()).getFoldersPath().toString());

        } else {
            args.putString("selection", PetEntry.COLUMN_PARENT + " is null AND " + PetEntry.COLUMN_LEARNING_LANGUAGE + " = ?");
            String[] selectionArgs = {landId.toString()};
            args.putStringArray("selectionArgs", selectionArgs);

//            Log.e("path", ((CatalogActivity)getActivity()).getFoldersPath().toString());
        }

        getLoaderManager().restartLoader(PET_LOADER, args, this);

    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelableArrayList(PATH_TREE, mTreePath);
        super.onSaveInstanceState(outState);
    }

    private void chooseMode(final long deckId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.mode_picker, null));
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();


        LinearLayout memorize = alertDialog.findViewById(R.id.memorize);
        memorize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();

                Intent intent = new Intent(getActivity(), MemorizeActivity.class);
                intent.putExtra("folder", mTreePath.get(mTreePath.size() - 1).getId());
                intent.putExtra("deck", deckId);
//                startActivity(intent);

                startActivityForResult(intent, RESULT_SETTINGS);


            }
        });

        LinearLayout spelling = alertDialog.findViewById(R.id.spelling);
        spelling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                Intent intent = new Intent(getActivity(), SpellingActivity.class);
                intent.putExtra("folder", mTreePath.get(mTreePath.size() - 1).getId());
                intent.putExtra("deck", deckId);
                startActivity(intent);
            }
        });
    }


    private void chooseRepeatMode() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.mode_picker, null));
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        LinearLayout memorize = alertDialog.findViewById(R.id.memorize);
        LinearLayout spelling = alertDialog.findViewById(R.id.spelling);


        TextView memorizeTitle = alertDialog.findViewById(R.id.memorizeTitle);
        TextView spellingTitle = alertDialog.findViewById(R.id.spellingTitle);

        ImageView memorizeImage = alertDialog.findViewById(R.id.memorizeIcon);
        ImageView spellingImage = alertDialog.findViewById(R.id.spellingIcon);

        int disabledItemColor = ContextCompat.getColor(getActivity(), R.color.textColorDisabledItem);

        if (mMemQuantity == 0) {
//            ((ViewManager)memorize.getParent()).removeView(memorize);
            memorize.setEnabled(false);
            memorizeTitle.setTextColor(disabledItemColor);
            memorizeImage.setColorFilter(disabledItemColor);

        }

        if (mSpellQuantity == 0) {
//            ((ViewManager)spelling.getParent()).removeView(spelling);
            spelling.setEnabled(false);
            spellingTitle.setTextColor(disabledItemColor);
            spellingImage.setColorFilter(disabledItemColor);
//            ((ViewManager)spelling.getParent()).removeView(spelling);
        }

        memorize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                Intent intent = new Intent(getActivity(), MemorizeActivity.class);
                intent.putExtra("lang_learning", getArguments().getInt("language_learning"));
                startActivity(intent);
            }
        });

        spelling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                Intent intent = new Intent(getActivity(), SpellingActivity.class);
                intent.putExtra("lang_learning", getArguments().getInt("language_learning"));
                startActivity(intent);
            }
        });
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        if (i == REPEAT_LOADER) {

            String[] selectionArgs = null;
            if (bundle != null)
                selectionArgs = bundle.getStringArray("selectionArgs");

            return new CursorLoader(getActivity(),
                    WordContract.WordEntry.TO_REP_COUNT_URI,
                    null,
                    null,
                    selectionArgs,
                    null);

        } else {
            String[] projection = {
                    PetEntry._ID,
                    PetEntry.COLUMN_FOLDER_NAME,
                    PetEntry.COLUMN_IMAGE,
                    PetEntry.COLUMN_MARKED
            };

            String selection = null;
            String[] selectionArgs = null;

            if (bundle != null) {
                selection = bundle.getString("selection");
                selectionArgs = bundle.getStringArray("selectionArgs");
            }


            return new CursorLoader(getActivity(),
                    PetEntry.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    PetEntry._ID + " DESC");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (loader.getId() == REPEAT_LOADER) {

            data.moveToFirst();
            int memColumnIndex = data.getColumnIndex(WordContract.WordEntry.COLUMN_REPEAT_MEM);
            int spellColumnIndex = data.getColumnIndex(WordContract.WordEntry.COLUMN_REPEAT_SPELL);

            mMemQuantity = data.getInt(memColumnIndex);
            mSpellQuantity = data.getInt(spellColumnIndex);

            if ( mMemQuantity > 0 || mSpellQuantity > 0 ) {
                fab = rootView.findViewById(R.id.fab);
                fab.show();
            } else {
                fab = rootView.findViewById(R.id.fab);
                fab.hide();
            }

        } else {

            mFoldersQuantity = data.getCount();
            mAdapterNumber = 0;

            if (mFoldersQuantity != 0) {
                data.moveToFirst();
                Integer deckNumber = data.getInt(data.getColumnIndex(PetEntry._ID));
                if (deckNumber == 0) {
                    mAdapterNumber = 1;
                }
                emptyFolderView.setVisibility(View.GONE);

            } else {

                if (((CatalogActivity)getActivity()).getCurrentFolder() != null) {
                    if (((CatalogActivity)getActivity()).getCurrentFolder().getId() != 0L) {
                        TextView emptyTitle = rootView.findViewById(R.id.empty_title_text);
                        emptyTitle.setText(R.string.the_folder_is_empty);

                        TextView emptyDescription = rootView.findViewById(R.id.empty_subtitle_text);
                        emptyDescription.setText(R.string.create_folder_or_upload_words);
                    }
                }

                emptyFolderView.setVisibility(View.VISIBLE);
//                emptyView.setVisibility(View.INVISIBLE);
            }
            mCursorAdapter.swapCursor(data);

//            pathTextView.setText( ((CatalogActivity)getActivity()).getPath() );
//            Log.e("reload", "yes");
            ((CatalogActivity)getActivity()).updatePathTextView();
//            Log.e("reload", "yes");
//            ((CatalogActivity)getActivity()).updateFolderPageAdapter();
//            ((CatalogActivity)getActivity()).updateFolderPageAdapter();
//            Log.e("path", ((CatalogActivity)getActivity()).getFoldersPath().toString());

        }
    }

/*    public String getPath() {
        String path = "";

//        FoldersFragment.this.mTreePath.size();

        for (int c = 0; c < ((CatalogActivity)getActivity()).getFoldersPath().size(); c++) {
//            for (int c = 0; c < ((CatalogActivity)getActivity()).getFoldersPath().size(); c++) {
            path += PATH_SEPARATOR + ((CatalogActivity)getActivity()).getFoldersPath().get(c).getName();
        }
        StringBuilder sb = new StringBuilder(path);
        sb.deleteCharAt(0);
        path = sb.toString();
        return path;
    }*/


    public String getPath() {
        String path = "";
        for (int c = 0; c < FoldersFragment.this.mTreePath.size(); c++) {
            path += PATH_SEPARATOR + FoldersFragment.this.mTreePath.get(c).getName();
        }
        StringBuilder sb = new StringBuilder(path);
        sb.deleteCharAt(0);
        path = sb.toString();
        return path;
    }


    public void updatePathTextView() {
//        pathTextView.setText( ((CatalogActivity)getActivity()).getPath() );
        pathTextView.setText(getPath());
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
        updatePathTextView();
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId()==R.id.list) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;

            View view = info.targetView;
            LinearLayout markedBadge = view.findViewById(R.id.markedBadge);

            String[] menuItems;

            if (markedBadge.getVisibility() == View.VISIBLE) {
                // Its visible
                menuItems = getResources().getStringArray(R.array.menu1);
            } else {
                // Either gone or invisible
                menuItems = getResources().getStringArray(R.array.menu);
            }

//            String[] menuItems = getResources().getStringArray(R.array.menu);
            for (int i = 0; i<menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItemIndex = item.getItemId();

        View view = info.targetView;
        LinearLayout markedBadge = view.findViewById(R.id.markedBadge);

        String[] menuItems = getResources().getStringArray(R.array.menu);
        String menuItemName = menuItems[menuItemIndex];


        ImageView folderImage = view.findViewById(R.id.folderImage);
//        String imageName = getResources().getResourceName(R.id.folderImage);
//        String lastCharacters = getLastCharacters(imageName, 4);
//        if ( !".png".equals(lastCharacters) ) {}
//        if (isDeck(folderImage)) {}



        long infoId = info.id;

        switch (menuItemIndex) {
            case 0:

                if (isFolder(folderImage)) {
                    if (markedBadge.getVisibility() == View.VISIBLE) {
                        // Its visible
                        markFolder(infoId, false);
                    } else {
                        // Either gone or invisible
                        markFolder(infoId, true);
                    }

                } else {

                    if (markedBadge.getVisibility() == View.VISIBLE) {
                        markDeckSwitch(infoId, false);
                        markedBadge.setVisibility(View.INVISIBLE);

                    } else {
                        markDeckSwitch(infoId, true);
                        markedBadge.setVisibility(View.VISIBLE);

                    }

                }
                return true;
            case 1:

                if (isFolder(folderImage)) {
                    Class activityClass = EditorActivity.class;
                    Intent intent = new Intent(getActivity(), activityClass);
                    Uri currentPetUri = ContentUris.withAppendedId(PetEntry.CONTENT_URI, infoId);
                    intent.setData(currentPetUri);
                    startActivity(intent);
                } else {
                    Class activityClass = EditorDeckActivity.class; // Need to be changed
                    Intent intent = new Intent(getActivity(), activityClass);
                    intent.putExtra("folder", ((CatalogActivity)getActivity()).getCurrentFolder().getId() );
                    intent.putExtra("deck", infoId);
                    startActivityForResult(intent, RESULT_SETTINGS);
//                    Log.e("folder id is", getCurrentFolder().getId() + "");
                }

                return true;
            case 2:
                if (isFolder(folderImage)) {
                    onDeletePressed(infoId);
                } else {
                    onDeleteWordsPressed(infoId);
                }

//                Toast.makeText(getActivity(), String.format("Selected %s for item %s", menuItemName, infoId),
//                        Toast.LENGTH_SHORT).show();
                return true;

        }
        return true;
    }

    private void onDeletePressed(final Long folder) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.delete_folder_title);
        builder.setMessage(R.string.delete_folder_msg);
        builder.setPositiveButton(R.string.ok_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteFolder(folder);
            }
        });
        builder.setNegativeButton(R.string.cancel_deleting, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private void onDeleteWordsPressed(final Long deck) {

        final pathItem folder = new pathItem(
                    ((CatalogActivity)getActivity()).getCurrentFolder().getId(),
                    ((CatalogActivity)getActivity()).getCurrentFolder().getName()
                );

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.delete_deck_title);
        builder.setMessage(R.string.delete_deck_msg);
        builder.setPositiveButton(R.string.ok_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteDeck(folder, deck);
            }
        });
        builder.setNegativeButton(R.string.cancel_deleting, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteFolder(Long folder) {
        String [] arguments = new String[1];
        arguments[0] = folder.toString();
        String selectionClause = PetEntry._ID + " = ?";
        Uri currentPetUri = ContentUris.withAppendedId(PetEntry.CONTENT_URI, folder);
//        int rowsDeleted = getActivity().getContentResolver().delete(PetEntry.CONTENT_URI, selectionClause, arguments);
//        Log.e("CatalogActivity", rowsDeleted + " rows deleted from pet database");
        getActivity().getContentResolver().delete(currentPetUri, selectionClause, arguments);

//        refreshMemWords();
        ((CatalogActivity)getActivity()).refreshMemWords();
    }


    private void deleteDeck(pathItem folder, Long deck) {
        String [] arguments = new String[2];
        arguments[0] = folder.toString();
        arguments[1] = deck.toString();
        String selectionClause = PetEntry._ID + " = ?";
        getActivity().getContentResolver().delete(WordContract.WordEntry.CONTENT_URI, selectionClause, arguments);

        ((CatalogActivity)getActivity()).refreshDecks();
        ((CatalogActivity)getActivity()).refreshMemWords();

        mCursorAdapter.notifyDataSetChanged();
    }


    public void refreshMemWords() {
        Bundle repeatArgs = new Bundle();
        Integer repeatLangLearningInteger = getArguments().getInt("language_learning");
        String repeatLangLearning = repeatLangLearningInteger.toString();

//        String repeatLangLearning = lang.toString();
//        Log.e("learning lang is", repeatLangLearning);
        String[] repeatSelectionArgs = new String[]{ "1",  repeatLangLearning};
        repeatArgs.putStringArray("selectionArgs", repeatSelectionArgs);
        getLoaderManager().restartLoader(REPEAT_LOADER, repeatArgs, FoldersFragment.this);


//        pathTextView.setText(getPath());

//        DetailOnPageChangeListener
//        ((CatalogActivity)getActivity()).re
//        Integer opa = ((CatalogActivity) getActivity()).getCurrentFragment();
//        opa.
//        Log.e("opopopopopopop0", ""+getArguments().getInt("jopa"));

    }



    @Override
    public void onPause() {
        super.onPause();
//        clearTreePath();
//        ((CatalogActivity)getActivity()).refreshDecks();
        updatePathTextView();
    }



    public pathItem getCurrentFolder() {
        return mTreePath.get(mTreePath.size() - 1);
    }


    private void markFolder(Long infoId, boolean markValue) {
        Uri currentPetUri = ContentUris.withAppendedId(PetEntry.CONTENT_URI, infoId);
        ContentValues values = new ContentValues();
        int value = 0;
        if (markValue)
            value = 1;
        values.put(PetEntry.COLUMN_MARKED, value);
//        int rowsAffected =
                getActivity().getContentResolver().update(currentPetUri, values, null, null);
    }


    private void markDeckSwitch(Long infoId, boolean isToCreate) {

        if (isToCreate) {

//            Log.e(PATH_TREE + " onMark ", mTreePath.toString());
            ContentValues values = new ContentValues();
            values.put(DeckContract.DeckEntry.COLUMN_DECK, infoId);
//            values.put(DeckContract.DeckEntry.COLUMN_FOLDER, mSettings.getLong(SettingsContract.LAST_FOLDER, 0));

            values.put(DeckContract.DeckEntry.COLUMN_FOLDER, ((CatalogActivity)getActivity()).getCurrentFolder().getId());

            getActivity().getContentResolver().insert(DeckContract.DeckEntry.CONTENT_URI, values);
//            Log.e(PATH_TREE + " onMark ", mTreePath.toString());
        } else {

            String selection = DeckContract.DeckEntry.COLUMN_FOLDER + " = ? AND " + DeckContract.DeckEntry.COLUMN_DECK + " = ?";
            String[] selectionArgs = new String[2];
//            Long folderLong = mSettings.getLong(SettingsContract.LAST_FOLDER, 0);
            Long folderLong = ((CatalogActivity)getActivity()).getCurrentFolder().getId();
            selectionArgs[0] = folderLong.toString();
            selectionArgs[1] = infoId.toString();

//                    int rowsDeleted =
            getActivity().getContentResolver().delete(DeckContract.DeckEntry.CONTENT_URI, selection, selectionArgs);

        }

        ((CatalogActivity)getActivity()).refreshDecks();

//        refreshDecks();
//        Bundle args=new Bundle();
//        args.putString("selection", PetEntry.COLUMN_PARENT + " = ?");
//        Long idLong = mSettings.getLong(SettingsContract.LAST_FOLDER, 0);
//        String idString = idLong.toString();
//        String[] selectionArgs = {idString};
//        args.putStringArray("selectionArgs", selectionArgs);
//        getLoaderManager().restartLoader(PET_LOADER, args, FoldersFragment.this);

    }


    @Override
    public void onBackPressed() {
        if (FoldersFragment.this.mTreePath.size() > 1) {


            mTreePath.remove(mTreePath.size() - 1);

//            SharedPreferences.Editor editor = mSettings.edit();
//            editor.putLong(SettingsContract.LAST_FOLDER, mTreePath.get(mTreePath.size() - 1).getId());
//            editor.apply();

            ((CatalogActivity)getActivity()).refreshDecks();
//            refreshDecks();
//            Log.e("opa", mTreePath.toString());

            return;
        }
        else {
            this.getActivity().finish();
            return;
        }
    }

//    @Override
//    public void refreshDecksDogNail() {
//
//    }


    private boolean isFolder(ImageView v) {
        if (v.getDrawable().getConstantState() == getResources().getDrawable( R.drawable.ic_deck).getConstantState())
            return false;
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        clearTreePath();

    }

}