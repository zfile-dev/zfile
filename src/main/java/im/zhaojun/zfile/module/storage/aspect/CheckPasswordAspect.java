package im.zhaojun.zfile.module.storage.aspect;

import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.core.util.CollectionUtils;
import im.zhaojun.zfile.core.util.FileUtils;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.password.model.dto.VerifyResultDTO;
import im.zhaojun.zfile.module.password.service.PasswordConfigService;
import im.zhaojun.zfile.module.storage.annotation.CheckPassword;
import im.zhaojun.zfile.module.storage.annotation.CheckPasswords;
import im.zhaojun.zfile.module.storage.context.StorageSourceContext;
import im.zhaojun.zfile.module.storage.service.StorageSourceService;
import im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
	@Around(value = "@annotation(im.zhaojun.zfile.module.storage.annotation.CheckPassword) || @annotation(im.zhaojun.zfile.module.storage.annotation.CheckPasswords)")
	public Object around(ProceedingJoinPoint point) throws Throwable {
		Signature s = point.getSignature();
		MethodSignature ms = (MethodSignature) s;
		Method method = ms.getMethod();
		List<CheckPassword> checkPasswordList = new ArrayList<>();

		CheckPasswords checkPasswords = method.getAnnotation(CheckPasswords.class);
		CheckPassword checkPassword = method.getAnnotation(CheckPassword.class);
		if (checkPasswords != null) {
			CollectionUtils.addAll(checkPasswordList, checkPasswords.value());
		} else if (checkPassword != null) {
			checkPasswordList.add(checkPassword);
		} else {
			return point.proceed();
		}


		for (CheckPassword item : checkPasswordList) {
			boolean pathIsDirectory = item.pathIsDirectory();
			String storageKeyFieldExpression = item.storageKeyFieldExpression();
			String passwordFieldExpression = item.passwordFieldExpression();
			String pathFieldExpression = item.pathFieldExpression();

			Object[] args = point.getArgs();

			String storageKeyFieldValue = getFieldValue(args, storageKeyFieldExpression);
			String passwordFieldValue = getFieldValue(args, passwordFieldExpression);
			String pathFieldValue = getFieldValue(args, pathFieldExpression);

			if (!pathIsDirectory) {
				pathFieldValue = FileUtils.getParentPath(pathFieldValue);
			}

			Integer storageId = storageSourceService.findIdByKey(storageKeyFieldValue);

			if (storageId == null) {
				throw new BizException(ErrorCode.BIZ_STORAGE_NOT_FOUND);
			}

			AbstractBaseFileService<?> targetService = StorageSourceContext.getByStorageId(storageId);
			String fullPath = StringUtils.concat(targetService.getCurrentUserBasePath(), pathFieldValue);
			VerifyResultDTO verifyResultDTO = passwordConfigService.verifyPassword(storageId, fullPath, passwordFieldValue);
			if (!verifyResultDTO.isPassed()) {
				throw new BizException(verifyResultDTO.getErrorCode());
			}
		}
		return point.proceed();
	}
	
	
	public String getFieldValue(Object target, String expression) {
		SpelExpressionParser parser = new SpelExpressionParser();
		Expression exp = parser.parseExpression(expression);
		return (String) exp.getValue(target);
	}
	
}