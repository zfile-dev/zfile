package im.zhaojun.zfile.module.storage.controller.helper;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import im.zhaojun.zfile.module.storage.model.request.GetGoogleDriveListRequest;
import im.zhaojun.zfile.module.storage.model.result.GoogleDriveInfoResult;
import im.zhaojun.zfile.core.util.AjaxJson;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhaojun
 */
@Api(tags = "gd 工具辅助模块")
@Controller
@RequestMapping("/gd")
public class GoogleDriveHelperController {
	
	@PostMapping("/drives")
	@ResponseBody
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "获取 gd drives 列表")
	public AjaxJson<List<GoogleDriveInfoResult>> getDrives(@Valid @RequestBody GetGoogleDriveListRequest googleDriveListRequest) {
		List<GoogleDriveInfoResult> bucketNameList = new ArrayList<>();
		String accessToken = googleDriveListRequest.getAccessToken();
		
		HttpRequest httpRequest = HttpUtil.createGet("https://www.googleapis.com/drive/v3/drives");
		httpRequest.header("Authorization", "Bearer " + accessToken);
		
		HttpResponse httpResponse = httpRequest.execute();
		
		String body = httpResponse.body();
		JSONObject jsonObject = JSON.parseObject(body);
		JSONArray drives = jsonObject.getJSONArray("drives");
		
		for (int i = 0; i < drives.size(); i++) {
			JSONObject drive = drives.getJSONObject(i);
			String id = drive.getString("id");
			String name = drive.getString("name");
			bucketNameList.add(new GoogleDriveInfoResult(id, name));
		}
		
		return AjaxJson.getSuccessData(bucketNameList);
	}
	
}