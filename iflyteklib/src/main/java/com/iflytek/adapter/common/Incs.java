package com.iflytek.adapter.common;

public interface Incs {
	public static int VW_MSG_WAKEUP = 20;
	public static int MVW_MSG_WAKEUP = 21;
	public static int MVW_MSG_WAKEINIT = 22;
	public static int MSG_TYPE_SR = 30;

	public static int SR_MSG_SETPARAM = 42;
	public static int SR_MSG_ENDAUDIODATA_RETURN = 43;
	public static int SR_MSG_RECREATE_SR = 44;

	public static int SR_DISPLAY_STRING = 50;

	public static int SR_SETPARAMS_RESULT = 60;
	public static String szCmd = "{\"grm\":[{\"dictname\": \"cmd\",\"dictcontant\": ["
			+ "{ \"name\": \"移动号码\", \"id\": 0 },"
			+ "{ \"name\": \"联通号码\", \"id\": 1 },"
			+ "{ \"name\": \"电信号码\", \"id\": 2 }]}]}";
	public static String NLPszCmd = "{\"nlptext\": \"今天天气怎么样？\"}";
}
