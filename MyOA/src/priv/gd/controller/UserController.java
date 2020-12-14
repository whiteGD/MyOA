package priv.gd.controller;

import cn.hutool.captcha.LineCaptcha;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import priv.gd.pojo.*;
import priv.gd.service.EmployeeService;
import priv.gd.service.SysService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class UserController {
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private SysService sysService;

    /**
     * 登录
     * @param username
     * @param password
     * @param checkcode
     * @param request
     * @param model
     * @return
     */
    @RequestMapping(value = "/login")
    public String login(String username, String password, String checkcode, HttpServletRequest request, Model model){

        HttpSession session = request.getSession();
        LineCaptcha lineCaptcha = (LineCaptcha) session.getAttribute("lineCaptcha");
        Subject subject = SecurityUtils.getSubject();
        //判断用户是否二次登录
        if (subject.isAuthenticated()){
            subject.logout();
        }
        if(lineCaptcha.verify(checkcode)){
            AuthenticationToken token = new UsernamePasswordToken(username, password);
            //6.进行认证
            try {
                subject.login(token);
            }catch (UnknownAccountException e) {
                model.addAttribute("errorMsg", "用户账号不存在");
            }catch (IncorrectCredentialsException e){
                model.addAttribute("errorMsg", "密码不正确");
            }
        }else{
            model.addAttribute("errorMsg", "验证码不正确");
        }
        if(subject.isAuthenticated()){
            ActiveUser activeUser = (ActiveUser) subject.getPrincipal();
            session.setAttribute("activeUser",activeUser);
            System.out.println(activeUser.getMenuTree());
            if(null!=activeUser.getMenuTree()&&activeUser.getMenuTree().size()>0){
                for (MenuTree mt:activeUser.getMenuTree()) {
                    System.out.println("menuTree:"+mt.getName());
                }
            }
            return "redirect:index.jsp";
        }
        return "forward:/login.jsp";
    }

    /*@RequestMapping("index")
    public String index(){
        return "forward:index.jsp";
    }*/

    /**
     * 退出
     * @param session
     * @return
     */
    @RequestMapping("logout")
    public String logout(HttpSession session){
        session.invalidate();
        return "redirect:login.jsp";
    }

    /**
     * 用户管理
     * @param
     * @return
     */
    @RequestMapping("findUserList")
    public ModelAndView findUserList() {
        ModelAndView mv = new ModelAndView();
        List<SysRole> allRoles = sysService.findAllRoles();
        List<EmployeeCustom> userList = employeeService.findUserAndRoleList();
        for (EmployeeCustom ec:userList) {
            System.out.println(ec);
        }
        mv.addObject("allRoles", allRoles);
        mv.addObject("userList", userList);

        mv.setViewName("userlist");
        return mv;
    }

    /**
     * 角色添加
     * @return
     */
    @RequestMapping("toAddRole")
    public ModelAndView toAddRole() {
        List<MenuTree> allPermissions = sysService.loadMenuTree();
        List<SysPermission> menus = sysService.findAllMenus();
//        List<SysRole> permissionList = sysService.findRolesAndPermissions();

        ModelAndView mv = new ModelAndView("rolelist");
        mv.addObject("allPermissions", allPermissions);
        mv.addObject("menuTypes", menus);
//        mv.addObject("roleAndPermissionsList", permissionList);

        return mv;

    }

    /**
     * 角色列表
     * @return
     */
    @RequestMapping("findRoles")  //rest
    public ModelAndView findRoles() {
        ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
        List<SysRole> roles = sysService.findAllRoles();
        List<MenuTree> allMenuAndPermissions = sysService.getAllMenuAndPermision();

        ModelAndView mv = new ModelAndView("permissionlist");
        mv.addObject("allRoles", roles);
        mv.addObject("activeUser",activeUser);
        mv.addObject("allMenuAndPermissions", allMenuAndPermissions);
        return mv;
    }

    /**
     * 用户管理-查看用户角色和权限
     * @param userName
     * @return
     */
    @RequestMapping("viewPermissionByUser")
    @ResponseBody
    public SysRole viewPermissionByUser(String userName) {
        return sysService.findRolesAndPermissionsByUserId(userName);
    }

    /**
     * 用户管理-修改角色
     * @param roleId
     * @param userId
     * @return
     */
    @RequestMapping("assignRole")
    @ResponseBody
    public Map<String, String> assignRole(String roleId, String userId) {
        Map<String, String> map = new HashMap<>();
        try {
            employeeService.updateEmployeeRole(roleId, userId);
            map.put("msg", "分配权限成功");
        } catch (Exception e) {
            e.printStackTrace();
            map.put("msg", "分配权限失败");
        }
        return map;
    }



    /**
     * 用户管理-添加用户
     * @param user
     * @return
     */
    @RequestMapping("saveUser")
    @ResponseBody
    public Map<String, String> saveUser(Employee user) {
        Md5Hash md5Hash = new Md5Hash(user.getPassword(),"eteokues",2);
        user.setPassword(md5Hash.toString());
        user.setSalt("eteokues");
        Map<String, String> map = new HashMap<>();
        try {
            employeeService.addUser(user);
            map.put("saveUserMessage","添加成功");
        }catch (Exception e) {
            map.put("saveUserMessage","添加失败,账号或邮箱重复");
        }
        return map;
    }

    /**
     * 用户管理-查看上一级管理人员
     * @param level
     * @return
     */
    @RequestMapping("findNextManager")
    @ResponseBody
    public List<Employee> findNextManager(int level) {
        level++; //加一，表示下一个级别
        List<Employee> list = employeeService.findEmployeeByLevel(level);
        System.out.println(list);
        return list;

    }

    /**
     * 角色列表-查看编辑信息
     * @param roleId
     * @return
     */
    @RequestMapping("loadMyPermissions")
    @ResponseBody
    public List<SysPermission> loadMyPermissions(String roleId) {
        List<SysPermission> list = sysService.findPermissionsByRoleId(roleId);
        return list;
    }

    /**
     * 角色列表-编辑修改权限
     * @param roleId
     * @param permissionIds
     * @return
     */
    @RequestMapping("updateRoleAndPermission")
    @ResponseBody
    public Map<String,String> updateRoleAndPermission(String roleId,int[] permissionIds) {
        sysService.updateRoleAndPermissions(roleId, permissionIds);
        Map<String,String> map = new HashMap<>();
        map.put("msg","修改成功");
        return map;
    }

    /**
     * 角色添加-保存权限
     * @param permission
     * @return
     */
    @RequestMapping("saveSubmitPermission")
    @ResponseBody
    public Map<String,String> saveSubmitPermission(SysPermission permission) {
        if (permission.getAvailable() == null) {
            permission.setAvailable("0");
        }
        sysService.addSysPermission(permission);
        HashMap<String, String> map = new HashMap<>();
        map.put("sspMSG","保存成功");
        return map;
    }

    /**
     * 角色添加-添加角色和权限
     * @param role
     * @param permissionIds
     * @return
     */
    @RequestMapping("saveRoleAndPermissions")
    @ResponseBody
    public Map<String, String> saveRoleAndPermissions(SysRole role,int[] permissionIds) {
        //添加之前查询是否角色名冲突
        String roleName = role.getName();
        SysRole sysRole = sysService.findRoleByRoleName(roleName);
        Map<String, String> map = new HashMap<>();
        if(null == sysRole){
            System.out.println(111);
            System.out.println(sysRole);
            //设置role主键，使用uuid
            String uuid = UUID.randomUUID().toString();
            role.setId(uuid);
            //默认可用
            role.setAvailable("1");
            sysService.addRoleAndPermissions(role, permissionIds);
            map.put("srapMSG","保存成功");
        }else {
            System.out.println(222);
            map.put("srapMSG","保存失败，角色名重复");
        }
        return map;
    }
}
