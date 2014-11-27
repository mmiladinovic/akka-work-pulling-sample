package com.mmiladinovic.events;

import java.io.Serializable;

/**
 * Created by miroslavmiladinovic on 27/11/2014.
 */
public class WorkToBeDone implements Serializable {

    public final Object work;

    public WorkToBeDone(Object work) {
        this.work = work;
    }
}
