package com.chinatsp.ifly.entity;

import com.chinatsp.ifly.base.BaseEntity;

public class MultiChoiceEvent {
    public static final int EVENT_END = 1;
    public static final int EVENT_VIA = 2;
    public static final int EVENT_ADD_POINTS = 3;
    public int mEventType = -1;
    public BaseEntity mEntity;

    public MultiChoiceEvent(int eventType, BaseEntity entity) {
        this.mEventType = eventType;
        this.mEntity = entity;
    }

    @Override
    public String toString() {
        return "MultiChoiceEvent{" +
                "mEventType=" + mEventType +
                ", mEntity=" + mEntity +
                '}';
    }
}
