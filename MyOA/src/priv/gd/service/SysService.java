package priv.gd.service;


import org.activiti.engine.identity.User;
import priv.gd.pojo.SysPermission;
import priv.gd.pojo.MenuTree;
import priv.gd.pojo.SysRole;
import priv.gd.pojo.SysUserRole;

import java.util.List;

public interface SysService {
    /**
     * 查询加载菜单
     * @return
     */
    public List<MenuTree> loadMenuTree();

    /**
     * 根据用户名查询权限
     * @param roleId
     * @return
     */
    public List<SysPermission> findPermissionListByUserId(String roleId) throws Exception;

    /**
     * 查询所有角色
     * @return
     */
    List<SysRole> findAllRoles();

    /**
     * 查询所有一级(父)菜单
     * @return
     */
    List<SysPermission> findAllMenus();

    /**
     * 查询菜单和权限
     * @return
     */
    List<MenuTree> getAllMenuAndPermision();

    /**
     * 通过用户名查询角色和权限
     * @param userName
     * @return
     */
    SysRole findRolesAndPermissionsByUserId(String userName);

    /**
     * 通过角色id查询权限
     * @param roleId
     * @return
     */
    List<SysPermission> findPermissionsByRoleId(String roleId);

    /**
     * 通过角色id修改角色权限
     * @param roleId
     * @param permissionIds
     */
    void updateRoleAndPermissions(String roleId, int[] permissionIds);

    /**
     * 添加新权限
     * @param permission
     */
    void addSysPermission(SysPermission permission);

    /**
     *  添加角色和角色对应的权限
     * @param role
     * @param permissionIds
     */
    void addRoleAndPermissions(SysRole role, int[] permissionIds);

    /**
     * 通过角色id查询角色内权限的菜单
     * @return
     */
    List<MenuTree> findMenuTreeByRoleName(String name);

    /**
     * 根据用户id查询角色拥有的权限
     * @param username
     * @return
     */
    List<SysPermission> findPermissionListByUserId2(String username) throws Exception;

    /**
     * 通过角色名查找角色信息
     * @param roleName
     * @return
     */
    SysRole findRoleByRoleName(String roleName);

    /**
     * 通过角色id查询用户集合
     * @param roleId
     * @return
     */
    List<SysUserRole> findUserByRoleId(String roleId);

    /**
     * 通过角色id删除角色信息
     * @param roleId
     */
    void deleteRole(String roleId);

    /**
     * 通过角色id删除角色对应的权限信息
     * @param roleId
     */
    void deletePermissions(String roleId);
}
