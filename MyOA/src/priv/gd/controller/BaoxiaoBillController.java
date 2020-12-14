package priv.gd.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import priv.gd.pojo.ActiveUser;
import priv.gd.pojo.BaoxiaoBill;
import priv.gd.service.BaoxiaoService;
import priv.gd.service.WorkFlowService;
import priv.gd.utils.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class BaoxiaoBillController {

    @Autowired
    private BaoxiaoService baoxiaoService;

    @Autowired
    private WorkFlowService workFlowService;

    /**
     * 显示我的报销单列表
     * @return
     */
    @RequestMapping("myBaoxiaoBill")
    public String myBaoxiaoBill(Integer page,Integer limit,Model model){
        int pageNow = 0;
        int pageSize = 6;
        if(null != page && !page.equals("")){
            pageNow = page;
        }
        if(null != limit && !limit.equals("")){
            pageSize = limit;
        }
        ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
        Page pageList = baoxiaoService.findLeaveBillPageListByUser(activeUser.getId(),pageNow,pageSize);
        List<BaoxiaoBill> list = pageList.getResult();
        long total = pageList.getTotal();
        int pageNum = pageList.getPageNum();
        System.out.println("当前页:"+pageNum);
        System.out.println("total:"+total);
        model.addAttribute("baoxiaoList",list);
        model.addAttribute("count",total);
        model.addAttribute("pageNow",pageNum);
        model.addAttribute("pageCount",total%pageSize==0?total%pageSize:total%pageSize+1);
        return "baoxiaobill";
    }

    /**
     * 通过报销表查看流程图
     * @param billId
     * @param model
     * @return
     */
    @RequestMapping("viewCurrentImageByBill")
    public String viewCurrentImageByBill(long billId,ModelMap model) {
        String BUSSINESS_KEY = Constants.BAOXIAO_KEY + "." + billId;
        Task task = workFlowService.findTaskByBussinessKey(BUSSINESS_KEY);
        /**一：查看流程图*/
        //1：获取任务ID，获取任务对象，使用任务对象获取流程定义ID，查询流程定义对象
        ProcessDefinition pd = workFlowService.findProcessDefinitionByTaskId(task.getId());

        model.addAttribute("deploymentId", pd.getDeploymentId());
        model.addAttribute("imageName", pd.getDiagramResourceName());
        /**二：查看当前活动，获取当期活动对应的坐标x,y,width,height，将4个值存放到Map<String,Object>中*/
        Map<String, Object> map = workFlowService.findCoordingByTask(task.getId());

        model.addAttribute("acs", map);
        return "viewimage";
    }

    @RequestMapping("leaveBillAction_delete")
    public String leaveBillDelete(String id, Model model){
        baoxiaoService.deleteBaoxiaoBillById(id);
        model.addAttribute("leaveBillMessage","删除成功");
        return "forward:/myBaoxiaoBill";
    }
}
