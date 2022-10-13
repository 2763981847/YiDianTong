package com.oreki.yygh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oreki.yygh.cmn.client.DictFeignClient;
import com.oreki.yygh.common.util.AuthContextHolder;
import com.oreki.yygh.enums.DictEnum;
import com.oreki.yygh.model.cmn.Dict;
import com.oreki.yygh.model.user.Patient;
import com.oreki.yygh.service.PatientService;
import com.oreki.yygh.mapper.PatientMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 27639
 * @description 针对表【patient(就诊人表)】的数据库操作Service实现
 * @createDate 2022-10-13 15:33:58
 */
@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient>
        implements PatientService {
    @Resource
    private DictFeignClient dictFeignClient;

    /**
     * 拿到当前用户的就诊人列表
     *
     * @param userId 用户id
     * @return 就诊人列表
     */
    @Override
    public List<Patient> listPatients(Long userId) {
        //构建查询
        LambdaQueryWrapper<Patient> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Patient::getUserId, userId);
        //操作数据库进行查询
        List<Patient> list = this.list(queryWrapper);
        //封装具体信息到paramMap中
        list.parallelStream().forEach(this::packagePatient);
        return list;
    }

    /**
     * 根据id查询就诊人信息
     *
     * @param id 就诊人id
     * @return 就诊人信息
     */
    @Override
    public Patient getPatientById(Long id) {
        Patient patient = this.getById(id);
        this.packagePatient(patient);
        return patient;
    }

    /**
     * patient参数的其他参数封装
     *
     * @param patient patient对象
     */
    private void packagePatient(Patient patient) {
        //拿到证件类型的名称
        String certificatesTypeString =
                (String) dictFeignClient.getDictName(DictEnum.CERTIFICATES_TYPE.getDictCode(), patient.getCertificatesType()).getData();
        //联系人证件类型
        String contactsCertificatesTypeString =
                String.valueOf(dictFeignClient.getDictName(DictEnum.CERTIFICATES_TYPE.getDictCode(), patient.getContactsCertificatesType()).getData());
        //省
        String provinceString = String.valueOf(dictFeignClient.getDictName(patient.getProvinceCode()).getData());
        //市
        String cityString = String.valueOf(dictFeignClient.getDictName(patient.getCityCode()).getData());
        //区
        String districtString = String.valueOf(dictFeignClient.getDictName(patient.getDistrictCode()).getData());
        patient.getParam().put("certificatesTypeString", certificatesTypeString);
        patient.getParam().put("contactsCertificatesTypeString", contactsCertificatesTypeString);
        patient.getParam().put("provinceString", provinceString);
        patient.getParam().put("cityString", cityString);
        patient.getParam().put("districtString", districtString);
        patient.getParam().put("fullAddress", provinceString + cityString + districtString + patient.getAddress());
    }
}




