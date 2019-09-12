package com.stdio.excelreader;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.stdio.excelreader.adapters.ItemClickListener;
import com.stdio.excelreader.adapters.Section;
import com.stdio.excelreader.adapters.SectionedExpandableLayoutHelper;
import com.stdio.excelreader.models.DataModel;
import com.stdio.excelreader.models.Item;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;


public class MainActivity extends AppCompatActivity implements ItemClickListener {

    RecyclerView mRecyclerView;
    ArrayList<DataModel> mainArrayList = new ArrayList<>();
    DialogProperties properties = new DialogProperties();
    FilePickerDialog dialog;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    SectionedExpandableLayoutHelper sectionedExpandableLayoutHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setting the recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        sectionedExpandableLayoutHelper = new SectionedExpandableLayoutHelper(this,
                mRecyclerView, this, 3);

        database= FirebaseDatabase.getInstance();
        try {
            database.setPersistenceEnabled(true);
        }
        catch (DatabaseException e) {

        }
        myRef = database.getReference("items");

        getData();

        setFilePickerProperties();
        dialog = new FilePickerDialog(MainActivity.this,properties);
        dialog.setTitle("Выберите xls документ");
        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                boolean exception = false;
                System.out.println(files[0]);
                files[0] = files[0].replace("/mnt/sdcard/", "/storage/emulated/0/");
                FileInputStream file = null;
                XSSFWorkbook workbook = null;
                try {
                    file = new FileInputStream(files[0]);
                    // формируем из файла экземпляр HSSFWorkbook
                    workbook = new XSSFWorkbook(file);
                } catch (FileNotFoundException e) {
                    Toast.makeText(MainActivity.this, "File not found\n" + files[0], Toast.LENGTH_LONG).show();
                    exception = true;
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    exception = true;
                }
                if (!exception) {
                    mainArrayList.clear();
                    myRef.removeValue();
                    xlsReader(workbook);
                }

            }
        });
    }

    private void xlsReader(XSSFWorkbook workbook) {

        String article = null, barcode = null, name = null, count = null, address = null;

        String result = "";
        // выбираем первый лист для обработки
        // нумерация начинается с 0
        XSSFSheet sheet = workbook.getSheetAt(0);

        // получаем Iterator по всем строкам в листе
        Iterator<Row> rowIterator = sheet.iterator();

        int counter = 0;
        ArrayList<Item> arrayList = new ArrayList<>();

        //проходим по всему листу
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (row.getRowNum() != 0) {
                Double tmpDoubleVal = 0.0;
                long longValue = 0;
                Iterator<Cell> cells = row.iterator();
                while (cells.hasNext()) {
                    Cell cell = cells.next();
                    int cellType = cell.getCellType();
                    //перебираем возможные типы ячеек
                    switch (cellType) {
                        case Cell.CELL_TYPE_STRING:
                            result += cell.getStringCellValue() + "=";
                            break;
                        case Cell.CELL_TYPE_NUMERIC:
                            tmpDoubleVal = cell.getNumericCellValue();
                            longValue = tmpDoubleVal.longValue();
                            result += longValue + "=";
                            break;

                        case Cell.CELL_TYPE_FORMULA:
                            tmpDoubleVal = cell.getNumericCellValue();
                            longValue = tmpDoubleVal.longValue();
                            result += longValue + "=";
                            break;
                        default:
                            result += cell.getStringCellValue() + "=";
                            break;
                    }
                }
                String[] subStr;
                String delimeter = "="; // Разделитель
                subStr = result.split(delimeter); // Разделения строки str с помощью метода split()
                // Вывод результата на экран
                arrayList.clear();
                System.out.println(subStr.length);
                for (int i = 0; i < subStr.length; i++) {
                    System.out.println(i);
                    System.out.println(subStr[i]);
                    arrayList.add(new Item(subStr[i], i));
                }
                if (!arrayList.isEmpty()) {
                    counter++;
                    DataModel item = new DataModel(String.valueOf(counter), arrayList);
                    myRef.push().setValue(item);
                }
                result = "";
            }
        }
    }

    private void setFilePickerProperties() {
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.offset = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = null;
    }

    private void getData() {
        mainArrayList = new ArrayList();

        Query myQuery = myRef;
        myQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                DataModel item = dataSnapshot.getValue(DataModel.class);
                sectionedExpandableLayoutHelper.addSection(item.sectionName,item.arrayList);
                if (item.arrayList != null && item.arrayList.size() != 0) {
                    for (int i = 0; i < item.arrayList.size(); i++) {
                        System.out.println(item.arrayList.get(i).getName());
                    }
                    System.out.println("AAA" + item.arrayList.get(0).getName());
                    sectionedExpandableLayoutHelper.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.action_import:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    dialog.show();
                } else {
                    requestReadPermission();
                }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestReadPermission() {
        ActivityCompat.requestPermissions(this, new String[] {
                Manifest.permission.READ_EXTERNAL_STORAGE
        }, REQUEST_EXTERNAL_STORAGE);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case FilePickerDialog.EXTERNAL_READ_PERMISSION_GRANT: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(dialog!=null)
                    {   //Show dialog if the read permission has been granted.
                        dialog.show();
                    }
                }
                else {
                    //Permission has not been granted. Notify the user.
                    Toast.makeText(MainActivity.this,"Permission is Required for getting list of files",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void itemClicked(Item item) {
        Toast.makeText(this, "Item: " + item.getName() + " clicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void itemClicked(Section section) {
        Toast.makeText(this, "Section: " + section.getName() + " clicked", Toast.LENGTH_SHORT).show();
    }
}
