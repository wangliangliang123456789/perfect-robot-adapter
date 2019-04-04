package com.robot.adapter.Api;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;

/**
 * 核心发放
 *
 * @author 亮亮
 */
@Api(tags = "核心发放", description = "核心发放")
public interface CoreIssueApi {


    public JSONObject queryMonthStatusForIncome( String duration
            ,String date);

    /**
     * 12 号场景 收入的金额明细
     *
     * @param date   yyyy-MM月份
     * @param cardCode 卡号
     * @return
     */

    public JSONObject getIncomeDetails( String date,String cardCode);

    /**
     * 13号场景 收入发放情况
     *
     * @param cardCode 卡号
     * @param date  yyyy-MM 月份
     * @return
     */

    public JSONObject incomeDistributionInformation(String date, String cardCode);

    /**
     * 14 号场景 劳务收入的汇退情况
     *
     * @param date  yyyy-MM 月份
     * @param cardCode 卡号
     * @return
     */

    public JSONObject getRefundLaborServicesInformation(String date,String cardCode);

    /**
     * 16号场景 发票余款额度查询
     *
     * @param date  yyyy-MM    月份
     * @param cardCode 卡号
     * @return
     */

    public JSONObject getCheckBalanceInvoice(String date,  String cardCode);
}
