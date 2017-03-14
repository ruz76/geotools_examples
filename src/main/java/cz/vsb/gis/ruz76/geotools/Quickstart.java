package cz.vsb.gis.ruz76.geotools;

public class Quickstart {

    public static void main(String[] args) throws Exception {
        Map m = new Map();
        //m.addLayer("D:\\ruz76\\geoserver-2.9.1\\data_dir\\data\\shapefiles\\states.shp");
        //m.addLayer("D:\\ruz76\\geoserver-2.9.1\\data_dir\\data\\shapefiles\\routes.shp");
        m.addLayer("data/states.shp");
        m.addLayer("data/routes.shp");
        //m.printStates_Routes();
        //m.printStates_States();
        m.printStates_States(10, 0.05); //0.05 is experimental - you can specify another buffer tolerance
        //m.show();
    }

}