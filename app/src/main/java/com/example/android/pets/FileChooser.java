package com.example.android.pets;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.text.DateFormat;
import android.os.Bundle;
import android.app.ListActivity;
import android.content.Intent;
import android.view.View;
import android.widget.ListView;

public class FileChooser extends ListActivity {

    private File currentDir;
    private FileArrayAdapter adapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentDir = new File("/sdcard/");
        fill(currentDir);
    }
    private void fill(File f)
    {
        File[]dirs = f.listFiles();
        this.setTitle("Current Dir: "+f.getName());
        List<FilesItem>dir = new ArrayList<FilesItem>();
        List<FilesItem>fls = new ArrayList<FilesItem>();
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
                    dir.add(new FilesItem(ff.getName(),num_item,date_modify,ff.getAbsolutePath(),"directory_icon"));
                }
                else
                {
                    int dotPosition= ff.getName().lastIndexOf(".");
                    String ext = ff.getName().substring(dotPosition + 1, ff.getName().length());

                    if (ext.equals("xlsx")) {
                        fls.add(new FilesItem(ff.getName(),ff.length() + " Byte", date_modify, ff.getAbsolutePath(),"file_icon"));
                    }
//                    fls.add(new Item(ff.getName(),ff.length() + " Byte", date_modify, ff.getAbsolutePath(),"file_icon"));
                }
            }
        }catch(Exception e)
        {

        }
        Collections.sort(dir);
        Collections.sort(fls);
        dir.addAll(fls);
        if(!f.getName().equalsIgnoreCase("sdcard"))
            dir.add(0,new FilesItem("..","Parent Directory","",f.getParent(),"directory_up"));
        adapter = new FileArrayAdapter(FileChooser.this, R.layout.list_item_files, dir);
        this.setListAdapter(adapter);
    }
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);
        FilesItem o = adapter.getItem(position);
        if(o.getImage().equalsIgnoreCase("directory_icon")||o.getImage().equalsIgnoreCase("directory_up")){
            currentDir = new File(o.getPath());
            fill(currentDir);
        }
        else
        {
            onFileClick(o);
        }
    }
    private void onFileClick(FilesItem o)
    {
        //Toast.makeText(this, "Folder Clicked: "+ currentDir, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.putExtra("GetPath",currentDir.toString());
        intent.putExtra("GetFileName",o.getName());
        setResult(RESULT_OK, intent);
        finish();
    }
}