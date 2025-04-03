package im.zhaojun.zfile.core.util.matcher.impl;

import cn.hutool.core.util.ReUtil;
import im.zhaojun.zfile.core.constant.RuleTypeConstant;
import im.zhaojun.zfile.core.util.matcher.AbstractRuleMatcher;
import lombok.extern.slf4j.Slf4j;

/**
 * 正则匹配器
 *
 * @author zhaojun
 */
@Slf4j
public class RegexRuleMatcher extends AbstractRuleMatcher {

    @Override
    public boolean match(String ruleExpression, String testStr) {
        boolean match = ReUtil.isMatch(ruleExpression, testStr);
        if (log.isDebugEnabled()) {
            log.debug("正则匹配结果: {}, 规则表达式: {}, 测试值: {}", match, ruleExpression, testStr);
        }
        return match;
    }

    @Override
    public boolean contains(String ruleExpression, String testStr) {
        boolean match = ReUtil.contains(ruleExpression, testStr);
        if (log.isDebugEnabled()) {
            log.debug("正则部分匹配结果: {}, 规则表达式: {}, 测试值: {}", match, ruleExpression, testStr);
        }
        return match;
    }

    @Override
    public String getRuleType() {
        return RuleTypeConstant.REGEX;
    }

}