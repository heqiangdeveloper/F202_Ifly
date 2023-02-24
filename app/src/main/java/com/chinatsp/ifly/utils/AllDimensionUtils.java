package com.chinatsp.ifly.utils;
import com.chinatsp.ifly.entity.AllDimension;
import com.chinatsp.ifly.entity.StkCmdBean;
import com.example.mxextend.entity.LocationInfo;
import com.example.mxextend.entity.RouteSummary;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ytkj on 2019/5/24.
 */

public final class AllDimensionUtils {

    public static final String TAG = AllDimensionUtils.class.getName();

    public static final int PAGE_SET_HOMEORCOMP_FRAGMENT_STARTID = 184;

    public static final int PAGE_TEAMTRIP_DESTINATION_FRAGMENT_STARTID = 378;

    public static final int PAGE_SEARCHRESULT_LIST_WXPOSITION_FRAGMENT_STARTID = 295;

    public static final int PAGE_SEARCHRESULT_LIST_ADDHOME_FRAGMENT_STARTID = 235;

    public static final int PAGE_FAVORITE_FRAGMENT_STARTID = 154;

    public static final int PAGE_SEARCHRESULT_LIST_FRAGMENT_STARTID = 46;

    public static final int PAGE_SUGGESTION_FRAGMENT_STARTID = 1700;

    public static final int PAGE_MESSAGE_PUSH_FRAGMENT_STARTID = 671;

    public static final int PAGE_MULTIROUTE_FRAGMENT_STARTID = 5000;

    public static final int HISTORY_SIZE = 2, WXPOSITION_SIZE = 3, FAVORITE_SIZE = 3, SEARCHRESULT_SIZE = 3, SUGGESTION_SIZE = 20, ROUTESUMMARY_SIZE = 6;


    private AllDimensionUtils() {
    }

    //将同一页面的静态指令集和动态指令集合在一起
    public static String getAllStkCmd(List<AllDimension> allDimensions, String stkCmd) {
        String allStkCmd = stkCmd;
        if (allDimensions == null || allDimensions.size() == 0) {
            return allStkCmd;
        }
        StkCmdBean stkCmdBean = GsonUtil.stringToObject(stkCmd, StkCmdBean.class);
        stkCmdBean.getAllDimensions().addAll(allDimensions);
        allStkCmd = GsonUtil.objectToString(stkCmdBean);
        LogUtils.d(TAG, allStkCmd);
        return allStkCmd;
    }

    //获取需要动态添加注册的指令集
    public static List<AllDimension> getAllDimensions(int id, List<LocationInfo> resultList, int size) {
        List<AllDimension> allDimensions = new ArrayList<>(size);

        if (resultList == null || resultList.size() == 0) {
            return allDimensions;
        }

        List<LocationInfo> afterResultList;
        if (resultList.size() > size) {
            afterResultList = resultList.subList(0, size);
        } else {
            afterResultList = resultList;
        }


        for (LocationInfo locationInfo : afterResultList) {
            AllDimension allDimension = getAllDimension(id++, locationInfo);
            allDimensions.add(allDimension);
        }
        return allDimensions;

    }

    //获取需要动态添加注册的指令集
    public static List<AllDimension> getAllDimensionsByRouteSummary(int id, List<RouteSummary> resultList, int size) {
        List<AllDimension> allDimensions = new ArrayList<>(size);

        if (resultList == null || resultList.size() == 0) {
            return allDimensions;
        }

        List<RouteSummary> afterResultList;
        if (resultList.size() > size) {
            afterResultList = resultList.subList(0, size);
        } else {
            afterResultList = resultList;
        }


        for (RouteSummary routeSummary : afterResultList) {
            AllDimension allDimension = getAllDimension(id++, routeSummary);
            allDimensions.add(allDimension);
        }
        return allDimensions;

    }


    private static AllDimension getAllDimension(int id, LocationInfo locationInfo) {
        AllDimension allDimension = new AllDimension();
        if (locationInfo != null) {
            allDimension.setId(id);
            allDimension.setdimension(getDimension(locationInfo));
        }
        return allDimension;
    }

    private static AllDimension getAllDimension(int id, RouteSummary routeSummary) {
        AllDimension allDimension = new AllDimension();
        if (routeSummary != null) {
            allDimension.setId(id);
            allDimension.setdimension(getDimension(routeSummary));
        }
        return allDimension;
    }

    private static List<AllDimension.DimensionBean> getDimension(RouteSummary routeSummary) {
        List<AllDimension.DimensionBean> dimensions = new ArrayList<>();
        if (routeSummary != null) {
            AllDimension.DimensionBean dimensionBean = new AllDimension.DimensionBean();
            dimensionBean.setVal(routeSummary.getRouteDesc());
            dimensions.add(dimensionBean);
        }
        return dimensions;
    }

    private static List<AllDimension.DimensionBean> getDimension(LocationInfo locationInfo) {
        List<AllDimension.DimensionBean> dimensions = new ArrayList<>();
        if (locationInfo != null) {
            AllDimension.DimensionBean dimensionBean = new AllDimension.DimensionBean();
            dimensionBean.setVal(locationInfo.getName());
            dimensions.add(dimensionBean);
        }
        return dimensions;
    }

}
