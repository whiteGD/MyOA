package priv.gd.service;

import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import priv.gd.pojo.BaoxiaoBill;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface WorkFlowService {
    /**
     * 保存部署流程
     * @param in
     * @param filename
     */
    void saveNewDeploye(InputStream in, String filename);

    /**
     * 查看部署对象信息
     * @return
     */
    List<Deployment> findDeploymentList();

    /**
     * 查看流程定义信息
     * @return
     */
    List<ProcessDefinition>findProcessDefinitionList();

    /**
     * 启动流程
     * @param baoxiaoId
     * @param username
     */
    void saveStartProcess(Long baoxiaoId, String username);

    /**
     * 通过用户名查询待办任务列表
     * @param username
     * @return
     */
    List<Task> findTaskListByName(String username);

    /**
     * 通过任务id查找报销表信息
     * @param taskId
     * @return
     */
    BaoxiaoBill findBaoxiaoBillByTaskId(String taskId);

    /**
     * 通过任务id查找批注
     * @param taskId
     * @return
     */
    List<Comment> findCommentByTaskId(String taskId);

    /**
     * 通过任务id查找结果
     * @param taskId
     * @return
     */
    List<String> findOutComeListByTaskId(String taskId);

    /**
     * 通过报销单ID，查询历史的批注信息
     * @param id
     * @return
     */
    List<Comment> findCommentByBaoxiaoBillId(long id);

    /**
     * 提交任务
     * @param id
     * @param taskId
     * @param comment
     * @param outcome
     * @param username
     */
    void saveSubmitTask(long id, String taskId, String comment, String outcome, String username);

    /**
     * 通过任务id获取流程定义对象
     * @param taskId
     * @return
     */
    ProcessDefinition findProcessDefinitionByTaskId(String taskId);

    /**
     * 通过任务id获得当前活动对应的坐标x,y,width,height
     * @param taskId
     * @return
     */
    Map<String, Object> findCoordingByTask(String taskId);

    /**
     * 通过部署id和名字，将部署流程图转换成流
     * @param deploymentId
     * @param imageName
     */
    InputStream findImageInputStream(String deploymentId, String imageName);

    /**
     * 通过BussinessKey查找任务信息
     * @param BUSSINESS_KEY
     * @return
     */
    Task findTaskByBussinessKey(String BUSSINESS_KEY);

    /**
     * 通过部署id删除部署信息
     * @param deploymentId
     */
    void deleteProcessDefinitionByDeploymentId(String deploymentId);
}
