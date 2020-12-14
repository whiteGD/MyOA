package priv.gd.service.impl;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import priv.gd.mapper.BaoxiaoBillMapper;
import priv.gd.pojo.BaoxiaoBill;
import priv.gd.service.WorkFlowService;
import priv.gd.utils.Constants;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

@Service
public class WorkFlowServiceImpl implements WorkFlowService {

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private BaoxiaoBillMapper baoxiaoBillMapper;
    @Autowired
    private HistoryService historyService;

    @Override
    public void saveNewDeploye(InputStream in, String filename) {
        ZipInputStream zipInputStream = new ZipInputStream(in);
        repositoryService.createDeployment()
                .name(filename)
                .addZipInputStream(zipInputStream)
                .deploy();
    }

    @Override
    public List<Deployment> findDeploymentList() {
        List<Deployment> list = repositoryService.createDeploymentQuery()
                .orderByDeploymenTime().desc().list();
        return list;
    }

    @Override
    public List<ProcessDefinition> findProcessDefinitionList() {
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery()
                .orderByProcessDefinitionVersion()
                .desc().list();
        return list;
    }

    @Override
    public void saveStartProcess(Long baoxiaoId, String username) {
        //使用当前对象获取到流程定义的key（对象的名称就是流程定义的key）
        String key= Constants.BAOXIAO_KEY;
        /**
         *    从Session中获取当前任务的办理人，使用流程变量设置下一个任务的办理人
         * inputUser是流程变量的名称，
         * 获取的办理人是流程变量的值
         */
        Map<String, Object> variables = new HashMap<String,Object>();
        variables.put("inputUser", username);//表示惟一用户

        //格式：baoxiao.id的形式（使用流程变量）
        String objId = key+"."+baoxiaoId;
        variables.put("objId", objId);
        //5：使用流程定义的key，启动流程实例，同时设置流程变量，同时向正在执行的执行对象表中的字段BUSINESS_KEY添加业务数据，同时让流程关联业务
        runtimeService.startProcessInstanceByKey(key,objId,variables);
    }

    @Override
    public List<Task> findTaskListByName(String username) {
        List<Task> list = taskService.createTaskQuery()
                .taskAssignee(username)//指定个人任务查询
                .orderByTaskCreateTime().desc()
                .list();
        return list;
    }

