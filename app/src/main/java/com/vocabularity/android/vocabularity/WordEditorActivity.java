package com.vocabularity.android.vocabularity;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.vocabularity.android.vocabularity.data.WordContract;
import com.vocabularity.android.vocabularity.data.WordContract.WordEntry;

import java.io.File;

public class WordEditorActivity extends AppCompatActivity  implements
        LoaderManager.LoaderCallbacks<Cursor>{

    private EditText mWordEditText;
    private EditText mTranslationEditText;
//    private boolean mWordHasChanged = false;
    private Uri mCurrentPetUri;

    private Button finishButton;
    private Button saveButton;


    /** Identifier for the pet data loader */
    private static final int EXISTING_PET_LOADER = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_editor);

        mWordEditText = findViewById(R.id.edit_word);
        mTranslationEditText = findViewById(R.id.edit_translation);

        Intent intent = getIntent();
        mCurrentPetUri = intent.getData();

        setTitle(getString(R.string.editor_activity_title_new_word));

/*//        if (mCurrentPetUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_word));
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
//            invalidateOptionsMenu();
        *//*} else {
            setTitle(getString(R.string.editor_activity_title_edit_pet));
            getLoaderManager().initLoader(EXISTING_PET_LOADER, null, this);
        }*/

        finishButton = findViewById(R.id.cancel_button);
        saveButton = findViewById(R.id.ok_button);

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveWord();
                setResult(1);
            }
        });






        /*mWordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                final String wordString = mWordEditText.getText().toString().trim();
                final String translationString = mTranslationEditText.getText().toString().trim();

                if ("".equals(wordString) || "".equals(translationString)
                        || TextUtils.isEmpty(wordString) || TextUtils.isEmpty(translationString)) {
                    mWordEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                    mTranslationEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);

                    setSwitchableFocusForEditText(mWordEditText);
                    setSwitchableFocusForEditText(mTranslationEditText);
                } else {
                    mWordEditText.setImeOptions(EditorInfo.IME_ACTION_GO);
                    mTranslationEditText.setImeOptions(EditorInfo.IME_ACTION_GO);

                    saveWordOnEnter(mWordEditText);
                    saveWordOnEnter(mTranslationEditText);

//                    setSwitchableFocusForEditText(mWordEditText);
//                    setSwitchableFocusForEditText(mTranslationEditText);
                }
            }
        });

        mTranslationEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {

                final String wordString = mWordEditText.getText().toString().trim();
                final String translationString = mTranslationEditText.getText().toString().trim();

                if ("".equals(wordString) || "".equals(translationString)
                        || TextUtils.isEmpty(wordString) || TextUtils.isEmpty(translationString)) {
                    mWordEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                    mTranslationEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);

                    setSwitchableFocusForEditText(mWordEditText);
                    setSwitchableFocusForEditText(mTranslationEditText);
                } else {
                    mWordEditText.setImeOptions(EditorInfo.IME_ACTION_GO);
                    mTranslationEditText.setImeOptions(EditorInfo.IME_ACTION_GO);

                    saveWordOnEnter(mWordEditText);
                    saveWordOnEnter(mTranslationEditText);

//                    setSwitchableFocusForEditText(mWordEditText);
//                    setSwitchableFocusForEditText(mTranslationEditText);


                }
            }
        });*/



        mWordEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {

                    if ("".equals(mWordEditText.getText().toString().trim()) || "".equals(mTranslationEditText.getText().toString().trim())
                            || TextUtils.isEmpty(mWordEditText.getText().toString().trim()) || TextUtils.isEmpty(mTranslationEditText.getText().toString().trim())) {
                        mWordEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        mTranslationEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);

                        setSwitchableFocusForEditText(mWordEditText);
                        setSwitchableFocusForEditText(mTranslationEditText);
                    } else {
                        mWordEditText.setImeOptions(EditorInfo.IME_ACTION_GO);
                        mTranslationEditText.setImeOptions(EditorInfo.IME_ACTION_GO);

                        saveWordOnEnter(mWordEditText);
                        saveWordOnEnter(mTranslationEditText);

//                    setSwitchableFocusForEditText(mWordEditText);
//                    setSwitchableFocusForEditText(mTranslationEditText);
                    }
                    return true;

                }
                return false;
            }
        });

        mTranslationEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {

                    if ("".equals(mWordEditText.getText().toString().trim()) || "".equals(mTranslationEditText.getText().toString().trim())
                            || TextUtils.isEmpty(mWordEditText.getText().toString().trim()) || TextUtils.isEmpty(mTranslationEditText.getText().toString().trim())) {
                        mWordEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        mTranslationEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);

                        setSwitchableFocusForEditText(mWordEditText);
                        setSwitchableFocusForEditText(mTranslationEditText);
                    } else {
                        mWordEditText.setImeOptions(EditorInfo.IME_ACTION_GO);
                        mTranslationEditText.setImeOptions(EditorInfo.IME_ACTION_GO);

                        saveWordOnEnter(mWordEditText);
                        saveWordOnEnter(mTranslationEditText);

//                    setSwitchableFocusForEditText(mWordEditText);
//                    setSwitchableFocusForEditText(mTranslationEditText);

                    }
                    return true;
                }
                return false;
            }
        });



