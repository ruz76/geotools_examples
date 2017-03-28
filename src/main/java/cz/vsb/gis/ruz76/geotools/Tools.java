package cz.vsb.gis.ruz76.geotools;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import org.geotools.data.*;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jencek on 28.3.17.
 */
public class Tools {
    /*
        * Finds routes that are along shared border of state that is under area limit and surrounding state
        * */
    public void printStates_States(double arealimit, double distance, AreaRange arearange, BufferedWriter bw) throws Exception {
        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();

        FileDataStore states_store = FileDataStoreFinder.getDataStore(new File("data/states.shp"));
        SimpleFeatureSource states = states_store.getFeatureSource();
        FileDataStore routes_store = FileDataStoreFinder.getDataStore(new File("data/routes.shp"));
        SimpleFeatureSource routes = routes_store.getFeatureSource();

        SimpleFeatureCollection states_fc = DataUtilities.collection(states.getFeatures());
        SimpleFeatureCollection routes_fc = DataUtilities.collection(routes.getFeatures());

        ListFeatureCollection states_fc_list = new ListFeatureCollection(states_fc);
        ListFeatureCollection states_fc_list2 = new ListFeatureCollection(states_fc);

        SimpleFeatureIterator states_sfi = states_fc_list2.features();
        ArrayList<String> modified_states = new ArrayList();

        //List<SimpleFeature> featuresout = new ArrayList<>();

        //Loop to all states
        while (states_sfi.hasNext()) {
            SimpleFeature state = states_sfi.next();

            MultiPolygon p = (MultiPolygon) state.getDefaultGeometry();
            //System.out.println(p.getArea());
            //Gets only polygons under limited area
            if (p.getArea() < arealimit && !modified_states.contains(state.getAttribute("STATE_FIPS").toString())) {

                System.out.println("---- " + state.getAttribute("STATE_NAME") + " ----");
                Geometry state_buffer = ((MultiPolygon) state.getDefaultGeometry()).buffer(distance);

                Filter filter = ff.touches(ff.property("the_geom"), ff.literal(state.getDefaultGeometry()));
                SimpleFeatureIterator states_nei_sfi = states_fc_list.subCollection(filter).features();
                bw.write("S;" + state.getDefaultGeometry() + "\n");

                Geometry geomout = (Geometry) state.getDefaultGeometry();
                SimpleFeature state_to_remove = null;
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
                    boolean isRoute = false;
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
                            isRoute = true;
                        }
                        System.out.println("Route id: " + route.getAttribute("id") + " Length: " + route_intersection.getLength() + " From: " + buffer_intersection.getLength());
                    }
                    if (!isRoute) {
                        GeometryFactory factory = new GeometryFactory();
                        ArrayList c = new ArrayList();
                        c.add(state_nei.getDefaultGeometry());
                        c.add(state.getDefaultGeometry());
                        GeometryCollection gc = (GeometryCollection) factory.buildGeometry(c);
                        Geometry union = gc.union();
                        if (arearange.getFit(union.getArea()) >= arearange.getFit(geomout.getArea())) {
                            geomout = union;
                            state_to_remove = state_nei;
                        }
                    }
                }
                //System.out.println(gc.union());
                bw.write("U;" + geomout + "\n");
                states_fc_list.remove(state);
                states_fc_list.remove(state_to_remove);
                //states_fc_list2.remove(state_to_remove);
                modified_states.add(state_to_remove.getAttribute("STATE_FIPS").toString());
                //states_fc_list.removeIf(state_to_remove.getID() == siteID)
                /*
                Filter filterstateid = ff.equal( ff.property( "STATE_FIPS"), ff.literal( state.getAttribute("STATE_FIPS").toString() ) );
                SimpleFeatureIterator state_id_sfi = states_fc_list.subCollection(filterstateid).features();
                if (state_id_sfi.hasNext()) {
                    SimpleFeature state_to_change = state_id_sfi.next();
                    state_to_change.setDefaultGeometry(geomout);
                }*/
                //states_fc_list
                //state.getID();
                state.setDefaultGeometry(geomout);
                states_fc_list.add(state);
                //Only for debug purposes
                //saveFeatureCollectionToShapefile("/tmp/test.shp", states_fc_list, states_fc_list.getSchema());
                //System.out.println("End Feature");
            }
            //featuresout.add(state);
        }
        saveFeatureCollectionToShapefile("/tmp/test.shp", states_fc_list, states_fc_list.getSchema());
        System.out.println("End");
    }

    private void saveFeatureCollectionToShapefile(String path, ListFeatureCollection features, SimpleFeatureType TYPE) throws MalformedURLException, IOException {
        File newFile = new File(path);

        java.util.Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put("url", newFile.toURI().toURL());
        //params.put("create spatial index", Boolean.TRUE);

        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

        ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);

        newDataStore.createSchema(TYPE);
        newDataStore.forceSchemaCRS(TYPE.getGeometryDescriptor().getCoordinateReferenceSystem());

        Transaction transaction = new DefaultTransaction("create");

        String typeName = newDataStore.getTypeNames()[0];
        SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);
        SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
        featureStore.setTransaction(transaction);
        featureStore.addFeatures(features);
        transaction.commit();
        transaction.close();
    }

     /*For states that are smaller than defined limit prints surrounding states*/
    /*public void printStates_States(double arealimit, double distance) throws Exception {
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
    }*/
}

