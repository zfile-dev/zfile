package im.zhaojun.zfile.module.storage.aspect;

import cn.hutool.core.util.StrUtil;
import im.zhaojun.zfile.module.password.model.dto.VerifyResultDTO;
import im.zhaojun.zfile.module.password.service.PasswordConfigService;
import im.zhaojun.zfile.module.storage.service.StorageSourceService;
import im.zhaojun.zfile.core.exception.PasswordVerifyException;
import im.zhaojun.zfile.core.exception.StorageSourceException;
import im.zhaojun.zfile.core.util.CodeMsg;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.storage.annotation.CheckPassword;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;

/**
 * 检查密码切面
 *
 * @author zhaojun
 */
@Aspect
@Component
@Slf4j
public class CheckPasswordAspect {
	
	@Resource
	private PasswordConfigService passwordConfigService;
	
	@Resource
	private StorageSourceService storageSourceService;
	
	/**
	 * 校验密码
	 *
	 * @param   point
	 *          连接点
	 *
	 * @return  方法运行结果
	 */
	@Around(value = "@annotation(im.zhaojun.zfile.module.storage.annotation.CheckPassword)")
	public Object around(ProceedingJoinPoint point) throws Throwable {
		Signature s = point.getSignature();
		MethodSignature ms = (MethodSignature) s;
		Method method = ms.getMethod();
		CheckPassword checkPassword = method.getAnnotation(CheckPassword.class);
		boolean pathIsDirectory = checkPassword.pathIsDirectory();
		String storageKeyFieldExpression = checkPassword.storageKeyFieldExpression();
		String passwordFieldExpression = checkPassword.passwordFieldExpression();
		String pathFieldExpression = checkPassword.pathFieldExpression();
		
		Object[] args = point.getArgs();
		
		String storageKeyFieldValue = getFieldValue(args, storageKeyFieldExpression);
		String passwordFieldValue = getFieldValue(args, passwordFieldExpression);
		String pathFieldValue = getFieldValue(args, pathFieldExpression);
		
		if (!pathIsDirectory) {
			pathFieldValue = StringUtils.getParentPath(pathFieldValue);
		}
		
		Integer storageId = storageSourceService.findIdByKey(storageKeyFieldValue);
		
		if (storageId == null) {
			String message = StrUtil.format("执行文件操作「{}」时检测到存储源不存在", storageKeyFieldValue);
			throw new StorageSourceException(CodeMsg.STORAGE_SOURCE_NOT_FOUND, storageId, message);
		}
		
		VerifyResultDTO verifyResultDTO = passwordConfigService.verifyPassword(storageId, pathFieldValue, passwordFieldValue);
		if (verifyResultDTO.isPassed()) {
			return point.proceed();
		} else {
			throw new PasswordVerifyException(verifyResultDTO.getCode(), verifyResultDTO.getMsg());
		}
	}
	
	
	public String getFieldValue(Object target, String expression) {
		SpelExpressionParser parser = new SpelExpressionParser();
		Expression exp = parser.parseExpression(expression);
		return (String) exp.getValue(target);
	}
	
}