package im.zhaojun.zfile.core.util.matcher;

import im.zhaojun.zfile.core.util.matcher.impl.AntPathRuleMatcher;
import im.zhaojun.zfile.core.util.matcher.impl.IpRuleMatcher;
import im.zhaojun.zfile.core.util.matcher.impl.RegexRuleMatcher;
import im.zhaojun.zfile.core.util.matcher.impl.SpringSimpleRuleMatcher;

import java.util.HashMap;
import java.util.Map;

/**
 * 规则匹配器工厂, 用于获取规则匹配器实例.
 *
 * @author zhaojun
 */
public class RuleMatcherFactory {

    private static final Map<String, IRuleMatcher> RULE_MATCHER_MAP = new HashMap<>();

    static {
        IpRuleMatcher ipRuleMatcher = new IpRuleMatcher();
        RULE_MATCHER_MAP.put(ipRuleMatcher.getRuleType(), ipRuleMatcher);

        RegexRuleMatcher regexRuleMatcher = new RegexRuleMatcher();
        RULE_MATCHER_MAP.put(regexRuleMatcher.getRuleType(), regexRuleMatcher);

        AntPathRuleMatcher antPathRuleMatcher = new AntPathRuleMatcher();
        RULE_MATCHER_MAP.put(antPathRuleMatcher.getRuleType(), antPathRuleMatcher);

        SpringSimpleRuleMatcher springSimpleRuleMatcher = new SpringSimpleRuleMatcher();
        RULE_MATCHER_MAP.put(springSimpleRuleMatcher.getRuleType(), springSimpleRuleMatcher);
    }

    public static IRuleMatcher getRuleMatcher(String ruleType) {
        if (ruleType == null || ruleType.isEmpty()) {
            return null;
        }

        return RULE_MATCHER_MAP.get(ruleType);
    }

}