package com.aimrp.org.api.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class OrganizationCreateRequest {
    @NotBlank(message = "组织编码不能为空")
    private String orgCode;
    @NotBlank(message = "组织名称不能为空")
    private String orgName;
    private Long parentId;
    private String orgType;
    private String manager;
    private String remark;
}
