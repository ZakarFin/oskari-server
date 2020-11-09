package org.oskari.usage;

import fi.nls.oskari.service.OskariComponent;

import java.util.Date;
import java.util.List;

public abstract class UsageDataService extends OskariComponent {
    public abstract void recordUsage(int maplayer);
    //public abstract void save(UsageData data);
    public abstract List<UsageData> getData(int maplayerId, Date start, Date end);
}
