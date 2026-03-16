package org.oskari.geojson;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Function;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.api.feature.FeatureVisitor;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.filter.Filter;
import org.geotools.api.filter.sort.SortBy;
import org.geotools.api.util.ProgressListener;

public class BoundsWrappingSimpleFeatureCollection implements SimpleFeatureCollection {

    private final SimpleFeatureCollection wrapped;
    private final Function<SimpleFeatureCollection, ReferencedEnvelope> boundsFn;

    public BoundsWrappingSimpleFeatureCollection(SimpleFeatureCollection wrapped, Function<SimpleFeatureCollection, ReferencedEnvelope> boundsFn) {
        this.wrapped = wrapped;
        this.boundsFn = boundsFn;
    }

    @Override
    public ReferencedEnvelope getBounds() {
        return boundsFn.apply(wrapped);
    }

    @Override
    public SimpleFeatureType getSchema() {
        return wrapped.getSchema();
    }

    @Override
    public String getID() {
        return wrapped.getID();
    }

    @Override
    public void accepts(FeatureVisitor visitor, ProgressListener progress) throws IOException {
        wrapped.accepts(visitor, progress);
    }

    @Override
    public boolean contains(Object o) {
        return wrapped.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> o) {
        return wrapped.containsAll(o);
    }

    @Override
    public boolean isEmpty() {
        return wrapped.isEmpty();
    }

    @Override
    public int size() {
        return wrapped.size();
    }

    @Override
    public Object[] toArray() {
        return wrapped.toArray();
    }

    @Override
    public <O> O[] toArray(O[] a) {
        return wrapped.toArray(a);
    }

    @Override
    public SimpleFeatureIterator features() {
        return wrapped.features();
    }

    @Override
    public SimpleFeatureCollection subCollection(Filter filter) {
        return new BoundsWrappingSimpleFeatureCollection(wrapped.subCollection(filter), boundsFn);
    }

    @Override
    public SimpleFeatureCollection sort(SortBy order) {
        return new BoundsWrappingSimpleFeatureCollection(wrapped.sort(order), boundsFn);
    }

}
