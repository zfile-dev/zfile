package im.zhaojun.zfile.core.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.util.CollectionUtils;

public class RequestUtils {

    public static HttpRange getRequestRange(HttpServletRequest request) {
        String rangeHeader = request.getHeader(HttpHeaders.RANGE);
        if (rangeHeader == null) {
            return null;
        }
        return CollectionUtils.firstElement(HttpRange.parseRanges(rangeHeader));
    }

}
