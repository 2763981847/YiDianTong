package com.oreki.yygh.service;

import com.oreki.yygh.model.hosp.Hospital;
import com.oreki.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface HospitalService {
    void save(Map<String, Object> paramMap);

    Hospital getByHoscode(String hoscode);

    Page<Hospital> listHospitals(int page, int limit, HospitalQueryVo hospitalQueryVo);

    boolean updateStatus(String id, int status);
}
