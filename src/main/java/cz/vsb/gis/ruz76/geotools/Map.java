package cz.vsb.gis.ruz76.geotools;

import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import org.geotools.data.DataUtilities;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by ruz76 on 24.11.2016.
 */
public class Map {
    MapContent map;
    public Map() {
        map = new MapContent();
        map.setTitle("Quickstart");
    }
    /*Shows the map with layers*/
    public void show() throws Exception {
        JMapFrame.showMap(map);
    }

    /*Adds SHP on path to map*/
    public void addLayer(String path) throws IOException {
        FileDataStore store = FileDataStoreFinder.getDataStore(new File(path));
        SimpleFeatureSource featureSource = store.getFeatureSource();
        Style style = SLD.createSimpleStyle(featureSource.getSchema());
        Layer layer = new FeatureLayer(featureSource, style);
        map.addLayer(layer);
    }

    /*Prints states that intersects with routes*/
    public void printStates_Routes() throws Exception {
        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
        SimpleFeatureSource states = (SimpleFeatureSource) map.layers().get(0).getFeatureSource();
        SimpleFeatureSource routes = (SimpleFeatureSource) map.layers().get(1).getFeatureSource();
        SimpleFeatureIterator routes_sfi = routes.getFeatures().features();
        while (routes_sfi.hasNext()) {
            SimpleFeature route = routes_sfi.next();
            System.out.println(route.getAttribute("id"));
            Filter filter = ff.intersects(ff.property("the_geom"), ff.literal(route.getDefaultGeometry()));
            SimpleFeatureIterator states_sfi = states.getFeatures(filter).features();
            while (states_sfi.hasNext()) {
                SimpleFeature state = states_sfi.next();
                System.out.println(state.getAttribute("STATE_NAME"));
            }
        }
    }

    /*For each state prints surrounding states*/
    public void printStates_States() throws Exception {
        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
        SimpleFeatureSource states = (SimpleFeatureSource) map.layers().get(0).getFeatureSource();
        SimpleFeatureIterator states_sfi = states.getFeatures().features();
        while (states_sfi.hasNext()) {
            SimpleFeature state = states_sfi.next();
            System.out.println("---- " + state.getAttribute("STATE_NAME") + " ----");
            Filter filter = ff.touches(ff.property("the_geom"), ff.literal(state.getDefaultGeometry()));
            SimpleFeatureIterator states_nei_sfi = states.getFeatures(filter).features();
            while (states_nei_sfi.hasNext()) {
                SimpleFeature state_nei = states_nei_sfi.next();
                System.out.println(state_nei.getAttribute("STATE_NAME"));
            }
        }
    }

    /*For states that are smaller than defined limit prints surrounding states*/
    public void printStates_States(double arealimit) throws Exception {
        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
        SimpleFeatureSource states = (SimpleFeatureSource) map.layers().get(0).getFeatureSource();
        SimpleFeatureIterator states_sfi = states.getFeatures().features();
        while (states_sfi.hasNext()) {
            SimpleFeature state = states_sfi.next();
            MultiPolygon p = (MultiPolygon) state.getDefaultGeometry();
            //System.out.println(p.getArea());
            if (p.getArea() < arealimit) {
                System.out.println("---- " + state.getAttribute("STATE_NAME") + " ----");
                Filter filter = ff.touches(ff.property("the_geom"), ff.literal(state.getDefaultGeometry()));
                SimpleFeatureIterator states_nei_sfi = states.getFeatures(filter).features();
                while (states_nei_sfi.hasNext()) {
                    SimpleFeature state_nei = states_nei_sfi.next();
                    System.out.println(state_nei.getAttribute("STATE_NAME"));
                }
            }
        }
    }

    /*For states that are smaller than defined limit prints surrounding states*/
    public void printStates_States(double arealimit, double distance) throws Exception {
        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();

        FileDataStore states_store = FileDataStoreFinder.getDataStore(new File("data/states.shp"));
        SimpleFeatureSource states = states_store.getFeatureSource();
        FileDataStore routes_store = FileDataStoreFinder.getDataStore(new File("data/routes.shp"));
        SimpleFeatureSource routes = routes_store.getFeatureSource();

        SimpleFeatureCollection states_fc = DataUtilities.collection(states.getFeatures());
        SimpleFeatureCollection routes_fc = DataUtilities.collection(routes.getFeatures());

        SimpleFeatureIterator states_sfi = states_fc.features();
        while (states_sfi.hasNext()) {
            SimpleFeature state = states_sfi.next();
            MultiPolygon p = (MultiPolygon) state.getDefaultGeometry();
            //System.out.println(p.getArea());
            if (p.getArea() < arealimit) {

                System.out.println("---- " + state.getAttribute("STATE_NAME") + " ----");
                //Probably not a good operator, but do not know yet which one to use to find line between two polygons
                //The line is not precise border between two polygons
                Filter filterroutes2 = ff.dwithin(ff.property("the_geom"), ff.literal(state.getDefaultGeometry()), distance, "dd");
                SimpleFeatureIterator routes_sfi2 = routes_fc.subCollection(filterroutes2).features();
                System.out.println("Routes close to state");
                ArrayList routesclosetostate = new ArrayList();
                while (routes_sfi2.hasNext()) {
                    SimpleFeature route = routes_sfi2.next();
                    System.out.println(route.getAttribute("id"));
                    routesclosetostate.add(route.getAttribute("id"));
                }

                Filter filter = ff.touches(ff.property("the_geom"), ff.literal(state.getDefaultGeometry()));
                SimpleFeatureIterator states_nei_sfi = states_fc.subCollection(filter).features();
                while (states_nei_sfi.hasNext()) {
                    SimpleFeature state_nei = states_nei_sfi.next();
                    System.out.println(state_nei.getAttribute("STATE_NAME"));

                    Filter filterroutes = ff.dwithin(ff.property("the_geom"), ff.literal(state_nei.getDefaultGeometry()), distance, "dd");
                    SimpleFeatureIterator routes_sfi = routes_fc.subCollection(filterroutes).features();
                    System.out.println("Routes close to surrounding state");
                    while (routes_sfi.hasNext()) {
                        SimpleFeature route = routes_sfi.next();
                        System.out.println(route.getAttribute("id"));
                        if (routesclosetostate.contains(route.getAttribute("id"))) {
                            System.out.println("There is a common route between states");
                            break;
                        }
                    }
                }
            }
        }
    }

}