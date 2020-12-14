package priv.gd.service.impl;

import org.springframework.stereotype.Service;
import priv.gd.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import priv.gd.pojo.*;
import priv.gd.service.SysService;

import java.util.List;
import java.util.UUID;

@Service
public class SysServiceImpl implements SysService {

    @Autowired
    private SysPermissionMapperCustom sysPermissionMapperCustom;
    @Autowired
    private SysRoleMapper roleMapper;
    @Autowired
    private SysRolePermissionMapper rolePermissionMapper;
    @Autowired
    private SysPermissionMapper sysPermissionMapper;
    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Override
    public List<MenuTree> loadMenuTree() {
        return sysPermissionMapperCustom.getMenuTree();
    }

    @Override
    public List<SysPermission> findPermissionListByUserId(String userid) throws Exception {
        return sysPermissionMapperCustom.findPermissionListByUserId(userid);
    }

    @Override
    public List<SysRole> findAllRoles() {
        return roleMapper.selectByExample(null);
    }

    @Override
    public List<SysPermission> findAllMenus() {
        SysPermissionExample sysPermissionExample = new SysPermissionExample();
        SysPermissionExample.Criteria criteria = sysPermissionExample.createCriteria();
        criteria.andTypeEqualTo("menu");
        List<SysPermission> permissions = sysPermissionMapper.selectByExample(sysPermissionExample);
        return permissions;
    }

    @Override
    public List<MenuTree> getAllMenuAndPermision() {
        return sysPermissionMapperCustom.getAllMenuAndPermision();
    }

    @Override
    public SysRole findRolesAndPermissionsByUserId(String userName) {
        return sysPermissionMapperCustom.findRoleAndPermissionListByUserId(userName);
    }

    @Override
    public List<SysPermission> findPermissionsByRoleId(String roleId) {
        return sysPermissionMapperCustom.findPermissionsByRoleId(roleId);
    }

    @Override
    public void updateRoleAndPermissions(String roleId, int[] permissionIds) {
        //先删除角色权限关系表中角色的权限关系
        SysRolePermissionExample example = new SysRolePermissionExample();
        SysRolePermissionExample.Criteria criteria = example.createCriteria();
        criteria.andSysRoleIdEqualTo(roleId);
        rolePermissionMapper.deleteByExample(example);
        //重新创建角色权限关系
        for (Integer pid : permissionIds) {
            SysRolePermission rolePermission = new SysRolePermission();
            String uuid = UUID.randomUUID().toString();
            rolePermission.setId(uuid);
            rolePermission.setSysRoleId(roleId);
            rolePermission.setSysPermissionId(pid.toString());

            rolePermissionMapper.insert(rolePermission);
        }
    }

    @Override
    public void addSysPermission(SysPermission permission) {
        sysPermissionMapper.insert(permission);
    }

    @Override
    public void addRoleAndPermissions(SysRole role, int[] permissionIds) {
        //添加角色
        roleMapper.insert(role);
        //添加角色和权限关系表
        for (int i = 0; i < permissionIds.length; i++) {
            SysRolePermission rolePermission = new SysRolePermission();
            //16进制随机码
            String uuid = UUID.randomUUID().toString();
            rolePermission.setId(uuid);
            rolePermission.setSysRoleId(role.getId());
            rolePermission.setSysPermissionId(permissionIds[i] + "");
            rolePermissionMapper.insert(rolePermission);
        }
    }

    @Override
    public List<MenuTree> findMenuTreeByRoleName(String name) {
        //通过用户名得到角色信息
        SysUserRoleExample sysUserRoleExample = new SysUserRoleExample();
        SysUserRoleExample.Criteria criteria = sysUserRoleExample.createCriteria();
        criteria.andSysUserIdEqualTo(name);
        List<SysUserRole> sysUserRoles = sysUserRoleMapper.selectByExample(sysUserRoleExample);
        SysUserRole sysUserRole = null;
        if(null!=sysUserRoles&&sysUserRoles.size()>0){
            sysUserRole = sysUserRoles.get(0);
        }
        List<MenuTree> list = null;
        //通过角色id查询菜单列表
        if(null != sysUserRole){
            list = sysPermissionMapperCustom.findMenuTreeByRoleId(sysUserRole.getSysRoleId());
        }
        return list;
    }

    @Override
    public List<SysPermission> findPermissionListByUserId2(String username) throws Exception {
        return sysPermissionMapperCustom.findPermissionListByUserId2(username);
    }

    @Override
    public SysRole findRoleByRoleName(String roleName) {
        SysRoleExample sysRoleExample = new SysRoleExample();
        SysRoleExample.Criteria criteria = sysRoleExample.createCriteria();
        criteria.andNameEqualTo(roleName);
        List<SysRole> sysRoles = roleMapper.selectByExample(sysRoleExample);
        if (null!=sysRoles&&sysRoles.size()>0){
            return sysRoles.get(0);
        }
        return null;
    }
}
