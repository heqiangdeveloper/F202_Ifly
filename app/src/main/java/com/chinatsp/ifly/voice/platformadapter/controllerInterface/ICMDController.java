package com.chinatsp.ifly.voice.platformadapter.controllerInterface;

import com.chinatsp.ifly.voice.platformadapter.entity.IntentEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.MvwLParamEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.StkResultEntity;
import com.iflytek.adapter.controllerInterface.IController;

/**
 * 命令操作相关的接口
 * @author yys
 *
 */
public interface ICMDController extends IController<IntentEntity, MvwLParamEntity, StkResultEntity> {

	void showAssistant(int whichName);

	void goToHome();

	void exit();

	void stopTTS();
}
