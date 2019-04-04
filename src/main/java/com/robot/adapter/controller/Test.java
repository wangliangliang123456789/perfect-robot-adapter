package com.robot.adapter.controller;

import com.alibaba.fastjson.JSONObject;
import com.robot.adapter.util.DateUtil;
import com.robot.adapter.util.ResultUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @ClassName ${ClassName}
 * @Description:
 * @Author 亮亮
 * @Date 2019/3/22  10:25
 * @Version
 **/
@Controller
@Api(tags = "测试Controller", description = "测试 swagger")
public class Test {

   Logger logger= LoggerFactory.getLogger(Test.class);
    @ApiOperation(value="测试方法",tags={"测试方法"},notes="")
    @GetMapping(value = "/test")
    @ResponseBody
    @ApiImplicitParam(value = "用户姓名",name = "name",required = false)
    public JSONObject test(String name) {
        System.out.println("你好");
        logger.info("你好");
        JSONObject object = ResultUtil.resultOK(new Date(),"日期");
        return object;
    }

    @org.junit.Test
    public  void main() throws Exception{
        String lastDayOfMonth = DateUtil.getLastDayOfMonth(new SimpleDateFormat("yyyy-MM-dd").parse("2019-05-21"));
        System.out.println(lastDayOfMonth+"");
    }


}
