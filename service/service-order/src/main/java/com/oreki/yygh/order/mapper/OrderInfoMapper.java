package com.oreki.yygh.order.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oreki.yygh.model.order.OrderInfo;
import com.oreki.yygh.vo.order.OrderCountQueryVo;
import com.oreki.yygh.vo.order.OrderCountVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 27639
 * @description 针对表【order_info(订单表)】的数据库操作Mapper
 * @createDate 2022-10-15 11:20:25
 * @Entity com.oreki.yygh.order.domain.OrderInfo
 */
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {
    List<OrderCountVo> selectOrderCount(@Param("vo") OrderCountQueryVo orderCountQueryVo);
}




