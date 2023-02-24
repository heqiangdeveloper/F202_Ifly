package com.chinatsp.ifly.voice.platformadapter.entity;

/**
 * ClassName: //TODO
 * Function: //TODO
 * Reason: //TODO
 *
 * @author: qlf
 * @version: v1.0
 * @date : 2020/8/6
 */

public class CarNumberEntity {
    /**
     * errcode : 0
     * msg : 上海市今天限行规则是禁止本市“沪C”号牌机动车辆通行
     * answer : {"text":"上海市今天限行规则是禁止本市\u201c沪C\u201d号牌机动车辆通行"}
     */

    private int errcode;
    private String msg;
    private AnswerBean answer;

    public int getErrcode() {
        return errcode;
    }

    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public AnswerBean getAnswer() {
        return answer;
    }

    public void setAnswer(AnswerBean answer) {
        this.answer = answer;
    }

    public static class AnswerBean {
        /**
         * text : 上海市今天限行规则是禁止本市“沪C”号牌机动车辆通行
         */

        private String text;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}
