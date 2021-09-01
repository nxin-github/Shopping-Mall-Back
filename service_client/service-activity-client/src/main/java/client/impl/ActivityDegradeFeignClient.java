package client.impl;

import client.ActivityFeignClient;
import com.atguigu.gmall.common.result.Result;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author 王木风
 */
@Component
public class ActivityDegradeFeignClient implements ActivityFeignClient {
    @Override
    public Result findAll() {
        return null;
    }

    @Override
    public Result getSeckillGoods(Long skuId) {
        return null;
    }

    @Override
    public Result<Map<String, Object>> trade() {
        return null;
    }
}
