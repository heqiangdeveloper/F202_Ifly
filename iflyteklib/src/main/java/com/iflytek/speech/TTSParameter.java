package com.iflytek.speech;

/**
 * Created by PeiXie on 2016/9/27.
 */

public class TTSParameter {
  ///< The code page of the input TTS text
	final public static int ISS_TTS_CODEPAGE_GBK = 936;
    final public static int ISS_TTS_CODEPAGE_UTF16LE = 1200;
    final public static int ISS_TTS_CODEPAGE_UTF8 = 65001;
	
	
	final public static int ISS_TTS_RES_MODE_FILE_HANDLE= 0	;/*!< TTS Res load type. by file handle */
	final public static int ISS_TTS_RES_MODE_MEMORY	=	 1;	/*!< TTS Res load type. load to memory */

	final public static int ISS_TTS_PARAM_SPEAKER =(0x00000500);
	
	//tagespeakers
	final public static int ivTTS_ROLE_XIAOYUAN	= 60100;	/**< XiaoYuan	(, Chinese) */
	final public static int ivTTS_ROLE_XIAOYAN	= 60020;	/**< XiaoYan	(, Chinese) */
	final public static int ivTTS_ROLE_XIAOFENG	= 60030;	/**< XiaoFeng	(, Chinese) */	
	final public static int ivTTS_ROLE_NANNAN	= 60130;	/**< NanNan		(, Chinese) */
	final public static int ivTTS_ROLE_XIAOXUE	= 65040;	/**< XiaoXue	(, Chinese) */
	final public static int ivTTS_ROLE_XIAOJIE	= 65060;	/**< XiaoJie	(, Chinese) */
	final public static int ivTTS_ROLE_JIAJIA	= 65180;	/**< JiaJia		(, Chinese) */
	
	final public static int ISS_TTS_PARAM_VOICE_SPEED =(0x00000502); /*!< Used to set the TTS voice speed */
	final public static int ISS_TTS_SPEED_MIN =(-32768)   ;          /*!< The slowest speed */
	final public static int ISS_TTS_SPEED_NORMAL_DEFAULT= (0);       /*!< The normal speed by default */
	final public static int ISS_TTS_SPEED_MAX= (+32767);             /*!< The fastest speed */

	final public static int ISS_TTS_PARAM_VOICE_PITCH =(0x00000503) ;/*!< Used to set the 
	TTS voice pitch */
	final public static int ISS_TTS_PITCH_MIN= (-32768);             /*!< The lowest pitch */
	final public static int ISS_TTS_PITCH_NORMAL_DEFAULT= (0)  ;     /*!< The normal pitch by default */
	final public static int ISS_TTS_PITCH_MAX =(+32767) ;            /*!< The highest pitch */

	final public static int ISS_TTS_PARAM_VOLUME =(0x00000504) ;  /*!< Used to set the TTS voice volume */
	final public static int ISS_TTS_VOLUME_MIN =(-32768) ;        /*!< The minimum volume */
	final public static int ISS_TTS_VOLUME_NORMAL =(0) ;          /*!< The normal volume */
	final public static int ISS_TTS_VOLUME_MAX_DEFAULT= (+32767) ;/*!< The maximum volume by default */

	final public static int ISS_TTS_PARAM_USERMODE=	(0x00000701);		/*!< user's mode */
	/* constants for values of parameter ivTTS_PARAM_USERMODE(ivTTS_PARAM_NAVIGATION_MODE) */
	final public static int ISS_TTS_VOLUME_USE_NORMAL=0	;		/*!< synthesize in the Mode of Normal */
	final public static int ISS_TTS_VOLUME_USE_NAVIGATION=1	;		/*!< synthesize in the Mode of Navigation */
	final public static int ISS_TTS_VOLUME_USE_MOBILE=2		;	/*!< synthesize in the Mode of Mobile */
	final public static int ISS_TTS_VOLUME_USE_EDUCATION=3	;		/*!< synthesize in the Mode of Education */
	final public static int ISS_TTS_VOLUME_USE_TV=4	;		/*!< synthesize in the Mode of TV */

	final public static int ISS_TTS_PARAM_VOLUME_INCREASE=	(0X00000505);		/* volume value increase */
	final public static int ISS_TTS_VOLUME_INCREASE_MIN=0;		/* minimized volume (default) */
	final public static int ISS_TTS_VOLUME_INCREASE_MAX	=10	;	/* maximized volume */

	final public static int ISS_TTS_PARAM_TMP_LOG_DIR  = (0X00000606) ;              /*Set tmp log directory for debugging*/
	
	// Log level.According to the requirement to combine mask.
	final public static int ISS_TTS_PARAM_LOG_LEVEL	= 				0X00000506;
	final public static String ISS_TTS_VOLUME_LOG_LEVEL_ALL	=		"-1";	// all info
	final public static String ISS_TTS_VOLUME_LOG_LEVEL_NONE =		"0";	// none
	final public static String ISS_TTS_VOLUME_LOG_LEVEL_CRIT =		"1";	// critical info
	final public static String ISS_TTS_VOLUME_LOG_LEVEL_ERROR =		"2";	// error info
	final public static String ISS_TTS_VOLUME_LOG_LEVEL_WARNING	=	"4";	// warnint info

	// Log output.According to the requirement to combine mask.
	final public static int ISS_TTS_PARAM_LOG_OUTPUT = 				0X00000507;
	final public static String ISS_TTS_VOLUME_LOG_OUTPUT_NONE =			"0";	// none
	final public static String ISS_TTS_VOLUME_LOG_OUTPUT_FILE =			"1";	// file
	final public static String ISS_TTS_VOLUME_LOG_OUTPUT_CONSOLE =		"2";	// console（except for android）
	final public static String ISS_TTS_VOLUME_LOG_OUTPUT_DEBUGGER =		"4";	// debugger
	final public static String ISS_TTS_VOLUME_LOG_OUTPUT_MSGBOX =		"8";	// message box

	// Log FileName
	final public static int ISS_TTS_PARAM_LOG_FILE_NAME	=(0X00000508);
}