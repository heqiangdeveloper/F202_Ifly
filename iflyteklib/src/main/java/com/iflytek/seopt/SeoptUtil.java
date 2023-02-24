package com.iflytek.seopt;

import android.util.Log;

import com.iflytek.mvw.MvwSession;
import com.iflytek.speech.NativeHandle;
import com.iflytek.speech.libissseopt;
import com.iflytek.sr.SrSession;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SeoptUtil {

    private final static String TAG = "xyj_SeoptUtil";

    /**
     * 获得定位方向
     *
     * @param lParamLeft  左路唤醒实例的数据
     * @param lParamRight 右路唤醒实例的数据
     * @return
     */
    public static String getDecDirection(String lParamLeft, String lParamRight) {
        try {
            //得分高的唤醒实例的得分
            int maxScoreScore;
            // 得分高的唤醒的能量
            double maxScorePower;
            //能量高的唤醒的得分
            int maxPowerScore;
            //能量高的唤醒的能量
            double maxPowerPower;
            // 得分较高的唤醒实例波束方向
            String maxScoreDirection;
            // 能量较高的唤醒实例波束方向
            String maxPowerDirection;
            String scoreKey = "nMvwScore";
            String powerKey = "PowerValue";
            JSONObject lParamLeftObj = new JSONObject(lParamLeft);
            int leftScore = lParamLeftObj.getInt(scoreKey);
            double leftPower = lParamLeftObj.getDouble(powerKey);
            JSONObject lParamRightObj = new JSONObject(lParamRight);
            int rightScore = lParamRightObj.getInt(scoreKey);
            double rightPower = lParamRightObj.getDouble(powerKey);
            if (leftScore > rightScore) {
                maxScoreScore = leftScore;
                maxScorePower = leftPower;
                maxScoreDirection = libissseopt.ISS_SEOPT_PARAM_BEAM_INDEX_VALUE_LEFT;
            } else {
                maxScoreScore = rightScore;
                maxScorePower = rightPower;
                maxScoreDirection = libissseopt.ISS_SEOPT_PARAM_BEAM_INDEX_VALUE_RIGTHT;
            }
            if (leftPower > rightPower) {
                maxPowerScore = leftScore;
                maxPowerPower = leftPower;
                maxPowerDirection = libissseopt.ISS_SEOPT_PARAM_BEAM_INDEX_VALUE_LEFT;
            } else {
                maxPowerScore = rightScore;
                maxPowerPower = rightPower;
                maxPowerDirection = libissseopt.ISS_SEOPT_PARAM_BEAM_INDEX_VALUE_RIGTHT;
            }

            float scoreDiffNormal = Math.abs((0.01f + maxScoreScore - maxPowerScore) / (0.1f + maxScoreScore));
            double powerDiffNormal = Math.abs((0.01f + maxPowerPower - maxScorePower) / (0.1f + maxPowerPower));
            Log.i(TAG, "scoreDiffNormal = " + scoreDiffNormal);
            Log.i(TAG, "powerDiffNormal = " + powerDiffNormal);
            if (scoreDiffNormal > 0.25f && powerDiffNormal < 0.15f) {
                return maxScoreDirection;
            } else {
                return maxPowerDirection;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 窄波束送入识别
     *
     * @param bufferFileLeft
     * @param bufferFileRight
     * @param sizeFileLeft
     */
    public static void appendSrData(NativeHandle phISSSeopt, SrSession srSession, byte[] bufferFileLeft, byte[] bufferFileRight,
                                    int sizeFileLeft) {
        int err = 0;
        try {
            long baseTime1 = System.currentTimeMillis();
            long inputSize = 0;
            long pcmTime = 0;
            long passTime = 0;
            for (int curPosition = 0; curPosition + libissseopt.ISS_SEOPT_FRAME_SHIFT * 2 <= sizeFileLeft; curPosition += libissseopt.ISS_SEOPT_FRAME_SHIFT * 2) {
                byte[] bufferIn = new byte[libissseopt.ISS_SEOPT_FRAME_SHIFT * 2 * 2];
                byte[] bufferOut = new byte[libissseopt.ISS_SEOPT_FRAME_SHIFT * 2 * 4];
                int[] sampleOut = new int[2];
                for (int j = 0; j < libissseopt.ISS_SEOPT_FRAME_SHIFT; j++) {
                    bufferIn[j * 4] = bufferFileLeft[j * 2 + curPosition];
                    bufferIn[j * 4 + 1] = bufferFileLeft[j * 2 + 1 + curPosition];
                    bufferIn[j * 4 + 2] = bufferFileRight[j * 2 + curPosition];
                    bufferIn[j * 4 + 3] = bufferFileRight[j * 2 + 1 + curPosition];
                }
                err = libissseopt.process(phISSSeopt,
                        bufferIn, (libissseopt.ISS_SEOPT_FRAME_SHIFT), bufferOut, sampleOut);
                byte[] buffer2Sr = new byte[libissseopt.ISS_SEOPT_FRAME_SHIFT * 2 * 2];
                for (int index = 0; index < sampleOut[0]; index++) {
                    buffer2Sr[index * 4] = bufferOut[index * 8 + 4];
                    buffer2Sr[index * 4 + 1] = bufferOut[index * 8 + 5];
                    buffer2Sr[index * 4 + 2] = bufferOut[index * 8 + 6];
                    buffer2Sr[index * 4 + 3] = bufferOut[index * 8 + 7];
                }
                srSession.appendAudioData(buffer2Sr, sampleOut[0] * 4);
                try {
                    inputSize += (sampleOut[0]) * 2;
                    pcmTime = inputSize / 32;
                    passTime = System.currentTimeMillis() - baseTime1;
                    if (pcmTime > passTime) {
                        Thread.sleep(pcmTime - passTime);
                    }
                } catch (InterruptedException e) {
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "e == " + e);
        }
    }


    /**
     * 窄波束送入唤醒
     *
     * @param bufferFileLeft
     * @param bufferFileRight
     * @param sizeFileLeft
     * @param mvwLeft
     * @param mvwRight
     */
    public static void appendMvwData(NativeHandle phISSSeopt, final byte[] bufferFileLeft, byte[] bufferFileRight,
                                     final int sizeFileLeft, MvwSession mvwLeft, MvwSession mvwRight) {
        int err = 0;
        try {
            int curPosition = 0;
            long baseTime = System.currentTimeMillis();
            long inputSize = 0;
            long pcmTime = 0;
            long passTime = 0;

            for (; curPosition + libissseopt.ISS_SEOPT_FRAME_SHIFT * 2 <= sizeFileLeft; curPosition += libissseopt.ISS_SEOPT_FRAME_SHIFT * 2) {
                byte[] bufferIn = new byte[libissseopt.ISS_SEOPT_FRAME_SHIFT * 2 * 2];
                byte[] bufferOut = new byte[libissseopt.ISS_SEOPT_FRAME_SHIFT * 2 * 4];
                int[] sampleOut = new int[2];

                for (int j = 0; j < libissseopt.ISS_SEOPT_FRAME_SHIFT; j++) {
                    bufferIn[j * 4] = bufferFileLeft[j * 2 + curPosition];
                    bufferIn[j * 4 + 1] = bufferFileLeft[j * 2 + 1 + curPosition];
                    bufferIn[j * 4 + 2] = bufferFileRight[j * 2 + curPosition];
                    bufferIn[j * 4 + 3] = bufferFileRight[j * 2 + 1 + curPosition];
                }

                err = libissseopt.process(phISSSeopt, bufferIn, (libissseopt.ISS_SEOPT_FRAME_SHIFT), bufferOut, sampleOut);
                byte[] bufferLeft = new byte[libissseopt.ISS_SEOPT_FRAME_SHIFT * 2];
                byte[] bufferRight = new byte[libissseopt.ISS_SEOPT_FRAME_SHIFT * 2];
                for (int index = 0; index < sampleOut[0]; index++) {
                    bufferLeft[index * 2] = bufferOut[index * 8];
                    bufferLeft[index * 2 + 1] = bufferOut[index * 8 + 1];
                    bufferRight[index * 2] = bufferOut[index * 8 + 2];
                    bufferRight[index * 2 + 1] = bufferOut[index * 8 + 3];
                }
                mvwLeft.appendAudioData(bufferLeft);
                mvwRight.appendAudioData(bufferRight);
                try {
                    inputSize += (sampleOut[0]) * 2;
                    pcmTime = inputSize / 32;
                    passTime = System.currentTimeMillis() - baseTime;
                    if (pcmTime > passTime) {
                        Thread.sleep(pcmTime - passTime);
                    }
                } catch (InterruptedException e) {
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "e == " + e);
        }
    }

    /**
     * 窄波束送入唤醒和识别
     *
     * @param bufferFileLeft
     * @param bufferFileRight
     * @param sizeFileLeft
     */
    public static void appendMvwSrData(NativeHandle phISSSeopt, SrSession srSession, byte[] bufferFileLeft, byte[] bufferFileRight,
                                       int sizeFileLeft, MvwSession mvwLeft,
                                       MvwSession mvwRight, int status) {
        int err = 0;

        try {
            int curPosition = 0;
            Date baseTime = null;

            if (bufferFileLeft[0] == (byte) 'R' && bufferFileLeft[1] == (byte) 'I' && bufferFileLeft[2] == 'F' && bufferFileLeft[3] == 'F' &&
                    bufferFileRight[0] == (byte) 'R' && bufferFileRight[1] == (byte) 'I' && bufferFileRight[2] == 'F' && bufferFileRight[3] == 'F') {
                curPosition += 44;
            } else {
            }
            SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
            baseTime = dfs.parse(dfs.format(new Date()));
            long inputSize = 0;
            long pcmTime = 0;
            long passTime = 0;

            for (; curPosition < sizeFileLeft; curPosition += libissseopt.ISS_SEOPT_FRAME_SHIFT * 2) {
                byte[] bufferIn = new byte[libissseopt.ISS_SEOPT_FRAME_SHIFT * 2 * 2];
                byte[] bufferOut = new byte[libissseopt.ISS_SEOPT_FRAME_SHIFT * 2 * 4];
                int[] sampleOut = new int[2];

                for (int j = 0; j < libissseopt.ISS_SEOPT_FRAME_SHIFT; j++) {
                    bufferIn[j * 4] = bufferFileLeft[j * 2 + curPosition];
                    bufferIn[j * 4 + 1] = bufferFileLeft[j * 2 + 1 + curPosition];
                    bufferIn[j * 4 + 2] = bufferFileRight[j * 2 + curPosition];
                    bufferIn[j * 4 + 3] = bufferFileRight[j * 2 + 1 + curPosition];
                }

                err = libissseopt.process(phISSSeopt, bufferIn, (libissseopt.ISS_SEOPT_FRAME_SHIFT), bufferOut, sampleOut);

                byte[] bufferLeft = new byte[libissseopt.ISS_SEOPT_FRAME_SHIFT * 2];
                byte[] bufferRight = new byte[libissseopt.ISS_SEOPT_FRAME_SHIFT * 2];
                byte[] buffer2Sr = new byte[libissseopt.ISS_SEOPT_FRAME_SHIFT * 2 * 2];
                for (int index = 0; index < sampleOut[0]; index++) {
                    bufferLeft[index * 2] = bufferOut[index * 8];
                    bufferLeft[index * 2 + 1] = bufferOut[index * 8 + 1];
                    bufferRight[index * 2] = bufferOut[index * 8 + 2];
                    bufferRight[index * 2 + 1] = bufferOut[index * 8 + 3];
                    buffer2Sr[index * 4] = bufferOut[index * 8 + 4];
                    buffer2Sr[index * 4 + 1] = bufferOut[index * 8 + 5];
                    buffer2Sr[index * 4 + 2] = bufferOut[index * 8 + 6];
                    buffer2Sr[index * 4 + 3] = bufferOut[index * 8 + 7];
                }

//                libisssr.appendAudioData(buffer2Sr, sampleOut[0] * 4);

                mvwLeft.appendAudioData(bufferLeft);
                mvwRight.appendAudioData(bufferRight);

                srSession.appendAudioData(buffer2Sr, sampleOut[0] * 4);

                try {
                    inputSize += (sampleOut[0]) * 2;
                    pcmTime = inputSize / 32;
                    passTime = dfs.parse(dfs.format(new Date())).getTime() - baseTime.getTime();
                    if (pcmTime > passTime) {
                        Thread.sleep(pcmTime - passTime);
                    }
                } catch (InterruptedException e) {
                }
            }
//            libisssr.appendAudioData(buffer2SrEnd, libissseopt.ISS_SEOPT_FRAME_SHIFT * 2 * 80);
//            seSession.appendAudioData(buffer2SrEnd, libissseopt.ISS_SEOPT_FRAME_SHIFT * 2 * 80);
//
//            Log.i(TAG, buffer2SrEnd.toString());
        } catch (Exception e) {
            Log.e(TAG, "e == " + e);
        }
    }

    /**
     * 将data拆分为左右声道
     *
     * @param data
     * @return
     */
    public static byte[][] splitStereoPcm(byte[] data) {
        if (data == null) {
            Log.w(TAG, "data == null");
            return new byte[0][0];
        }
        int monoLength = data.length / 2;
        byte[] leftByte = new byte[monoLength];
        byte[] rightByte = new byte[monoLength];
        byte[][] bytes = new byte[2][monoLength];
        for (int i = 0; i < monoLength; i++) {
            if (i % 2 == 0) {
                System.arraycopy(data, i * 2, leftByte, i, 2);
            } else {
                System.arraycopy(data, i * 2, rightByte, i - 1, 2);
            }
        }
        bytes[0] = leftByte;
        bytes[1] = rightByte;
        return bytes;
    }


    //单声道数据转双声道
    public static byte[] byteMerger(byte[] byte_1) {
        byte[] byte_2 = new byte[byte_1.length * 2];
        for (int i = 0; i < byte_1.length; i++) {
            if (i % 2 == 0) {
                byte_2[2 * i] = byte_1[i];
                byte_2[2 * i + 1] = byte_1[i + 1];
            } else {
                byte_2[2 * i] = byte_1[i - 1];
                byte_2[2 * i + 1] = byte_1[i];
            }
        }
        return byte_2;
    }

    public static byte[][] getSeoptByte1(byte[] oldCacheBuffer, byte[] buffer) {
        byte[] srBuffer;
        byte[] newCacheBuffer;
        if (oldCacheBuffer == null) {
            newCacheBuffer = new byte[buffer.length];
            System.arraycopy(buffer, 0, newCacheBuffer, 0, buffer.length);
        } else {
            byte[] localBuffer = new byte[oldCacheBuffer.length + buffer.length];
            System.arraycopy(oldCacheBuffer, 0, localBuffer, 0, oldCacheBuffer.length);
            System.arraycopy(buffer, 0, localBuffer, oldCacheBuffer.length, buffer.length);
            newCacheBuffer = new byte[localBuffer.length];
            System.arraycopy(localBuffer, 0, newCacheBuffer, 0, localBuffer.length);
        }
        int length = newCacheBuffer.length % 1024;
        if (length != 0) {
            srBuffer = new byte[newCacheBuffer.length - length];
            System.arraycopy(newCacheBuffer, 0, srBuffer, 0, srBuffer.length);
            byte[] localBuffer = new byte[length];
            System.arraycopy(newCacheBuffer, srBuffer.length, localBuffer, 0, length);
            newCacheBuffer = new byte[length];
            System.arraycopy(localBuffer, 0, newCacheBuffer, 0, length);
        } else {
            srBuffer = new byte[newCacheBuffer.length];
            System.arraycopy(newCacheBuffer, 0, srBuffer, 0, srBuffer.length);
            newCacheBuffer = null;
        }
        byte[][] allBytes = new byte[2][];
        allBytes[0] = newCacheBuffer;
        allBytes[1] = srBuffer;
        return allBytes;
    }
}
