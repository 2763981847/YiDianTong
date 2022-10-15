package com.oreki.yygh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oreki.yygh.common.exception.YyghException;
import com.oreki.yygh.common.result.ResultCodeEnum;
import com.oreki.yygh.common.util.MD5;
import com.oreki.yygh.model.hosp.HospitalSet;
import com.oreki.yygh.service.HospitalSetService;
import com.oreki.yygh.mapper.HospitalSetMapper;
import com.oreki.yygh.vo.hosp.HospitalSetQueryVo;
import com.oreki.yygh.vo.order.SignInfoVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Random;

/**
 * @author 27639
 * @description 针对表【hospital_set(医院设置表)】的数据库操作Service实现
 * @createDate 2022-09-25 20:46:34
 */
@Service
public class HospitalSetServiceImpl extends ServiceImpl<HospitalSetMapper, HospitalSet> implements HospitalSetService {

    /**
     * 分页查询医院设置
     *
     * @param current            当前页码
     * @param limit              每页条数
     * @param hospitalSetQueryVo 查询条件
     * @return 查询结果
     */
    @Override
    public Page findPageHospSet(long current, long limit, HospitalSetQueryVo hospitalSetQueryVo) {
        //构建查询wrapper
        LambdaQueryWrapper<HospitalSet> queryWrapper = new LambdaQueryWrapper<>();
        if (hospitalSetQueryVo != null) {
            String hoscode = hospitalSetQueryVo.getHoscode();
            String hosname = hospitalSetQueryVo.getHosname();
            queryWrapper.eq(!StringUtils.isEmpty(hoscode), HospitalSet::getHoscode, hoscode)
                    .like(!StringUtils.isEmpty(hosname), HospitalSet::getHosname, hosname);
        }
        //构建分页对象
        Page<HospitalSet> setPage = new Page<>(current, limit);
        this.page(setPage, queryWrapper);
        return setPage;
    }

    /**
     * 新增医院设置
     *
     * @param hospitalSet 新增的医院设置信息
     * @return 是否新增成功
     */
    @Override
    public boolean saveHospSet(HospitalSet hospitalSet) {
        //设置状态，1 可用 ，0 不可用
        hospitalSet.setStatus(1);
        //设置签名密钥
        Random random = new Random();
        hospitalSet.setSignKey(MD5.encrypt(System.currentTimeMillis() + "" + random.nextInt(1000)));
        return this.save(hospitalSet);
    }

    /**
     * 锁定或解锁医院设置
     *
     * @param id     医院id
     * @param status 修改状态值 1：解锁 0：锁定
     * @return 是否修改成功
     */
    @Override
    public boolean changeHospSetStatus(long id, int status) {
        LambdaUpdateWrapper<HospitalSet> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(HospitalSet::getId, id)
                .set(HospitalSet::getStatus, status);
        return this.update(updateWrapper);
    }

    /**
     * 根据医院编码拿到签名密钥
     *
     * @param hoscode 医院编码
     * @return 签名密钥
     */
    @Override
    public String getSignByHoscode(String hoscode) {
        LambdaQueryWrapper<HospitalSet> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HospitalSet::getHoscode, hoscode);
        HospitalSet hospitalSet = this.getOne(queryWrapper);
        if (hospitalSet != null) return hospitalSet.getSignKey();
        else return "";
    }

    /**
     * 获取医院签名信息
     *
     * @param hoscode 医院编码
     * @return 医院签名
     */
    @Override
    public SignInfoVo getSignInfoVo(String hoscode) {
        LambdaQueryWrapper<HospitalSet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HospitalSet::getHoscode, hoscode);
        HospitalSet hospitalSet = this.getOne(wrapper);
        if (null == hospitalSet) {
            throw new YyghException(ResultCodeEnum.HOSPITAL_OPEN);
        }
        SignInfoVo signInfoVo = new SignInfoVo();
        signInfoVo.setApiUrl(hospitalSet.getApiUrl());
        signInfoVo.setSignKey(hospitalSet.getSignKey());
        return signInfoVo;

    }
}
