package com.chinatsp.ifly.entity;

public class MessageListEvent {
    public enum ListEventType {
        LAST_PAGE,  //上一页
        NEXT_PAGE,  //下一页
        FINAL_PAGE, //最后一页
        SELECT_WHICH_ONE, //第几个
        SELECT_WHICH_PAGE, //第几页
    }

    public ListEventType eventType;
    public int index;//选择的角标
}