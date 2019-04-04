package com.robot.adapter.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.dtyunxi.rest.RestResponse;
import com.dtyunxi.yundt.cube.center.shop.api.dto.request.ShopDto;
import com.github.pagehelper.PageInfo;
import com.perfect.center.inventory.api.dto.request.ItemStorageQueryDto;
import com.perfect.center.inventory.api.dto.request.MortgageAccountQueryDto;
import com.perfect.center.inventory.api.dto.request.MortgageOrderQueryDto;
import com.perfect.center.inventory.api.dto.response.ItemStorageRespDto;
import com.perfect.center.inventory.api.dto.response.MortgageAccountRespDto;
import com.perfect.center.inventory.api.dto.response.MortgageItemRespDto;
import com.perfect.center.inventory.api.dto.response.MortgageOrderRespDto;
import com.perfect.center.inventory.api.query.*;
import com.perfect.center.item.api.dto.request.PerfectServiceItemListReqDto;
import com.perfect.center.item.api.dto.response.PerfectItemRespDto;
import com.perfect.center.item.api.dto.response.PerfectServiceItemListRespDto;
import com.perfect.center.item.api.query.IPerfectItemQueryApi;
import com.perfect.center.promotion.api.dto.request.PerfectElectronicCouponQueryReqDto;
import com.perfect.center.promotion.api.dto.request.PerfectFreightCouponQueryReqDto;
import com.perfect.center.promotion.api.dto.request.PerfectNormalCouponQueryReqDto;
import com.perfect.center.promotion.api.dto.response.PerfectCouponGrantRespDto;
import com.perfect.center.promotion.api.query.IPerfectCouponQueryApi;
import com.perfect.center.shop.api.query.IPerfectServiceCenterQueryApi;
import com.perfect.center.trade.api.dto.request.myorderlist.PerfectTradeItemReqDto;
import com.perfect.center.trade.api.dto.response.orderdetail.PerfectTradeAmountByUserAndDateRespDto;
import com.perfect.center.trade.api.dto.response.orderdetail.PerfectTradeItemRespDto;
import com.perfect.center.trade.api.dto.response.sallerreview.PerfectSellerForCrmRespDto;
import com.perfect.center.trade.api.dto.response.sallerreview.PerfectSellerOrderForCrmRespDto;
import com.perfect.center.trade.api.query.IPerfectOrderQueryApi;
import com.perfect.center.trade.api.query.IPerfectTradeItemQueryApi;
import com.perfect.center.user.api.dto.request.PerfectCtsUserReqDto;
import com.perfect.center.user.api.dto.response.PerfectCtsUserInfoRespDto;
import com.perfect.center.user.api.query.IPerfectCtsUserQueryApi;
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

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Controller
@RequestMapping("/adapter/store")
@ResponseBody
@Api(tags = "商城", description = "商城")
@SuppressWarnings("all")
public class StoreController{

    Logger logger = LoggerFactory.getLogger(StoreController.class);
    @Value("${PAGESIZE}")
    private Integer pageSize;

    /**
     * 顾客报单查询
     */
    @Autowired
    private IPerfectOrderQueryApi perfectOrderQueryApi;
    /**
     * 库存列表查询
     */
    @Autowired
    private IItemStorageQueryApi itemStorageQueryApi;
    /**
     * 库存明细查询
     */
    @Autowired
    private IItemStorageLogQueryApi itemStorageLogQueryApi;

    /**
     * 根据会员卡号查询会员信息
     */
    @Autowired
    private IPerfectCtsUserQueryApi perfectCtsUserQueryApi;
    /**
     * 个人实时购物明细
     */
    @Resource(name = "perfectTradeItemQueryApi")
    private IPerfectTradeItemQueryApi perfectTradeItemQueryApi;
    /**
     * 信誉额查询/押货保证金最大限额
     */
    @Autowired
    private IMortgageAccountQueryApi iMortgageAccountQueryApi;

    /**
     * 2.	押货单明细查询
     */
    @Autowired
    private IMortgageOrderQueryApi iMortgageOrderQueryApi;
    /**
     * 3.	核对产品库存
     */
    @Autowired
    private IStorageSnapshotQueryApi iStorageSnapshotQueryApi;


    /**
     * 查询服务中心基本信息
     */
    @Autowired
    private IPerfectServiceCenterQueryApi iPerfectServiceCenterQueryApi;

