package com.sam_chordas.android.stockhawk.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yaseen on 19/06/16.
 */
public class DateHighMain {
    public List<DateHigh> datehigh = new ArrayList<DateHigh>();

    public List<DateHigh> getDatehigh() {
        return datehigh;
    }

    /**
     *
     * @param datehigh
     * The datehigh
     */
    public void setDatehigh(List<DateHigh> datehigh) {
        this.datehigh = datehigh;
    }
    public void addDatehigh(DateHigh datehigh) {
        this.datehigh.add(datehigh);
    }
}
