package priv.gd.service;


import priv.gd.pojo.Employee;
import priv.gd.pojo.EmployeeCustom;

import java.util.List;

public interface EmployeeService {
    /**
     *  根据员工帐号查找员工
     */
    Employee findEmployeeByUserName(String username);

    /**
     * 根据员工上级id查找上级信息
     * @param managerId
     * @return
     */
    Employee findEmployeeManager(Long managerId);

    /**
     * 查询用户和所有角色
     * @return
     */
    List<EmployeeCustom> findUserAndRoleList();

    /**
     * 修改用户角色
     * @param roleId
     * @param userId
     */
    void updateEmployeeRole(String roleId, String userId);

    /**
     *  通过员工级别获得员工信息
     * @param level
     * @return
     */
    List<Employee> findEmployeeByLevel(int level);

    /**
     * 添加员工
     * @param user
     */
    void addUser(Employee user) throws Exception;
}
