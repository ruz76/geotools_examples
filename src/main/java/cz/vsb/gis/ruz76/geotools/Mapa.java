package cz.vsb.gis.ruz76.geotools;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
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
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;

/**
 * Created by ruz76 on 24.11.2016.
 */
public class Mapa {
    MapContent map;
    public Mapa() {
        map = new MapContent();
        map.setTitle("Quickstart");
    }
    /*Shows the map with layers*/
    public void show() throws Exception {
        JMapFrame jmf = new JMapFrame();
        jmf.setMapContent(map);
        jmf.enableToolBar(true);
        jmf.enableStatusBar(true);
        jmf.setSize(800, 600);
        jmf.getMapPane().addKeyListener(new KeyListenerForTools(map));
        jmf.getContentPane().add(new JButton("AAA"));
        //jmf.getToolBar().add(new JButton("AAA"));
        jmf.setVisible(true);

        //JMapFrame.showMap(map);
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

    /*
    * Finds routes that are along shared border of state that is under area limit and surrounding state
    * */
    public void printStates_States(double arealimit, double distance) throws Exception {
        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();

        FileDataStore states_store = FileDataStoreFinder.getDataStore(new File("data/states.shp"));
        SimpleFeatureSource states = states_store.getFeatureSource();
        FileDataStore routes_store = FileDataStoreFinder.getDataStore(new File("data/routes.shp"));
        SimpleFeatureSource routes = routes_store.getFeatureSource();

        SimpleFeatureCollection states_fc = DataUtilities.collection(states.getFeatures());
        SimpleFeatureCollection routes_fc = DataUtilities.collection(routes.getFeatures());

        SimpleFeatureIterator states_sfi = states_fc.features();
        //Loop to all states
        while (states_sfi.hasNext()) {
            SimpleFeature state = states_sfi.next();
            MultiPolygon p = (MultiPolygon) state.getDefaultGeometry();
            //System.out.println(p.getArea());
            //Gets only polygons under limited area
            if (p.getArea() < arealimit) {

                System.out.println("---- " + state.getAttribute("STATE_NAME") + " ----");
                Geometry state_buffer = ((MultiPolygon) state.getDefaultGeometry()).buffer(distance);

                Filter filter = ff.touches(ff.property("the_geom"), ff.literal(state.getDefaultGeometry()));
                SimpleFeatureIterator states_nei_sfi = states_fc.subCollection(filter).features();
                //Loops states that surrounds processed state

                while (states_nei_sfi.hasNext()) {
                    SimpleFeature state_nei = states_nei_sfi.next();
                    System.out.println(state_nei.getAttribute("STATE_NAME"));

                    //Creates intersection between buffer around processed state and processed surounding state
                    Geometry state_nei_buffer = ((MultiPolygon) state_nei.getDefaultGeometry()).buffer(distance);
                    Geometry buffer_intersection = state_nei_buffer.intersection(state_buffer);

                    Filter filterroutes = ff.intersects(ff.property("the_geom"), ff.literal(buffer_intersection));
                    SimpleFeatureIterator routes_sfi = routes_fc.subCollection(filterroutes).features();
                    //System.out.println("Routes between surrounding state and state");
                    //Loops routes that are in intersections between two states
                    while (routes_sfi.hasNext()) {
                        SimpleFeature route = routes_sfi.next();
                        Geometry route_intersection = buffer_intersection.intersection((Geometry) route.getDefaultGeometry());
                        //System.out.println(route_intersection);
                        //System.out.println(buffer_intersection);
                        //If the perimeter of the intersection of states is up to 10 times bigger than route that is in intersection
                        //then the route is probably along the whole intersection
                        //difficult to determine limit, but 10 seems to be good
                        //will do some tests on real data later
                        if ((buffer_intersection.getLength() / route_intersection.getLength()) < 10) { //10 is Experimental
                            System.out.println("Probably route along shared border between " + state.getAttribute("STATE_NAME") + " and " + state_nei.getAttribute("STATE_NAME"));
                        }
                        System.out.println("Route id: " + route.getAttribute("id") + " Length: " + route_intersection.getLength() + " From: " + buffer_intersection.getLength());
                    }
                }
            }
        }
    }

}

class KeyListenerForTools implements KeyListener {
    MapContent map;
    public KeyListenerForTools(MapContent map) {
        this.map = map;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        System.out.println("DEBUG");
        map.getViewport().isEditable();
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}