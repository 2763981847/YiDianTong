package com.oreki.yygh.service;


import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oreki.yygh.common.result.Result;
import com.oreki.yygh.model.hosp.HospitalSet;
import com.oreki.yygh.vo.hosp.HospitalQueryVo;
import com.oreki.yygh.vo.hosp.HospitalSetQueryVo;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author 27639
 * @description 针对表【hospital_set(医院设置表)】的数据库操作Service
 * @createDate 2022-09-25 20:46:34
 */
public interface HospitalSetService extends IService<HospitalSet> {
    Page findPageHospSet(long current, long limit, HospitalSetQueryVo hospitalSetQueryVo);

    boolean saveHospSet(HospitalSet hospitalSet);

    boolean changeHospSetStatus(long id, int status);

    String getSignByHoscode(String hoscode);
}
