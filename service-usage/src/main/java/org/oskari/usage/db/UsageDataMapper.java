package org.oskari.usage.db;

import org.apache.ibatis.annotations.Param;
import org.oskari.usage.UsageData;

import java.util.Date;
import java.util.List;

/**
 * Created by MHURME on 14.9.2015.
 */
public interface UsageDataMapper {
    UsageData find(@Param("id") int id);
    List<UsageData> getData(@Param("maplayer") int maplayer, @Param("start") Date start, @Param("end") Date end);
    void insert(UsageData rating);
    void update(UsageData rating);
    int getTotal(@Param("maplayer") int maplayer, @Param("date") Date date);

}
