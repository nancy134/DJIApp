package com.roboticaircraftinspection.roboticinspection.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CSVFile {
    FileInputStream fileInputStream;
    BufferedReader reader;
    public CSVFile(String path){
        try {
            fileInputStream = new FileInputStream(path);

        } catch (FileNotFoundException ex){
            Log.d("Robotic Inspection",ex.getLocalizedMessage());
        }

    }
    public List<String[]> read(){
        List<String[]> resultList = new ArrayList<String[]>();
        reader = new BufferedReader(new InputStreamReader(fileInputStream));
        try {
            String csvLine;
            while ((csvLine = reader.readLine()) != null) {
                String[] row = csvLine.split(",");
                resultList.add(row);
            }
        }
        catch (IOException ex) {
            throw new RuntimeException("Error in reading CSV file: "+ex);
        }

        finally {
            try {
                fileInputStream.close();
            }
            catch (IOException e) {
                throw new RuntimeException("Error while closing input stream: "+e);
            }
        }
        return resultList;
    }
}
