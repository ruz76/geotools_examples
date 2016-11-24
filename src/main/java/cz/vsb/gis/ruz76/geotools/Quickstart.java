package cz.vsb.gis.ruz76.geotools;

public class Quickstart {

    public static void main(String[] args) throws Exception {
        Map m = new Map();
        m.addLayer("D:\\ruz76\\geoserver-2.9.1\\data_dir\\data\\shapefiles\\states.shp");
        m.addLayer("D:\\ruz76\\geoserver-2.9.1\\data_dir\\data\\shapefiles\\routes.shp");
        m.printStates_Routes();
        //m.show();
    }


}