package com.robot.adapter.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.perfect.center.shop.api.query.IPerfectServiceCenterQueryApi;
import com.perfect.third.integration.api.query.robot.ICoreIssueApi;
import com.robot.adapter.util.ResultUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 核心发放
 *
 * @author 亮亮
 */
@Controller
@ResponseBody
@RequestMapping("/adapter/coreIssue")
@Api(tags = {"核心发放"})
@SuppressWarnings("all")
public class CoreIssueController  {


    Logger logger = LoggerFactory.getLogger(CoreIssueController.class);
    /**
     * 根据网点编号查询网点详细信息
     */
    @Autowired
    private IPerfectServiceCenterQueryApi iPerfectServiceCenterQueryApi;
    @Value("${PAGESIZE}")
    private Integer pageSize;

    @Autowired
    private ICoreIssueApi coreIssueApi;
    /**
     * 系统结算月份
     */
    @Autowired
    private CrmController crmController;

    /**
     * 新增_通用收入结算状态
     *
     * @return
     */
    @ApiOperation(value = "收入结算(通用)", tags = {"md_共用"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "duration", value = "时间段", required = false,dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "date", value = "日期", required = false,dataType = "String", paramType = "query")
    })
    @GetMapping(value = "/userInfo/queryMonthStatusForIncome")
    public JSONObject queryMonthStatusForIncome(@RequestParam(name = "duration", required = false) String duration,
                                                @RequestParam(name = "date", required = false) String date) {
        //系统最新结算月
        try {
            Date systemSettlementMonth = crmController.getNewestTime();
            if (systemSettlementMonth == null) {
                logger.error("获取不到系统结算时间");
                return ResultUtil.error("0404", "获取不到系统结算时间");
            }
            Map<String, Integer> map = new HashMap<>();
            map.put("systemSettlementMonth", Integer.parseInt(
                    new SimpleDateFormat("yyyyMM").format(systemSettlementMonth)));
            logger.info(JSON.toJSONString(map));
            if (duration != null && !"".equals(duration)) {
                String[] split = duration.split("~");
                String startMonth = split[0].substring(0, 6);
                String endMonth = split[1].substring(0, 6);
                map.put("startMonth", Integer.parseInt(startMonth));
                map.put("endMonth", Integer.parseInt(endMonth));
                logger.info(JSON.toJSONString(map));
            } else if (date != null && !"".equals(date)) {
                String startMonth = date.substring(0, 6);
                map.put("startMonth", Integer.parseInt(startMonth));
                map.put("endMonth", Integer.parseInt(startMonth));
                logger.info(JSON.toJSONString(map));
            }
            Integer startMonth = map.get("startMonth");
            Integer endMonth = map.get("endMonth");
            Integer systemSettlementMonth1 = map.get("systemSettlementMonth");
            logger.info("startMonth" + startMonth);
            logger.info("endMonth" + endMonth);
            logger.info("systemSettlementMonth1" + systemSettlementMonth1);
            if (startMonth > systemSettlementMonth1) {
                map.put("status", 0001);
                return ResultUtil.setAll("0001", "单月,多月未核算", map);
            }
            if (startMonth.equals(endMonth)) {
                if (startMonth <= systemSettlementMonth1) {
                    map.put("status", 0002);
                    return ResultUtil.setAll("0002", "单月已核算", map);
                }
            } else if (startMonth < endMonth) {
                if (startMonth <= systemSettlementMonth1) {
                    map.put("status", 0000);
                    return ResultUtil.setAll("0000", "多月已核算", map);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.error("0404", "调用失败");
        }
        logger.info("判断失败");
        return ResultUtil.error("0404", "调用失败");
    }

    /**
     * 12 号场景 收入的金额明细
     *
     * @param month  yyyy-MM月份
     * @param distNo 卡号
     * @return
     */
    @ApiOperation(value = "收入的金额明细", tags = {"md_收入金额明细"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "date", value = "日期", required = true,dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "cardCode", value = "会员卡号", required = true,dataType = "String", paramType = "query"),
    })
    @GetMapping(value = "/userInfo/getIncomeDetails")
    public JSONObject getIncomeDetails(String date, @RequestParam("cardCode") String distNo) {
        String startDate = "";
        Date date1 = new Date();

        try {
            Date yyyyMMdd = new SimpleDateFormat("yyyyMMdd").parse(date);
            startDate = new SimpleDateFormat("yyyy-MM").format(yyyyMMdd);
            JSONObject resultMap = coreIssueApi.getIncomeDetails(startDate, distNo, 1, pageSize);
//            JSONObject resultMap=JSON.parseObject(str);
            logger.info("返回数据：" + JSON.toJSONString(resultMap));
            if (!"200".equals(resultMap.getString("resultCode"))) {
                return ResultUtil.error("0404", "错误信息");
            }
            JSONArray data = resultMap.getJSONArray("data");
            logger.info("返回数据类型：" + data.getClass().getTypeName());
            if (data == null || data.size() == 0) {
                return ResultUtil.error("0000", "无数据");
            }
            JSONObject jsonObject = data.getJSONObject(0);
            //直销员劳务费 最后出款日期
            Date directSellerDateOfFinalPayment = jsonObject.getDate("directSellerDateOfFinalPayment");
            logger.info("直销员劳务费 最后出款日期" + JSON.toJSONString(directSellerDateOfFinalPayment, SerializerFeature.WriteDateUseDateFormat));
            //人才劳务费 最后出款日期
            Date personnelSellerDateOfFinalPayment = jsonObject.getDate("personnelSellerDateOfFinalPayment");
            logger.info("人才劳务费 最后出款日期" + JSON.toJSONString(personnelSellerDateOfFinalPayment, SerializerFeature.WriteDateUseDateFormat));
            if (directSellerDateOfFinalPayment == null && personnelSellerDateOfFinalPayment == null) {
                return ResultUtil.setAll("0001", "有数据，直销劳务费+人才劳务费均无最后出款时间", jsonObject);
            } else if (directSellerDateOfFinalPayment != null && personnelSellerDateOfFinalPayment != null) {
                long newDate = directSellerDateOfFinalPayment.getTime() - personnelSellerDateOfFinalPayment.getTime();
                if (newDate == 0 || newDate > 0) {
                    logger.info("当前日期：" + new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(date1) + "最后出款日期：" + JSON.toJSONString(directSellerDateOfFinalPayment, SerializerFeature.WriteDateUseDateFormat));
                    if ((date1.getTime() - directSellerDateOfFinalPayment.getTime()) / (1000 * 60 * 60 * 24) <= 9) {

                        return ResultUtil.setAll("0002", "，当前日期-最后出款时间<=9", jsonObject);
                    } else {
                        return ResultUtil.setAll("0003", "当前日期-最后出款时间>9", jsonObject);
                    }
                } else {
                    logger.info("当前日期：" + new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(date1) + "最后出款日期：" + JSON.toJSONString(personnelSellerDateOfFinalPayment, SerializerFeature.WriteDateUseDateFormat));
                    if ((date1.getTime() - personnelSellerDateOfFinalPayment.getTime()) / (1000 * 60 * 60 * 24) <= 9) {

                        return ResultUtil.setAll("0002", "当前日期-最后出款时间<=9", jsonObject);
                    } else {
                        return ResultUtil.setAll("0003", "当前日期-最后出款时间>9", jsonObject);
                    }
                }
            } else if (directSellerDateOfFinalPayment != null || personnelSellerDateOfFinalPayment != null) {
                if (directSellerDateOfFinalPayment == null) {
                    logger.info("当前日期：" + new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(date1) + "最后出款日期：" + JSON.toJSONString(personnelSellerDateOfFinalPayment, SerializerFeature.WriteDateUseDateFormat));
                    if ((date1.getTime() - personnelSellerDateOfFinalPayment.getTime()) / (1000 * 60 * 60 * 24) <= 9) {

                        return ResultUtil.setAll("0002", "", jsonObject);
                    } else {
                        return ResultUtil.setAll("0003", "当前日期-最后出款时间>9", jsonObject);
                    }
                } else if (personnelSellerDateOfFinalPayment == null) {
                    logger.info("当前日期：" + new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(date1) + "最后出款日期：" + JSON.toJSONString(directSellerDateOfFinalPayment, SerializerFeature.WriteDateUseDateFormat));
                    if ((date1.getTime() - directSellerDateOfFinalPayment.getTime()) / (1000 * 60 * 60 * 24) <= 9) {

                        return ResultUtil.setAll("0002", "", jsonObject);
                    } else {
                        return ResultUtil.setAll("0003", "当前日期-最后出款时间>9", jsonObject);
                    }
                }
            }
            logger.error("没有经过判断，业务出错");
            return ResultUtil.setAll("0000", "无数据", null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.error("0404", e.getMessage());
        }
    }

    /**
     * 13号场景 收入发放情况
     *
     * @param distNo 卡号
     * @param month  yyyy-MM 月份
     * @return
     */
    @ApiOperation(value = "收入发放情况", tags = {"md_收入发放情况（日期）"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "date", value = "日期", required = true,dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "cardCode", value = "会员卡号", required = true,dataType = "String", paramType = "query"),
    })
    @GetMapping(value = "/userInfo/getIncomeDistributionInformation")
    public JSONObject incomeDistributionInformation(String date, @RequestParam("cardCode") String distNo) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM");
        try {
            String startDate = sdf1.format(sdf.parse(date));
            logger.info("转换日期：" + startDate);
            JSONObject resultMap = coreIssueApi.incomeDistributionInformation(startDate, distNo);
            logger.info("核心发放 返回数据 ：" + JSON.toJSONString(resultMap));
            if (!"200".equals(resultMap.getString("resultCode"))) {
                return ResultUtil.error("0404", "调用失败");
            }
            JSONArray details = resultMap.getJSONArray("data");
            if (details == null || details.size() == 0) {
                return ResultUtil.error("0001", "无数据");
            }
            for (int i = 0; i < details.size(); i++) {
                details.getJSONObject(i).put("actualSendAmount", details.getJSONObject(i).getString("netAmount"));
                details.getJSONObject(i).put("grantDate", details.getJSONObject(i).getString("grantTime"));
            }
            return ResultUtil.resultOK(details, "data");
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.error("0404", "调用失败");
        }

    }

    /**
     * 14 号场景 劳务收入的汇退情况
     *
     * @param month  yyyy-MM 月份
     * @param distNo 卡号
     * @return
     */
    @ApiOperation(value = "劳务收入的汇退情况", tags = {"md_劳务收入的汇退情况(日期)"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "date", value = "日期", required = true,dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "cardCode", value = "会员卡号", required = true,dataType = "String", paramType = "query"),
    })
    @GetMapping(value = "/userInfo/getRefundLaborServicesInformationWithDate")
    public JSONObject getRefundLaborServicesInformation(@RequestParam("date") String month, @RequestParam("cardCode") String distNo) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM");
        try {
            Date parse = sdf.parse(month);
            JSONObject resultMap = coreIssueApi.getRefundLaborServicesInformation(sdf1.format(parse), distNo, 1, pageSize);
            logger.info("返回数据：" + JSON.toJSONString(resultMap, true));
            if (!"200".equals((String) resultMap.get("resultCode"))) {
                logger.error("调用失败" + JSON.toJSONString(resultMap));
                return ResultUtil.error("0404", "调用失败");
            }
            JSONArray resultData = resultMap.getJSONArray("data");
            if (resultData == null || resultData.size() == 0) {
                return ResultUtil.error("0001", "无数据");
            }
            return ResultUtil.resultOK(resultData, "data");
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.error("0404", "调用失败");
        }
    }

    /**
     * 16号场景 发票余款额度查询
     *
     * @param month  yyyy-MM    月份
     * @param distNo 卡号
     * @return
     */
    @ApiOperation(value = "发票余款额度查询", tags = {"md_发票余款额度查询"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "date", value = "日期", required = true,dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "cardCode", value = "会员卡号", required = true,dataType = "String", paramType = "query"),
    })
    @GetMapping(value = "/userInfo/getCheckBalanceInvoice")
    public JSONObject getCheckBalanceInvoice(String date, @RequestParam("cardCode") String distNo) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM");
        try {
            date = sdf1.format(sdf.parse(date));
            JSONObject resultMap = coreIssueApi.getCheckBalanceInvoice(date, distNo, 1, pageSize);
            logger.info("发票余额：" + JSON.toJSONString(resultMap));
            if (!"200".equals(resultMap.getString("resultCode"))) {
                logger.error("" + JSON.toJSONString(resultMap, true));
                return ResultUtil.error("0404", "调用失败");
            }
            JSONArray resultData = resultMap.getJSONArray("data");
            if (resultData == null || resultData.size() == 0) {
                return ResultUtil.error("0001", "无数据");
            }
            return ResultUtil.resultOK(resultData, "data");
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.error("0404", "调用失败");
        }
    }
}
