package im.zhaojun.zfile.core.util.matcher;

import java.util.Collection;

/**
 * 抽象规则匹配器, 实现了部分方法, 用于简化规则匹配器的实现.
 *
 * @author zhaojun
 */
public abstract class AbstractRuleMatcher implements IRuleMatcher {

    @Override
    public boolean contains(String ruleExpression, String testStr) {
        return match(ruleExpression, testStr);
    }

    @Override
    public boolean matchAny(Collection<String> ruleExpressionList, String testStr) {
        if (ruleExpressionList == null || ruleExpressionList.isEmpty()) {
            return false;
        }
        for (String ruleExpression : ruleExpressionList) {
            if (match(ruleExpression, testStr)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String matchAnyReturnFirst(Collection<String> ruleExpressionList, String testStr) {
        if (ruleExpressionList == null || ruleExpressionList.isEmpty()) {
            return null;
        }
        for (String ruleExpression : ruleExpressionList) {
            if (match(ruleExpression, testStr)) {
                return ruleExpression;
            }
        }
        return null;
    }

}