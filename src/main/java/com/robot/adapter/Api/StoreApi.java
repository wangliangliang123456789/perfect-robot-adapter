package com.robot.adapter.Api;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;


public interface StoreApi {


    /**
     * 压货单明细查询
     *
     * @param shopNo   网点编号
     * @param duration 开单日期
     * @return
     */

    public JSONObject selectMortgageInfo(String shopNo, String duration);



    /**
     * 核对产品库存
     * 有问题  待修改
     *
     * @param shopNo   网点编号
     * @param itemCode 产品编码
     * @return
     */

    public JSONObject queryItemStorageByList(String shopNo, String itemCode);

    /**
     * 押货保证金最大限额
     *
     * @param shopNo 网点编号
     * @return
     */

    public JSONObject maxQuota(String shopNo);

    /**
     * 查询用户电子礼券使用情况
     * @param duration 时间段
     * @param cardCode 会员卡号
     * @param typeDescription 券类型
     * @param statusDescription 使用状态
     * @return
     */

    public JSONObject getElectronicCoupnGrantByStatus(String duration, String cardCode
            , String typeDescription, String statusDescription);

    /**
     * 个人实时购货明细及积分
     *
     * @param cardCode 卡号
     * @param duration    月份
     * @return
     */

    public JSONObject queryTradeItems(String cardCode, String duration,Integer size);


    /**
     * 上月顾客报单查询
     *
     * @param cardCode 卡号
     * @param duration 月份
     * @return
     */

    public JSONObject queryOrderListForCrm(String cardCode, String duration);


}