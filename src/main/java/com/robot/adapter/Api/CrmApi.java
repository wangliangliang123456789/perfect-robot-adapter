package com.robot.adapter.Api;

import com.alibaba.fastjson.JSONObject;

/**
 * @ClassName
 * @Description: crm
 * @Author 亮亮
 * @Date 2019/1/14  17:21
 * @Version
 **/

public interface CrmApi {


    /**
     * 新增_通用积分状态
     *
     * @return
     */

    public JSONObject queryMonthStatusForIncome(String duration, String date);

    /**
     * 新增_md8_md16专用积分状态
     *
     * @return
     */

    public JSONObject queryIntergralDurationStatus(String duration, String date);



    /**
     * 查询葡萄酒积分
     *
     * @param duration        时间段
     * @param cardCode        卡号
     * @param typeDescription 葡萄酒类型
     * @return
     */

    public JSONObject getWineByManyMonth(String duration, String cardCode, String typeDescription);

    /**
     * 健康食品积分
     *
     * @param duration 时间段
     * @param cardCode 卡号
     * @return
     */

    public JSONObject getHealthFoodIntegralByByMany(String duration, String cardCode);

    /**
     * 玛丽艳积分
     *
     * @param duration 时间段
     * @param cardCode 卡号
     * @return
     */

    public JSONObject getMarieIntegralByByMany(String duration, String cardCode);

    /**
     * md_查询会员是否有密码
     *
     * @param cardCode 卡号
     * @return
     */

    public JSONObject getPwdStatus(String cardCode);

    /**
     * md_会员密码验证
     *
     * @param cardCode 卡号
     * @param pwd      密码
     * @return
     */

    public JSONObject getUserPassword(String cardCode, String pwd);

    /**
     * 修改密码
     *
     * @param cardCode     卡号
     * @param pwd          密码
     * @param channel      来源渠道：(商城 S，核心 H ， 客服 K ， CRM C )
     * @param createPerson 修改人
     * @return
     */

    public JSONObject updateUserPassword(String cardCode, String pwd, String channel, String createPerson);

    /**
     * 保险对象、生效/失效月份
     *
     * @param cardCode 卡号
     * @return
     */

    public JSONObject queryInsuranceStartOrEndTime(String cardCode);

    /**
     * 查询多月等级
     *
     * @param cardCode 卡号
     * @param duration 时间段
     * @return
     */

    public JSONObject getLevels(String cardCode, String duration);

    /**
     * 发送登录提示短信
     *
     * @param tel      手机号
     * @param cardCode 会员卡号
     * @return
     */

    public JSONObject sendLoginingSms(String tel, String cardCode);

    /**
     * md_(身份证校验)会员卡/优惠卡状态
     *
     * @param identityCard 身份证
     * @param userName   姓名
     * @return
     */

    public JSONObject getMemberStatusWithID(String identityCard, String userName);

    /**
     * md_会员卡/优惠卡状态
     *
     * @param cardCode 会员卡号
     * @return
     */

    public JSONObject getMemberStatus(String cardCode);

    /**
     * 短信
     *
     * @param tel     手机号
     * @param context 短信内容
     * @return
     */

    public JSONObject sendSms(String tel, String context);
}
