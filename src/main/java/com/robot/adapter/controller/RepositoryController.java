package com.robot.adapter.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.dtyunxi.rest.RestResponse;
import com.github.pagehelper.PageInfo;
import com.perfect.center.inventory.api.dto.request.MortgageOrderQueryDto;
import com.perfect.center.inventory.api.dto.response.MortgageItemRespDto;
import com.perfect.center.inventory.api.dto.response.MortgageOrderRespDto;
import com.perfect.center.inventory.api.query.IMortgageOrderQueryApi;
import com.perfect.center.item.api.dto.request.PerfectServiceItemListReqDto;
import com.perfect.center.item.api.dto.response.PerfectServiceItemListRespDto;
import com.perfect.center.item.api.query.IPerfectItemQueryApi;
import com.perfect.third.integration.api.dto.response.mall.ProConDayFareDataRespDto;
import com.perfect.third.integration.api.dto.response.mall.ProDeliveryProductRespDto;
import com.perfect.third.integration.api.dto.response.mall.ProDeliveryRecordRespDto;
import com.perfect.third.integration.api.dto.response.mall.ProOrderDeliveryRespDto;
import com.perfect.third.integration.api.query.mall.IProcedureQueryApi;
import com.robot.adapter.model.Product;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
@Controller
@ResponseBody
@RequestMapping("/adapter/repository")
@Api(tags = "完美旧仓储", description = "完美旧仓储")
public class RepositoryController  {
    Logger logger = LoggerFactory.getLogger(RepositoryController.class);
    /**
     * 压货单明细查询服务
     */
    @Autowired
    private IMortgageOrderQueryApi iMortgageOrderQueryApi;
    /**
     * 仓库服务
     */
    @Autowired
    private IProcedureQueryApi hsfProceduceQueryApi;
    /**
     * 根据产品id查询产品信息
     */
    @Autowired
    private IPerfectItemQueryApi iperfectItemQueryApi;

    @Value("${PAGESIZE}")
    private Integer pageSize;

