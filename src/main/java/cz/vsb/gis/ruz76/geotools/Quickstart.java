package cz.vsb.gis.ruz76.geotools;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Quickstart {

    public static void main(String[] args) throws Exception {
        Map m = new Map();
        //m.addLayer("D:\\ruz76\\geoserver-2.9.1\\data_dir\\data\\shapefiles\\states.shp");
        //m.addLayer("D:\\ruz76\\geoserver-2.9.1\\data_dir\\data\\shapefiles\\routes.shp");
        m.addLayer("data/states.shp");
        m.addLayer("data/routes.shp");
        //m.printStates_Routes();
        //m.printStates_States();
        //m.printStates_States(10, 0.05); //0.05 is experimental - you can specify another buffer tolerance
        //m.show();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("/tmp/union.wkt"))) {
            m.printStates_States(10, 0.05, new AreaRange(20, 40), bw); //0.05 is experimental - you can specify another buffer tolerance
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*AreaRange ar = new AreaRange(20, 40);
        System.out.println(ar.getFit(35));*/
    }

}