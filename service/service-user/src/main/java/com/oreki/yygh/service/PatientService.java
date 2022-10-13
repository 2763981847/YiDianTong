package com.oreki.yygh.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oreki.yygh.model.user.Patient;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 27639
* @description 针对表【patient(就诊人表)】的数据库操作Service
* @createDate 2022-10-13 15:33:58
*/
public interface PatientService extends IService<Patient> {

    List<Patient> listPatients(Long userId);

    Patient getPatientById(Long id);
}
