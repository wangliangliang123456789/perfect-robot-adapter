package com.robot.adapter.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.dtyunxi.rest.RestResponse;
import com.perfect.center.member.api.IMemberPwdApi;
import com.perfect.center.member.api.IMemberSearchApi;
import com.perfect.center.member.api.dto.response.MemberHistoryInfoMongoRespDto;
import com.perfect.center.member.api.dto.response.MemberInfoRespDto;
import com.perfect.center.member.api.dto.response.MemberPwdRespDto;
import com.perfect.center.member.api.query.IMemberCardQueryApi;
import com.perfect.center.member.api.query.IMemberPwdQueryApi;
import com.perfect.center.member.api.query.IMemberQueryApi;
import com.perfect.center.message.api.IPerfectMessageApi;
import com.perfect.center.message.api.dto.request.PerfectMessageReqDto;
import com.perfect.center.progress.api.dto.response.MemberIntegralRecordRespDto;
import com.perfect.center.progress.api.query.IMemberProgressQueryApi;
import com.perfect.center.user.api.dto.request.PerfectCtsUserReqDto;
import com.perfect.center.user.api.dto.response.PerfectCtsUserInfoRespDto;
import com.perfect.center.user.api.query.IPerfectCtsUserQueryApi;
import com.robot.adapter.util.DateUtil;
import com.robot.adapter.util.RestClient;
import com.robot.adapter.util.ResultUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName
 * @Description: crm 对接客服
 * @Author 亮亮
 * @Date 2019/1/14  17:21
 * @Version
 **/
@Controller
@ResponseBody
@RequestMapping("/adapter/crm")
@Api(tags = "CRM", description = "CRM")
@SuppressWarnings("all")
public class CrmController {

    Logger logger = LoggerFactory.getLogger(CrmController.class);
    @Autowired
    private HttpServletRequest request;

    @Value("${CrmHOSTPORT}")
    private String CrmHOSTPORT;
    /**
     * 查询会员卡状态
     */
    @Autowired
    private IMemberQueryApi memberQueryApi;
    /**
     * 发送短信接口
     */
    @Autowired
    private IPerfectMessageApi perfectMessageApi;
    /**
     * 查询userid
     */
    @Autowired
    private IPerfectCtsUserQueryApi perfectCtsUserQueryApi;
    /**
     * 系统结算月份
     */
    @Autowired
    private IMemberProgressQueryApi memberProgressQueryApi;
    /**
     * 查询会员卡信息
     */
    @Autowired
    private IMemberCardQueryApi memberCardQueryApi;

    @Autowired
    private IMemberSearchApi memberSearchApi;
    /**
     * 密码查询
     */
    @Autowired
    private IMemberPwdQueryApi memberPwdQueryApi;
    /**
     * 修改密码
     */
    @Autowired
    private IMemberPwdApi memberPwdApi;
    @Autowired
    private RestTemplate restTemplate;