    /**
     * 根据产品id查询产品信息
     */
    @Autowired
    private IPerfectItemQueryApi iperfectItemQueryApi;
    /**
     * 查询电子礼券 使用情况
     */
    @Autowired
    private IPerfectCouponQueryApi perfectCouponQueryApi;

    /**
     * 组合拆分查询
     */
    @Autowired
    private IItemBundleSplitQueryApi itemBundleSplitQueryApi;

    @Autowired
    private CrmController crmController;


    /**
     * 压货单明细查询
     *
     * @param shopNo  网点编号
     * @param creDate 开单日期
     * @return
     */
    @ApiOperation(value = "压货单明细查询", tags = {"md_压货单明细查询"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopNo", value = "网点编号", required = true,dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "duration", value = "时间段", required = true,dataType = "String", paramType = "query")
    })
    @RequestMapping(value = "/goods/getmortgageInfo", method = RequestMethod.GET)
    public JSONObject selectMortgageInfo(String shopNo, String duration) {
        try {
            List<String> dateList = DateUtil.durationToStartAndEnd(duration, "yyyy-MM-dd HH:mm:ss");
            List resultList = new ArrayList();
            List<MortgageOrderRespDto> respDtoList = null;
            MortgageOrderQueryDto dto = new MortgageOrderQueryDto();
            //网点编号
            dto.setServiceCenterCode(shopNo);
            dto.setStartTime(dateList.get(0));
            dto.setEndTime(dateList.get(1));
            logger.info("调商城接口入参：" + JSON.toJSONString(dto));
            RestResponse<PageInfo<MortgageOrderRespDto>> page = iMortgageOrderQueryApi.queryDetailByPage(dto, 1, pageSize);
            logger.info("调接口返货 压货单信息：" + JSON.toJSONString(page, true));
            if (!"0".equals(page.getResultCode())) {
                return ResultUtil.error("0404", page.getResultMsg());
            }
            if (page.getData() != null && page.getData().getList() != null && page.getData().getList().size() != 0) {
                respDtoList = page.getData().getList();
                for (MortgageOrderRespDto order : respDtoList) {
                    Map<String, Object> mortgageTime = new HashMap<>();
                    mortgageTime.put("mortgageTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            .format(order.getMortgageTime()));
                    order.setExtFields(mortgageTime);
                    List<MortgageItemRespDto> items = order.getMortgageItems();
                    //当前订单下的所有商品
                    List<String> skuCodes = new ArrayList<>();
                    for (int i = 0; i < items.size(); i++) {
                        if (items.get(i).getSkuCode() != null && !"".equals(items.get(i).getSkuCode())) {
                            skuCodes.add(items.get(i).getSkuCode());
                        }
                    }
                    PerfectServiceItemListReqDto perfectServiceItemListReqDto = new PerfectServiceItemListReqDto();
                    perfectServiceItemListReqDto.setSkuCodes(skuCodes);
                    RestResponse<List<PerfectServiceItemListRespDto>> listRestResponse =
                            iperfectItemQueryApi.queryServiceItemList(perfectServiceItemListReqDto);
                    logger.info("调接口返回产品信息：" + JSON.toJSONString(listRestResponse, true));
                    if (!"0".equals(listRestResponse.getResultCode())) {

                        return ResultUtil.error("0404", "调用失败");
                    }
                    List<PerfectServiceItemListRespDto> itemListRespDtos = listRestResponse.getData();
                    //为每个产品添加必要属性
                    for (int i = 0; i < items.size(); i++) {
                        Map<String, Object> resultMap = new HashMap<>();
                        resultMap.put("amount", order.getAmount());
                        resultMap.put("mortgageItemsNum", items.get(i).getNum());
                        resultMap.put("mortgageOrderNo", order.getMortgageOrderNo());
                        resultMap.put("mortgageTime", order.getMortgageTime());
                        if (order.getStatus() != null) {
                            if (order.getStatus() == 0) {
                                resultMap.put("status", "待发货");
                            } else if (order.getStatus() == 1) {
                                resultMap.put("status", "已发货");
                            } else if (order.getStatus() == 2) {
                                resultMap.put("status", "部分发货");
                            } else if (order.getStatus() == 3) {
                                resultMap.put("status", "已收货");
                            } else if (order.getStatus() == 4) {
                                resultMap.put("status", "取消");
                            }
                        } else {
                            logger.error("押货单状态为null");
                        }
                        for (int j = 0; j < itemListRespDtos.size(); j++) {
                            if (itemListRespDtos.get(j).getSkuCode() == null) {
                                logger.error("商品" + itemListRespDtos.get(j).getName() + "skuCode为空，进入下个商品");
                                continue;
                            }
                            if (itemListRespDtos.get(j).getSkuCode().equals(items.get(i).getSkuCode())) {
                                resultMap.put("mortgageItemsExtFields", itemListRespDtos.get(j));
                                break;
                            }
                        }
                        resultList.add(resultMap);
                        if(resultList.size()>80){
                           Map<String,Object> returnMap = new HashMap<>();
                            returnMap.put("data", null);
                            return ResultUtil.setAll("0009","数据超过80条",returnMap);
                        }
                        logger.info("成功 添加商品 ：" + JSON.toJSONString(resultMap, true));
                    }
                }
                return ResultUtil.resultOK(resultList, "data");
            } else {
                return ResultUtil.error("0001", "无数据");
            }
        } catch (Exception e) {
            logger.error("***************异常信息*************");
            e.printStackTrace();
            logger.error("***************异常信息*************");
            return ResultUtil.error("0404", e.getStackTrace().toString());
        }
    }




    /**
     * 核对产品库存
     * 有问题  待修改
     *
     * @param shopNo    网点编号
     * @param itemCode  产品编码
     * @return
     */
    @ApiOperation(value = "核对产品库存", tags = {"md_核对产品库存"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopNo", value = "网点编号", required = true,dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "itemCode", value = "产品编号", required = true,dataType = "String", paramType = "query")
    })
    @RequestMapping(value = "/goods/checkProductInventory", method = RequestMethod.GET)
    public JSONObject queryItemStorageByList(String shopNo, String itemCode) {
        try {
            RestResponse<PerfectItemRespDto> perfectItemRespDtoRestResponse = iperfectItemQueryApi.queryPerfectItemByCode(itemCode);
            logger.info("返回产品信息：" + JSON.toJSONString(perfectItemRespDtoRestResponse, SerializerFeature.WriteMapNullValue));
            if (!"0".equals(perfectItemRespDtoRestResponse.getResultCode())) {
                return ResultUtil.error("0404", "调用失败");
            }
            PerfectItemRespDto item = perfectItemRespDtoRestResponse.getData();
            if (item == null) {
                return ResultUtil.error("0001", "查询不到该产品信息");
            }
            ItemStorageQueryDto reDto = new ItemStorageQueryDto();
            for (int i = 0; i < 3; i++) {
                RestResponse<ShopDto> shopDtoRestResponse = iPerfectServiceCenterQueryApi.queryShopIdByCode(shopNo);
                if ("0".equals(shopDtoRestResponse.getResultCode())) {
                    Long id = shopDtoRestResponse.getData().getId();
                    if (id != null) {
                        reDto.setServiceCenterId(id);
                    }
                    break;
                }
                System.out.println("查询服务中心id 第" + (i + 1) + "次失败");
            }
            reDto.setSkuCode(itemCode);
            logger.info("传入参数：" + JSON.toJSONString(reDto, SerializerFeature.WriteMapNullValue));
            RestResponse<List<ItemStorageRespDto>> listRestResponse = itemStorageQueryApi.queryItemStorageByList(reDto);
            logger.info("返回参数：" + JSON.toJSONString(listRestResponse, SerializerFeature.WriteMapNullValue));
            if (!"0".equals(listRestResponse.getResultCode())) {
                return ResultUtil.error("0404", "调用失败");
            }
            List<ItemStorageRespDto> data = listRestResponse.getData();
            if (data == null || data.size() == 0) {
                return ResultUtil.error("0001", "无数据");
            }
            List resultList = new ArrayList();
            for (int i = 0; i < data.size(); i++) {
                JSONObject object = JSON.parseObject(JSON.toJSONString(data.get(i), SerializerFeature.WriteMapNullValue));
                object.put("itemName", item.getName());
                object.put("skuCode", data.get(i).getSkuCode().split("&")[1]);
                object.put("unit",item.getMeteringUnit());
                resultList.add(object);
            }
            Map<String ,Object> resultMap=new HashMap<>();
            resultMap.put("currentTime",new Date());
            resultMap.put("data",resultList);
            return ResultUtil.setAll("0000", "有数据",resultMap);
        } catch (Exception e) {
            logger.info("*****************************");
            e.printStackTrace();
            return ResultUtil.error("0404","调用失败");
        }

    }

    /**
     * 押货保证金最大限额
     *
     * @param shopNo 网点编号
     * @param month  资料月份
     * @return
     */
    @ApiOperation(value = "押货保证金最大限额", tags = {"词槽"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopNo", value = "网点编号", required = true,dataType = "String", paramType = "query")
    })
    @RequestMapping(value = "/maxquota", method = RequestMethod.GET)
    public JSONObject maxQuota(String shopNo) {
        try {
//            Date yyyyMMdd = new SimpleDateFormat("yyyyMMdd").parse(date);
//            String format = new SimpleDateFormat("yyyy-MM-dd").format(yyyyMMdd);
//            LocalDate localDate = LocalDate.parse(format);
            MortgageAccountQueryDto dto = new MortgageAccountQueryDto();
            //开始时间
//            dto.setStartTime(localDate.with(TemporalAdjusters.firstDayOfMonth()).toString());
            //结束时间
//            dto.setEndTime(localDate.with(TemporalAdjusters.lastDayOfMonth()).toString());
            //类型(1押货额度,2信誉额,3已汇款总额)
            dto.setType(1);
            //网点编号
            dto.setServiceCenterCode(shopNo);
            logger.info("调接口 参入参数:" + JSON.toJSONString(dto));
            RestResponse<MortgageAccountRespDto> mortgageAccountRespDto = iMortgageAccountQueryApi.queryMortgageAccount(dto);
            logger.info("调接口 返回结果:" + JSON.toJSONString(mortgageAccountRespDto));
            if (!"0".equals(mortgageAccountRespDto.getResultCode())) {
                return ResultUtil.error("0001", "无数据-接口报错");
            }
            MortgageAccountRespDto respDto = mortgageAccountRespDto.getData();
            if (respDto == null) {
                return ResultUtil.error("0001", "无数据");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("data", respDto);
            return ResultUtil.setAll("0000", "成功", map);
        } catch (Exception e) {
            logger.error("**************************************************");
            e.printStackTrace();
            return ResultUtil.error("0001", "无数据-接口报错");
        }

    }

    /**
     * 查询用户电子礼券使用情况
     *
     * @param month  月份
     * @param shopNo 卡号
     * @return
     */
    @ApiOperation(value = "查询用户电子礼券使用情况", tags = {"md_查询用户电子礼券使用情况(时间段)"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "duration", value = "时间段", required = true,dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "cardCode", value = "会员卡号", required = true,dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "typeDescription", value = "优惠券类型", required = true,defaultValue = "电子礼券",dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "statusDescription", value = "优惠券状态", required = true,defaultValue = "未使用",dataType = "String", paramType = "query")
    })
    @RequestMapping(value = "/userInfo/getElectronicCoupnGrantByStatusAndType", method = RequestMethod.GET)
    public JSONObject getElectronicCoupnGrantByStatus(String duration, @RequestParam("cardCode") String shopNo
            , String typeDescription, String statusDescription) {
        List resultList = new ArrayList();
//        返回结果
        RestResponse<PageInfo<PerfectCouponGrantRespDto>> response=null;
        try {
            String[] split = duration.split("~");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
            Date startDate = sdf.parse(split[0]);

            Date endDate0 = sdf.parse(split[1]);
            String lastDayOfMonth = DateUtil.getLastDayOfMonth(endDate0);
            Date endDate = sdf.parse(lastDayOfMonth);
            SimpleDateFormat sfm = new SimpleDateFormat("yyyyMMdd");
            SimpleDateFormat monthFormat = new SimpleDateFormat("yyyyMM");
            Integer startMonth = Integer.parseInt(monthFormat.format(startDate));
            Integer endMonth = Integer.parseInt(monthFormat.format(endDate));
            Integer typeValue = 2;
            if ("满减优惠券".equals(typeDescription.trim())) {
                typeValue = 1;
                PerfectNormalCouponQueryReqDto  reqDto = new PerfectNormalCouponQueryReqDto();
                reqDto.setType(typeValue);
                reqDto.setMemberCardNo(shopNo);
                reqDto.setStartClaimTime(startDate);
                reqDto.setEndClaimTime(endDate);
                logger.info("调满减优惠券入参：" + JSON.toJSONString(reqDto));
                response=perfectCouponQueryApi.queryCouponGrantByStatus(reqDto,1,pageSize);
            } else if ("电子礼券".equals(typeDescription.trim())) {
                typeValue = 2;
                PerfectElectronicCouponQueryReqDto  reqDto = new PerfectElectronicCouponQueryReqDto ();
                reqDto.setType(typeValue);
                reqDto.setMemberCardNo(shopNo);
                reqDto.setStartClaimTime(startDate);
                reqDto.setEndClaimTime(endDate);
                logger.info("调电子礼券入参：" + JSON.toJSONString(reqDto));
                response=perfectCouponQueryApi.queryElectronicCouponGrantByStatus(reqDto,1,pageSize);
            } else if ("运费抵扣券".equals(typeDescription.trim())) {
                typeValue = 3;
                PerfectFreightCouponQueryReqDto reqDto = new PerfectFreightCouponQueryReqDto ();
                reqDto.setType(typeValue);
                reqDto.setMemberCardNo(shopNo);
                reqDto.setStartClaimTime(startDate);
                reqDto.setEndClaimTime(endDate);
                logger.info("调运费抵扣券入参：" + JSON.toJSONString(reqDto));
                response=perfectCouponQueryApi.queryFreightCouponGrantByPage(reqDto,1,pageSize);
            }
            logger.info("电子礼券返回信息：" + JSON.toJSONString(response, SerializerFeature.WriteMapNullValue));
            if (!"0".equals(response.getResultCode())) {
                return ResultUtil.error("0404", "调用失败");
            }
            PageInfo<PerfectCouponGrantRespDto> data = response.getData();
            List<PerfectCouponGrantRespDto> list1 = data.getList();
            if (list1 == null || list1.size() == 0) {
                if (startMonth.equals(endMonth)) {
                    return ResultUtil.error("0002", "单月(有数据,返回结果中不包含'类型')");
                } else if (startMonth < endMonth) {
                    return ResultUtil.error("0005", "多月(有数据,返回结果中不包含'类型')");
                }
            } else {
                //查询数据不为空
                for (int i = 0; i < list1.size(); i++) {
                    list1.get(i).getShopCode();
                    Integer status = list1.get(i).getStatus();
                    Integer type = list1.get(i).getType();
                    Date effectiveBeginTime = list1.get(i).getEffectiveBeginTime();
                    Date effectiveEndTime = list1.get(i).getEffectiveEndTime();
                    Date claimTime = list1.get(i).getClaimTime();
                    Date useTime = list1.get(i).getUseTime();
                    BigDecimal voucherValue = list1.get(i).getVoucherValue().setScale(2);
                    JSONObject object = JSON.parseObject(JSON.toJSONString(list1.get(i)));
                    object.put("typeDescription", "满减优惠券");
                    object.put("voucherValue", voucherValue);
                    if (type == 1) {
                        object.put("typeDescription", "满减优惠券");
                    } else if (type == 2) {
                        object.put("typeDescription", "电子礼券");
                    } else if (type == 3) {
                        object.put("typeDescription", "运费抵扣券");
                    }
                    if (effectiveBeginTime != null) {
                        object.put("effectiveBeginTime", new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(effectiveBeginTime));
                    }
                    if (effectiveEndTime != null) {
                        object.put("effectiveEndTime", new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(effectiveEndTime));
                    }
                    if (claimTime != null) {
                        object.put("claimTime", new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(claimTime));
                    }
                    if (useTime != null) {
                        object.put("useTime", new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(useTime));
                    }
                    if (status == 1 && "已使用".equals(statusDescription.trim())) {
                        object.put("statusDescription", "已使用");
                        resultList.add(object);
                    }
                    if (status == 2 && "占用中".equals(statusDescription.trim())) {
                        object.put("statusDescription", "占用中");
                        resultList.add(object);
                    }
                    if (status == 3 && "未使用".equals(statusDescription.trim())) {
                        object.put("statusDescription", "未使用");
                        resultList.add(object);
                    }
                    if (status == 4 && "已失效".equals(statusDescription.trim())) {
                        object.put("statusDescription", "已失效");
                        resultList.add(object);
                    }
                    if (status == 5 && "已作废".equals(statusDescription.trim())) {
                        object.put("statusDescription", "已作废");
                        resultList.add(object);
                    }
                }
                if (resultList.size() != 0) {
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("data", resultList);

                    if (startMonth.equals(endMonth)) {
                        return ResultUtil.setAll("0000", "单月(有数据,返回结果中包含“使用状态”,'类型')", resultMap);
                    } else if (startMonth < endMonth) {
                        return ResultUtil.setAll("0003", "多月(有数据,返回结果中包含“使用状态”,'类型')", resultMap);
                    }
                } else {
                    if (startMonth.equals(endMonth)) {
                        return ResultUtil.setAll("0001", "单月(有数据,返回结果中包含“使用状态”,'类型')", null);
                    } else if (startMonth < endMonth) {
                        return ResultUtil.setAll("0004", "多月(有数据,返回结果中包含“使用状态”,'类型')", null);
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return ResultUtil.error("0404", "调用失败");
        }
        return ResultUtil.resultOK(resultList, "data");
    }

    /**
     * 个人实时购货明细及积分
     *
     * @param cardCode 卡号
     * @param month    月份
     * @return
     */
    @ApiOperation(value = "个人实时购货明细及积分", tags = {"md_查积分"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "duration", value = "时间段", required = true,dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "cardCode", value = "会员卡号", required = true,dataType = "String", paramType = "query")
    })
    @RequestMapping(value = "/userInfo/queryTradeItems", method = RequestMethod.GET)
    public JSONObject queryTradeItems(String cardCode, String duration) {
        List<String> dateList = DateUtil.durationToStartAndEnd(duration, "yyyy-MM");
        String startDate = dateList.get(0);
        String endDate = dateList.get(1);
        String userId = "";
        List resultList = new ArrayList();
        try {
            for (int i = 0; i < 3; i++) {
                try {
                    PerfectCtsUserReqDto reUserDto = new PerfectCtsUserReqDto();
                    reUserDto.setMemberCardNum(cardCode);
                    logger.info("个人实时购货明细：" + JSON.toJSONString(reUserDto));
                    RestResponse<PerfectCtsUserInfoRespDto> respUser = perfectCtsUserQueryApi.queryUserByDto(reUserDto);
                    if ("0".equals(respUser.getResultCode()) && respUser.getData() != null) {
                        userId = respUser.getData().getId() + "";
                        break;
                    } else {
                        continue;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
            PerfectTradeItemReqDto reqDte = new PerfectTradeItemReqDto();
            reqDte.setUserId(userId);
            //测试
            reqDte.setPageNum(1);
            reqDte.setPageSize(pageSize);
            LocalDate localDate = LocalDate.parse(startDate + "-01");
            //开始时间
            reqDte.setStartTime(localDate.with(TemporalAdjusters.firstDayOfMonth()).toString());
            //结束时间
            reqDte.setEndTime(DateUtil.getNextDay(localDate.with(TemporalAdjusters.lastDayOfMonth()).toString(), -1).toString());
            RestResponse<PageInfo<PerfectTradeItemRespDto>> restResponse = perfectTradeItemQueryApi.queryTradeItems(reqDte);
            if (!"0".equals(restResponse.getResultCode())) {
                logger.error("状态码：" + restResponse.getResultCode() + ",信息：" + restResponse.getResultMsg());
                return ResultUtil.error("0404", "调用失败");
            }
            Map<String, Object> map = new HashMap<>();
            PageInfo<PerfectTradeItemRespDto> data = restResponse.getData();
            if (data == null) {
                return ResultUtil.error("0001", "无数据");
            }
            List<PerfectTradeItemRespDto> list = data.getList();
            if (list == null || list.size() == 0) {
                return ResultUtil.error("0001", "无数据");
            } else {
                for (int i = 0; i < list.size(); i++) {
                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("createTime", list.get(i).getCreateTime());
                    itemMap.put("itemCode", list.get(i).getItemCode());
                    itemMap.put("itemName", list.get(i).getItemName());
                    itemMap.put("itemNum", list.get(i).getItemNum());
                    itemMap.put("itemPrice", list.get(i).getItemPrice());
                    itemMap.put("meteringUnit", list.get(i).getMeteringUnit());
                    itemMap.put("pv", list.get(i).getPv());
                    itemMap.put("returnNumber", list.get(i).getReturnNumber());
                    itemMap.put("totalAmount", list.get(i).getTotalAmount().setScale(2));
                    itemMap.put("totalPv", list.get(i).getTotalPv());
                    itemMap.put("tradeNo", list.get(i).getTradeNo());
                    itemMap.put("shopCode", list.get(i).getShopCode());
                    resultList.add(itemMap);
                    if(resultList.size()>80){
                        resultList=null;
                        break;
                    }

                }
                map.put("list", resultList);

            }
            logger.info("时间：" + localDate.with(TemporalAdjusters.firstDayOfMonth()).toString());
            logger.info("时间：" + DateUtil.getNextDay(localDate.with(TemporalAdjusters.lastDayOfMonth()).toString(), -1).toString());
            logger.info("userId：" + Long.parseLong(userId));
            RestResponse<PerfectTradeAmountByUserAndDateRespDto> response = perfectOrderQueryApi.queryTradeAmountByUserAndDate(localDate.with(TemporalAdjusters.firstDayOfMonth()).toString(),
                    DateUtil.getNextDay(localDate.with(TemporalAdjusters.lastDayOfMonth()).toString(), -1).toString(), Long.parseLong(userId));
            logger.info(JSON.toJSONString("返回总订单pv：" + response));
            logger.info(JSON.toJSONString(response.getData()));
            if ("0".equals(response.getResultCode())) {
                map.put("totalPv", response.getData().getTotalPv());
            } else {
                logger.error("状态码：" + response.getResultCode() + ",信息：" + response.getResultMsg());
                return ResultUtil.error("0404", "调用失败");
            }
            if(resultList==null){
                JSONObject resultMap = new JSONObject();
                resultMap.put("returnCode", "0009");
                resultMap.put("returnContent", map);
                resultMap.put("returnMessage", "数据大于80条");
                return resultMap;
            }
            JSONObject resultMap = new JSONObject();
            resultMap.put("returnCode", "0000");
            resultMap.put("returnContent", map);
            resultMap.put("returnMessage", "有数据");
            return resultMap;
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return ResultUtil.error("0004", "调用失败");
        }
    }

    /**
     * 上月顾客报单查询
     *
     * @param cardCode 卡号
     * @param duration 月份 格式yyyyMM
     * @return
     */
    @ApiOperation(value = "上月顾客报单查询", tags = {"md_查积分"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "duration", value = "时间段", required = true,dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "cardCode", value = "会员卡号", required = true,dataType = "String", paramType = "query")
    })
    @RequestMapping(value = "/userInfo/queryOrderListForCrm", method = RequestMethod.GET)
    public JSONObject queryOrderListForCrm(String cardCode, String duration) {
        String[] split = duration.split("~");
        String startDate = DateUtil.dataFormatter(DateUtil.dataParse(split[0], "yyyyMMdd HH:mm:ss"), "yyyyMM");
        String endDate = DateUtil.dataFormatter(DateUtil.dataParse(split[1], "yyyyMMdd HH:mm:ss"), "yyyyMM");
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
            RestResponse<PerfectSellerForCrmRespDto> response = perfectOrderQueryApi
                    .queryOrderListForCrm(sdf.parse(startDate), cardCode);
            logger.info("返回结果：" + JSON.toJSONString(response));
            if (!"0".equals(response.getResultCode())) {
                return ResultUtil.error("0404", "调用失败");
            }
            PerfectSellerForCrmRespDto data = response.getData();
            if (data == null) {
                return ResultUtil.error("0001", "无数据");
            }
            List<PerfectSellerOrderForCrmRespDto> orderList = data.getOrderList();
            if (orderList == null || orderList.size() == 0) {
                return ResultUtil.error("0001", "无数据");
            }
            if(orderList.size()>80){
                data.setOrderList(null);
                Map<String ,Object> resultMap=new HashMap<>();
                resultMap.put("data",data);
                return ResultUtil.setAll("0009", "data",resultMap);
            }
            for (int i = 0; i < orderList.size(); i++) {
                orderList.get(i).setItemInfo(null);
            }
            return ResultUtil.resultOK(response.getData(), "data");
        } catch (ParseException e) {
            e.printStackTrace();
            return ResultUtil.error("0001", "联系管理员");
        }
    }


}