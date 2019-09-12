package com.stdio.excelreader;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.stdio.excelreader.adapters.ItemClickListener;
import com.stdio.excelreader.adapters.Section;
import com.stdio.excelreader.adapters.SectionedExpandableLayoutHelper;
import com.stdio.excelreader.models.DataModel;
import com.stdio.excelreader.models.Item;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements ItemClickListener {

    RecyclerView mRecyclerView;
    ArrayList<DataModel> mainArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setting the recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        saveData();
        getData();
    }

    private void saveData() {
        ArrayList<Item> arrayList = new ArrayList<>();

        int tmpManager = 0;
        int counter = 0;

        while (tmpManager < 3) {
            arrayList.clear();
            String[] subStr = {"iPhone", "iPad", "iPod", "iMac"};
            System.out.println(subStr.length);
            for (int i = 0; i < subStr.length; i++) {
                System.out.println(i);
                System.out.println(subStr[i]);
                arrayList.add(new Item(subStr[i], i));
            }
            tmpManager++;
            mainArrayList.add(new DataModel(String.valueOf(counter), arrayList));
            counter++;
        }
    }

    private void getData() {
        SectionedExpandableLayoutHelper sectionedExpandableLayoutHelper = new SectionedExpandableLayoutHelper(this,
                mRecyclerView, this, 3);
        for (int i = 0; i < mainArrayList.size(); i++) {
            sectionedExpandableLayoutHelper.addSection(mainArrayList.get(i).sectionName, mainArrayList.get(i).arrayList);
            sectionedExpandableLayoutHelper.notifyDataSetChanged();
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