    /**
     * 新增_通用积分状态
     *
     * @return
     */
    @ApiOperation(value = "通用积分状态", tags = {"md_共用"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "date", value = "日期", required = false,dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "duration", value = "时间段", required = false,dataType = "String", paramType = "query")
    })
    @GetMapping(value = "/userInfo/queryMonthStatusForIntergral")
    public JSONObject queryMonthStatusForIncome(@RequestParam(name = "duration", required = false) String duration,
                                                @RequestParam(name = "date", required = false) String date) {
        //系统最新结算月
        try {
            Date systemSettlementMonth = getNewestTime();
            if (systemSettlementMonth == null) {
                logger.error("无系统结算月");
                return ResultUtil.setAll("0404", "无系统结算月", null);
            }
            Map<String, Integer> map = new HashMap<>();
            map.put("systemSettlementMonth", Integer.parseInt(
                    new SimpleDateFormat("yyyyMM").format(systemSettlementMonth)));
            if (duration != null && !"".equals(duration)) {
                List<String> dateList = DateUtil.durationToStartAndEnd(duration, "yyyyMM");
                if (dateList != null) {
                    String startMonth = dateList.get(0);
                    String endMonth = dateList.get(1);
                    map.put("startMonth", Integer.parseInt(startMonth));
                    map.put("endMonth", Integer.parseInt(endMonth));
                } else {
                    logger.error("data:" + date + ",duration:" + duration);
                }

            } else if (date != null && !"".equals(date)) {
                String startMonth = date.substring(0, 6);
                logger.info("日期：" + startMonth);
                map.put("startMonth", Integer.parseInt(startMonth));
                map.put("endMonth", Integer.parseInt(startMonth));
            } else {
                logger.error("data:" + date + ",duration:" + duration);
            }
            if (map.get("startMonth") > map.get("systemSettlementMonth")) {
                if (map.get("startMonth") <= map.get("endMonth")) {
                    map.put("status", 0001);
                    return ResultUtil.setAll("0001", "单月,多月未核算", map);
                }
            }
            if (map.get("startMonth").equals(map.get("endMonth"))) {
                if (map.get("startMonth") <= map.get("systemSettlementMonth")) {
                    map.put("status", 0002);
                    return ResultUtil.setAll("0002", "单月已核算", map);
                }
            } else if (map.get("startMonth") < map.get("endMonth")) {
                if (map.get("startMonth") <= map.get("systemSettlementMonth")) {
                    map.put("status", 0000);
                    return ResultUtil.setAll("0000", "多月已核算", map);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.error("0404", "调用失败");
        }
        logger.error("错误++++++++++++++++++++++++++");
        return ResultUtil.error("0404", "调用失败");

    }

    /**
     * 新增_md8_md16专用积分状态
     *
     * @return
     */
    @ApiOperation(value = "_md8_md16专用积分状态", tags = {"md_共用"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "date", value = "日期", required = false,dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "duration", value = "时间段", required = false,dataType = "String", paramType = "query")
    })
    @GetMapping(value = "/userInfo/queryIntergralDurationStatus")
    public JSONObject queryIntergralDurationStatus(@RequestParam(name = "duration", required = false) String duration,
                                                @RequestParam(name = "date", required = false) String date) {
        //系统最新结算月
        try {
            Date systemSettlementMonth = getNewestTime();
            if (systemSettlementMonth == null) {
                logger.error("无系统结算月");
                return ResultUtil.setAll("0404", "无系统结算月", null);
            }
            Map<String, Integer> map = new HashMap<>();
            map.put("systemSettlementMonth", Integer.parseInt(
                    new SimpleDateFormat("yyyyMM").format(systemSettlementMonth)));
            if (duration != null && !"".equals(duration)) {
                List<String> dateList = DateUtil.durationToStartAndEnd(duration, "yyyyMM");
                if (dateList != null) {
                    String startMonth = dateList.get(0);
                    String endMonth = dateList.get(1);
                    map.put("startMonth", Integer.parseInt(startMonth));
                    map.put("endMonth", Integer.parseInt(endMonth));
                } else {
                    logger.error("data:" + date + ",duration:" + duration);
                }
            } else if (date != null && !"".equals(date)) {
                String startMonth = DateUtil.dataFormatter(DateUtil.dataParse(date, "yyyyMMdd"), "yyyyMM");
                map.put("startMonth", Integer.parseInt(startMonth));
                map.put("endMonth", Integer.parseInt(startMonth));
            }
            if (map.get("startMonth").equals(map.get("endMonth"))) {
                //当前月份
                int newMonth = Integer.parseInt(DateUtil.getLastMonth(0));
                //上个月份
                int upMonth = Integer.parseInt(DateUtil.getLastMonth(-1));
                if (map.get("startMonth") > newMonth) {
                    map.put("status", 0000);
                    return ResultUtil.setAll("0000", "单月(大于本月)", map);
                } else if (map.get("startMonth").equals(newMonth)) {
                    map.put("status", 0001);
                    return ResultUtil.setAll("0001", "单月(本月)", map);
                } else if (map.get("startMonth").equals(upMonth) && map.get("startMonth") > map.get("systemSettlementMonth")) {
                    map.put("status", 0002);
                    return ResultUtil.setAll("0002", "(单月)上个月且未结算", map);
                } else if (map.get("startMonth") <= upMonth && map.get("startMonth") < map.get("systemSettlementMonth")) {
                    map.put("status", 0003);
                    return ResultUtil.setAll("0003", "(单月)上个月(或上个月之前)已结算", map);
                }
            } else if (map.get("startMonth") < map.get("endMonth")) {
                if (map.get("startMonth") <= map.get("systemSettlementMonth")) {
                    map.put("status", 0004);
                    return ResultUtil.setAll("0004", "(多月)包含已结算", map);
                } else if (map.get("startMonth") > map.get("systemSettlementMonth")) {
                    map.put("status", 0005);
                    return ResultUtil.setAll("0005", "(多月)都未结算", map);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.error("0404", "调用失败");
        }
        return null;
    }

    /**
     * 获取最新系统结算日期
     *
     * @return
     */
    public Date getNewestTime() {
        for (int i = 0; i < 3; i++) {
            try {
                RestResponse<MemberIntegralRecordRespDto> newestTime = memberProgressQueryApi.getNewestTime();
                if ("0".equals(newestTime.getResultCode()) && newestTime.getData() != null) {
                    return newestTime.getData().getCreateTime();
                } else {
                    logger.error("第" + (i + 1) + "次获取系统最新结算日期失败");
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("第" + (i + 1) + "次获取系统最新结算日期失败,异常信息" + e.getMessage());
                continue;
            }
        }
        return null;

    }

    /**
     * 判断级别
     *
     * @param month  月份
     * @param distNo 卡号
     * @return
     */
    public Integer getLevel(Integer month, String distNo) {
        try {
            RestResponse<MemberHistoryInfoMongoRespDto> response =
                    memberSearchApi.listMemberHistoryInfoByCardcodeAndMonth(distNo, month);
            if (!"0".equals(response.getResultCode())) {
                return -1;
            }
            return response.getData().getLevel();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 查询葡萄酒积分
     *
     * @param duration        时间段
     * @param cardCode        卡号
     * @param typeDescription 葡萄酒类型
     * @return
     */
    @ApiOperation(value = "查询葡萄酒积分", tags = {"md_查询葡萄酒积分（时间段,不做级别判断）"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cardCode", value = "会员卡号", required = true,dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "duration", value = "时间段", required = true,dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "typeDescription", value = "葡萄酒类型", required = true, defaultValue = "国际葡萄酒",dataType = "String", paramType = "query")
    })
    @RequestMapping(value = "/userInfo/getWineIntegral", method = RequestMethod.GET)
    public JSONObject getWineByManyMonth(String duration, String cardCode, String typeDescription) {
        logger.info("查询时间：" + duration + ",卡号：" + cardCode);
        List<Map<String, Object>> list = null;
        try {
            List<String> dateList = DateUtil.durationToStartAndEnd(duration, "yyyyMM");
            String startDate = dateList.get(0);
            String endDate = dateList.get(1);
            BigDecimal totalPoints1 = new BigDecimal(0);
            List<String> monthList = DateUtil.getMonthBetween(startDate, endDate);
            List<Integer> months = new ArrayList<>();
            for (int i = 0; i < monthList.size(); i++) {
                months.add(Integer.parseInt(monthList.get(i)));
            }
            list = new ArrayList<>();
            Map<String, Object> map = new HashMap<>();
            RestResponse<List<MemberIntegralRecordRespDto>> months1 = memberProgressQueryApi
                    .getMemberIntegralRecordByCardCodeAndMonths(months, cardCode);
            logger.info("返回数据：" + JSON.toJSONString(months1));
            if (!"0".equals(months1.getResultCode())) {
                logger.error("发生异常：代码：" + months1.getResultCode() + ",信息:" + months1.getResultMsg());
                return ResultUtil.error(months1.getResultCode(), months1.getResultMsg());
            }
            /**
             * 积分月份集合
             */
            List<MemberIntegralRecordRespDto> data = months1.getData();
            if (data == null || data.size() == 0) {
                return ResultUtil.setAll("0002", "无数据(多月,单月)", null);
            }
            for (int i = 0; i < data.size(); i++) {
                //国际葡萄酒积分
                if ("国际葡萄酒".equals(typeDescription)) {
                    map = new HashMap<>();
                    BigDecimal amount16 = data.get(i).getAmount16();
                    BigDecimal amount17 = data.get(i).getAmount17();
                    if (null == amount16 && null == amount17) {
                        continue;
                    }
                    amount16 = amount16 == null ? new BigDecimal(0) : amount16;
                    amount17 = amount17 == null ? new BigDecimal(0) : amount17;
                    BigDecimal International = amount16.add(amount17);
                    map.put("month", data.get(i).getMonth());
                    map.put("type", "国际葡萄酒");
                    map.put("integral", International);
                    totalPoints1 = totalPoints1.add(International);
                    list.add(map);
                } else if ("南非葡萄酒".equals(typeDescription)) {
                    map = new HashMap<>();
                    BigDecimal amount19 = data.get(i).getAmount19();
                    BigDecimal amount20 = data.get(i).getAmount20();
                    if (null == amount19 && null == amount20) {
                        continue;
                    }
                    amount19 = amount19 == null ? new BigDecimal(0) : amount19;
                    amount20 = amount20 == null ? new BigDecimal(0) : amount20;
                    BigDecimal International = amount19.add(amount20);
                    map.put("month", data.get(i).getMonth());
                    map.put("type", "南非葡萄酒");
                    map.put("integral", International);
                    totalPoints1 = totalPoints1.add(International);
                    list.add(map);
                } else if ("国产红酒".equals(typeDescription)) {
                    map = new HashMap<>();
                    BigDecimal amount22 = data.get(i).getAmount22();
                    BigDecimal amount23 = data.get(i).getAmount23();
                    if (null == amount22 && null == amount23) {
                        continue;
                    }
                    amount22 = amount22 == null ? new BigDecimal(0) : amount22;
                    amount23 = amount23 == null ? new BigDecimal(0) : amount23;
                    BigDecimal International = amount22.add(amount23);
                    map.put("month", data.get(i).getMonth());
                    map.put("type", "国产红酒");
                    map.put("integral", International);
                    totalPoints1 = totalPoints1.add(International);
                    list.add(map);
                } else if ("智利葡萄酒".equals(typeDescription)) {
                    map = new HashMap<>();
                    BigDecimal amount25 = data.get(i).getAmount25();
                    BigDecimal amount26 = data.get(i).getAmount26();
                    if (null == amount25 && null == amount26) {
                        continue;
                    }
                    amount25 = amount25 == null ? new BigDecimal(0) : amount25;
                    amount26 = amount26 == null ? new BigDecimal(0) : amount26;
                    BigDecimal International = amount25.add(amount26);
                    map.put("month", data.get(i).getMonth());
                    map.put("type", "智利葡萄酒");
                    map.put("integral", International);
                    totalPoints1 = totalPoints1.add(International);
                    list.add(map);
                }
            }
            if (list == null || list.size() == 0) {
                return ResultUtil.error("0002", "单月多月无数据");
            }
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("data", list);
            resultMap.put("totalIntegral", totalPoints1);
            logger.info(request.getRequestURL().toString());
            logger.info(JSON.toJSONString(list));
            Integer totalIntegral = Integer.parseInt(totalPoints1.intValue() + "");
            logger.info("总积分" + totalIntegral);
            if (totalIntegral != 0 && list != null && startDate.equals(endDate)) {
                return ResultUtil.setAll("0000", "单月有数据(合计总积分不为0)", resultMap);
            } else if (totalIntegral != 0 && list != null && !startDate.equals(endDate)) {
                return ResultUtil.setAll("0001", "多月有数据(合计总积分不为0)", resultMap);
            } else if (list == null || list.size() == 0) {
                return ResultUtil.setAll("0002", "无数据(多月,单月)", resultMap);
            } else if (totalIntegral == 0) {
                return ResultUtil.setAll("0003", "单月多月(合计总积分为0)", resultMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return ResultUtil.error("0404", "调用失败");
        }
        return ResultUtil.resultOK(list, "data");
    }

    /**
     * 健康食品积分
     *
     * @param duration 时间段
     * @param cardCode 卡号
     * @return
     */
    @ApiOperation(value = "健康食品积分", tags = {"md_查询健康食品积分 （时间段,无级别判断）"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cardCode", value = "会员卡号", required = true,dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "duration", value = "时间段", required = true,dataType = "String", paramType = "query")
    })
    @RequestMapping(value = "/userInfo/getHealthFoodIntegral", method = RequestMethod.GET)
    public JSONObject getHealthFoodIntegralByByMany(String duration, String cardCode) {
        logger.info("查询时间：" + duration + ",卡号：" + cardCode);
        Date newestTime = getNewestTime();
        if (newestTime == null) {
            logger.error("无系统结算月");
            return ResultUtil.setAll("0500", "无系统结算月", null);
        }
        String systemSettlementMonth = new SimpleDateFormat("yyyyMM").format(newestTime);
        List<String> dateList = DateUtil.durationToStartAndEnd(duration, "yyyyMM");
        String startDate = dateList.get(0);
        String endDate = dateList.get(1);
        List<String> monthList = DateUtil.getMonthBetween(startDate, endDate);
        List<Integer> months = new ArrayList<>();
        Map<String, Object> monthData = new HashMap<>();
        for (int i = 0; i < monthList.size(); i++) {
            months.add(Integer.parseInt(monthList.get(i)));
        }
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        try {
            RestResponse<List<MemberIntegralRecordRespDto>> months1 = memberProgressQueryApi
                    .getMemberIntegralRecordByCardCodeAndMonths(months, cardCode);
            logger.info(JSON.toJSONString(months1, true));

            if (!"0".equals(months1.getResultCode())) {
                return ResultUtil.error("0404", "调用失败");
            }
            /**
             * 积分月份集合
             */
            List<MemberIntegralRecordRespDto> data = months1.getData();
            if (data == null) {
                return ResultUtil.error("0002", "无数据(单月,多月)");
            }
            for (int i = 0; i < data.size(); i++) {
                //健康食品积分
                map = new HashMap<>();
                BigDecimal amount13 = data.get(i).getAmount13();
                BigDecimal amount14 = data.get(i).getAmount14();
                if (null == amount13 && null == amount14) {
                    continue;
                }
                amount13 = amount13 == null ? new BigDecimal(0) : amount13;
                amount14 = amount14 == null ? new BigDecimal(0) : amount14;
                BigDecimal International = amount13.add(amount14);
                map.put("month", data.get(i).getMonth());
                map.put("type", "健康食品积分");
                map.put("integral", International);
                list.add(map);
            }
            if (list == null || list.size() == 0) {
                return ResultUtil.error("0002", "单月多月无数据");
            }
            Map<String, Object> resultMap = new HashMap<>();
            if (startDate.equals(endDate)) {
                resultMap.put("data", list);
                return ResultUtil.setAll("0000", "有数据(单月)", resultMap);
            } else if (!startDate.equals(endDate)) {
                resultMap.put("data", list);
                return ResultUtil.setAll("0001", "有数据(多月)", resultMap);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResultUtil.error("0404", "调用失败");
        }
        return ResultUtil.error("0404", "调用失败");
    }

    /**
     * 玛丽艳积分
     *
     * @param duration 时间段
     * @param cardCode 卡号
     * @return
     */
    @ApiOperation(value = "玛丽艳积分", tags = {"md_玛丽艳积分"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cardCode", value = "会员卡号", required = true,dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "duration", value = "时间段", required = true,dataType = "String", paramType = "query")
    })
    @RequestMapping(value = "/userInfo/getMarieIntegral", method = RequestMethod.GET)
    public JSONObject getMarieIntegralByByMany(String duration, String cardCode) {
        try {
            Date newestTime = getNewestTime();
            if (newestTime == null) {
                logger.error("无系统结算月");
                return ResultUtil.setAll("0404", "无系统结算月", null);
            }
            String systemSettlementMonth = new SimpleDateFormat("yyyyMM").format(newestTime);
            String[] split = duration.split("~");
            List<String> dateList = DateUtil.durationToStartAndEnd(duration, "yyyyMM");
            if (dateList == null) {
                logger.error("日期转换失败");
                return ResultUtil.error("0404", "调用失败");
            }
            String startDate = dateList.get(0);
            String endDate = dateList.get(1);
            List<String> monthList = DateUtil.getMonthBetween(startDate, endDate);
            List<Integer> months = new ArrayList<>();
            Map<String, Object> monthData = new HashMap<>();
            //个人总积分
            BigDecimal totalPersonalIntegral = new BigDecimal(0);
            //多月总积分
            BigDecimal manyIntegral = new BigDecimal(0);

            for (int i = 0; i < monthList.size(); i++) {
                months.add(Integer.parseInt(monthList.get(i)));
            }
            List<Map<String, Object>> list = new ArrayList<>();
            Map<String, Object> map = new HashMap<>();
            RestResponse<List<MemberIntegralRecordRespDto>> months1 = memberProgressQueryApi
                    .getMemberIntegralRecordByCardCodeAndMonths(months, cardCode);
            logger.info("返回数据" + JSON.toJSONString(months1));
            if (!"0".equals(months1.getResultCode())) {
                return ResultUtil.error("0404", "调用失败");
            }
            /**
             * 积分月份集合
             */
            List<MemberIntegralRecordRespDto> data = months1.getData();
            if (data == null || data.size() == 0) {
                return ResultUtil.error("0002", "无数据");
            }
            for (int i = 0; i < data.size(); i++) {
                map = new HashMap<>();
                BigDecimal amount9 = data.get(i).getAmount9();
                BigDecimal amount10 = data.get(i).getAmount10();
                BigDecimal amount11 = data.get(i).getAmount11();
                if (null == amount9 && null == amount10 && null == amount11) {
                    continue;
                }
                amount9 = amount9 == null ? new BigDecimal(0) : amount9;
                amount10 = amount9 == null ? new BigDecimal(0) : amount10;
                amount11 = amount9 == null ? new BigDecimal(0) : amount11;
                BigDecimal international = amount10.add(amount11);
                map.put("personalIntegral", amount9);
                map.put("clientIntegral", amount10);
                map.put("assistIntegral", amount11);
                map.put("month", data.get(i).getMonth());
                map.put("type", "玛丽艳积分");
                map.put("totalIntegral", international);
                list.add(map);
                totalPersonalIntegral = totalPersonalIntegral.add(amount9);
                manyIntegral = manyIntegral.add(international);
            }
            if (list == null || list.size() == 0) {
                return ResultUtil.error("0002", "单月多月无数据");
            }
            int startMonth = Integer.parseInt(startDate);
            int endMonth = Integer.parseInt(endDate);
            if (startMonth == endMonth) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("totalPersonalIntegral", totalPersonalIntegral);
                resultMap.put("manyIntegral", manyIntegral);
                resultMap.put("data", list);
                return ResultUtil.setAll("0000", "单月有数据", resultMap);
            } else if (startMonth < endMonth) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("totalPersonalIntegral", totalPersonalIntegral);
                resultMap.put("manyIntegral", manyIntegral);
                resultMap.put("data", list);
                return ResultUtil.setAll("0001", "多月有数据", resultMap);
            }
            return ResultUtil.error("0404", "调用失败");
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.error("0404", "调用失败");
        }
    }

    /**
     * md_查询会员是否有密码
     *
     * @param cardCode 卡号
     * @return
     */
    @ApiOperation(value = "查询会员是否有密码", tags = {"查询会员是否有密码"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cardCode", value = "会员卡号", required = true,dataType = "String", paramType = "query")
    })
    @RequestMapping(value = "/userInfo/getPwdStatus", method = RequestMethod.GET)
    public JSONObject getPwdStatus(String cardCode) {
        try {
            RestResponse<MemberPwdRespDto> response = memberPwdQueryApi.queryPwdStatus(cardCode);
            if (!"0".equals(response.getResultCode())) {
                return ResultUtil.error("0404", "信息调用失败");
            }
            Map<String, String> map = new HashMap<>();
            map.put("userName", response.getData().getUserName());
            map.put("cardCode", response.getData().getCardCode());
            if (response.getData().getPwdStatus() == 0) {
                map.put("pwdStatus", "无");
                return ResultUtil.setAll("0001", "无密码", map);
            } else if (response.getData().getPwdStatus() == 1) {
                map.put("pwdStatus", "有");
                return ResultUtil.setAll("0000", "有密码", map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResultUtil.error("0404", "信息调用失败");
    }

    /**
     * md_会员密码验证
     *
     * @param cardCode 卡号
     * @param pwd      密码
     * @return
     */
    @ApiOperation(value = "会员密码验证", tags = {"md_共用"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cardCode", value = "会员卡号", required = true,dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "pwd", value = "密码", required = true,dataType = "String", paramType = "query")
    })
    @RequestMapping(value = "/userInfo/getUserPassword", method = RequestMethod.GET)
    public JSONObject getUserPassword(String cardCode, String pwd) {
        try {
            if (cardCode == null || cardCode.equals("")) {
                return ResultUtil.error("0404", "卡号为null");
            }
            String substring = pwd.substring(pwd.length() - 1);
            Map<String, Object> map = new HashMap<>();
            RestResponse<Boolean> response = memberPwdQueryApi.queryUserPassword(cardCode, pwd);
            logger.info("验证会员密码状态：" + JSON.toJSONString(response, true));
            if (!"0".equals(response.getResultCode())) {
                if ("4036".equals(response.getResultCode())) {
                    map.put("result", false);
                    return ResultUtil.setAll("0001", response.getResultMsg(), map);
                } else {
                    return ResultUtil.setAll("0404", "信息调用失败", null);
                }
            }
            if (response.getData()) {
                map.put("result", response.getData());
                return ResultUtil.setAll("0000", "密码正确", map);
            } else {
                map.put("result", response.getData());
                return ResultUtil.setAll("0001", "密码错误", map);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.error("0404", "调用失败");
        }

    }

    /**
     * 修改密码
     *
     * @param cardCode     卡号
     * @param pwd          密码
     * @param channel      来源渠道：(商城 S，核心 H ， 客服 K ， CRM C )
     * @param createPerson 修改人
     * @return
     */
    @ApiOperation(value = "修改密码", tags = {"修改密码"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cardCode", value = "会员卡号", required = true,dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "pwd", value = "密码", required = true,dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "channel", value = "来源渠道：(商城 S，核心 H ， 客服 K ， CRM C )", required = true,dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "createPerson", value = "修改人", required = true,dataType = "String", paramType = "query")
    })
    @RequestMapping(value = "/updateUserPassword", method = RequestMethod.POST)
    public JSONObject updateUserPassword(String cardCode, String pwd, String channel, String createPerson) {
        RestResponse<Boolean> response = memberPwdApi.updateUserPassword(cardCode, pwd, channel, createPerson);
        if (!"0".equals(response.getResultCode())) {
            return ResultUtil.error(response.getResultCode(), response.getResultMsg());
        }
        return ResultUtil.resultOK(response.getData(), "result");
    }

    /**
     * 保险对象、生效/失效月份
     *
     * @param cardCode 卡号
     * @return
     */
    @ApiOperation(value = "保险对象、生效/失效月份", tags = {"md_保险对象、生效/失效月份"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cardCode", value = "会员卡号", required = true,dataType = "String", paramType = "query")
    })
    @RequestMapping(value = "/userInfo/queryInsuranceStartOrEndTime", method = RequestMethod.GET)
    public JSONObject queryInsuranceStartOrEndTime(String cardCode) {
        RestTemplate a = RestClient.getClient();
        String url = CrmHOSTPORT + "/iscrm/insurance/queryInsuranceStartOrEndTime";
        Map<String, Object> map = new HashMap<>();
        List<String> cardCodes = new ArrayList<>();
        cardCodes.add(cardCode);
        map.put("cardCodes", cardCodes);
        String infra = JSON.toJSONString(map);
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        HttpEntity<String> formEntity = new HttpEntity<String>(infra, headers);
        List resultList = new ArrayList();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            String d = a.postForObject(url, formEntity, String.class);
            logger.info("返回数据：" + d);
            JSONObject object = JSONObject.parseObject(d);
            if (object.getInteger("code") == 200) {
                JSONArray data = object.getJSONArray("data");
                if (data == null || data.size() == 0) {
                    return ResultUtil.error("0000", "无数据");
                } else if (data.size() == 1) {
                    resultMap.put("data", data);
                    return ResultUtil.setAll("0002", "有数据(仅含正卡)", resultMap);
                } else if (data.size() == 2) {
                    if (data.getJSONObject(0).getInteger("insuredIdentity") == 0
                            || data.getJSONObject(1).getInteger("insuredIdentity") == 1) {
                        resultList.add(data.get(0));
                        resultList.add(data.get(1));
                    } else if (data.getJSONObject(0).getInteger("insuredIdentity") == 1
                            || data.getJSONObject(1).getInteger("insuredIdentity") == 0) {
                        resultList.add(data.get(1));
                        resultList.add(data.get(0));
                    } else {
                        resultList.addAll(data);
                        resultMap.put("data", resultList);
                        return ResultUtil.setAll("0003"
                                , "都是正卡"
                                , resultMap);
                    }
                    resultMap.put("data", resultList);
                    return ResultUtil.setAll("0001"
                            , "有数据(含正副卡,且把正卡放在数组第一个元素,副卡放在数组第二个元素)"
                            , resultMap);
                }
            } else {
                return ResultUtil.error("0404", "调用失败");
            }
        } catch (Exception e) {
            logger.error("--------异常信息---------");
            logger.error(e.getMessage());
            e.printStackTrace();
            return ResultUtil.error("0404", "调用失败");
        }
        return ResultUtil.error("0404", "调用失败");
    }

    /**
     * 查询多月等级
     *
     * @param cardCode 卡号
     * @param duration 时间段
     * @return
     */
    @ApiOperation(value = "查询多月等级", tags = {"md_级别（多月）"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cardCode", value = "会员卡号", required = true,dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "duration", value = "时间段", required = true,dataType = "String", paramType = "query")
    })
    @RequestMapping(value = "/userInfo/getLevels", method = RequestMethod.GET)
    public JSONObject getLevels(String cardCode, String duration) {
        List<String> dateList1 = DateUtil.durationToStartAndEnd(duration, "yyyyMM");
        if (dateList1 == null) {
            logger.error("日期转换失败");
            return ResultUtil.error("0404", "调用失败");
        }
        String startDate = dateList1.get(0);
        String endDate = dateList1.get(1);
        //多月级别
        List<MemberHistoryInfoMongoRespDto> resultList = new ArrayList<>();
        List<String> dateList = DateUtil.getMonthBetween(startDate, endDate);
        //系统结算月份
        String systemSettlementMonth = new SimpleDateFormat("yyyyMM").format(getNewestTime());
        for (int i = 0; i < dateList.size(); i++) {
            for (int j = 0; j < 3; j++) {
                try {
                    if (Integer.parseInt(systemSettlementMonth) < Integer.parseInt(dateList.get(i))) {
                        logger.warn("系统计算月：" + systemSettlementMonth + ",查询月：" + dateList.get(i));
                        break;
                    }
                    RestResponse<MemberHistoryInfoMongoRespDto> forObject = memberSearchApi.listMemberHistoryInfoByCardcodeAndMonth(cardCode, Integer.parseInt(dateList.get(i)));
                    logger.info("查询级别：" + dateList.get(i) + ",返回结果:" + JSON.toJSONString(forObject));
                    if (!"0".equals(forObject.getResultCode())) {
                        logger.error("查询级别 ： 卡号：" + cardCode + ",月份 " + dateList.get(i) + "失败 第" + j + "次:"
                                + "错误信息：" + forObject.getResultCode() + "====" + forObject.getResultMsg());
                        continue;
                    }
                    MemberHistoryInfoMongoRespDto respDto = forObject.getData();
                    if (respDto != null) {
                        resultList.add(respDto);
                        break;
                    }

                } catch (Exception e) {
                    logger.error(e.getStackTrace().toString());
                    if (i == 2) {
                        continue;
                    }
                    return ResultUtil.error("0404", "调用失败");
                }
            }
        }
        List<JSONObject> list = new ArrayList();
        for (int i = 0; i < resultList.size(); i++) {
            MemberHistoryInfoMongoRespDto respDto = resultList.get(i);
            Integer level = respDto.getLevel();

            Integer maxLevel = respDto.getMaxLevel();
            logger.info("月份信息：" + JSON.toJSONString(respDto));
            JSONObject objectMap = JSON.parseObject(JSON.toJSONString(respDto, SerializerFeature.WriteMapNullValue), JSONObject.class);
            logger.info("月份信息Map：" + JSON.toJSONString(objectMap));

            if (respDto.getMonth() == null) {
                logger.warn("" + JSON.toJSONString(respDto));
                logger.warn("未结算,进入下个月份");
                continue;
            }
            Integer month = respDto.getMonth();
            if (maxLevel != null && maxLevel >= 5 && level != null && level == 0) {
                objectMap.put("levelDescription", "不合格得客户代表");
            } else if (maxLevel != null && maxLevel < 5 && level != null && level == 0) {
                objectMap.put("levelDescription", "优惠客户");
            } else if (level != null && (level == 1 || level == 2 || level == 3 || level == 4)) {
                objectMap.put("levelDescription", "优惠客户");
            } else if (level != null && level == 5) {
                objectMap.put("levelDescription", "客户代表");
            } else if (level != null && level == 6) {
                objectMap.put("levelDescription", "客户经理");
            } else if (level != null && level == 7) {
                objectMap.put("levelDescription", "中级客户经理");
            } else if (level != null && level == 8) {
                objectMap.put("levelDescription", "客户总监");
            } else if (level != null && level == 9) {
                objectMap.put("levelDescription", "高级客户总监");
            } else if (level != null && level == 10) {
                objectMap.put("levelDescription", "资深客户总监");
            } else if (level != null && level == 11) {
                objectMap.put("levelDescription", "客户总经理");
            } else if (level == null && maxLevel == null) {
                logger.error(respDto.getMonth() + "等级和最高等级全为null,");
                continue;
            }
            if(list.size()!=0&&month.equals(list.get(list.size()-1).getInteger("month"))){
                logger.warn("i="+i+",month="+month+",上一个等级信息："+list.get(list.size()-1).getInteger("month"));
                continue;
            }
            list.add(objectMap);
        }
        String returnCode = "0000";
        String returnMessage = "";
        if (startDate.equals(endDate) && list.size() > 0) {
            returnCode = "0000";
            returnMessage = "有数据(单月)";
        } else if (!startDate.equals(endDate) && list.size() > 0) {
            returnCode = "0001";
            returnMessage = "有数据(多月)";
        } else if (list.size() == 0) {
            returnCode = "0002";
            returnMessage = "无数据";
        }
        Map<String, Object> resMap = new HashMap<>();
        resMap.put("data", list);
        resMap.put("total", list.size());
        resMap.put("systemSettlementMonth", systemSettlementMonth);
        JSONObject obj = new JSONObject();
        obj.put("returnCode", returnCode);
        obj.put("returnMessage", returnMessage);
        obj.put("returnContent", resMap);
        return obj;
    }

    /**
     * 发送登录提示短信
     *
     * @param tel      手机号
     * @param cardCode 会员卡号
     * @return
     */
    @ApiOperation(value = "发送登录提示短信", tags = {"md_共用"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cardCode", value = "会员卡号", required = true,dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "tel", value = "电话号码", required = true,dataType = "String", paramType = "query")
    })
    @RequestMapping(value = "/userInfo/sendLoginingSms", method = RequestMethod.GET)
    public JSONObject sendLoginingSms(String tel, String cardCode) {
        PerfectMessageReqDto reqDto = new PerfectMessageReqDto();
        logger.info(cardCode.length() + "chandu");
        String subCarCode = cardCode.substring(cardCode.length() - 4, cardCode.length());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        String context = "【完美中国】会员编号尾数为 " + subCarCode + " 于" + sdf.format(new Date()) + "正在查询个人信息。如非本人操作，请修改查询密码。";
        reqDto.setMsgType(1);
        reqDto.setContent(context);
        reqDto.setTargets(tel);
        reqDto.setExtension("3");
        try {
            RestResponse<Void> response = perfectMessageApi.sendSms(reqDto);
            logger.info(context);
            return ResultUtil.setAll("0000", "发送成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return ResultUtil.error("0404", "调用失败");
        }
    }

    /**
     * md_(身份证校验)会员卡/优惠卡状态
     *
     * @param identityCard 身份证
     * @param name         姓名
     * @return
     */
    @ApiOperation(value = "(身份证校验)会员卡/优惠卡状态", tags = {"md_(身份证校验)会员卡/优惠卡状态"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "identityCard", value = "身份证号", required = true,dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "userName", value = "会员姓名", required = true,dataType = "String", paramType = "query")
    })
    @RequestMapping(value = "/userInfo/getMemberStatusWithID", method = RequestMethod.GET)
    public JSONObject getMemberStatusWithID(String identityCard, String userName) {
        PerfectCtsUserInfoRespDto user = null;
        try {
            PerfectCtsUserReqDto reqDto = new PerfectCtsUserReqDto();
            reqDto.setIdentityCard(identityCard);
            RestResponse<PerfectCtsUserInfoRespDto> response = perfectCtsUserQueryApi
                    .queryUserByDto(reqDto);
            logger.info(JSON.toJSONString(response, true));
            if (!"0".equals(response.getResultCode())) {
                return ResultUtil.error("0404", "调用失败");
            }
            user = response.getData();
            if (user == null) {
                logger.error("找不到身份证号：" + identityCard + "的用户信息");
                return ResultUtil.error("0006", "无数据");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResultUtil.error("0404", "调用失败");
        }
        JSONObject memberStatus = getMemberStatus(null, user.getId());
        logger.info("调接口getMemberStatus(String cardCode, Long userId)："+JSON.toJSONString(memberStatus,true));
        String returnCode = memberStatus.getString("returnCode");
        JSONObject returnContent = memberStatus.getJSONObject("returnContent");
        String returnMessage = memberStatus.getString("returnMessage");
        if (!"0404".equals(returnCode) && !"0006".equals(returnCode)) {
            if (returnContent == null) {
                logger.warn(JSON.toJSONString(memberStatus));
                return ResultUtil.error("0006", "无数据");
            }
            String userName1 = returnContent.getString("userName");
            if (userName.equals(userName1)) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("userInfo", returnContent);
                return ResultUtil.setAll(returnCode, returnMessage, resultMap);
            } else {
                return ResultUtil.setAll("0005", "有数据,名字不一致", returnContent);
            }
        }
        return memberStatus;

    }

    /**
     * md_会员卡/优惠卡状态
     *
     * @param cardCode 身份证
     * @param name     姓名
     * @return
     */
    @ApiOperation(value = "会员卡/优惠卡状态", tags = {"md_共用"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cardCode", value = "会员卡号", required = true,dataType = "String", paramType = "query")
    })
    @RequestMapping(value = "/userInfo/getMemberStatus", method = RequestMethod.GET)
    public JSONObject getMemberStatus(String cardCode) {
        JSONObject memberStatus = getMemberStatus(cardCode, null);
        logger.info("调接口getMemberStatus(String cardCode, Long userId)："+JSON.toJSONString(memberStatus,true));
        String returnCode = memberStatus.getString("returnCode");
        logger.info("returnCode:"+returnCode);
        JSONObject returnContent = memberStatus.getJSONObject("returnContent");
        logger.info("returnContent:"+returnContent);
        String returnMessage = memberStatus.getString("returnMessage");
        if (!"0404".equals(returnCode) && !"0006".equals(returnCode)) {
            if (returnContent == null) {
                logger.warn(JSON.toJSONString(memberStatus));
                return ResultUtil.error("0006", "无数据");
            }
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("userInfo", returnContent);
            return ResultUtil.setAll(returnCode, returnMessage, resultMap);
        }
        return ResultUtil.error("0404", "调用失败");
    }


    /**
     * 根据会userId查询 会员卡状态
     *
     * @param userId userId
     * @return
     */
    public JSONObject getMemberStatus(String cardCode, Long userId) {
        try {
            RestResponse<MemberInfoRespDto> status;
            if (cardCode == null) {
                status = memberQueryApi.getMemberInfoDr01ByCardCodeOrUserId(cardCode, userId);
            } else if (userId == null) {
                status = memberQueryApi.getMemberInfoDr01ByCardCodeOrUserId(cardCode, userId);
            } else {
                return ResultUtil.error("0404", "参数错误：userId:" + userId + ",cardCode:" + cardCode);
            }
            logger.info("优惠卡信息：" + JSON.toJSONString(status, SerializerFeature.WriteMapNullValue));
            if (!"0".equals(status.getResultCode())) {
                logger.error("调用失败数据" + JSON.toJSONString(status, true));
                return ResultUtil.error("0404", "调用失败");
            }
            MemberInfoRespDto data = status.getData();
            if (data == null) {
                return ResultUtil.error("0006", "无数据");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("cardCode", data.getMainCardCode());
            map.put("userName", data.getMainUserName());
            if (data.getStatus() == 0) {
                map.put("endTime", data.getEndTime());
                map.put("startTime", data.getStartTime());
                map.put("status", "有效");
                return ResultUtil.setAll("0000", "有效的", map);
            } else if (data.getStatus() == 1) {
                map.put("status", "冻结");
                map.put("endTime", data.getFrozenEndTime());
                map.put("startTime", data.getFrozenStartTime());
                return ResultUtil.setAll("0001", "冻结", map);
            } else if (data.getStatus() == 2) {
                map.put("status", "过期");
                map.put("updateTime", data.getCancelDate());
                return ResultUtil.setAll("0002", "过期", map);
            } else if (data.getStatus() == 3) {
                map.put("status", "未激活");
                return ResultUtil.setAll("0003", "未激活", map);
            } else if (data.getStatus() == 4) {
                map.put("updateTime", data.getCancelDate());
                map.put("status", "取消");
                return ResultUtil.setAll("0004", "取消", map);
            } else {
                return ResultUtil.setAll("0404", "调用失败", map);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.setAll("0404", "调用失败", null);
        }
    }

    /**
     * 短信
     *
     * @param tel     手机号
     * @param context 短信内容
     * @return
     */
    @ApiOperation(value = "短信", tags = {"短信"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tel", value = "手机号", required = true,dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "context", value = "短信内容", required = true,dataType = "String", paramType = "query")
    })
    @RequestMapping(value = "/userInfo/sendSms", method = RequestMethod.GET)
    public JSONObject sendSms(String tel, String context) {
        PerfectMessageReqDto reqDto = new PerfectMessageReqDto();
        reqDto.setMsgType(1);
        reqDto.setContent(context);
        reqDto.setTargets(tel);
        reqDto.setExtension("3");
        try {
            RestResponse<Void> response = perfectMessageApi.sendSms(reqDto);
            return ResultUtil.setAll(response.getResultCode(), response.getResultMsg(), null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.error("0404", "调用失败");
        }
    }
}
