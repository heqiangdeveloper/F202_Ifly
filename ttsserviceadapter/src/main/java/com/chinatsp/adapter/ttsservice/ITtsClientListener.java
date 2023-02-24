//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.chinatsp.adapter.ttsservice;

public interface ITtsClientListener {
  void onPlayBegin();

  void onPlayCompleted();

  void onPlayInterrupted();

  void onProgressReturn(int textIndex, int textLength);

  void onTtsInited(boolean state, int errId);
}
