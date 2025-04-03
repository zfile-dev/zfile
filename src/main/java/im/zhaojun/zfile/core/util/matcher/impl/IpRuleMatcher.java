package im.zhaojun.zfile.core.util.matcher.impl;

import im.zhaojun.zfile.core.constant.RuleTypeConstant;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.core.util.matcher.AbstractRuleMatcher;
import lombok.extern.slf4j.Slf4j;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

/**
 * <p>IP 匹配器, 用于 IP 规则，支持匹配完整 IP 或 IP 段，同时支持 IPV4 和 IPV6.</p>
 *
 *
 * <ul>
 *     <li>IPV4 示例：</li>
 *     <ul>
 *          <li>127.0.0.1</li>
 *          <li>192.168.0.0/24</li>
 *     <ul/>
 * </ul>
 *
 * <p>
 *
 * <ul>
 *     <li>IPV6 示例：</li>
 *     <ul>
 *          <li>0:0:0:0:0:0:0:1</li>
 *          <li>0:0:0:0:0:0:0:0/64</li>
 *     <ul/>
 * </ul>
 * <p>
 *
 * @author zhaojun
 */
@Slf4j
public class IpRuleMatcher extends AbstractRuleMatcher {

    @Override
    public boolean match(String ruleExpression, String testStr) {
        try {
            InetAddress inetAddress = InetAddress.getByName(testStr);
            IpRule rule = createRule(ruleExpression);
            boolean match = rule != null && rule.matches(inetAddress);
            if (log.isDebugEnabled()) {
                log.debug("IP 匹配结果: {}, 规则表达式: {}, 测试值: {}, 校验规则: {}", match, ruleExpression, testStr, rule);
            }
            return match;
        } catch (UnknownHostException e) {
            log.error("IP 地址解析失败, ruleExpression: {}, testStr: {}", ruleExpression, testStr);
        }
        return false;
    }

    @Override
    public String getRuleType() {
        return RuleTypeConstant.IP;
    }

    private IpRule createRule(String ruleExpression) {
        if (isValidIpv4(ruleExpression)) {
            return new Ipv4Rule(ruleExpression);
        } else if (isValidIpv6(ruleExpression)) {
            return new Ipv6Rule(ruleExpression);
        } else if (isValidIpv4Range(ruleExpression)) {
            return new Ipv4RangeRule(ruleExpression);
        } else if (isValidIpv6Range(ruleExpression)) {
            return new Ipv6RangeRule(ruleExpression);
        } else {
            return null;
        }
    }

    private boolean isValidIpv4(String ipAddress) {
        String ipv4Pattern = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
        return Pattern.matches(ipv4Pattern, ipAddress);
    }

    private boolean isValidIpv6(String ipAddress) {
        try {
            InetAddress.getByName(ipAddress);
            return ipAddress.contains(":");
        } catch (UnknownHostException e) {
            return false;
        }
    }

    private boolean isValidIpv4Range(String ipRange) {
        String ipv4RangePattern = "^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}/\\d{1,2}$";
        return Pattern.matches(ipv4RangePattern, ipRange);
    }

    private boolean isValidIpv6Range(String ipRange) {
        String ipv6RangePattern = "^([0-9A-Fa-f]{0,4}:){1,7}([0-9A-Fa-f]{0,4})?/[0-9]{1,3}$";
        return Pattern.matches(ipv6RangePattern, ipRange);
    }
    
    private interface IpRule {

        boolean matches(InetAddress ipAddress);

        String getExpression();

    }

    private static class Ipv4Rule implements IpRule {
        
        private final String expression;

        Ipv4Rule(String expression) {
            this.expression = expression;
        }

        @Override
        public boolean matches(InetAddress ipAddress) {
            if (ipAddress instanceof Inet6Address) {
                return false;
            }
            return ipAddress.getHostAddress().equals(expression);
        }

        @Override
        public String getExpression() {
            return expression;
        }
    }

