package com.chinatsp.ifly.video;

/**
 * Created by ytkj on 2018/10/30.
 */

public class MediaConstantsDef {
    public MediaConstantsDef() {
    }

    public static enum PLAY_STATE {
        IDLE(-1),
        PREPARED(0),
        STOP(1),
        PLAY(2),
        PAUSE(3);

        int value;

        private PLAY_STATE(int v) {
            this.value = v;
        }
    }

    public static enum SCAN_STATUS {
        IDLE,
        SCANING,
        SCAN_FINISH;

        private SCAN_STATUS() {
        }
    }

    public static enum MEDIA_TYPE {
        IMAGE,
        INVALID,
        MUSIC,
        VIDEO;

        private MEDIA_TYPE() {
        }
    }

    public static enum SHUFFLE_MODE {
        SHUFFLE_OFF,
        SHUFFLE_ON;

        private SHUFFLE_MODE() {
        }
    }

    public static enum REPEAT_MODE {
        REPEAT_ALL,
        REPEAT_FOLDER,
        REPEAT_OFF,
        REPEAT_ONE;

        private REPEAT_MODE() {
        }
    }

    public static enum PLAY_MODE {
        REPEAT_ALL,
        REPEAT_FOLDER,
        REPEAT_ONE,
        SHUFFLE_ON;

        private PLAY_MODE() {
        }
    }
}
