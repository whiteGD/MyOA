package priv.gd.mapper;


import org.apache.ibatis.annotations.Param;
import priv.gd.pojo.BaoxiaoBill;
import priv.gd.pojo.BaoxiaoBillExample;

import java.util.List;

public interface BaoxiaoBillMapper {
    int countByExample(BaoxiaoBillExample example);

    int deleteByExample(BaoxiaoBillExample example);

    int deleteByPrimaryKey(Long id);

    int insert(BaoxiaoBill record);

    int insertSelective(BaoxiaoBill record);

    List<BaoxiaoBill> selectByExample(BaoxiaoBillExample example);

    BaoxiaoBill selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") BaoxiaoBill record, @Param("example") BaoxiaoBillExample example);

    int updateByExample(@Param("record") BaoxiaoBill record, @Param("example") BaoxiaoBillExample example);

    int updateByPrimaryKeySelective(BaoxiaoBill record);

    int updateByPrimaryKey(BaoxiaoBill record);
}