//        if (mWordEditText.getText().toString().trim() == "") {
//            mWordEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
//            mTranslationEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
//
//            goToEditText(mTranslationEditText);
//        }
//
//        if (mTranslationEditText.getText().toString().trim() == "") {
//            mWordEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
//            mTranslationEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
//
//            goToEditText(mWordEditText);
//        }

//        mWordEditText
//        mTranslationEditText;

    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            /*// Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save pet to database
                saveWord();
                // Exit activity
                setResult(1);
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
//                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar*/
            case android.R.id.home:

                finish();

                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                /*if (!mWordHasChanged) {
                    NavUtils.navigateUpFromSameTask(WordEditorActivity.this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(WordEditorActivity.this);
                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);*/
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void saveWordOnEnter(EditText editText) {
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_ENTER ){
                    saveWord();
                    return true;
                }
                return false;
            }
        });
    }

    public void setSwitchableFocusForEditText(final EditText editText) {
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_ENTER ){
                    if (mWordEditText.hasFocus())
                        mTranslationEditText.requestFocus();
                    if (mTranslationEditText.hasFocus())
                        mWordEditText.requestFocus();
//                    editText.requestFocus();
                    return true;
                }
                return false;
            }
        });
    }


    /**
     * Get user input from editor and save pet into database.
     */
    private void saveWord() {

        String wordString = mWordEditText.getText().toString().trim();
        String translationString = mTranslationEditText.getText().toString().trim();
        Long folderIdLong = getIntent().getLongExtra("folder_id", 1L);
        int languageLearningId = getIntent().getIntExtra("language_learning", 1);
//        Long folderIdLong = 1L;

        if ("".equals(wordString) || "".equals(translationString)
                || TextUtils.isEmpty(wordString) || TextUtils.isEmpty(translationString)) {
//            Log.e("empty", "fields");
            return;
        }

        /*if (mCurrentPetUri == null &&
                TextUtils.isEmpty(wordString)) {
            // Since no fields were modified, we can return early without creating a new pet.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }*/


        ContentValues values = new ContentValues();
        values.put(WordEntry.COLUMN_WORD, wordString);
        values.put(WordEntry.COLUMN_TRANSLATION, translationString);
        values.put(WordEntry.COLUMN_FOLDER, folderIdLong);
        values.put(WordEntry.COLUMN_LANGUAGE_LEARNING, languageLearningId);




        // Determine if this is a new or existing pet by checking if mCurrentPetUri is null or not
//        if (mCurrentPetUri == null) {
            // This is a NEW pet, so insert a new pet into the provider,
            // returning the content URI for the new pet.
            Uri newUri = getContentResolver().insert(WordEntry.CONTENT_URI, values);
            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.card_saved),
                        Toast.LENGTH_SHORT).show();

                mWordEditText.setText("");
                mTranslationEditText.setText("");

            }
        /*} else {
            // Otherwise this is an EXISTING pet, so update the pet with content URI: mCurrentPetUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentPetUri will already identify the correct row in the database that
            // we want to modify.

            int rowsAffected = getContentResolver().update(mCurrentPetUri, values, null, null);
            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }*/
    }


    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
