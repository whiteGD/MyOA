package priv.gd.utils;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.apache.shiro.SecurityUtils;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import priv.gd.pojo.ActiveUser;
import priv.gd.pojo.Employee;
import priv.gd.service.EmployeeService;

public class ManagerTaskHandler implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        //获得WebApplicationContext
        WebApplicationContext webApplicationContext = ContextLoader.getCurrentWebApplicationContext();
        //获得当前用户
        ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
        //通过spring得到bean对象
        EmployeeService employeeService = (EmployeeService) webApplicationContext.getBean("employeeServiceImpl");
        Employee manager = employeeService.findEmployeeManager(activeUser.getManagerId());
        delegateTask.setAssignee(manager.getName());
    }
}
