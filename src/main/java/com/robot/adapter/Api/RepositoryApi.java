package com.robot.adapter.Api;

import com.alibaba.fastjson.JSONObject;


public interface RepositoryApi{

    /**
     * 24号接口：押货单发货查询
     *
     * @param shopNo  网点编号
     * @param duration  开单日期  格式2018-12-12
     * @return
     */

    public JSONObject getOrderDelivery(String shopNo, String duration) ;

    /**
     * 26号场景： 合同期查询
     * 还没有数据，带联调
     *
     * @param shopNo 网点编号
     * @return
     */

    public JSONObject getContractDays(String shopNo) ;


}
