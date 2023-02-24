package com.chinatsp.ifly.voice.platformadapter.utils;

import android.car.CarNotConnectedException;
import android.content.Context;
import android.util.Log;

import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.db.GuideBookDbDao;
import com.chinatsp.ifly.db.entity.GuideBook;
import com.chinatsp.ifly.utils.AppConfig;
import com.chinatsp.ifly.utils.DateUtils;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.ThreadPoolUtils;
import com.chinatsp.ifly.voice.platformadapter.manager.MXSdkManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static android.car.hardware.constant.HVAC.HVAC_ON_REQ;
import static android.car.hardware.constant.VehicleAreaId.SEAT_ROW_1_LEFT;
import static android.car.hardware.hvac.CarHvacManager.ID_HVAC_POWER_ON;

public class GuideBookUtils {
    private static final String TAG = "GuideBookUtils";
    private static GuideBookUtils sInstance;
    private Context mContext;
    private GuideBookUtils(Context context) {
        this.mContext = context;
    }

    public static GuideBookUtils getInstance(Context context) {
        if (sInstance == null) {
            synchronized (GuideBookUtils.class) {
                if (sInstance == null) {
                    sInstance = new GuideBookUtils(context);
                }
            }
        }
        return sInstance;
    }

    public void getGuideTipMessageList(OnCallback callback) {
        String dateStr = getAuthorizedDate();
        Date authorizedDate = DateUtils.converToDate(dateStr);
        //未实名验证
        if (authorizedDate == null) {
            getGuideTipMessages(GuideBook.SCENE_UNACTIVE, callback);
            return;
        }

        Date currentDate = new Date();
        int gapDays = DateUtils.getGapCount(authorizedDate, currentDate);

        if (gapDays < 30) { //已实名验证少于1个月
            getGuideTipMessages(GuideBook.SCENE_NEWER, callback);
        } else if (gapDays > 30 && gapDays < 3 * 30) {//已实名验证大于1个月但小于2个月
            getGuideTipMessages(GuideBook.SCENE_PROFICIENT, callback);
        } else { //已实名验证超过3个月
            getGuideTipMessages(GuideBook.SCENE_ALL, callback);
        }
    }

