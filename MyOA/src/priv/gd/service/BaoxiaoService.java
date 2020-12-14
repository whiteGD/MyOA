package priv.gd.service;

import com.github.pagehelper.Page;
import priv.gd.pojo.BaoxiaoBill;

import java.util.List;

public interface BaoxiaoService {
    /**
     * 通过用户id查找用户用户报销单
     * @param id
     * @return
     */
    List<BaoxiaoBill> findLeaveBillListByUser(long id);

    /**
     * 保存用户报销单信息
     * @param baoxiaoBill
     */
    void saveBaoxiao(BaoxiaoBill baoxiaoBill);

    /**
     * 报销报销单ID，查询报销单对象
     * @param id
     * @return
     */
    BaoxiaoBill findBaoxiaoBillById(long id);

    /**
     *
     */
    void deleteBaoxiaoBillById(String id);

    /**
     * 通过用户id查找用户报销单信息，分页效果
     * @param id
     * @param pageNow
     * @param pageSize
     * @return
     */
    Page findLeaveBillPageListByUser(long id, int pageNow, int pageSize);
}
