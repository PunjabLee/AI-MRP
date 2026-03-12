package com.aimrp.bom.infrastructure.feign;

import com.aimrp.common.result.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * BOM 服务 Feign 客户端
 * 用于其他模块调用BOM模块
 */
@FeignClient(name = "aimrp-bom", path = "/api/bom")
public interface BomFeignClient {

    /**
     * 根据物料编码查询BOM
     */
    @GetMapping("/item/{itemCode}")
    ApiResponse<Map<String, Object>> getBomByItemCode(@PathVariable("itemCode") String itemCode);

    /**
     * 根据物料编码查询BOM子项
     */
    @GetMapping("/children/{itemCode}")
    ApiResponse<List<Map<String, Object>>> getBomChildren(@PathVariable("itemCode") String itemCode);

    /**
     * 展开BOM
     */
    @PostMapping("/expand")
    ApiResponse<List<Map<String, Object>>> expandBom(
            @RequestParam("itemCode") String itemCode,
            @RequestParam("quantity") Double quantity,
            @RequestParam(value = "level", defaultValue = "5") Integer level);

    /**
     * 查询BOM map（所有BOM关系）
     */
    @GetMapping("/map")
    ApiResponse<List<Map<String, Object>>> selectBomMap();
}