    public void getGuideTipMessages(final String scene, final OnCallback callback) {

        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                if (GuideBookDbDao.getInstance(mContext).queryAllByScene(scene).size() == 0) {
                    callback.onFail();
                    return;
                }

                //1. 查找指令集
                List<GuideBook> lowGuideBooks = GuideBookDbDao.getInstance(mContext).queryByCondition(scene, GuideBook.PRIORITY_LOW);
                List<GuideBook> normalGuideBooks = GuideBookDbDao.getInstance(mContext).queryByCondition(scene, GuideBook.PRIORITY_NORMAL);
                List<GuideBook> highGuideBooks = new ArrayList<>();

                //正在导航时
                if (isNaving()) {
                    List<GuideBook> navingGuidebooks = GuideBookDbDao.getInstance(mContext).queryByCondition(scene, GuideBook.PRIORITY_NAVING);
                    highGuideBooks.addAll(navingGuidebooks);
                }

                //音乐正在播放时
                if (isMusicGainedFocus()) {
                    List<GuideBook> musicFgGuidebooks = GuideBookDbDao.getInstance(mContext).queryByCondition(scene, GuideBook.PRIORITY_MUSIC_FG);
                    highGuideBooks.addAll(musicFgGuidebooks);
                } else {  //未打开音乐时
                    List<GuideBook> musicBgGuidebooks = GuideBookDbDao.getInstance(mContext).queryByCondition(scene, GuideBook.PRIORITY_MUSIC_BG);
                    normalGuideBooks.addAll(musicBgGuidebooks);
                }

                //电台获取到焦点时
                if (isRadioGainedFocus()) {
                    List<GuideBook> radioFgGuidebooks = GuideBookDbDao.getInstance(mContext).queryByCondition(scene, GuideBook.PRIORITY_RADIO_FG);
                    highGuideBooks.addAll(radioFgGuidebooks);
                }

                //空调打开时
                if (isAirOpen()) {
                    List<GuideBook> airConditionGuidebooks = GuideBookDbDao.getInstance(mContext).queryByCondition(scene, GuideBook.PRIORITY_AIRCONDITION_OPEN);
                    highGuideBooks.addAll(airConditionGuidebooks);
                }

                //有电动后备箱配置
                if (hasBackTrunk()) {
                    List<GuideBook> backTrunkGuidebooks = GuideBookDbDao.getInstance(mContext).queryByCondition(scene, GuideBook.PRIORITY_BACKTRUNK_CONFIG);
                    normalGuideBooks.addAll(backTrunkGuidebooks);
                }

                //2 从普通优先级、低优先级和高优先级中取随机数
                List<GuideBook> selectList = new ArrayList<>();
                GuideBook high;
                GuideBook normal;
                GuideBook low;
                //有高优先级, 高:中:低 = 5:4:1
                Log.d(TAG,"highGuideBooks.size() == " + highGuideBooks.size());
                if (highGuideBooks.size() > 0) {
                    for (int i = 0; i < 5; i++) {
                        high = highGuideBooks.get(new Random().nextInt(highGuideBooks.size()));
                        selectList.add(high);
                    }

                    for (int i = 0; i < 4; i++) {
                        if (normalGuideBooks.size() > 0) {
                            normal = normalGuideBooks.get(new Random().nextInt(normalGuideBooks.size()));
                            selectList.add(normal);
                        }
                    }
                    if (lowGuideBooks.size() > 0) {
                        low = lowGuideBooks.get(new Random().nextInt(lowGuideBooks.size()));
                        selectList.add(low);
                    }
                }
                //无高优先级, 中:低 = 9:1
                else if (selectList.size() == 0) {
                    Log.d(TAG,"selectList.size() == " + selectList.size());
                    for (int i = 0; i < 9; i++) {
                        if (normalGuideBooks.size() > 0) {
                            normal = normalGuideBooks.get(new Random().nextInt(normalGuideBooks.size()));
                            selectList.add(normal);
                        }

                    }
                    if (lowGuideBooks.size() > 0) {
                        low = lowGuideBooks.get(new Random().nextInt(lowGuideBooks.size()));
                        selectList.add(low);
                    }
                }

                //筛选出来的结果
                callback.onSuccess(selectList);
            }
        });
    }

    public void updateUsageCount(GuideBook finalGuide) {
        //3, 更新使用次数
        GuideBookDbDao.getInstance(mContext).updateUsageCountById(finalGuide.id, finalGuide.usageCount += 1);

        //4, 判断某指令如果使用超过30次，降低优先级
        GuideBook book = GuideBookDbDao.getInstance(mContext).queryById(finalGuide.id);
        if(book != null) {
//            if (GuideBook.PRIORITY_NORMAL.equals(book.priority) && book.usageCount > 30) {
//                GuideBookDbDao.getInstance(mContext).updateUsageCountById(book.id, 0);
//                GuideBookDbDao.getInstance(mContext).updatePriorityById(book.id, GuideBook.PRIORITY_LOW);
//            } else if (!GuideBook.PRIORITY_NORMAL.equals(book.priority) && !GuideBook.PRIORITY_LOW.equals(book.priority)
//                    && book.usageCount > 50) {
//                GuideBookDbDao.getInstance(mContext).updateUsageCountById(book.id, 0);
//                GuideBookDbDao.getInstance(mContext).updatePriorityById(book.id, GuideBook.PRIORITY_LOW);
//            } else if (GuideBook.PRIORITY_LOW.equals(book.priority) && book.usageCount > 30) {
//                GuideBookDbDao.getInstance(mContext).deleteById(book.id);
//            }
            if(book.usageCount > 30)
                GuideBookDbDao.getInstance(mContext).updateUsageCountById(book.id, 0);
        } else {
            LogUtils.e(TAG, "queryById happen error, cannot be null");
        }
    }

    //TODO 返回实名验证的字符串yyyy-MM-dd
    private String getAuthorizedDate() {
        LogUtils.d(TAG, "getAuthorizedDate :" + "yy-MM-dd");
        return "";
    }

    /**
     * 空调是否打开
     *
     * @return
     */
    private boolean isAirOpen() {
        try {
            if(AppConfig.INSTANCE.mCarHvacManager != null) {
                int status = AppConfig.INSTANCE.mCarHvacManager.getIntProperty(ID_HVAC_POWER_ON, SEAT_ROW_1_LEFT);
                //HVAC_ON_REQ表示已打开
                if (status == HVAC_ON_REQ) {
                    return true;
                }
            }
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * TODO
     * 有电动后备箱
     *
     * @return
     */
    private boolean hasBackTrunk() {
        return false;
    }

    /**
     * 音乐是否持有焦点
     */
    private boolean isMusicGainedFocus() {
        String curAudioFocus = SharedPreferencesUtils.getString(mContext, AppConstant.KEY_AUDIO_FOCUS_PKGNAME, "");
        return AppConstant.PACKAGE_NAME_MUSIC.equals(curAudioFocus);
    }

    /**
     * 电台是否持有焦点
     */
    private boolean isRadioGainedFocus() {
        String curAudioFocus = SharedPreferencesUtils.getString(mContext, AppConstant.KEY_AUDIO_FOCUS_PKGNAME, "");
        return AppConstant.PACKAGE_NAME_RADIO.equals(curAudioFocus);
    }

    /**
     * 正在导航中
     */
    private boolean isNaving() {
        return MXSdkManager.getInstance(mContext).isNaving();
    }

    public interface OnCallback {
        void onSuccess(List<GuideBook> guideBookList);

        void onFail();
    }
}
