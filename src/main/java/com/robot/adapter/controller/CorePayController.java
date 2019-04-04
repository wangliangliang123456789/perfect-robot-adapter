package com.robot.adapter.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.perfect.third.integration.api.query.robot.ICorePayApi;
import com.robot.adapter.util.DateUtil;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 核心计酬
 *
 * @author 亮亮
 */
@Controller
@ResponseBody
@RequestMapping("/adapter/corePay")
@Api(tags = "核心计酬", description = "核心计酬")
@SuppressWarnings("all")
public class CorePayController {

    Logger logger = LoggerFactory.getLogger(CorePayController.class);
    @Value("${PAGESIZE}")
    private Integer pageSiza;
    @Autowired
    private ICorePayApi iCorePayApi;
    @Autowired
    private CrmController crmController;

    /**
     * 小组累计分
     *
     * @return
     */
    @ApiOperation(value = "小组累积分和个人累积分", tags = {"md_小组累计分和个人累积分"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cardCode", value = "会员卡号", required = true,dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "date", value = "日期", required = true,dataType = "String", paramType = "query")
    })
    @RequestMapping(value = {"/userInfo/getGroupPoint","/userInfo/getIndividualPoint"}, method = RequestMethod.GET)
    public JSONObject getGroupPoint(String cardCode, String date) {
        logger.info("小组累计分");
        String substring = date.substring(0, 6);
        try {
            JSONObject listRestResponse = iCorePayApi.queryGroupIntegral(cardCode, Integer.parseInt(substring));
            logger.info("返回数据："+JSON.toJSONString(listRestResponse,true));
            if ("200".equals(listRestResponse.getString("code"))) {
                JSONArray data = listRestResponse.getJSONArray("data");
                List list = new ArrayList();
                if (data != null && data.size() > 0) {
                    for (int i = 0; i < data.size(); i++) {
                        JSONObject objectMap = JSON.parseObject(JSON.toJSONString(data.get(i)));
                        objectMap.put("month", substring);
                        list.add(objectMap);
                    }
                    return ResultUtil.resultOK(list, "data");
                } else {
                    return ResultUtil.error("0001", "无数据");
                }
            } else {
               logger.info("调接口返回数据："+JSON.toJSONString(listRestResponse));
               return ResultUtil.error("0404", "调用失败");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResultUtil.error("0404", "调用失败");
        }
    }

    /**
     * 2号场景 月结积分
     *
     * @param distNo 卡号
     * @param date   月份  201211
     * @return
     */
    @ApiOperation(value = "月结积分", tags = {"md_查积分"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cardCode", value = "会员卡号", required = true,dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "duration", value = "时间段", required = true,dataType = "String", paramType = "query")
    })
    @RequestMapping(value = "/userInfo/getMonthIntegral", method = RequestMethod.GET)
    public JSONObject getMonthIntegral(@RequestParam("cardCode") String distNo, String duration) {
        String[] split = duration.split("~");
        String startDate = DateUtil.dataFormatter(DateUtil.dataParse(split[0], "yyyyMMdd HH:mm:ss"), "yyyyMM");
        String endDate = DateUtil.dataFormatter(DateUtil.dataParse(split[1], "yyyyMMdd HH:mm:ss"), "yyyyMM");
        JSONObject forObject = null;
        try {
            forObject = iCorePayApi.getMonthIntegral(distNo, startDate, endDate, 1, pageSiza);
            logger.info("返回数据："+JSON.toJSONString(forObject));
            JSONArray data = forObject.getJSONArray("data");
            if ("200".equals(forObject.getString("code"))&&(data==null)||data.size()==0) {
                logger.error(JSON.toJSONString(forObject));
                return ResultUtil.error("0002", "无数据(单月,多月)");
            }
            if (!"200".equals(forObject.getString("code"))) {
                logger.error(JSON.toJSONString(forObject));
                return ResultUtil.error("0404", "调用失败");
            }
            for (int i = 0; i < data.size(); i++) {
                Integer level = data.getJSONObject(i).getInteger("pin");
                logger.info("当前等级："+level);
                Integer maxLevel = data.getJSONObject(i).getInteger("highestPin");
                logger.info("最高等级："+maxLevel);
                if (maxLevel != null && maxLevel >= 5 && level != null && level == 0) {
                    data.getJSONObject(i).put("pinDescription", "不合格得客户代表");
                } else if (maxLevel != null && maxLevel < 5 && level != null && level == 0) {
                    data.getJSONObject(i).put("pinDescription", "优惠客户");
                } else if (level != null && (level == 1 || level == 2 || level == 3 || level == 4)) {
                    data.getJSONObject(i).put("pinDescription", "优惠客户");
                } else if (level != null && level == 5) {
                    data.getJSONObject(i).put("pinDescription", "客户代表");
                } else if (level != null && level == 6) {
                    data.getJSONObject(i).put("pinDescription", "客户经理");
                } else if (level != null && level == 7) {
                    data.getJSONObject(i).put("pinDescription", "中级客户经理");
                } else if (level != null && level == 8) {
                    data.getJSONObject(i).put("pinDescription", "客户总监");
                } else if (level != null && level == 9) {
                    data.getJSONObject(i).put("pinDescription", "高级客户总监");
                } else if (level != null && level == 10) {
                    data.getJSONObject(i).put("pinDescription", "资深客户总监");
                } else if (level != null && level == 11) {
                    data.getJSONObject(i).put("pinDescription", "客户总经理");
                }
            }
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("data", data);
            if (startDate.equals(endDate) && data != null && data.size() != 0) {
                return ResultUtil.setAll("0000", "单月有数据", resultMap);
            } else if (Integer.parseInt(startDate) < Integer.parseInt(endDate)
                    && data != null && data.size() != 0) {
                return ResultUtil.setAll("0001", "多月有数据", resultMap);
            } else {
                return ResultUtil.setAll("0404", "调用失败", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.error("0404", e.getMessage());
        }
    }


    /**
     * 6号 ，7 号接口 持续进步奖的分值
     *
     * @param disNo    会员卡号
     * @param duration 时间段
     * @return
     */
    @ApiOperation(value = "持续进步积分", tags = {"md_持续进步奖的分值"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cardCode", value = "会员卡号", required = true,dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "duration", value = "时间段", required = true,dataType = "String", paramType = "query")
    })
    @RequestMapping(value = "/userInfo/getCountPassNum", method = RequestMethod.GET)
    public JSONObject getCountPassNum(@RequestParam("cardCode") String distNo, String duration) {

        Date newestTime = crmController.getNewestTime();
        if (newestTime == null) {
            logger.error("无系统结算月");
            return ResultUtil.setAll("0404", "无系统结算月", null);
        }
        JSONArray resultList=new JSONArray();
        String systemSettlementMonth = new SimpleDateFormat("yyyyMM").format(newestTime);
        String[] split = duration.split("~");
        String startDate = DateUtil.dataFormatter(DateUtil.dataParse(split[0], "yyyyMMdd HH:mm:ss"), "yyyyMM");
        String endDate = DateUtil.dataFormatter(DateUtil.dataParse(split[1], "yyyyMMdd HH:mm:ss"), "yyyyMM");
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> resultMap = new HashMap<>();
        JSONArray list = null;
        Integer totalScore = 0;
        JSONObject forObject = null;
        try {
            forObject = iCorePayApi
                    .getCountPassNum(distNo, startDate, endDate, 1, pageSiza);
            logger.info("返回数据:"+JSON.toJSONString(forObject,true));
            if (!"200".equals(forObject.getString("code"))) {
                logger.error("返回信息；"+JSON.toJSONString(forObject));
                String resultMsg = forObject.getString("msg");
                logger.error("返回异常信息："+resultMsg);
                return ResultUtil.error("0404","调用失败");
            }
            logger.info("核心返回 data 类型："+forObject.get("data").getClass().getTypeName());
            logger.info("集合 类型："+JSONArray.class.getTypeName());
            if(forObject.get("data").getClass().getTypeName().equals(JSONArray.class.getTypeName())){
                return ResultUtil.error("0002","无数据");
            }
            JSONObject data = forObject.getJSONObject("data");
            list = data.getJSONArray("list");

            for (int i = 0; i < list.size(); i++) {
                Integer passNum_ = list.getJSONObject(i).getInteger("countPassNum");
                if(passNum_!=null){
                    totalScore += passNum_;
                    resultList.add(list.getJSONObject(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.error("0404", "调用失败");
        }
        if(resultList.size()==0){
            return ResultUtil.error("0002","无数据");
        }
        resultMap = new HashMap<>();
        resultMap.put("systemSettlementMonth", systemSettlementMonth);
        resultMap.put("totalScore", totalScore);
        resultMap.put("list",resultList);
        resultMap.put("settlementStatus", "已结算");
        resultMap.put("monthStart",startDate);
        resultMap.put("monthEnd", endDate);
        if (startDate.equals(endDate)) {
            return ResultUtil.setAll("0000", "有数据(单月)", resultMap);
        } else {
            return ResultUtil.setAll("0001", "有数据(多月)", resultMap);
        }
    }
}