    private static class Ipv6Rule implements IpRule {
        
        private final String expression;

        Ipv6Rule(String expression) {
            this.expression = expression;
        }

        @Override
        public boolean matches(InetAddress ipAddress) {
            if (ipAddress instanceof Inet6Address) {
                return ipAddress.getHostAddress().equals(expression);
            }
            return false;
        }

        @Override
        public String getExpression() {
            return expression;
        }
    }

    private static class Ipv4RangeRule implements IpRule {
        
        private final String expression;
        private final int prefixLength;

        Ipv4RangeRule(String expression) {
            this.expression = expression.substring(0, expression.indexOf('/'));
            this.prefixLength = Integer.parseInt(expression.substring(expression.indexOf('/') + 1));
        }

        @Override
        public boolean matches(InetAddress ipAddress) {
            if (ipAddress instanceof Inet6Address) {
                return false;
            }
            String[] rangeParts = expression.split("\\.");
            byte[] rangeAddrBytes = new byte[4];
            for (int i = 0; i < rangeParts.length; i++) {
                rangeAddrBytes[i] = (byte) Integer.parseInt(rangeParts[i]);
            }

            byte[] ipValue = ipAddress.getAddress();
            if (ipValue.length != 4) {
                return false;
            }

            for (int i = 0; i < prefixLength / 8; i++) {
                if (rangeAddrBytes[i] != ipValue[i]) {
                    return false;
                }
            }

            int remainingBits = prefixLength % 8;
            if (remainingBits != 0) {
                int rangeByte = rangeAddrBytes[prefixLength / 8];
                int ipByte = ipValue[prefixLength / 8];

                int shift = 8 - remainingBits;
                int mask = 0xFF >> shift;

                return (rangeByte >> shift & mask) == (ipByte >> shift & mask);
            }

            return true;
        }

        @Override
        public String getExpression() {
            return expression + StringUtils.SLASH + prefixLength;
        }
    }

    private static class Ipv6RangeRule implements IpRule {
        
        private final String expression;
        private final int prefixLength;

        Ipv6RangeRule(String expression) {
            this.expression = expression.substring(0, expression.indexOf('/'));
            this.prefixLength = Integer.parseInt(expression.substring(expression.indexOf('/') + 1));
        }

        @Override
        public boolean matches(InetAddress ipAddress) {
            if (ipAddress instanceof Inet6Address) {
                byte[] ipValue = ipAddress.getAddress();
                byte[] rangeValue = Inet6AddressConverter.convert(expression);

                if (ipValue.length != 16) {
                    return false;
                }

                for (int i = 0; i < prefixLength / 8; i++) {
                    if (rangeValue[i] != ipValue[i]) {
                        return false;
                    }
                }

                int remainingBits = prefixLength % 8;
                if (remainingBits != 0) {
                    int rangeByte = rangeValue[prefixLength / 8];
                    int ipByte = ipValue[prefixLength / 8];

                    int shift = 8 - remainingBits;
                    int mask = 0xFF >> shift;

                    return (rangeByte >> shift & mask) == (ipByte >> shift & mask);
                }

                return true;
            }
            return false;
        }

        @Override
        public String getExpression() {
            return expression + StringUtils.SLASH + prefixLength;
        }
    }

    // Utility class to convert IPv6 address to byte array
    private static class Inet6AddressConverter {
        public static byte[] convert(String ipv6Address) {
            byte[] ipAddress = new byte[16];
            String[] blocks = ipv6Address.split(":");

            for (int i = 0; i < blocks.length; i++) {
                String block = blocks[i];
                if (!block.isEmpty()) {
                    ipAddress[i * 2] = (byte) Integer.parseInt(block.substring(0, 2), 16);
                    ipAddress[i * 2 + 1] = (byte) Integer.parseInt(block.substring(2, 4), 16);
                }
            }

            return ipAddress;
        }
    }
}