package priv.gd.mapper;

import priv.gd.pojo.EmployeeCustom;
import priv.gd.pojo.MenuTree;
import priv.gd.pojo.SysPermission;
import priv.gd.pojo.SysRole;

import java.util.List;


public interface SysPermissionMapperCustom {

	//根据用户id查询菜单
	public List<SysPermission> findMenuListByUserId(String userid)throws Exception;
	//根据用户id查询权限url
	public List<SysPermission> findPermissionListByUserId(String userid)throws Exception;
	public List<MenuTree> getMenuTree();

	public List<SysPermission> getSubMenu();

	public List<EmployeeCustom> findUserAndRoleList();

	public SysRole findRoleAndPermissionListByUserId(String userId);

	public List<SysRole> findRoleAndPermissionList();

	public List<SysPermission> findMenuAndPermissionByUserId(String userId);

	public List<MenuTree> getAllMenuAndPermision();

	public List<SysPermission> findPermissionsByRoleId(String roleId);

	public List<MenuTree> findMenuTreeByRoleId(String roleId);

	public List<SysPermission> findPermissionListByUserId2(String userid)throws Exception;
}
