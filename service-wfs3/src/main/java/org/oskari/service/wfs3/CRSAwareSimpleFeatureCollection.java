package org.oskari.service.wfs3;

import java.io.IOException;
import java.util.Collection;

import org.geotools.api.feature.FeatureVisitor;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.filter.Filter;
import org.geotools.api.filter.sort.SortBy;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.util.ProgressListener;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;

public class CRSAwareSimpleFeatureCollection implements SimpleFeatureCollection {

    private final SimpleFeatureCollection wrapped;
    private final CoordinateReferenceSystem crs;

    public CRSAwareSimpleFeatureCollection(SimpleFeatureCollection wrapped, CoordinateReferenceSystem crs) {
        this.wrapped = wrapped;
        this.crs = crs;
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
    public ReferencedEnvelope getBounds() {
        ReferencedEnvelope e = wrapped.getBounds();
        if (e == null) {
            return e;
        }
        e.setCoordinateReferenceSystem(crs);
        return e;
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
        return new CRSAwareSimpleFeatureCollection(wrapped.subCollection(filter), crs);
    }

    @Override
    public SimpleFeatureCollection sort(SortBy order) {
        return new CRSAwareSimpleFeatureCollection(wrapped.sort(order), crs);
    }

}
