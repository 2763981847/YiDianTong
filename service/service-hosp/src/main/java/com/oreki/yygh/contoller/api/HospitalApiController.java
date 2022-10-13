package com.oreki.yygh.contoller.api;

import com.oreki.yygh.common.result.Result;
import com.oreki.yygh.model.hosp.Hospital;
import com.oreki.yygh.service.DepartmentService;
import com.oreki.yygh.service.HospitalService;
import com.oreki.yygh.service.ScheduleService;
import com.oreki.yygh.vo.hosp.DepartmentVo;
import com.oreki.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author : Fu QiuJie
 * @date : 2022/10/7 22:32
 */
@Api(tags = "医院管理接口")
@RestController
@RequestMapping("/api/hosp/hospital")
public class HospitalApiController {
    @Resource
    private HospitalService hospitalService;
    @Resource
    private DepartmentService departmentService;

    @Resource
    private ScheduleService scheduleService;

    @ApiOperation(value = "获取医院分页列表")
    @GetMapping("{page}/{limit}")
    public Result listHospitalPages(@PathVariable int page, @PathVariable int limit, HospitalQueryVo hospitalQueryVo) {
        Page<Hospital> hospitals = hospitalService.listHospitals(page, limit, hospitalQueryVo);
        return Result.ok(hospitals);
    }

    @ApiOperation("根据医院名称查询医院列表")
    @GetMapping("listByHosname/{hosname}")
    public Result listByHosname(@PathVariable String hosname) {
        List<Hospital> hospitals = hospitalService.listByHosname(hosname);
        return Result.ok(hospitals);
    }

    @ApiOperation("根据医院编号查询科室列表")
    @GetMapping("listDepartments/{hoscode}")
    public Result listDepartments(@PathVariable String hoscode) {
        List<DepartmentVo> departmentVos = departmentService.listDepartmentsTree(hoscode);
        return Result.ok(departmentVos);
    }

    @ApiOperation("根据医院编号获取预约挂号的详情信息")
    @GetMapping("getHospitalDetail/{hoscode}")
    public Result getHospitalDetail(@PathVariable String hoscode) {
        Hospital hospital = hospitalService.getHospitalDetail(hoscode);
        return Result.ok(hospital);
    }
}
