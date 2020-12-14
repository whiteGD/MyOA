package priv.gd.controller;

import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import priv.gd.pojo.ActiveUser;
import priv.gd.pojo.BaoxiaoBill;
import priv.gd.service.BaoxiaoService;
import priv.gd.service.WorkFlowService;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Controller
public class WorkFlowController {
    @Autowired
    private WorkFlowService workFlowService;
    @Autowired
    private BaoxiaoService baoxiaoService;

    @RequestMapping(value = "deployProcess")
    public String deployProcess(String processName, MultipartFile fileName) {

        try {
            workFlowService.saveNewDeploye(fileName.getInputStream(), processName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //跳转到 查看流程
        return "redirect:/processDefinitionList";
    }

    @RequestMapping("processDefinitionList")
    public ModelAndView processDefinitionList(){
        //通过构造方法设置跳转页面
        ModelAndView mav = new ModelAndView("workflow_list");
        //查询部署对象信息
        List<Deployment> depList = workFlowService.findDeploymentList();
        //查询流程定义信息
        List<ProcessDefinition> pdList = workFlowService.findProcessDefinitionList();
        mav.addObject("depList",depList);
        mav.addObject("pdList",pdList);
        return mav;
    }

    /**
     * 报销申请,流程开始
     * @param baoxiaoBill
     * @return
     */
    @RequestMapping("/saveStartBaoxiao")
    public String saveStartBaoxiao(BaoxiaoBill baoxiaoBill) {
        System.out.println("报销备注"+baoxiaoBill.getRemark());

        //设置当前时间
        baoxiaoBill.setCreatdate(new Date());
        ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
        baoxiaoBill.setUserId(activeUser.getId());
        //更新状态从0变成1（初始录入-->审核中）
        baoxiaoBill.setState(1);
        baoxiaoService.saveBaoxiao(baoxiaoBill);

        workFlowService.saveStartProcess(baoxiaoBill.getId(), activeUser.getUsername());
        return "redirect:/myTaskList";
    }


    /**
     * 查看我的待办事务
     * @return
     */
    @RequestMapping("myTaskList")
    public ModelAndView getTaskList() {
        ModelAndView mv = new ModelAndView();
        ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
        List<Task> list = workFlowService.findTaskListByName(activeUser.getUsername());

        mv.addObject("taskList", list);
        mv.setViewName("workflow_task");
        return mv;
    }

    /**
     * 办理任务
     * @param taskId
     * @return
     */
    @RequestMapping("/viewTaskForm")
    public ModelAndView viewTaskForm(String taskId) {
        ModelAndView mv = new ModelAndView();

        BaoxiaoBill bill = this.workFlowService.findBaoxiaoBillByTaskId(taskId);
        List<Comment> list = this.workFlowService.findCommentByTaskId(taskId);
        List<String> outcomeList = this.workFlowService.findOutComeListByTaskId(taskId);

        mv.addObject("baoxiaoBill", bill);
        mv.addObject("commentList", list);
        mv.addObject("outcomeList", outcomeList);
        mv.addObject("taskId", taskId);

        mv.setViewName("approve_baoxiao");
        return mv;
    }

    /**
     *  查看历史的批注信息
     */
    @RequestMapping("viewHisComment")
    public String viewHisComment(long id, ModelMap model){
        BaoxiaoBill bill = baoxiaoService.findBaoxiaoBillById(id);
        model.addAttribute("baoxiaoBill", bill);
        List<Comment> commentList = workFlowService.findCommentByBaoxiaoBillId(id);
        model.addAttribute("commentList", commentList);

        return "workflow_commentlist";
    }

    /**
     * 提交任务
     * @param id
     * @param taskId
     * @param comment
     * @param outcome
     * @return
     */
    @RequestMapping("submitTask")
    @ResponseBody
    public Map<String,String> submitTask(String id,String taskId,String comment,String outcome){
        Map<String,String> map = new HashMap<>();
        ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
        String username = activeUser.getUsername();
        workFlowService.saveSubmitTask(Long.parseLong(id), taskId, comment, outcome, username);
        map.put("submitTaskMSG","办理成功");
        return map;
    }

    /**
     * 查看当前流程图（查看当前活动节点，并使用红色的框标注）
     */
    @RequestMapping("viewCurrentImage")
    public String viewCurrentImage(String taskId,ModelMap model){
        /**一：查看流程图*/
        //1：获取任务ID，获取任务对象，使用任务对象获取流程定义ID，查询流程定义对象
        ProcessDefinition pd = workFlowService.findProcessDefinitionByTaskId(taskId);

        model.addAttribute("deploymentId", pd.getDeploymentId());
        model.addAttribute("imageName", pd.getDiagramResourceName());
        /**二：查看当前活动，获取当前活动对应的坐标x,y,width,height，将4个值存放到Map<String,Object>中*/
        Map<String, Object> map = workFlowService.findCoordingByTask(taskId);

        model.addAttribute("acs", map);
        return "viewimage";
    }

    /**
     * 查看流程图
     * @throws Exception
     */
    @RequestMapping("/viewImage")
    public String viewImage(String deploymentId, String imageName, HttpServletResponse response) throws Exception{
        InputStream in = workFlowService.findImageInputStream(deploymentId,imageName);
        ServletOutputStream os = response.getOutputStream();
        byte[] b = new byte[1024];
        while((in.read(b))!=-1){
            os.write(b);
        }
        in.close();
        os.close();
        return null;
    }

    /**
     * 删除部署信息
     * @param deploymentId
     * @return
     */
    @RequestMapping("delDeployment")
    @ResponseBody
    public Map<String, String> delDeployment(String deploymentId){
        Map<String, String> map = new HashMap<>();
        //使用部署对象ID，删除流程定义
        workFlowService.deleteProcessDefinitionByDeploymentId(deploymentId);
        map.put("delDeploymentMessage","删除成功");
        return map;
    }
}
