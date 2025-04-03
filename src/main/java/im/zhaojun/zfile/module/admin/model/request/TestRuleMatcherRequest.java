package im.zhaojun.zfile.module.admin.model.request;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class TestRuleMatcherRequest {

    @NotBlank(message = "规则类型不能为空")
    private String ruleType;

    @NotBlank(message = "规则不能为空")
    private String rules;

    @NotBlank(message = "测试值不能为空")
    private String testValue;

}