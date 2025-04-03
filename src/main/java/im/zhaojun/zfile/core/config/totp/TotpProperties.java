package im.zhaojun.zfile.core.config.totp;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
    prefix = "totp"
)
public class TotpProperties {
    private static final int DEFAULT_SECRET_LENGTH = 32;
    private static final int DEFAULT_CODE_LENGTH = 6;
    private static final int DEFAULT_TIME_PERIOD = 30;
    private static final int DEFAULT_TIME_DISCREPANCY = 1;
    private final Secret secret = new Secret();
    private final Code code = new Code();
    private final Time time = new Time();

    public TotpProperties() {
    }

    public Secret getSecret() {
        return this.secret;
    }

    public Code getCode() {
        return this.code;
    }

    public Time getTime() {
        return this.time;
    }

    public static class Time {
        private int period = 30;
        private int discrepancy = 1;

        public Time() {
        }

        public int getPeriod() {
            return this.period;
        }

        public void setPeriod(int period) {
            this.period = period;
        }

        public int getDiscrepancy() {
            return this.discrepancy;
        }

        public void setDiscrepancy(int discrepancy) {
            this.discrepancy = discrepancy;
        }
    }

    public static class Code {
        private int length = 6;

        public Code() {
        }

        public int getLength() {
            return this.length;
        }

        public void setLength(int length) {
            this.length = length;
        }
    }

    public static class Secret {
        private int length = 32;

        public Secret() {
        }

        public int getLength() {
            return this.length;
        }

        public void setLength(int length) {
            this.length = length;
        }
    }
}
