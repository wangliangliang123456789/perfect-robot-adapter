package com.robot.adapter.Api;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * 完美旧仓储
 *
 * @author 亮亮
 * @date 2018年12月26日
 */

public interface ThirdPartyApi {

    /**
     * 23号场景，产品生产日期查询
     *
     * @param repositoryNo 网点编号
     * @param productionNo 产品编号
     * @return
     */

    public JSONObject getProductionDate(String repositoryNo, String productionNo);

    /**
     * 31号场景，促销活动赠品
     *
     * @param disNo 网点编号
     * @param duration  发货日期
     * @return
     */

    public JSONObject getLargesses(String disNo, String duration);

}
