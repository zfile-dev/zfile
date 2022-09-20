package im.zhaojun.zfile.module.storage.schedule;

import im.zhaojun.zfile.module.storage.context.StorageSourceContext;
import im.zhaojun.zfile.module.storage.service.base.RefreshTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 刷新使用所有 AccessToken 机制的定时任务, 存储源实现 #{@link RefreshTokenService} 接口, 表示是需要刷新 AccessToken 的存储源
 *
 * @author zhaojun
 */
@Configuration
@EnableScheduling
@Slf4j
public class AccessTokenRefreshSchedule {

	@Resource
	private StorageSourceContext storageSourceContext;

	/**
	 * 项目启动 30 秒后, 每 10 分钟执行一次刷新 OneDrive Token 的定时任务.
	 */
	@Scheduled(fixedRate = 1000 * 60 * 10, initialDelay = 1000 * 10)
	public void autoRefreshAccessToken() {
		log.info("开始执行需要定期刷新 AccessToken 存储源的定时任务");

		Map<Integer, RefreshTokenService> refreshTokenServiceMap = storageSourceContext.getAllRefreshTokenStorageSource();

		for (Map.Entry<Integer, RefreshTokenService> refreshTokenServiceEntry : refreshTokenServiceMap.entrySet()) {
			Integer storageId = refreshTokenServiceEntry.getKey();
			RefreshTokenService refreshTokenService = refreshTokenServiceEntry.getValue();
			try {
				refreshTokenService.refreshAccessToken();
				log.info("成功刷新存储源 AccessToken, 存储源 id: {}, 存储源类型: {}",
						storageId, refreshTokenService.getStorageTypeEnum().getDescription());
			} catch (Exception e) {
				log.error("刷新存储源 AccessToken 失败, 存储源 id: {}, 存储源类型: {}",
						storageId, refreshTokenService.getStorageTypeEnum().getDescription(), e);
			}
		}
		
		log.info("执行需要定期刷新 AccessToken 存储源的定时任务完成");
	}

}