    /**
     * 24号接口：押货单发货查询
     *
     * @param shopNo  网点编号
     * @param creDate 开单日期  格式2018-12-12
     * @return
     */
    @ApiOperation(value = "押货单发货查询", tags = {"md_押货单发货查询"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopNo", value = "网点编号", required = true,dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "duration", value = "开单时间段", required = true,dataType = "String", paramType = "query")
    })
    @RequestMapping(value = "/goods/getOrderDelivery", method = RequestMethod.GET)
    public JSONObject getOrderDelivery(String shopNo, String duration) {
        //发货单发货列表
        String[] split = duration.split("~");
        List list = new ArrayList();
        List<MortgageOrderRespDto> respDtoList = null;
        MortgageOrderQueryDto dto = new MortgageOrderQueryDto();
        //设置开单日期
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
            SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dto.setStartTime(simpleDateFormat1.format(simpleDateFormat.parse(split[0])));
            dto.setEndTime(simpleDateFormat1.format(simpleDateFormat.parse(split[1])));
            //网点编号
            dto.setServiceCenterCode(shopNo);
            logger.info("调接口入参："+JSON.toJSONString(dto));
            //订货单号列表
            RestResponse<PageInfo<MortgageOrderRespDto>> page = iMortgageOrderQueryApi.queryDetailByPage(dto, 1, pageSize);
            logger.info("返回数据："+JSON.toJSONString(page,SerializerFeature.WriteMapNullValue));
            if (!"0".equals(page.getResultCode())) {
                return ResultUtil.error("0001", page.getResultMsg());
            }
            if(page.getData()==null){
                return ResultUtil.error("0001","无数据");
            }
            respDtoList = page.getData().getList();
            if (respDtoList == null || respDtoList.size() == 0) {
                return ResultUtil.error("0001","无数据");
            }
//            遍历订单列表
            for (MortgageOrderRespDto order : respDtoList) {
                //商品总数量Map  商品编码 为Key，商品数量为value
//                Map<String, Integer> itemMap = new HashMap<>();
                String mortgageOrderNo = order.getMortgageOrderNo();
//                //获取订单中的产品列表
//                List<MortgageItemRespDto> items = order.getMortgageItems();
//                PerfectServiceItemListReqDto reqDto = new PerfectServiceItemListReqDto();
//                List<String> skuCodeList = new ArrayList<>();
//                for (int i = 0; i < items.size(); i++) {
//                    skuCodeList.add(items.get(i).getSkuCode());
//                }
//                reqDto.setSkuCodes(skuCodeList);
//                try {
//                    //根据产品skuCode查询产品的详细信息
//                    RestResponse<List<PerfectServiceItemListRespDto>> responseData = iperfectItemQueryApi.queryServiceItemList(reqDto);
//                    logger.info("返回商品列表："+JSON.toJSONString(responseData,true));
////                    if ("0".equals(responseData.getResultCode())) {
////                        if(responseData.getData()==null||responseData.getData().size()==0){
////                            logger.error("订单:" + order.getMortgageOrderNo() + " 查询商品失败!,查询下一个订单");
////                            continue;
////                        }
////                        List<PerfectServiceItemListRespDto> data1 = responseData.getData();
////                        for (int i = 0; i < items.size(); i++) {
////                            for (int j = 0; j < data1.size(); j++) {
////                                if (items.get(i).getSkuCode().equals(data1.get(j).getSkuCode())) {
//////                                    itemMap.put(data1.get(j).getCode()
//////                                            , items.get(i).getNum());
////                                    logger.info("添加 商品skuCode：" + items.get(i).getSkuCode() + "总数量成功！ ");
////                                    break;
////                                }
////                                logger.warn("没找到商品 skuId：" + items.get(i).getSkuCode());
////                            }
////                        }
////                    } else {
////                        return ResultUtil.error(responseData.getResultCode(), responseData.getResultMsg());
////                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    logger.error("订单:" + order.getMortgageOrderNo() + " 失败!,查询下一个订单");
//                    continue;
//                }
                try {
                    logger.info("调接口传入参数："+JSON.toJSONString(order.getMortgageOrderNo()));
                    RestResponse<ProOrderDeliveryRespDto> delivery = hsfProceduceQueryApi.getOrderDelivery(order.getMortgageOrderNo());
                    logger.info("调接口返回数据："+JSON.toJSONString(delivery,SerializerFeature.WriteMapNullValue));
                    if (!"0".equals(delivery.getResultCode())) {
                        //如果当前订单查不到数据  进入下一个订单
                        logger.info("错误信息：" + delivery.getResultMsg());
                        continue;
                    }
                    //当前压货单数据
                    ProOrderDeliveryRespDto respDto = delivery.getData();
                    //压货单状态
                    String deliveryStatus = respDto.getDeliveryStatus();
                    //发货单商品列表
                    List<ProDeliveryRecordRespDto> deliveryRecords = respDto.getDeliveryRecord();
                    if(deliveryRecords==null||deliveryRecords.size()==0){
                        logger.info("查询无数据，进入下个订单");
                        continue;
                    }
                    for (int i = 0; i < deliveryRecords.size(); i++) {
                        List<ProDeliveryProductRespDto> deliveryProducts = deliveryRecords.get(i)
                                .getDeliveryProduct();
                        for (int j = 0; j < deliveryProducts.size(); j++) {
                            deliveryProducts.get(j).getProductCode();
                            Map<String, Object> map = new HashMap<>();
                            map.put("arrivalTime", deliveryRecords.get(i).getArrivalTime());
                            map.put("deliveryTime", deliveryRecords.get(i).getDeliveryTime());
                            map.put("shipmentNo", deliveryRecords.get(i).getShipmentNo());
                            //压货单发货状态
                            map.put("orderStatus", deliveryStatus);
//                            if ("0".equals(deliveryStatus)) {
//
//                            } else if ("1".equals(deliveryStatus)) {
//                                map.put("orderStatus", "已发货");
//                            } else if ("2".equals(deliveryStatus)) {
//                                map.put("orderStatus", "部分发货");
//                            } else if ("3".equals(deliveryStatus)) {
//                                map.put("orderStatus", "已收货");
//                            } else if ("4".equals(deliveryStatus)) {
//                                map.put("orderStatus", "取消");
//                            }
//                            map.put("productNum", itemMap.get(deliveryProducts.get(j).getProductCode()));
                            map.put("mortgageTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                    .format(order.getMortgageTime()));
                            map.put("mortgageOrderNo", mortgageOrderNo);
                            map.put("deliveryProductFormat", deliveryProducts.get(j).getFormat());
                            map.put("deliveryProductNum", deliveryProducts.get(j).getNum());
                            map.put("deliveryProductProductCode", deliveryProducts.get(j).getProductCode());
                            map.put("deliveryProductProductName", deliveryProducts.get(j).getProductName());
                            map.put("deliveryProductUnit", deliveryProducts.get(j).getUnit());
                            list.add(map);
                            if(list.size()>80){
                                Map<String, Object> resultMap = new HashMap<>();
                                list=null;
                                resultMap.put("deliveryRecord", list);
                                return ResultUtil.setAll("0009","数据超过80条",resultMap);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("****************************************");
                    e.printStackTrace();
                    logger.error("当前订单查询失败，进入下一个订单" + order.getMortgageOrderNo());
                }
            }
        } catch (Exception e) {
            logger.info("**************异常信息**************");
            e.printStackTrace();
            logger.info("**************异常信息**************");
            return ResultUtil.error("0404", "调用失败");
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("deliveryRecord", list);
        if(list.size()>0){
            return ResultUtil.setAll("0000","有数据",resultMap);
        }else {
            return ResultUtil.setAll("0001","无数据",resultMap);
        }

    }

    /**
     * 26号场景： 合同期查询
     * 还没有数据，带联调
     *
     * @param shopNo 网点编号
     * @return
     */
    @ApiOperation(value = "合同期查询", tags = {"词槽"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopNo", value = "网点编号", required = true,dataType = "String", paramType = "query"),
    })
    @RequestMapping(value = "/getContractDays", method = RequestMethod.GET)
    public JSONObject getContractDays(String shopNo) {
        try {
            logger.info("传入参数："+shopNo);
            RestResponse<List<ProConDayFareDataRespDto>> daysAndFare = hsfProceduceQueryApi.getContractDaysAndFare(shopNo);
            logger.info("返回参数："+JSON.toJSONString(daysAndFare, SerializerFeature.WriteMapNullValue));
            if (!"0".equals(daysAndFare.getResultCode())) {
                return ResultUtil.error("0001", daysAndFare.getResultMsg());
            }
            Map<String,Object> map=new HashMap<>();
            if(daysAndFare.getData()==null||daysAndFare.getData().size()==0){
                logger.info("无数据");
                return ResultUtil.error("0001", daysAndFare.getResultMsg());
            }
            map.put("data",daysAndFare.getData().get(0));
            return ResultUtil.setAll("0000", "成功",map);
        } catch (Exception e) {
            logger.error("*******************************************************");
            e.printStackTrace();
            return ResultUtil.error("0001", "无数据-接口报错");
        }
    }

    /**
     * 25号场景 ：核对发货明细
     *  已和发货单 合并，暂时弃用
     * @param shopNo  网点编号
     * @param creDate 开单日期
     * @return
     */
    @RequestMapping(value = "/checkDelivery", method = RequestMethod.GET)
    public JSONObject checkDelivery(String shopNo, @RequestParam(value = "duration.start") String durationStart
            , @RequestParam(value = "duration.end") String durationEnd) {
        //发货单发货列表
        List list = new ArrayList();
        List<MortgageOrderRespDto> respDtoList = null;
        MortgageOrderQueryDto dto = new MortgageOrderQueryDto();
        //设置开单日期
        LocalDateTime _durationStart = DateUtil.dataParse(durationStart, "yyyyMMdd HH:mm:ss");
        LocalDateTime _durationEnd = DateUtil.dataParse(durationEnd, "yyyyMMdd HH:mm:ss");
        dto.setStartTime(DateUtil.dataFormatter(_durationStart, "yyyy-MM-dd HH:mm:ss"));
        dto.setEndTime(DateUtil.dataFormatter(_durationEnd, "yyyy-MM-dd HH:mm:ss"));
        //网点编号
        dto.setServiceCenterCode(shopNo);
        try {
            //订货单号列表
            RestResponse<PageInfo<MortgageOrderRespDto>> page = iMortgageOrderQueryApi.queryDetailByPage(dto, 1, pageSize);
            if (!"0".equals(page.getResultCode())) {
                return ResultUtil.error("0001", page.getResultMsg());
            }
            respDtoList = page.getData().getList();
            if (respDtoList == null || respDtoList.size() == 0) {
                return ResultUtil.resultOK();
            }
            //遍历订单列表
            for (MortgageOrderRespDto order : respDtoList) {
                //商品总数量Map  商品编码 为Key，商品数量为value
                Map<String, Integer> itemMap = new HashMap<>();
                //获取订单号
                String mortgageOrderNo = order.getMortgageOrderNo();
                //获取订单中的产品列表
                List<MortgageItemRespDto> items = order.getMortgageItems();
                PerfectServiceItemListReqDto reqDto = new PerfectServiceItemListReqDto();
                List<String> skuCodeList = new ArrayList<>();
                for (int i = 0; i < items.size(); i++) {
                    Integer num = items.get(i).getNum();
                    logger.info(JSON.toJSONString(items.get(i), true));
                    if (items.get(i).getSkuCode() != null) {
                        skuCodeList.add(items.get(i).getSkuCode());
                    }
                }
                reqDto.setSkuCodes(skuCodeList);
                try {
                    //根据产品skuId查询产品的详细信息
                    RestResponse<List<PerfectServiceItemListRespDto>> responseData = iperfectItemQueryApi.queryServiceItemList(reqDto);
                    if ("0".equals(responseData.getResultCode())) {
                        if (responseData.getData() == null) {
                            logger.warn("查询不到该订单下商品的详细信息：" + order.getMortgageOrderNo());
                            logger.warn("进入下一个订单");
                            continue;
                        }
                        List<PerfectServiceItemListRespDto> data1 = responseData.getData();
                        for (int i = 0; i < items.size(); i++) {
                            for (int j = 0; j < data1.size(); j++) {
                                if (data1.get(j).getSkuCode().equals(items.get(i).getSkuCode())) {
                                    itemMap.put(data1.get(j).getCode()
                                            , items.get(i).getNum());
                                    logger.info("添加 商品skuCode：" + items.get(i).getSkuCode() + "总数量成功！ ");
                                    break;
                                }
                                logger.warn("没找到商品 skuId：" + items.get(i).getSkuCode());
                            }
                        }
                    } else {
                        return ResultUtil.error(responseData.getResultCode(), responseData.getResultMsg());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("订单:" + order.getMortgageOrderNo() + " 失败!,查询下一个订单");
                    continue;
                }
                try {
                    //已发货的订单信息
                    RestResponse<ProOrderDeliveryRespDto> delivery = hsfProceduceQueryApi.getOrderDelivery(mortgageOrderNo);
                    //未发货的订单信息
                    if (!"0".equals(delivery.getResultCode())) {
                        //如果当前订单查不到数据  进入下一个订单
                        continue;
                    }
                    //当前压货单数据
                    ProOrderDeliveryRespDto respDto = delivery.getData();
                    //压货单的商品列表List<ProDeliveryRecordDto> pros =
                    List<ProDeliveryRecordRespDto> pros = respDto.getDeliveryRecord();
                    for (int i = 0; i < pros.size(); i++) {
                        //原先产品集合 List<ProDeliveryProductDto> products =
                        List<ProDeliveryProductRespDto> products = pros.get(i).getDeliveryProduct();
                        //扩展产品集合
                        List<ProDeliveryProductRespDto> productsVo = new ArrayList<>();
                        for (int j = 0; j < products.size(); j++) {
                            Product productvo = new Product(products.get(j));
                            productvo.setMap(new HashMap<>());
                            System.out.println(JSON.toJSONString(productvo));
                            //发货总数
                            Integer num = itemMap.get(productvo.getProductCode());
                            //已发货数
                            Integer num1 = Math.toIntExact(productvo.getNum());
                            //剩余数量
                            Integer residue = num - num1;
                            //设置剩余数量
                            productvo.getMap().put("residue", residue);
                            //重置总发货数
                            itemMap.put(productvo.getProductCode(), residue);
                            productsVo.add(productvo);
                        }
                        //清空原先产品列表数据
                        products.clear();
                        //添加扩展 产品集合
                        products.addAll(productsVo);
                        pros.get(i).setDeliveryProduct(products);
                    }
                    respDto.setDeliveryRecord(pros);
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("orderNo", mortgageOrderNo);
                    resultMap.put("orderInfo", respDto);
                    list.add(resultMap);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("失败订单号：" + mortgageOrderNo);
                    //查询订单失败，进入下一个订单
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.error("0001", "信息错误");
        }
        return ResultUtil.resultOK(list, "data");
    }

    /**
     * 27号场景 货运跟踪
     *
     * @param shopNo  网点编号
     * @param creDate 开单日期
     * @return
     */
    @RequestMapping(value = "/getCargoTracking", method = RequestMethod.GET)
    public JSONObject getCargoTracking(String shopNo, @RequestParam(value = "duration.start") String durationStart
            , @RequestParam(value = "duration.end") String durationEnd) {
        //发货单发货列表
        List list = new ArrayList();
        List<MortgageOrderRespDto> respDtoList = null;
        MortgageOrderQueryDto dto = new MortgageOrderQueryDto();
        //设置开单日期
        LocalDateTime _durationStart = DateUtil.dataParse(durationStart, "yyyyMMdd HH:mm:ss");
        LocalDateTime _durationEnd = DateUtil.dataParse(durationEnd, "yyyyMMdd HH:mm:ss");
        dto.setStartTime(DateUtil.dataFormatter(_durationStart, "yyyy-MM-dd HH:mm:ss"));
        dto.setEndTime(DateUtil.dataFormatter(_durationEnd, "yyyy-MM-dd HH:mm:ss"));
        //网点编号
        dto.setServiceCenterCode(shopNo);
        try {
            //订货单号列表
            RestResponse<PageInfo<MortgageOrderRespDto>> page = iMortgageOrderQueryApi.queryDetailByPage(dto, 1, pageSize);
            if (!"0".equals(page.getResultCode())) {
                return ResultUtil.error("0001", page.getResultMsg());
            }
            respDtoList = page.getData().getList();
            if (respDtoList == null || respDtoList.size() == 0) {
                return ResultUtil.resultOK();
            }
//            遍历订单列表
            for (MortgageOrderRespDto order : respDtoList) {
//            获取压货单号
                try {
                    String mortgageOrderNo = order.getMortgageOrderNo();
                    RestResponse<ProOrderDeliveryRespDto> delivery = hsfProceduceQueryApi.getOrderDelivery(mortgageOrderNo);
                    if (!"0".equals(delivery.getResultCode())) {
                        //如果当前订单查不到数据  进入下一个订单
                        System.out.println("错误");
                        continue;
                    }
                    //当前压货单数据
                    ProOrderDeliveryRespDto respDto = delivery.getData();
                    //当前押货数据加入集合
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("orderNo", order.getMortgageOrderNo());
                    //录单日期 对应押货日期
                    resultMap.put("orderTime", new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(order.getMortgageTime()));
                    resultMap.put("orderInfo", respDto);
                    list.add(resultMap);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("查询异常单号：" + order.getMortgageOrderNo());
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.error("0001", e.getMessage());
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("orders", list);
        //合同期查询RestResponse<ProConDayFareDataRespDto> daysAndFare =
        try {
            RestResponse<List<ProConDayFareDataRespDto>> daysAndFare = hsfProceduceQueryApi.getContractDaysAndFare(shopNo);
            if (!"0".equals(daysAndFare.getResultCode())) {
                return ResultUtil.error("0001", daysAndFare.getResultMsg());
            }
            resultMap.put("days", daysAndFare.getData().get(0).getDays());
        } catch (Exception e) {
            e.printStackTrace();

        }
        return ResultUtil.resultOK(resultMap, "data");
    }
}