    @Override
    public BaoxiaoBill findBaoxiaoBillByTaskId(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId()).singleResult();
        String bussiness_key = processInstance.getBusinessKey();
        //通过BusinessKey去对应报销表中的id
        String baoxiaobillId = "";
        if (!bussiness_key.isEmpty()){
            System.out.println("true");
            baoxiaobillId = bussiness_key.split("\\.")[1];
        }
        BaoxiaoBill baoxiaoBill = baoxiaoBillMapper.selectByPrimaryKey(Long.parseLong(baoxiaobillId));
        return baoxiaoBill;
    }

    @Override
    public List<Comment> findCommentByTaskId(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        List<Comment> processInstanceComments =
                taskService.getProcessInstanceComments(task.getProcessInstanceId());
        return processInstanceComments;
    }

    @Override
    public List<String> findOutComeListByTaskId(String taskId) {
        //返回存放连线的名称集合
        ArrayList<String> list = new ArrayList<>();
        //1:使用任务ID，查询任务对象
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        //2:通过流程定义ID查询ProcessDefinitionEntiy对象,ProcessDefinitionEntiy对象保存着流程图中的连接线和连接对象的id和name
        ProcessDefinitionEntity processDefinitionEntity =
                (ProcessDefinitionEntity) repositoryService.getProcessDefinition(task.getProcessDefinitionId());
        //3:使用流程实例ID，查询正在执行的执行对象表，返回流程实例对象
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId()).singleResult();
        //获取当前活动的id
        String act_id = processInstance.getActivityId();
        //4：获取当前的活动
        ActivityImpl activityImpl = processDefinitionEntity.findActivity(act_id);
        //5：获取当前流程图中的活动完成之后连接线集合
        List<PvmTransition> pvmList = activityImpl.getOutgoingTransitions();
        //判断是否为空
        if (null!=pvmList&&pvmList.size()>0){
            for (PvmTransition pvm:pvmList) {
                String name = (String) pvm.getProperty("name");
                //如果不为空,则添加流程图中连接线的名称
                if(StringUtils.isNotBlank(name)){
                    list.add(name);
                }else {     //如果为空,则添加默认按钮属性
                    list.add("默认提交");
                }
            }
        }
        return list;
    }

    @Override
    public List<Comment> findCommentByBaoxiaoBillId(long id) {
        String bussiness_key = Constants.BAOXIAO_KEY +"."+id;
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceBusinessKey(bussiness_key).singleResult();
        List<Comment> commentList = taskService.getProcessInstanceComments(historicProcessInstance.getId());
        return commentList;
    }

    @Override
    public void saveSubmitTask(long id, String taskId, String comment, String outcome, String username) {
        /**
         * 1：在完成之前，添加一个批注信息，向act_hi_comment表中添加数据，用于记录对当前申请人的一些审核信息
         */
        //使用任务ID，查询任务对象，获取流程流程实例ID
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

        //获取流程实例ID
        String processInstanceId = task.getProcessInstanceId();
        /**
         * 注意：添加批注的时候，由于Activiti底层代码是使用：
         * String userId = Authentication.getAuthenticatedUserId();
         * CommentEntity comment = new CommentEntity();
         * comment.setUserId(userId);
         * 所有需要从Session中获取当前登录人，作为该任务的办理人（审核人），对应act_hi_comment表中的User_ID的字段，不过不添加审核人，该字段为null
         * 所以要求，添加配置执行使用Authentication.setAuthenticatedUserId();添加当前任务的审核人
         * */
        //加当前任务的审核人
        Authentication.setAuthenticatedUserId(username);
        //添加批注
        taskService.addComment(taskId, processInstanceId, comment);
        /**
         *  如果连线的名称是“默认提交”，那么就不需要设置，如果不是，就需要设置流程变量
         *  在完成任务之前，设置流程变量，按照连线的名称，去完成任务
         *  流程变量的名称：outcome
         *  流程变量的值：连线的名称
         */
        Map<String, Object> variables = new HashMap<>();
        if(outcome!=null && !outcome.equals("默认提交")){
            variables.put("message", outcome);
            //3：使用任务ID，完成当前人的个人任务，同时流程变量
            taskService.complete(taskId, variables);
        } else {
            taskService.complete(taskId);
        }
        /**
         *  在完成任务之后，判断流程是否结束
         *  如果流程结束了，更新请假单表的状态从1变成2（审核中-->审核完成）
         */
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()//
                .processInstanceId(processInstanceId)//使用流程实例ID查询
                .singleResult();
        //如果processInstance等于空代表流程结束了
        if(null==processInstance){
            //更新请假单表的状态从1变成2（审核中-->审核完成）
            BaoxiaoBill bill = baoxiaoBillMapper.selectByPrimaryKey(id);
            bill.setState(2);
            baoxiaoBillMapper.updateByPrimaryKey(bill);
        }
    }

    @Override
    public ProcessDefinition findProcessDefinitionByTaskId(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        ProcessDefinition processDefinition = repositoryService.getProcessDefinition(task.getProcessDefinitionId());
        return processDefinition;
    }

    @Override
    public Map<String, Object> findCoordingByTask(String taskId) {
        //存放坐标
        Map<String, Object> map = new HashMap<String,Object>();
        //使用任务ID，查询任务对象
        Task task = taskService.createTaskQuery()//
                .taskId(taskId)//使用任务ID查询
                .singleResult();
        //获取流程定义的ID
        String processDefinitionId = task.getProcessDefinitionId();
        //获取流程定义的实体对象（对应.bpmn文件中的数据）
        ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity)repositoryService.getProcessDefinition(processDefinitionId);
        //流程实例ID
        String processInstanceId = task.getProcessInstanceId();
        //使用流程实例ID，查询正在执行的执行对象表，获取当前活动对应的流程实例对象
        ProcessInstance pi = runtimeService.createProcessInstanceQuery()//创建流程实例查询
                .processInstanceId(processInstanceId)//使用流程实例ID查询
                .singleResult();
        //获取当前活动的ID
        String activityId = pi.getActivityId();
        //获取当前活动对象
        ActivityImpl activityImpl = processDefinitionEntity.findActivity(activityId);//活动ID
        //获取坐标
        map.put("x", activityImpl.getX());
        map.put("y", activityImpl.getY());
        map.put("width", activityImpl.getWidth());
        map.put("height", activityImpl.getHeight());
        return map;
    }

    @Override
    public InputStream findImageInputStream(String deploymentId, String imageName) {
        InputStream is = repositoryService.getResourceAsStream(deploymentId, imageName);
        return is;
    }

    @Override
    public Task findTaskByBussinessKey(String BUSSINESS_KEY) {
        Task task = this.taskService.createTaskQuery().processInstanceBusinessKey(BUSSINESS_KEY).singleResult();
        return task;
    }

    @Override
    public void deleteProcessDefinitionByDeploymentId(String deploymentId) {
        repositoryService.deleteDeployment(deploymentId, true);
    }


}
