package im.zhaojun.zfile.core.util.matcher;

import java.util.Collection;

/**
 * 规则匹配器接口
 *
 * @author zhaojun
 */
public interface IRuleMatcher {

    /**
     * 匹配规则
     *
     * @param   ruleExpression
     *          规则表达式
     *
     * @param   testStr
     *          测试字符串
     *
     * @return  是否匹配
     */
    boolean match(String ruleExpression, String testStr);

    /**
     * 部分匹配规则
     *
     * @param   ruleExpression
     *          规则表达式
     *
     * @param   testStr
     *          测试字符串
     *
     * @return  是否部分匹配
     */
    boolean contains(String ruleExpression, String testStr);


    /**
     * 匹配规则, 可以匹配多个规则表达式, 只要有一个匹配成功, 则返回 true
     *
     * @param   ruleExpressionList
     *          规则表达式列表
     *
     * @param   testStr
     *          测试字符串
     *
     * @return  是否匹配
     */
    boolean matchAny(Collection<String> ruleExpressionList, String testStr);


    /**
     * 匹配规则, 可以匹配多个规则表达式, 只要有一个匹配成功, 则返回第一个匹配成功的表达式。
     *
     * @param   ruleExpressionList
     *          规则表达式列表
     *
     * @param   testStr
     *          测试字符串
     *
     * @return  匹配成功的第一个表达式
     */
    String matchAnyReturnFirst(Collection<String> ruleExpressionList, String testStr);


    /**
     * 获取规则类型
     *
     * @return  规则类型
     */
    String getRuleType();


}
