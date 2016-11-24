package cz.vsb.gis.ruz76.geotools;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
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
}