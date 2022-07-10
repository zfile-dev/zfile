package im.zhaojun.zfile.admin.annoation.select.impl;

import im.zhaojun.zfile.admin.annoation.StorageParamItem;
import im.zhaojun.zfile.admin.annoation.StorageParamSelect;
import im.zhaojun.zfile.admin.model.param.IStorageParam;
import im.zhaojun.zfile.admin.annoation.model.StorageSourceParamDef;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * 编码格式动态参数.
 *
 * @author zhaojun
 */
public class EncodingStorageParamSelect implements StorageParamSelect {

	@Override
	public List<StorageSourceParamDef.Options> getOptions(StorageParamItem storageParamItem, IStorageParam targetParam) {
		List<StorageSourceParamDef.Options> options = new ArrayList<>();

		for (String name : Charset.availableCharsets().keySet()) {
			StorageSourceParamDef.Options option = new StorageSourceParamDef.Options(name);
			options.add(option);
		}
		return options;
	}

}