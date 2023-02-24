package com.chinatsp.ifly.voice.platformadapter.entity;

public class AnswerEntity {
    public String text ;
    public String answerType ;
    public String emotion ;
    public String topicID ;
    public String type;
    public Question question;
    public String display_text;

    public static class Question {

        public String question;
        public String question_ws;
    }
}
