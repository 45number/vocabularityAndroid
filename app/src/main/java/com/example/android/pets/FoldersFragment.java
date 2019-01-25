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



public class FoldersFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, OnBackPressed {

    private static final int PET_LOADER = 0;

    private static final int REPEAT_LOADER = 1;



    PetCursorAdapter mCursorAdapter;
    private int mAdapterNumber;
    ListView petListView;


    SharedPreferences mSettings;

    View rootView;
    FloatingActionButton fab;
//    LinearLayout markedBadge;


//    FloatingActionButton rootFab;


    TextToSpeech tts;
    private int s;

    private int mMemQuantity = 0;
    private int mSpellQuantity = 0;

    private static final int RESULT_SETTINGS = 3;
    private static final int RESULT_SETTINGS_LANGUAGES = 4;
    private static final int RESULT_FILE_EXPLORER = 5;

    private static final String PATH_TREE = "path";


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
            mTreePath.add(new pathItem(0L));
        } else {
            mTreePath = savedInstanceState.getParcelableArrayList(PATH_TREE);
        }


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
        View emptyView = rootView.findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyView);

        mCursorAdapter = new PetCursorAdapter(getActivity(), null);
        petListView.setAdapter(mCursorAdapter);


        registerForContextMenu(petListView);

        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                if (mAdapterNumber == 0) {
                    mTreePath.add(new pathItem(id));

                    Bundle args=new Bundle();
                    args.putString("selection", PetEntry.COLUMN_PARENT + " = ?");
                    Long idLong = id;
                    String idString = idLong.toString();
                    String[] selectionArgs = {idString};
                    args.putStringArray("selectionArgs", selectionArgs);
                    getLoaderManager().restartLoader(PET_LOADER, args, FoldersFragment.this);

                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putLong(SettingsContract.LAST_FOLDER, getCurrentFolder().getId());
                    editor.apply();

                    Log.e(PATH_TREE + " onClick ", mTreePath.toString());

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
//        String idString = "";
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


        return rootView;
    }

    private void ConvertTextToSpeech(final String toSay) {
        new Thread(new Runnable() {
            public void run() {
                tts.speak(toSay, TextToSpeech.QUEUE_FLUSH, null);
            }
        }).start();
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

        Bundle repeatArgs = new Bundle();
        Integer repeatLangLearningInteger = getArguments().getInt("language_learning");
        String repeatLangLearning = repeatLangLearningInteger.toString();
        String[] repeatSelectionArgs = new String[]{ "1",  repeatLangLearning};
        repeatArgs.putStringArray("selectionArgs", repeatSelectionArgs);
        getLoaderManager().restartLoader(REPEAT_LOADER, repeatArgs, FoldersFragment.this);
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
                if ( mTreePath.size() == 1 || (mFoldersQuantity > 0 &&  mAdapterNumber == 0) ) {
//                    DialogSelection ds = new DialogSelection();
//                    Bundle args=new Bundle();
//                    String[] selectionArgs = {"Hello", "opa"};
//                    args.putStringArray("selectionArgs", selectionArgs);
//                    Dialog alertDialog = ds.onCreateDialog(null);
//                    alertDialog.show();
//                    addFolder();
                    chooseAddMode(true, false, false);
//                    pickAddingOption();
                    return true;
                } else if (mAdapterNumber == 1) {
//                    addWords();
                    chooseAddMode(false, true, true);
//                    pickAddingOption();
                    return true;
                } else {
//                    pickAddingOption();
                    chooseAddMode(true,true,true);
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    private void pickAddingOption() {
        final String[] mCatsName ={"Add folder", "Add words", "Upload from excel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(mCatsName, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    // Respond to a click on the "Insert dummy data" menu option
                    case 0:
                        addFolder();
                        break;
                    case 1:
                        addWords();
                        break;
                    case 2:
                        
                        break;
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    } */











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
        intent.putExtra("folder_id", mTreePath.get(mTreePath.size() - 1));
        intent.putExtra("language_learning", getArguments().getInt("language_learning"));
        startActivity(intent);
    }

    private void addWords() {
        Intent intent = new Intent(getActivity(), WordEditorActivity.class);
        intent.putExtra("folder_id", mTreePath.get(mTreePath.size() - 1));
        intent.putExtra("language_learning", getArguments().getInt("language_learning"));
        startActivityForResult(intent, 1);
    }

    private void uploadExcel() {
        Intent intent1 = new Intent(getActivity(), FileChooser.class);
        intent1.putExtra("folder_id", mTreePath.get(mTreePath.size() - 1));
        intent1.putExtra("language_learning", getArguments().getInt("language_learning"));
        startActivityForResult(intent1,RESULT_FILE_EXPLORER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case 1:
                refreshDecks();
                break;
            case 2:
                break;
            case RESULT_SETTINGS:
                refreshDecks();
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
                refreshDecks();
                break;
        }
    }

    private void refreshDecks() {

        Bundle args=new Bundle();

        Integer landId = getArguments().getInt("language_learning");

//        pathItem currentFolder = getCurrentFolder();


        if (mSettings.getLong(SettingsContract.LAST_FOLDER, 0) != 0L) {
            args.putString("selection", PetEntry.COLUMN_PARENT + " = ? AND " + PetEntry.COLUMN_LEARNING_LANGUAGE + " = ?");

            Long parentLong = mSettings.getLong(SettingsContract.LAST_FOLDER, 0);
            String parent = parentLong.toString();
//                    mTreePath.get(mTreePath.size() - 1).toString();

            String[] selectionArgs = {parent, landId.toString()};
            args.putStringArray("selectionArgs", selectionArgs);

            Log.e("0000000", "0000000000");
            Log.e("0000000", parent);
        } else {
            args.putString("selection", PetEntry.COLUMN_PARENT + " is null AND " + PetEntry.COLUMN_LEARNING_LANGUAGE + " = ?");
            String[] selectionArgs = {landId.toString()};
            args.putStringArray("selectionArgs", selectionArgs);

            Log.e("1111111111", "111111111");
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
                intent.putExtra("folder", mTreePath.get(mTreePath.size() - 1));
                intent.putExtra("deck", deckId);
                startActivity(intent);


            }
        });

        LinearLayout spelling = alertDialog.findViewById(R.id.spelling);
        spelling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                Intent intent = new Intent(getActivity(), SpellingActivity.class);
                intent.putExtra("folder", mTreePath.get(mTreePath.size() - 1));
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

//            Log.e("uuuuu", selectionArgs.toString());
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
                    null);
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
//                Log.e("jk", "Jooooooooooooooooooopaaaaaaaaaaaaaaaaaaaa");
            } else {
//                Log.e("jk", "Sraka");
//                fab.setVisibility(View.GONE);
//                rootFab = ((CatalogActivity)getActivity()).getFab();

                fab = rootView.findViewById(R.id.fab);
                fab.hide();
//                Log.e("jk", "Srachnaya");

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
            }
            mCursorAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
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
//                fab.setBackgroundColor(Color.RED);
//                Fragment f = getActivity().getFragmentManager().findFragmentById(R.id.fragment_container);
//                fab = rootView.findViewById(R.id.fab);
//                fab.hide();

