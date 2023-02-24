package com.chinatsp.ifly.utils;

public enum StarEnum {
    HALF_STAR(0.5f),
    ONE_STAR(1.0f),
    ONE_HALF_STAR(1.5f),
    TWO_STAR(2.0f),
    TWO_HALF_STAR(2.5f),
    THREE_STAR(3.0f),
    THREE_HALF_STAR(3.5f),
    FOUR_STAR(4.0f),
    FOUR_HALF_STAR(4.5f),
    FIVE_STAR(5.0f);

    private float value;

    StarEnum(float value) {
        this.value = value;
    }

    public float getValue() {
        return value;
    }

    public static boolean isInclude(float value) {
        boolean include = false;
        for (StarEnum e : StarEnum.values()) {
            if (e.getValue() == value) {
                include = true;
                break;
            }
        }
        return include;
    }
}