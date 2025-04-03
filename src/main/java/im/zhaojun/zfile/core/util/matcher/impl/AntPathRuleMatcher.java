package im.zhaojun.zfile.core.util.matcher.impl;

import im.zhaojun.zfile.core.constant.RuleTypeConstant;
import im.zhaojun.zfile.core.util.matcher.AbstractRuleMatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

/**
 * Ant 路径匹配器, 用于匹配路径规则.
 *
 * @author zhaojun
 */
@Slf4j
public class AntPathRuleMatcher extends AbstractRuleMatcher {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public boolean match(String ruleExpression, String testStr) {
        boolean match = pathMatcher.match(ruleExpression, testStr);
        if (log.isDebugEnabled()) {
            log.debug("Ant 表达式匹配结果: {}, 规则表达式: {}, 测试值: {}", match, ruleExpression, testStr);
        }
        return match;
    }

    @Override
    public String getRuleType() {
        return RuleTypeConstant.ANT_PATH;
    }

}