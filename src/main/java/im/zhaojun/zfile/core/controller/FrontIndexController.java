package im.zhaojun.zfile.core.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 处理前端首页 Controller
 *
 * @author zhaojun
 */
@Controller
public class FrontIndexController {

	/**
	 * 所有未找到的页面都跳转到首页, 用户解决 vue history 直接访问 404 的问题
	 *
	 * @return  转发到 /index.html
	 */
	@RequestMapping(value = "/**/{[path:[^\\.]*}")
	public String redirect() {
		// Forward to home page so that route is preserved.
		return "forward:/";
	}

}