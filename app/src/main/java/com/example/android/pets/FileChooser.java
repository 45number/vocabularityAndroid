package com.example.android.pets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.text.DateFormat;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ListActivity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


import com.example.android.pets.data.WordContract.WordEntry;


import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class FileChooser extends AppCompatActivity //ListActivity
//        implements LoaderManager.LoaderCallbacks<Cursor>
{

    private File currentDir;
    private FileArrayAdapter adapter;

    ArrayList<XYValue> uploadData;

    ListView listView;

    View progressBackground;
    CardView progressCard;

//    List<FilesItem>dir;
//    List<FilesItem>fls;

//    FilesItem o;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_file_explorer);
        listView = findViewById(R.id.list);

        progressBackground = findViewById(R.id.progressBackground);
        progressCard = findViewById(R.id.progressCard);
        progressBackground.setVisibility(View.GONE);
        progressCard.setVisibility(View.GONE);


//        dir = new ArrayList<>();
//        fls = new ArrayList<>();

        currentDir = new File("/sdcard/");
        fill(currentDir);
        uploadData = new ArrayList<>();


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                FilesItem o = adapter.getItem(i);

                if(o.getImage().equalsIgnoreCase("ic_folder")||o.getImage().equalsIgnoreCase("directory_up")){
                    currentDir = new File(o.getPath());
                    fill(currentDir);
                } else {
                    onFileClick(o);
                }
            }
        });
    }

    private void fill(File f) {
        File[]dirs = f.listFiles();
//        "Current Dir: "+
        this.setTitle(f.getName());
        List<FilesItem> dir = new ArrayList<>();
        List<FilesItem> fls = new ArrayList<>();
        try{
            for(File ff: dirs)
            {
                Date lastModDate = new Date(ff.lastModified());
                DateFormat formater = DateFormat.getDateTimeInstance();
                String date_modify = formater.format(lastModDate);
                if(ff.isDirectory()){

                    // create new filename filter
                    FilenameFilter fileNameFilter = new FilenameFilter() {

                        @Override
                        public boolean accept(File dir, String name) {

                            if (new File(dir, name).isDirectory())
                                return true;

                            if(name.lastIndexOf('.')>0) {

                                // get last index for '.' char
                                int lastIndex = name.lastIndexOf('.');

                                // get extension
                                String str = name.substring(lastIndex);

                                // match path name extension
                                if(str.equals(".xlsx")) {
                                    return true;
                                }
                            }

                            return false;
                        }
                    };

                    File[] fbuf = ff.listFiles(fileNameFilter);

                    int buf = 0;
                    if(fbuf != null){
                        buf = fbuf.length;
                    }
                    else buf = 0;
                    String num_item = String.valueOf(buf);
                    if(buf == 0) num_item = num_item + " item";
                    else num_item = num_item + " items";

                    //String formated = lastModDate.toString();
//                    dir.add(new FilesItem(ff.getName(),num_item,date_modify,ff.getAbsolutePath(),"directory_icon"));
                    dir.add(new FilesItem(ff.getName(),num_item,date_modify,ff.getAbsolutePath(),"ic_folder"));
                } else {
                    int dotPosition= ff.getName().lastIndexOf(".");
                    String ext = ff.getName().substring(dotPosition + 1, ff.getName().length());

                    if (ext.equals("xlsx")) {
                        fls.add(new FilesItem(ff.getName(),ff.length() + " Byte", date_modify, ff.getAbsolutePath(),"ic_excel_file_explorer"));
                    }
//                    fls.add(new Item(ff.getName(),ff.length() + " Byte", date_modify, ff.getAbsolutePath(),"file_icon"));
                }
            }
        }catch(Exception e) {
            Log.e("exception", "Here I am");
        }
        Collections.sort(dir);
        Collections.sort(fls);
        dir.addAll(fls);
        if(!f.getName().equalsIgnoreCase("sdcard"))
            dir.add(0,new FilesItem("..","Parent Directory","",f.getParent(),"ic_folder"));
//            dir.add(0,new FilesItem("..","Parent Directory","",f.getParent(),"directory_up"));
//        adapter = new FileArrayAdapter(FileChooser.this, R.layout.list_item_files, dir);
        adapter = new FileArrayAdapter(this, R.layout.list_item_files, dir);
//        this.setListAdapter(adapter);

//        Log.e("oo", listView.toString());
        listView.setAdapter(adapter);
    }



    /*@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);
        FilesItem o = adapter.getItem(position);

        if(o.getImage().equalsIgnoreCase("ic_folder")||o.getImage().equalsIgnoreCase("directory_up")){
            currentDir = new File(o.getPath());
            fill(currentDir);
        } else {
            onFileClick(o);
        }
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
//                this.onBackPressed();
//                break;
            }
        }
        return true;
    }


    @Override
    public void onBackPressed() {
        if (!currentDir.getName().equalsIgnoreCase("sdcard")) {
            fill(currentDir.getParentFile());
            currentDir = currentDir.getParentFile();
        } else {
            super.onBackPressed();
        }
    }

    private void onFileClick(final FilesItem o)
    {

        progressBackground.setVisibility(View.VISIBLE);
        progressCard.setVisibility(View.VISIBLE);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                //TODO your background code
                readExcelData(currentDir.toString()+"/"+o.getName());
                setResult(5);
                finish();
            }
        });

    }

    private void readExcelData(String filePath) {

        File inputFile = new File(filePath);

        Log.e("hello", "hello");

        try {
            InputStream inputStream = new FileInputStream(inputFile);
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheetAt(0);
            int rowsCount = sheet.getPhysicalNumberOfRows();

            Log.e("getPhysicalNumberOfRows", rowsCount+"");

//            if (rowsCount == 0)
//                Log.e("rows", "zero");
////                return;

            //outter loop, loops through rows
            int r = 0;
            int r1 = 0;
            while (r < rowsCount) {

                Row row = sheet.getRow(r1);

//                if (r1 == 0 && row == null)
//                    Log.e("r1", "zero");

                if (row == null) {
                    r1++;
                    continue;
                }

                Cell wordCell = row.getCell(0);
                Cell translateCell = row.getCell(1);

                

                if (wordCell == null || wordCell.getCellType() == Cell.CELL_TYPE_BLANK
                        || translateCell == null || translateCell.getCellType() == Cell.CELL_TYPE_BLANK) {
                    r1++;
                    continue;
                }

                saveWord(wordCell.toString(), translateCell.toString());

                r1++;
                r++;
            }

        }catch (FileNotFoundException e) {
            Log.e("FileChooser", "readExcelData: FileNotFoundException. " + e.getMessage() );
        } catch (IOException e) {
            Log.e("FileChooser", "readExcelData: Error reading inputstream. " + e.getMessage() );
        }

    }







    private void saveWord(String wordString, String translationString) {

        Long folderIdLong = getIntent().getLongExtra("folder_id", 1L);
        int languageLearningId = getIntent().getIntExtra("language_learning", 1);

        ContentValues values = new ContentValues();
        values.put(WordEntry.COLUMN_WORD, wordString);
        values.put(WordEntry.COLUMN_TRANSLATION, translationString);
        values.put(WordEntry.COLUMN_FOLDER, folderIdLong);
        values.put(WordEntry.COLUMN_LANGUAGE_LEARNING, languageLearningId);

        Uri newUri = getContentResolver().insert(WordEntry.CONTENT_URI, values);

    }
}