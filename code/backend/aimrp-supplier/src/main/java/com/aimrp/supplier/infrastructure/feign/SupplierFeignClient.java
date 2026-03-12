package com.aimrp.supplier.infrastructure.feign;

import com.aimrp.common.result.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 供应商服务 Feign 客户端
 * 用于其他模块调用供应商模块
 */
@FeignClient(name = "aimrp-supplier", path = "/api/suppliers")
public interface SupplierFeignClient {

    /**
     * 查询所有供应商
     */
    @GetMapping("/list")
    ApiResponse<List<Map<String, Object>>> listSuppliers();

    /**
     * 根据编码查询供应商
     */
    @GetMapping("/{supplierCode}")
    ApiResponse<Map<String, Object>> getSupplierByCode(@PathVariable("supplierCode") String supplierCode);
}
