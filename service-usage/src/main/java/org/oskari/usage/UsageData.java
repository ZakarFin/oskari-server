package org.oskari.usage;

import java.util.Date;

public class UsageData {
    private int id;
    private Date date;
    private int maplayer;
    private int total = 0;
    private String maplayer_name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getMaplayer() {
        return maplayer;
    }

    public void setMaplayer(int maplayer) {
        this.maplayer = maplayer;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getMaplayer_name() {
        return maplayer_name;
    }

    public void setMaplayer_name(String maplayer_name) {
        this.maplayer_name = maplayer_name;
    }
}
