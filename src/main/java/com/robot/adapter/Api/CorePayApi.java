package com.robot.adapter.Api;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * 核心计酬
 *
 * @author 亮亮
 */

public interface CorePayApi {


    /**
     * 小组累计分
     *
     * @return
     */

    public JSONObject getGroupPoint(String cardCode, String date);

    /**
     * 2号场景 月结积分
     *
     * @param distNo 卡号
     * @param date   月份  201211
     * @return
     */

    public JSONObject getMonthIntegral( String cardCode, String duration);

    /**
     * 6号 ，7 号接口 持续进步奖的分值
     *
     * @param disNo    会员卡号
     * @param duration 时间段
     * @return
     */

    public JSONObject getCountPassNum( String cardCode, String duration);
}
