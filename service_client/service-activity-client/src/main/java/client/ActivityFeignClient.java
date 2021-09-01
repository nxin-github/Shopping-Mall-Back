package client;

import client.impl.ActivityDegradeFeignClient;
import com.atguigu.gmall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * @author 王木风
 */
@FeignClient(value = "service-activity",fallback = ActivityDegradeFeignClient.class)
public interface ActivityFeignClient {

    //  发送数据到feign 上， 给web-all!
    /**
     * 返回全部列表
     *
     * @return
     */
    @GetMapping("/api/activity/seckill/findAll")
    Result findAll();

    /**
     * 获取实体
     *
     * @param skuId
     * @return
     */
    @GetMapping("/api/activity/seckill/getSeckillGoods/{skuId}")
    Result getSeckillGoods(@PathVariable("skuId") Long skuId);

    /**
     * 订单页面显示
     * @return
     */
    @GetMapping("/api/activity/seckill/auth/trade")
    Result<Map<String, Object>> trade();
}
