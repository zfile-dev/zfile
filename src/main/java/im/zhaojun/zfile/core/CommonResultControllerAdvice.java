package im.zhaojun.zfile.core;

import im.zhaojun.zfile.core.util.AjaxJson;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @author zhaojun
 */
@ControllerAdvice
public class CommonResultControllerAdvice implements ResponseBodyAdvice<Object> {

	@Override
	public boolean supports(MethodParameter returnType,
							@NonNull Class<? extends HttpMessageConverter<?>> converterType) {
		return AbstractJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
	}

	@Override
	@NonNull
	public final Object beforeBodyWrite(@Nullable Object body,
										@NonNull MethodParameter returnType,
										@NonNull MediaType contentType,
										@NonNull Class<? extends HttpMessageConverter<?>> converterType,
										@NonNull ServerHttpRequest request,
										@NonNull ServerHttpResponse response) {
		MappingJacksonValue container = getOrCreateContainer(body);
		// The contain body will never be null
		beforeBodyWriteInternal(container, contentType, returnType, request, response);
		return container;
	}

	/**
	 * Wrap the body in a {@link MappingJacksonValue} value container (for providing
	 * additional serialization instructions) or simply cast it if already wrapped.
	 */
	private MappingJacksonValue getOrCreateContainer(Object body) {
		return body instanceof MappingJacksonValue ? (MappingJacksonValue) body :
				new MappingJacksonValue(body);
	}

	private void beforeBodyWriteInternal(MappingJacksonValue bodyContainer,
										 MediaType contentType,
										 MethodParameter returnType,
										 ServerHttpRequest request,
										 ServerHttpResponse response) {
		// Get return body
		Object returnBody = bodyContainer.getValue();

		if (returnBody instanceof AjaxJson) {
			// If the return body is instance of BaseResponse, then just do nothing
			AjaxJson<?> baseResponse = (AjaxJson<?>) returnBody;
			baseResponse.setTraceId(MDC.get("traceId"));
		}
	}

}