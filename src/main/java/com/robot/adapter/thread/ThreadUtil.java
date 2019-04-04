package com.robot.adapter.thread;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @ClassName ${ClassName}
 * @Description:
 * @Author 亮亮
 * @Date 2019/3/22  10:59
 * @Version
 **/
public class ThreadUtil {

    private static ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    public void setThreadPoolTaskExecutor(ThreadPoolTaskExecutor threadPoolTaskExecutor) {
        ThreadUtil.threadPoolTaskExecutor = threadPoolTaskExecutor;
    }

    //线程池

    public static  <T> List<T> main(ThreadParm threadParm, Class<T> clazz) {
        Date date = new Date();
        //累积计算所有请求的总扣款数--计算任务提前开始且每个都是分开的不相互影响
        List<Future<T>> futureList = new ArrayList<Future<T>>();
        for (int i = 0; i < 200; i++) {
            Callable<T> task = new Callable<T>() {
                @Override
                public T call() throws Exception {
                    return  threadParm.dispose(clazz);
                }
            };
            Future<T> future = threadPoolTaskExecutor.submit(task);
            futureList.add(future);
        }
        List<T> list=new ArrayList<>();
        for (int i = 0; i < futureList.size(); i++) {
            try {
                T object = futureList.get(i).get();
                list.add(object);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        System.out.println("开始时间"+ JSON.toJSONString(JSON.toJSONString(date, SerializerFeature.WriteDateUseDateFormat)));
        System.out.println("结束时间"+JSON.toJSONString(JSON.toJSONString(new Date(), SerializerFeature.WriteDateUseDateFormat)));
       return list;
    }


}
