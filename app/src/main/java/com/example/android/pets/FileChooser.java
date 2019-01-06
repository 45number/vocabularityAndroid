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

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ListActivity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class FileChooser extends ListActivity {

    private File currentDir;
    private FileArrayAdapter adapter;

    ArrayList<XYValue> uploadData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentDir = new File("/sdcard/");
        fill(currentDir);
        uploadData = new ArrayList<>();
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
    private void onFileClick(final FilesItem o)
    {
        /*//Toast.makeText(this, "Folder Clicked: "+ currentDir, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.putExtra("GetPath",currentDir.toString());
        intent.putExtra("GetFileName",o.getName());
        setResult(RESULT_OK, intent);
        finish();*/



        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                //TODO your background code
                ArrayList<ArrayList<String>> dict = readExcelData(currentDir.toString()+"/"+o.getName());

//                Log.e("8888", dict.toString());

                Intent intent = new Intent();
                intent.putExtra("GetPath",currentDir.toString());
                intent.putExtra("GetFileName",o.getName());
                setResult(RESULT_OK, intent);
            }
        });

//        finish();
//        Log.e("Path", currentDir.toString()+"/"+o.getName() );
    }



    private  ArrayList<ArrayList<String>> readExcelData(String filePath) {
        Log.d("FileChooser", "readExcelData: Reading Excel File.");

        //decarle input file
        File inputFile = new File(filePath);

        ArrayList<ArrayList<String>> dict = new ArrayList<>();

        try {
            InputStream inputStream = new FileInputStream(inputFile);
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheetAt(0);
            int rowsCount = sheet.getPhysicalNumberOfRows();
//            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
//            StringBuilder sb = new StringBuilder();

            //outter loop, loops through rows
            /*for (int r = 1; r < rowsCount; r++) {
                Row row = sheet.getRow(r);
                int cellsCount = row.getPhysicalNumberOfCells();
                //inner loop, loops through columns
                for (int c = 0; c < cellsCount; c++) {
                    //handles if there are to many columns on the excel sheet.
                    if(c>2){
                        Log.e("FileChooser", "readExcelData: ERROR. Excel File Format is incorrect! " );
                        toastMessage("ERROR: Excel File Format is incorrect!");
                        break;
                    }else{
                        String value = getCellAsString(row, c, formulaEvaluator);
                        String cellInfo = "r:" + r + "; c:" + c + "; v:" + value;
                        Log.d("FileChooser", "readExcelData: Data from row: " + cellInfo);
                        sb.append(value + ", ");
                    }
                }
                sb.append(":");
            }
            Log.d("FileChooser", "readExcelData: STRINGBUILDER: " + sb.toString());*/




            //outter loop, loops through rows
            int r = 0;
            int r1 = 0;
            while (r < rowsCount) {

                Row row = sheet.getRow(r1);

//                Log.e("rrrrrrrrrrrr", ""+r);

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

//                Log.e("r is", ""+r);
//                Log.e("Word", wordCell.toString());
//                Log.e("Translate", translateCell.toString());

                ArrayList<String> pair = new ArrayList<>();
                pair.add(wordCell.toString());
                pair.add(translateCell.toString());

                dict.add(pair);
//                Log.e("Rows count", ""+rowsCount);

                r1++;
                r++;
            }

            /*for (int r = 0; r < rowsCount; r++) {
                Row row = sheet.getRow(r);


                if (row == null) {
                    r--;
                    continue;
                }


                Cell wordCell = row.getCell(0);
                Cell translateCell = row.getCell(1);

                if (wordCell == null || wordCell.getCellType() == Cell.CELL_TYPE_BLANK
                        || translateCell == null || translateCell.getCellType() == Cell.CELL_TYPE_BLANK) {
                    r--;
                    continue;
                }


                Log.e("Word", wordCell.toString());
                Log.e("Translate", translateCell.toString());
                Log.e("Rows count", ""+rowsCount);


                *//*if (row != null) {
                    Cell wordCell = row.getCell(0);
                    if (wordCell != null || wordCell.getCellType() != Cell.CELL_TYPE_BLANK) {
                        Log.e("Word", wordCell.toString());
                    }

                    Cell translateCell = row.getCell(1);
                    if (translateCell != null || translateCell.getCellType() != Cell.CELL_TYPE_BLANK) {
                        Log.e("Translate", translateCell.toString());
                    }
                }*//*






                *//*int cellsCount = row.getPhysicalNumberOfCells();
                //inner loop, loops through columns
                for (int c = 0; c < cellsCount; c++) {
                    //handles if there are to many columns on the excel sheet.
                    if(c>2){
                        Log.e("FileChooser", "readExcelData: ERROR. Excel File Format is incorrect! " );
                        toastMessage("ERROR: Excel File Format is incorrect!");
                        break;
                    }else{
                        String value = getCellAsString(row, c, formulaEvaluator);
                        String cellInfo = "r:" + r + "; c:" + c + "; v:" + value;
                        Log.d("FileChooser", "readExcelData: Data from row: " + cellInfo);
                        sb.append(value + ", ");
                    }
                }
                sb.append(":");*//*
            }
//            Log.d("FileChooser", "readExcelData: STRINGBUILDER: " + sb.toString());*/




//            parseStringBuilder(sb);

        }catch (FileNotFoundException e) {
            Log.e("FileChooser", "readExcelData: FileNotFoundException. " + e.getMessage() );
        } catch (IOException e) {
            Log.e("FileChooser", "readExcelData: Error reading inputstream. " + e.getMessage() );
        }

        return dict;
    }


