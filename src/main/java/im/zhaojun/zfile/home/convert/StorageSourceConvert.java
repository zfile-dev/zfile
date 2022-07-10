package im.zhaojun.zfile.home.convert;

import im.zhaojun.zfile.admin.model.result.storage.StorageSourceAdminResult;
import im.zhaojun.zfile.home.model.result.StorageSourceConfigResult;
import im.zhaojun.zfile.home.model.result.StorageSourceResult;
import im.zhaojun.zfile.admin.model.entity.StorageSource;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * StorageSource 转换器
 *
 * @author zhaojun
 */
@Component
@Mapper(componentModel = "spring")
public interface StorageSourceConvert {


	/**
	 * 将 StorageSource 转换为 StorageSourceResult
	 *
	 * @param   list
	 *          StorageSource 列表
	 *
	 * @return  StorageSourceResult 列表
	 */
	List<StorageSourceResult> entityToResultList(List<StorageSource> list);


	/**
	 * 将 StorageSource 转换为 StorageSourceConfigResult
	 *
	 * @param   storageSource
	 *          StorageSource 实体
	 *
	 * @return  StorageSourceConfigResult 实体
	 */
	StorageSourceConfigResult entityToConfigResult(StorageSource storageSource);


	/**
	 * 将 StorageSource 转换为 StorageSourceAdminResult
	 *
	 * @param   list
	 *          StorageSource 列表
	 *
	 * @return  StorageSourceAdminResult 列表
	 */
	List<StorageSourceAdminResult> entityToAdminResultList(List<StorageSource> list);


}