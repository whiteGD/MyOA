package priv.gd.mapper;

import priv.gd.pojo.SysPermission;
import priv.gd.pojo.TreeMenu;

import java.util.List;

public interface SysPermissionCustomMapper {

	
	public List<TreeMenu> getTreeMenu();
	
	public List<SysPermission> getSubMenu(int id);
}
