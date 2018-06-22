package framework.service;

import framework.entity.po.auth.Menu;

public interface MenuService {
	
	/**
	 * 保存菜单实体
	 * @param menu
	 * @return
	 */
	Menu saveMenu(Menu menu);

}
