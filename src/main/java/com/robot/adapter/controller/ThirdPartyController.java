package com.robot.adapter.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.dtyunxi.rest.RestResponse;
import com.perfect.third.integration.api.dto.response.mall.InfocenterAuthRespDto;
import com.perfect.third.integration.api.query.mall.IAuthQueryApi;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 第三方接口
 *
 * @author 亮亮
 * @date 2018年12月26日
 */
@Controller
@ResponseBody
@RequestMapping("/adapter/thirdParty")
@Api(tags = "完美旧仓储", description = "完美旧仓储")
@SuppressWarnings("all")
public class ThirdPartyController {
    @Value("${ThirdPartyHOSTPORT}")
    private String ThirdPartyHOSTPORT;
    private Logger logger = LoggerFactory.getLogger(ThirdPartyController.class);
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private IAuthQueryApi authQueryApi;

    /**
     * 23号场景，产品生产日期查询
     *
     * @param repositoryNo 仓库号
     * @param productionNo 产品编号
     * @return
     */
    @ApiOperation(value = "产品生产日期查询", tags = {"md_产品生产日期查询"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "repositoryNo", value = "网点编号", required = true,dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "productionNo", value = "产品编号", required = true,dataType = "String", paramType = "query")
    })
    @RequestMapping(value = "/goods/getProductionDate", method = RequestMethod.GET)
    public JSONObject getProductionDate(String repositoryNo, String productionNo) {
        //此处添入要测试的具体的url
        String url = ThirdPartyHOSTPORT + "/procedure" +
                "/getseekinvstockavailjk2?agentno=" + repositoryNo + "&inv_no=" + productionNo;
        JSONArray list = null;
        try {
            JSONObject restObject = getReturnByURL(url);
            logger.info("返回数据：" + JSON.toJSONString(restObject, SerializerFeature.WriteMapNullValue));
            if (!"0".equals(restObject.getString("resultCode"))) {
                return ResultUtil.error("0404", (String) restObject.get("resultMsg"));
            }
            list = restObject.getJSONArray("data");
            if(list==null||list.size()==0){
                return ResultUtil.error("0001", "无数据");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.error("0404", "调用失败");
        }
        return ResultUtil.resultOK(list, "data");
    }

    /**
     * 31号场景，促销活动赠品
     *
     * @param agentno 网点编号
     * @param cmonth  发货日期
     * @return
     */
    @ApiOperation(value = "促销活动赠品", tags = {"md_促销活动赠品"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "disNo", value = "网点编号", required = true,dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "duration", value = "时间段", required = true,dataType = "String", paramType = "query")
    })
    @RequestMapping(value = "/goods/getLargesses", method = RequestMethod.GET)
    public JSONObject getLargesses(String disNo, String duration) {
        String[] split = duration.split("~");
        SimpleDateFormat yyyyMM = new SimpleDateFormat("yyyyMM");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM");
        List<String> monthBetween = DateUtil.getMonthBetween(split[0].substring(0, 6), split[1].substring(0, 6));
        JSONArray list = new JSONArray();
        for (int i = 0; i < monthBetween.size(); i++) {
            try {
                Date parse = yyyyMM.parse(monthBetween.get(i));
                String format = dateFormat.format(parse);
                //此处添入要测试的具体的url
                String url = ThirdPartyHOSTPORT + "/procedure/zszlcseekzxitemjk2?agentno=" + disNo
                        + "&cmonth=" + format;
                logger.info("访问URL：" + url);
                JSONObject restObject = getReturnByURL(url);
                logger.info("返回数据：" + JSON.toJSONString(restObject, true));
                if (!"0".equals(restObject.getString("resultCode"))) {
                    logger.error("请求失败，进入下个月份");
                    continue;
                }
                list.addAll(restObject.getJSONArray("data"));
                if(list.size()>80){
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("searchStartMonth", monthBetween.get(0));
                    resultMap.put("searchEndMonth", monthBetween.get(monthBetween.size() - 1));
                    resultMap.put("data", null);
                    return ResultUtil.setAll("0009", "有数据，数据大于80条", resultMap);
                }
            } catch (Exception e) {
                logger.info("***********异常信息*************");
                e.printStackTrace();
                logger.info("***********************************");
            }
        }
        Map<String, Object> resultMap = new HashMap<>();

        resultMap.put("searchStartMonth", monthBetween.get(0));
        resultMap.put("searchEndMonth", monthBetween.get(monthBetween.size() - 1));
        resultMap.put("data", list);
        if (monthBetween.get(0).equals(monthBetween.get(monthBetween.size() - 1)) && list.size() > 0) {
            return ResultUtil.setAll("0000", "单月有数据", resultMap);
        } else if (!monthBetween.get(0).equals(monthBetween.get(monthBetween.size() - 1)) && list.size() > 0) {
            return ResultUtil.setAll("0001", "多月有数据", resultMap);
        } else if (list.size() == 0) {
            return ResultUtil.setAll("0002", "单月多月无数据", resultMap);
        } else {
            logger.info("返回数据:" + JSON.toJSONString(resultMap));
            logger.info("没有满足条件");
        }
        return ResultUtil.resultOK(list, "data");
    }

    public HttpHeaders getTokenHeader() {
        InfocenterAuthRespDto data = null;
        RestResponse<InfocenterAuthRespDto> token = authQueryApi.getToken();
        logger.info("返回 toKen："+JSON.toJSONString(token));
        if ("0".equals(token.getResultCode())) {
            data = token.getData();
        }
        String auth = new StringBuilder("appid=")
                .append(data.getAppid())
                .append(",appkey=").append(data.getAppkey())
                .append(",token=").append(data.getToken()).toString();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", auth);
        return headers;
    }

    public JSONObject getReturnByURL(String url) {
        HttpHeaders headers = null;
        try {
            RestTemplate restTemplate1=new RestTemplate();
            String invStockUrl = url;
            headers = getTokenHeader();
            HttpEntity<?> requestEntity = new HttpEntity<>(null, headers);
            ResponseEntity<JSONObject> response = restTemplate1.exchange(
                    invStockUrl, HttpMethod.GET, requestEntity, JSONObject.class);
            JSONObject testbody = response.getBody();

            logger.info("请求完美，返回数据"+JSON.toJSONString(testbody));
            return testbody;
        } catch (Exception e) {
            logger.error("************异常**************");
            e.printStackTrace();
            logger.error("************异常**************");
            JSONObject obj = new JSONObject();
            obj.put("resultCode", "0001");
            obj.put("resultMsg", e.getMessage() + "访问url：" + url);
            obj.put("data", e.getStackTrace());
            obj.put("headers", headers);
            return obj;
        }
    }

}
