package com.oreki.yygh.controller;

import com.oreki.yygh.common.result.Result;
import com.oreki.yygh.common.util.AuthContextHolder;
import com.oreki.yygh.model.user.Patient;
import com.oreki.yygh.service.PatientService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author : Fu QiuJie
 * @date : 2022/10/13 15:20
 */

@Api(tags = "就诊人接口")
@RestController
@RequestMapping("/api/user/patient")
public class PatientApiController {

    @Resource
    private PatientService patientService;

    @ApiOperation("获取就诊人列表")
    @GetMapping("/auth")
    public Result listPatients(HttpServletRequest request) {
        //从request中拿到当前用户id
        Long userId = AuthContextHolder.getUserId(request);
        List<Patient> list = patientService.listPatients(userId);
        return Result.ok(list);
    }

    @ApiOperation("添加就诊人")
    @PostMapping("/auth")
    public Result savePatient(@RequestBody Patient patient, HttpServletRequest request) {
        Long userId = AuthContextHolder.getUserId(request);
        patient.setUserId(userId);
        boolean flag = patientService.save(patient);
        return Result.judge(flag);
    }

    @ApiOperation("根据id查询就诊人信息")
    @GetMapping("/auth/{id}")
    public Result getPatientById(@PathVariable Long id) {
        Patient patient = patientService.getPatientById(id);
        return Result.ok(patient);
    }

    @ApiOperation("根据id修改就诊人信息")
    @PutMapping("/auth")
    public Result updatePatient(@RequestBody Patient patient) {
        boolean flag = patientService.updateById(patient);
        return Result.judge(flag);
    }

    @ApiOperation("删除就诊人")
    @DeleteMapping("/auth/{id}")
    public Result removePatient(@PathVariable Long id) {
        boolean flag = patientService.removeById(id);
        return Result.judge(flag);
    }
}