//                if ( !".png".equals(lastCharacters) ) {
                if (isFolder(folderImage)) {
//                    Toast.makeText(getActivity(), String.format("Selected %s for item %s", menuItemName, infoId),
//                            Toast.LENGTH_SHORT).show();
//                    Log.e("opa", "0");
                    if (markedBadge.getVisibility() == View.VISIBLE) {
                        // Its visible
                        markFolder(infoId, false);
//                        Log.e("opa", "0 1");
                    } else {
                        // Either gone or invisible
                        markFolder(infoId, true);
//                        Log.e("opa", "0 2");
                    }

                } else {

//                    View view1 = info.targetView;
//                    LinearLayout markedBadge1 = view1.findViewById(R.id.markedBadge);
//                    Log.e("opa", "1");

                    if (markedBadge.getVisibility() == View.VISIBLE) {
                        markDeckSwitch(infoId, false);
//                        Log.e("opa", "1 1 " + getCurrentFolder());
                        markedBadge.setVisibility(View.INVISIBLE);

                    } else {
                        markDeckSwitch(infoId, true);
//                        Log.e("opa", "1 2 " + getCurrentFolder());
                        markedBadge.setVisibility(View.VISIBLE);

                    }

//                    Log.e("Da da da", mTreePath.toString());
//                    refreshDecks();
//                    Toast.makeText(getActivity(), String.format("Selected %s for item %s", menuItemName, infoId),
//                            Toast.LENGTH_SHORT).show();

                }
                return true;
            case 1:
                Class activityClass = EditorActivity.class;
                if (mAdapterNumber == 1)
                    activityClass = EditorActivity.class; // Need to be changed
                Intent intent = new Intent(getActivity(), activityClass);
                Uri currentPetUri = ContentUris.withAppendedId(PetEntry.CONTENT_URI, infoId);
                intent.setData(currentPetUri);
                startActivity(intent);
                return true;
            case 2:
                if (isFolder(folderImage)) {
//                if (mAdapterNumber == 0) {
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

        final pathItem folder = new pathItem(mSettings.getLong(SettingsContract.LAST_FOLDER, 0));
//                getCurrentFolder();

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

        refreshMemWords();
    }


    private void deleteDeck(pathItem folder, Long deck) {
        String [] arguments = new String[2];
        arguments[0] = folder.toString();
        arguments[1] = deck.toString();
//        String selectionClause = PetEntry._ID + " = ?";
        String selectionClause = PetEntry._ID + " = ?";
//        Uri currentPetUri = ContentUris.withAppendedId(PetEntry.CONTENT_URI, folder);
        getActivity().getContentResolver().delete(WordContract.WordEntry.CONTENT_URI, selectionClause, arguments);

//        petListView.remove
//        mCursorAdapter.remove
//        petListView.removeViewsInLayout(1, 2);
//        petListView.removeView(petListView.getChildAt(2));

        refreshMemWords();
        refreshDecks();

        mCursorAdapter.notifyDataSetChanged();
    }


    private void refreshMemWords() {
        Bundle repeatArgs = new Bundle();
        Integer repeatLangLearningInteger = getArguments().getInt("language_learning");
        String repeatLangLearning = repeatLangLearningInteger.toString();

//        String repeatLangLearning = lang.toString();
//        Log.e("learning lang is", repeatLangLearning);
        String[] repeatSelectionArgs = new String[]{ "1",  repeatLangLearning};
        repeatArgs.putStringArray("selectionArgs", repeatSelectionArgs);
        getLoaderManager().restartLoader(REPEAT_LOADER, repeatArgs, FoldersFragment.this);

//        DetailOnPageChangeListener
//        ((CatalogActivity)getActivity()).re
//        Integer opa = ((CatalogActivity) getActivity()).getCurrentFragment();
//        opa.
//        Log.e("opopopopopopop0", ""+getArguments().getInt("jopa"));

    }




    private pathItem getCurrentFolder() {
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

//        Uri currentPetUri = ContentUris.withAppendedId(DeckContract.DeckEntry.CONTENT_URI, infoId);

        if (isToCreate) {
//            Log.e("Mark", "Mark");
            //        Uri newUri =
            Log.e(PATH_TREE + " onMark ", mTreePath.toString());
            ContentValues values = new ContentValues();
            values.put(DeckContract.DeckEntry.COLUMN_DECK, infoId);
            values.put(DeckContract.DeckEntry.COLUMN_FOLDER, mSettings.getLong(SettingsContract.LAST_FOLDER, 0));

//            getCurrentFolder().toString()

            getActivity().getContentResolver().insert(DeckContract.DeckEntry.CONTENT_URI, values);
            Log.e(PATH_TREE + " onMark ", mTreePath.toString());
        } else {
//            Log.e("UnMark", "UnMark");

            String selection = DeckContract.DeckEntry.COLUMN_FOLDER + " = ? AND " + DeckContract.DeckEntry.COLUMN_DECK + " = ?";
            String[] selectionArgs = new String[2];
            Long folderLong = mSettings.getLong(SettingsContract.LAST_FOLDER, 0);
            selectionArgs[0] = folderLong.toString();
            selectionArgs[1] = infoId.toString();

//                    int rowsDeleted =
            getActivity().getContentResolver().delete(DeckContract.DeckEntry.CONTENT_URI, selection, selectionArgs);

        }

        refreshDecks();

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

            SharedPreferences.Editor editor = mSettings.edit();
            editor.putLong(SettingsContract.LAST_FOLDER, mTreePath.get(mTreePath.size() - 1).getId());
            editor.apply();

            refreshDecks();
            Log.e("opa", mTreePath.toString());

            return;
        }
        else {
            this.getActivity().finish();
            return;
        }
    }


    private boolean isFolder(ImageView v) {
        if (v.getDrawable().getConstantState() == getResources().getDrawable( R.drawable.ic_deck).getConstantState())
            return false;
        return true;
    }

//    private boolean isDeckMarked(ImageView v) {
//        if (v.getDrawable().getConstantState() == getResources().getDrawable( R.drawable.ic_deck).getConstantState())
//            return false;
//        return true;
//    }


}