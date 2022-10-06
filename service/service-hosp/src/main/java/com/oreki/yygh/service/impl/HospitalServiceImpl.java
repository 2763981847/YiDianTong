package com.oreki.yygh.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.oreki.yygh.cmn.client.DictFeignClient;
import com.oreki.yygh.model.hosp.Hospital;
import com.oreki.yygh.repository.HospitalRepository;
import com.oreki.yygh.service.HospitalService;
import com.oreki.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class HospitalServiceImpl implements HospitalService {
    @Resource
    private HospitalRepository hospitalRepository;
    @Resource
    private DictFeignClient dictFeignClient;

    /**
     * 保存医院信息
     *
     * @param paramMap 参数表
     */
    @Override
    public void save(Map<String, Object> paramMap) {
        //将map中的参数转为实体对象
        String jsonString = JSONObject.toJSONString(paramMap);
        Hospital hospital = JSON.parseObject(jsonString, Hospital.class);
        //判断数据库中是否含有该数据
        String hoscode = hospital.getHoscode();
        Hospital hosp = hospitalRepository.getHospitalByHoscode(hoscode);
        //如果存在该数据，进行修改。不存在该数据则进行新增
        if (hosp != null) {
            hospital.setId(hosp.getId());
            hospital.setStatus(hosp.getStatus());
            hospital.setCreateTime(hosp.getCreateTime());
        } else {
            hospital.setStatus(0);
            hospital.setCreateTime(new Date());
        }
        hospital.setUpdateTime(new Date());
        hospital.setIsDeleted(0);
        hospitalRepository.save(hospital);
    }

    @Override
    public Hospital getByHoscode(String hoscode) {
        Hospital hosp = hospitalRepository.getHospitalByHoscode(hoscode);
        return hosp;
    }

    /**
     * 医院分页查询
     *
     * @param page            当前页
     * @param limit           每页数据量
     * @param hospitalQueryVo 查询条件
     * @return 查询到的分页对象
     */
    @Override
    public Page<Hospital> listHospitals(int page, int limit, HospitalQueryVo hospitalQueryVo) {
        //创建pageable对象
        Pageable pageable = PageRequest.of(page - 1, limit);
        //创建example对象
        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo, hospital);
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase(true);
        Example<Hospital> example = Example.of(hospital, exampleMatcher);
        //查询结果
        Page<Hospital> pages = hospitalRepository.findAll(example, pageable);
        List<Hospital> hospitals = pages.getContent();
        hospitals.forEach(this::setHospital);
        return pages;
    }

    /**
     * 根据id更新医院上线状态
     *
     * @param id     医院ID
     * @param status 要修改为的状态 1-已上线 0-未上线
     * @return
     */
    @Override
    public boolean updateStatus(String id, int status) {
        Hospital hospital = hospitalRepository.getHospitalById(id);
        if (hospital == null) {
            return false;
        }
        hospital.setStatus(status);
        hospital.setUpdateTime(new Date());
        hospitalRepository.save(hospital);
        return true;
    }

    private Hospital setHospital(Hospital hospital) {
        //先根据dictCode和value查询医院的等级名称
        String hostype = (String) dictFeignClient.getDictName("Hostype", hospital.getHostype()).getData();
        //再根据value查询医院的地址名称
        String province = (String) dictFeignClient.getDictName(hospital.getProvinceCode()).getData();
        String city = (String) dictFeignClient.getDictName(hospital.getCityCode()).getData();
        String district = (String) dictFeignClient.getDictName(hospital.getDistrictCode()).getData();
        hospital.getParam().put("hostype", hostype);
        hospital.getParam().put("fullAddress", province + city + district);
        return hospital;
    }
}
