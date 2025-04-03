package im.zhaojun.zfile.core.util.matcher.impl;

import im.zhaojun.zfile.core.constant.RuleTypeConstant;
import im.zhaojun.zfile.core.util.matcher.AbstractRuleMatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.PatternMatchUtils;

/**
 * 使用 {@link org.springframework.util.PatternMatchUtils} 来匹配规则
 *
 * @author zhaojun
 */
@Slf4j
public class SpringSimpleRuleMatcher extends AbstractRuleMatcher {

    @Override
    public boolean match(String ruleExpression, String testStr) {
        boolean match = PatternMatchUtils.simpleMatch(ruleExpression, testStr);
        if (log.isDebugEnabled()) {
            log.debug("Spring Simple 规则匹配结果: {}, 规则表达式: {}, 测试值: {}", match, ruleExpression, testStr);
        }
        return match;
    }

    @Override
    public String getRuleType() {
        return RuleTypeConstant.SPRING_SIMPLE;
    }

    @Override
    public boolean contains(String ruleExpression, String testStr) {
        return match("*" + ruleExpression + "*", testStr);
    }

}