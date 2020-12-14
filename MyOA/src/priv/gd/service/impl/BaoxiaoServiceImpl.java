package priv.gd.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import priv.gd.mapper.BaoxiaoBillMapper;
import priv.gd.pojo.BaoxiaoBill;
import priv.gd.pojo.BaoxiaoBillExample;
import priv.gd.service.BaoxiaoService;

import java.util.Collections;
import java.util.List;

@Service
public class BaoxiaoServiceImpl implements BaoxiaoService {

    @Autowired
    private BaoxiaoBillMapper baoxiaoBillMapper;

    @Override
    public List<BaoxiaoBill> findLeaveBillListByUser(long id) {
        BaoxiaoBillExample baoxiaoBillExample = new BaoxiaoBillExample();
        BaoxiaoBillExample.Criteria criteria = baoxiaoBillExample.createCriteria();
        criteria.andUserIdEqualTo(id);
        List<BaoxiaoBill> baoxiaoBills = baoxiaoBillMapper.selectByExample(baoxiaoBillExample);
        //降序排序
//        Collections.reverse(baoxiaoBills);
        return baoxiaoBills;
    }

    @Override
    public void saveBaoxiao(BaoxiaoBill baoxiaoBill) {
        //判断用户是保存还是修改
        Long id = baoxiaoBill.getId();
        if(null==id){
            //进行添加
            baoxiaoBillMapper.insert(baoxiaoBill);
        }else{
            baoxiaoBillMapper.updateByPrimaryKey(baoxiaoBill);
        }
    }

    @Override
    public BaoxiaoBill findBaoxiaoBillById(long id) {
        BaoxiaoBill bill = baoxiaoBillMapper.selectByPrimaryKey(id);
        return bill;
    }

    @Override
    public void deleteBaoxiaoBillById(String id) {
        baoxiaoBillMapper.deleteByPrimaryKey(Long.parseLong(id));
    }

    @Override
    public Page findLeaveBillPageListByUser(long id, int pageNow, int pageSize) {
        BaoxiaoBillExample baoxiaoBillExample = new BaoxiaoBillExample();
        BaoxiaoBillExample.Criteria criteria = baoxiaoBillExample.createCriteria();
        criteria.andUserIdEqualTo(id);
        Page<BaoxiaoBill> page = PageHelper.startPage(pageNow, pageSize);
        baoxiaoBillMapper.selectByExample(baoxiaoBillExample);

        return page;
    }
}
