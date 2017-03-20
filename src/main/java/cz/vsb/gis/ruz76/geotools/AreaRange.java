package cz.vsb.gis.ruz76.geotools;

/**
 * Created by jencek on 20.3.17.
 */
public class AreaRange {
    private double min;
    private double max;

    public AreaRange(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    private double getCenter() {
        return min + ((max - min) / 2);
    }

    private double getRange() {
        return max - min;
    }

    public double getFit(double value) {
        if (value < min) return 0;
        if (value > max) return 0;
        double diff = Math.abs(getCenter() - value);
        double diff_range = diff / (getRange()/2);
        return diff_range;
    }
}