//    private String getCellValue(Cell cell) {
//        if (cell != null || cell.getCellType() != Cell.CELL_TYPE_BLANK) {
//            return cell.toString();
////            Log.e("opa", cell.toString());
//        }
//        return;
//    }


    /*private String getCellAsString(Row row, int c, FormulaEvaluator formulaEvaluator) {
        String value = "";
        try {
            Cell cell = row.getCell(c);
            CellValue cellValue = formulaEvaluator.evaluate(cell);
            switch (cellValue.getCellType()) {
                case Cell.CELL_TYPE_BOOLEAN:
                    value = ""+cellValue.getBooleanValue();
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    double numericValue = cellValue.getNumberValue();
                    if(HSSFDateUtil.isCellDateFormatted(cell)) {
                        double date = cellValue.getNumberValue();
                        SimpleDateFormat formatter =
                                new SimpleDateFormat("MM/dd/yy");
                        value = formatter.format(HSSFDateUtil.getJavaDate(date));
                    } else {
                        value = ""+numericValue;
                    }
                    break;
                case Cell.CELL_TYPE_STRING:
                    value = ""+cellValue.getStringValue();
                    break;
                default:
            }
        } catch (NullPointerException e) {

            Log.e("FileChooser", "getCellAsString: NullPointerException: " + e.getMessage() );
        }
        return value;
    }*/


    /*public void parseStringBuilder(StringBuilder mStringBuilder){
        Log.d("FileChooser", "parseStringBuilder: Started parsing.");

        // splits the sb into rows.
        String[] rows = mStringBuilder.toString().split(":");

        //Add to the ArrayList<XYValue> row by row
        for(int i=0; i<rows.length; i++) {
            //Split the columns of the rows
            String[] columns = rows[i].split(",");

            //use try catch to make sure there are no "" that try to parse into doubles.
            try{
                double x = Double.parseDouble(columns[0]);
                double y = Double.parseDouble(columns[1]);

                String cellInfo = "(x,y): (" + x + "," + y + ")";
                Log.d("FileChooser", "ParseStringBuilder: Data from row: " + cellInfo);

                //add the the uploadData ArrayList
                uploadData.add(new XYValue(x,y));

            }catch (NumberFormatException e){//you

                Log.e("FileChooser", "parseStringBuilder: NumberFormatException: " + e.getMessage());

            }
        }

        printDataToLog();
    }*/






//    private void printDataToLog() {
//        Log.d("FileChooser", "printDataToLog: Printing data to log...");
//
//        for(int i = 0; i< uploadData.size(); i++){
//            double x = uploadData.get(i).getX();
//            double y = uploadData.get(i).getY();
//            Log.d("FileChooser", "printDataToLog: (x,y): (" + x + "," + y + ")");
//        }
//    }

//    private void toastMessage(String message){
//        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
//    }





}