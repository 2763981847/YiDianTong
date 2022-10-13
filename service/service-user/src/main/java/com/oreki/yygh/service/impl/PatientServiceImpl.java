package com.oreki.yygh.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oreki.yygh.Patient;
import com.oreki.yygh.service.PatientService;
import com.oreki.yygh.mapper.PatientMapper;
import org.springframework.stereotype.Service;

/**
* @author 27639
* @description 针对表【patient(就诊人表)】的数据库操作Service实现
* @createDate 2022-10-13 15:33:58
*/
@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient>
    implements PatientService{

}




