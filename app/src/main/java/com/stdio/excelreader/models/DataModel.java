package com.stdio.excelreader.models;

import java.util.ArrayList;

public class DataModel {
    public String sectionName;
    public ArrayList<Item> arrayList;

    public DataModel() {
    }

    public DataModel(String sectionName, ArrayList<Item> arrayList) {
        this.sectionName = sectionName;
        this.arrayList = arrayList;
    }

}