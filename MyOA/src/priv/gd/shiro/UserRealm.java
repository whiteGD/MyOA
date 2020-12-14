package priv.gd.shiro;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import priv.gd.pojo.ActiveUser;
import priv.gd.pojo.Employee;
import priv.gd.pojo.MenuTree;
import priv.gd.pojo.SysPermission;
import priv.gd.service.EmployeeService;
import priv.gd.service.SysService;

import java.util.ArrayList;
import java.util.List;

public class UserRealm extends AuthorizingRealm {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private SysService sysService;

    /**
     * 认证
     * @param token
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        System.out.println("开始认证用户信息");
        String username = (String)token.getPrincipal();

        //去数据库查询用户信息
        Employee employee = employeeService.findEmployeeByUserName(username);
        if(null==employee){
            return null;
        }

        //查询权限
        List<SysPermission> permissions = null;
        try {
            permissions = sysService.findPermissionListByUserId(username);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //查询菜单
//        List<MenuTree> menuTree = sysService.loadMenuTree();
        //自定义方式查询用户权限内的菜单列表
        List<MenuTree> menuTree = sysService.findMenuTreeByRoleName(employee.getName());
        //把用户的身份信息重新封装
        ActiveUser activeUser = new ActiveUser();
        activeUser.setId(employee.getId());
        activeUser.setUserid(employee.getName());
        activeUser.setUsercode(employee.getName());
        activeUser.setUsername(employee.getName());
        activeUser.setManagerId(employee.getManagerId());
        activeUser.setMenuTree(menuTree);
        activeUser.setPermissions(permissions);
        String password_db = employee.getPassword();    // 数据库中的密码,密文
        String salt = employee.getSalt();           //数据库保存的盐值

        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(activeUser, password_db, ByteSource.Util.bytes(salt), this.getName());
        return info;
    }

    /**
     * 授权
     * @param principal
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principal) {
        System.out.println("开始授权");
        ActiveUser activeUser = (ActiveUser) principal.getPrimaryPrincipal();

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

        //添加权限
        List<SysPermission> permissions = activeUser.getPermissions();
        List<String> permisionList = new ArrayList<>();
        //判断数据是否为空
        if(null!=permissions&&permissions.size()>0){
            for (SysPermission sysPermission : permissions) {
                permisionList.add(sysPermission.getPercode());
            }
        }
        info.addStringPermissions(permisionList);
        return info;
    }
}
