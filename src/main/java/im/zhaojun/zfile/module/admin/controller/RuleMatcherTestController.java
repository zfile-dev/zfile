package im.zhaojun.zfile.module.admin.controller;

import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.core.util.matcher.IRuleMatcher;
import im.zhaojun.zfile.core.util.matcher.RuleMatcherFactory;
import im.zhaojun.zfile.module.admin.model.request.TestRuleMatcherRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author zhaojun
 */
@Tag(name = "规则匹配辅助 Controller")
@Slf4j
@RequestMapping("/admin")
@RestController
public class RuleMatcherTestController {

    /**
     * 根据传入的规则和测试值, 测试规则是否匹配, 规则支持多个, 用换行符分割. 如果匹配, 则返回匹配的规则表达式行.
     * @param   testRuleMatcherRequest
     *          测试规则匹配请求
     *
     * @return  匹配成功的第一个表达式
     */
    @PostMapping("/rule-test")
    public AjaxJson<String> testRule(@RequestBody @Valid TestRuleMatcherRequest testRuleMatcherRequest) {
        if (testRuleMatcherRequest == null) {
            return AjaxJson.getSuccessData(null);
        }
        String rules = testRuleMatcherRequest.getRules();
        String testValue = testRuleMatcherRequest.getTestValue();
        if (StringUtils.isBlank(testValue) || StringUtils.isBlank(rules)) {
            return AjaxJson.getSuccessData(null);
        }

        List<String> ruleList = StringUtils.split(rules, StringUtils.LF);
        IRuleMatcher ipRuleMatcher = RuleMatcherFactory.getRuleMatcher(testRuleMatcherRequest.getRuleType());
        return AjaxJson.getSuccessData(ipRuleMatcher.matchAnyReturnFirst(ruleList, testValue));
    }

}