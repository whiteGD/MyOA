package priv.gd.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import priv.gd.mapper.EmployeeMapper;
import priv.gd.mapper.SysPermissionMapperCustom;
import priv.gd.mapper.SysUserRoleMapper;
import priv.gd.pojo.*;
import priv.gd.service.EmployeeService;

import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;
    @Autowired
    private SysPermissionMapperCustom permissionMapper;
    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Override
    public Employee findEmployeeByUserName(String username) {
        EmployeeExample employeeExample = new EmployeeExample();
        EmployeeExample.Criteria criteria = employeeExample.createCriteria();
        criteria.andNameEqualTo(username);
        List<Employee> employees = employeeMapper.selectByExample(employeeExample);
        if(null!=employees&&employees.size()>0){
            return employees.get(0);
        }
        return null;
    }

    @Override
    public Employee findEmployeeManager(Long managerId) {
        Employee employee = employeeMapper.selectByPrimaryKey(managerId);
        return employee;
    }

    @Override
    public List<EmployeeCustom> findUserAndRoleList() {
        return permissionMapper.findUserAndRoleList();
    }

    @Override
    public void updateEmployeeRole(String roleId, String userId) {
        SysUserRoleExample example = new SysUserRoleExample();
        SysUserRoleExample.Criteria criteria = example.createCriteria();
        criteria.andSysUserIdEqualTo(userId);

        SysUserRole userRole = userRoleMapper.selectByExample(example).get(0);
        userRole.setSysRoleId(roleId);

        userRoleMapper.updateByPrimaryKey(userRole);
    }

    @Override
    public List<Employee> findEmployeeByLevel(int level) {
        EmployeeExample example = new EmployeeExample();
        EmployeeExample.Criteria criteria = example.createCriteria();
        criteria.andRoleEqualTo(level);
        List<Employee> list = employeeMapper.selectByExample(example);
        return list;
    }

    @Override
    public void addUser(Employee user) throws Exception {
        //在用户表添加信息
        employeeMapper.insert(user);
        EmployeeExample employeeExample = new EmployeeExample();
        EmployeeExample.Criteria criteria = employeeExample.createCriteria();
        criteria.andNameEqualTo(user.getName());
        List<Employee> employees = employeeMapper.selectByExample(employeeExample);
        //在sys_user_role添加信息
        SysUserRole sysUserRole = new SysUserRole();
        sysUserRole.setId(employees.get(0).getId().toString());
        sysUserRole.setSysUserId(user.getName());
        sysUserRole.setSysRoleId(user.getRole().toString());
        userRoleMapper.insert(sysUserRole);
    }
}
