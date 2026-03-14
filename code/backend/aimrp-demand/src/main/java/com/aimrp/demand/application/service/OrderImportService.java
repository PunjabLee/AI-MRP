package com.aimrp.demand.application.service;

import com.aimrp.demand.domain.entity.SalesOrder;
import com.aimrp.demand.infrastructure.persistence.mapper.SalesOrderMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单导入服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderImportService {
    
    private final SalesOrderMapper salesOrderMapper;
    
    /**
     * 导入订单
     * 
     * @param file Excel文件
     * @return 导入结果
     */
    public ImportResult importOrders(MultipartFile file) {
        log.info("开始导入订单 - 文件名: {}", file.getOriginalFilename());
        
        ImportResult result = new ImportResult();
        List<Map<String, Object>> successList = new ArrayList<>();
        List<Map<String, String>> errorList = new ArrayList<>();
        
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            // 检查表头
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                result.setSuccess(false);
                result.setMessage("Excel文件为空或格式错误");
                return result;
            }
            
            // 解析表头
            Map<String, Integer> headerMap = parseHeader(headerRow);
            
            // 验证必要字段
            if (!validateHeader(headerMap)) {
                result.setSuccess(false);
                result.setMessage("缺少必要字段，需要: customerCode, itemCode, qty, deliveryDate");
                return result;
            }
            
            // 逐行解析数据
            int successCount = 0;
            int errorCount = 0;
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                try {
                    // 解析订单
                    SalesOrder order = parseOrder(row, headerMap);
                    
                    // 生成订单编号
                    order.setOrderNo("SO" + System.currentTimeMillis() + String.format("%03d", i));
                    order.setStatus("PENDING");
                    
                    // 保存到数据库
                    salesOrderMapper.insert(order);
                    
                    successCount++;
                    successList.add(Map.of(
                        "row", i + 1,
                        "orderNo", order.getOrderNo(),
                        "customerCode", order.getCustomerCode()
                    ));
                    
                } catch (Exception e) {
                    errorCount++;
                    errorList.add(Map.of(
                        "row", String.valueOf(i + 1),
                        "error", e.getMessage()
                    ));
                    log.warn("导入第{}行失败: {}", i + 1, e.getMessage());
                }
            }
            
            result.setSuccess(true);
            result.setMessage(String.format("导入完成 - 成功: %d, 失败: %d", successCount, errorCount));
            result.setSuccessCount(successCount);
            result.setErrorCount(errorCount);
            result.setSuccessList(successList);
            result.setErrorList(errorList);
            
        } catch (Exception e) {
            log.error("导入订单失败", e);
            result.setSuccess(false);
            result.setMessage("导入失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 解析表头
     */
    private Map<String, Integer> parseHeader(Row headerRow) {
        Map<String, Integer> headerMap = new HashMap<>();
        
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null) {
                String value = getCellValue(cell).toString().trim();
                headerMap.put(value, i);
            }
        }
        
        return headerMap;
    }
    
    /**
     * 验证表头
     */
    private boolean validateHeader(Map<String, Integer> headerMap) {
        return headerMap.containsKey("customerCode") ||
               headerMap.containsKey("customer_code") ||
               headerMap.containsKey("客户编码");
    }
    
    /**
     * 解析订单行
     */
    private SalesOrder parseOrder(Row row, Map<String, Integer> headerMap) {
        SalesOrder order = new SalesOrder();
        
        // 客户编码
        String customerCode = getCellValue(row, headerMap, "customerCode", "customer_code", "客户编码");
        order.setCustomerCode(customerCode);
        
        // 客户名称
        String customerName = getCellValue(row, headerMap, "customerName", "customer_name", "客户名称");
        order.setCustomerName(customerName);
        
        // 物料编码
        String itemCode = getCellValue(row, headerMap, "itemCode", "item_code", "物料编码");
        order.setItemCode(itemCode);
        
        // 物料名称
        String itemName = getCellValue(row, headerMap, "itemName", "item_name", "物料名称");
        order.setItemName(itemName);
        
        // 数量
        String qtyStr = getCellValue(row, headerMap, "qty", "quantity", "数量");
        if (qtyStr != null && !qtyStr.isEmpty()) {
            order.setQty(new java.math.BigDecimal(qtyStr));
        }
        
        // 单价
        String priceStr = getCellValue(row, headerMap, "unitPrice", "unit_price", "单价");
        if (priceStr != null && !priceStr.isEmpty()) {
            order.setUnitPrice(new java.math.BigDecimal(priceStr));
        }
        
        // 金额
        String amountStr = getCellValue(row, headerMap, "amount", "totalAmount", "金额");
        if (amountStr != null && !amountStr.isEmpty()) {
            order.setTotalAmount(new java.math.BigDecimal(amountStr));
        }
        
        // 交货日期
        String deliveryDate = getCellValue(row, headerMap, "deliveryDate", "delivery_date", "交货日期");
        if (deliveryDate != null && !deliveryDate.isEmpty()) {
            order.setDeliveryDate(parseDate(deliveryDate));
        }
        
        // 备注
        String remark = getCellValue(row, headerMap, "remark", "备注");
        order.setRemark(remark);
        
        // 优先级
        String priorityStr = getCellValue(row, headerMap, "priority", "优先级");
        if (priorityStr != null && !priorityStr.isEmpty()) {
            order.setPriority(Integer.parseInt(priorityStr));
        } else {
            order.setPriority(5);
        }
        
        order.setOrderDate(LocalDate.now());
        
        return order;
    }
    
    /**
     * 获取单元格值
     */
    private String getCellValue(Row row, Map<String, Integer> headerMap, String... keys) {
        for (String key : keys) {
            Integer index = headerMap.get(key);
            if (index != null && index < row.getLastCellNum()) {
                Cell cell = row.getCell(index);
                if (cell != null) {
                    return getCellValue(cell).toString().trim();
                }
            }
        }
        return null;
    }
    
    /**
     * 获取单元格值
     */
    private Object getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                }
                return cell.getNumericCellValue();
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
    
    /**
     * 解析日期
     */
    private LocalDate parseDate(String dateStr) {
        try {
            // 尝试多种日期格式
            String[] patterns = {"yyyy-MM-dd", "yyyy/MM/dd", "yyyyMMdd", "yyyy年MM月dd日"};
            
            for (String pattern : patterns) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                    return LocalDate.parse(dateStr, formatter);
                } catch (Exception e) {
                    // 继续尝试下一个格式
                }
            }
            
            // 尝试Excel日期格式
            return LocalDate.now();
            
        } catch (Exception e) {
            log.warn("日期解析失败: {}", dateStr);
            return LocalDate.now();
        }
    }
    
    /**
     * 导入结果
     */
    @Data
    public static class ImportResult {
        private boolean success;
        private String message;
        private int successCount;
        private int errorCount;
        private List<Map<String, Object>> successList;
        private List<Map<String, String>> errorList;
    }
}
