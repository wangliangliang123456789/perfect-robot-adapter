/**
 * LogAop.java
 *
 * Shanghai NTT DATA Synergy Software Co., Ltd. All Rights Reserved.
 * @author wyl
 * @date 2016-10-18
 */

package com.robot.adapter.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * 日志操作类
 * @author 亮亮
 */
@Aspect
@Component
public class LogAop {
    public void before(Logger logger, JoinPoint call){
        String className = call.getTarget().getClass().getName();
        String methodName = call.getSignature().getName();
        logger.info("开始执行:"+methodName+"()方法...");
        logger.info("参数："+ JSON.toJSONString(call.getArgs()));
    }
    public void afterThrowing(Logger logger, JoinPoint call){
        String className = call.getTarget().getClass().getName();
        String methodName = call.getSignature().getName();
        logger.info(""+methodName+"()方法抛出了异常...");
    }
    public void afterReturn(Logger logger, JoinPoint call){
        String className = call.getTarget().getClass().getName();
        String methodName = call.getSignature().getName();

        logger.info(""+methodName+"()方法执行结束...");
    }
    public void after(JoinPoint call) throws Throwable {
        String className = call.getTarget().getClass().getName();
        String methodName = call.getSignature().getName();
    }

    /**
     * 用来做环绕通知的方法可以第一个参数定义为org.aspectj.lang.ProceedingJoinPoint类型
     * @param call
     * @return
     * @throws Throwable
     */
    @Around("execution(* com.robot.adapter.controller..*(..))")
    public Object doAround(ProceedingJoinPoint call) throws Throwable {
        Logger logger = LoggerFactory.getLogger(call.getTarget().getClass().getName());
        Object result = null;
        //相当于前置通知
        this.before(logger,call);
        try {
            //相当于后置通知
            result = call.proceed();
            this.afterReturn(logger,call);
        } catch (Throwable e) {
            //相当于异常抛出后通知
            this.afterThrowing(logger,call);
            logger.warn("-----------异常------------");
            logger.warn(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            e.printStackTrace();
        }finally{
            logger.info("出参:"+JSON.toJSONString(result, SerializerFeature.WriteMapNullValue,SerializerFeature.WriteDateUseDateFormat));
            //相当于最终通知
            this.after(call);
        }
        return result;
    }
}