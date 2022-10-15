package com.oreki.yygh.contoller.api;

import com.oreki.yygh.common.result.Result;
import com.oreki.yygh.model.hosp.Hospital;
import com.oreki.yygh.service.DepartmentService;
import com.oreki.yygh.service.HospitalService;
import com.oreki.yygh.service.HospitalSetService;
import com.oreki.yygh.service.ScheduleService;
import com.oreki.yygh.vo.hosp.DepartmentVo;
import com.oreki.yygh.vo.hosp.HospitalQueryVo;
import com.oreki.yygh.vo.hosp.ScheduleOrderVo;
import com.oreki.yygh.vo.order.SignInfoVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.xml.crypto.Data;
import java.util.Date;
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

    @Resource
    private HospitalSetService hospitalSetService;

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

    @ApiOperation("获取可预约排班数据")
    @GetMapping("auth/bookingSchedule/{page}/{limit}/{hoscode}/{depcode}")
    public Result getBookingSchedulePage(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Integer page,
            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Integer limit,
            @ApiParam(name = "hoscode", value = "医院code", required = true)
            @PathVariable String hoscode,
            @ApiParam(name = "depcode", value = "科室code", required = true)
            @PathVariable String depcode) {
        return Result.ok(scheduleService.getBookingSchedulePage(page, limit, hoscode, depcode));
    }

    @ApiOperation(value = "获取排班数据")
    @GetMapping("auth/schedule/{hoscode}/{depcode}/{workDate}")
    public Result listScheduleDetails(
            @ApiParam(name = "hoscode", value = "医院code", required = true)
            @PathVariable String hoscode,
            @ApiParam(name = "depcode", value = "科室code", required = true)
            @PathVariable String depcode,
            @ApiParam(name = "workDate", value = "排班日期", required = true)
            @PathVariable String workDate) {
        return Result.ok(scheduleService.listScheduleDetails(hoscode, depcode, new DateTime(workDate).toDate()));
    }

    @ApiOperation(value = "根据id获取排班信息")
    @GetMapping("auth/schedule/{id}")
    public Result getScheduleById(@PathVariable String id) {
        return Result.ok(scheduleService.getScheduleById(id));
    }

    @ApiOperation(value = "根据排班id获取预约下单数据")
    @GetMapping("inner/getScheduleOrderVo/{scheduleId}")
    public ScheduleOrderVo getScheduleOrderVo(
            @ApiParam(name = "scheduleId", value = "排班id", required = true)
            @PathVariable("scheduleId") String scheduleId) {
        return scheduleService.getScheduleOrderVo(scheduleId);
    }

    @ApiOperation(value = "获取医院签名信息")
    @GetMapping("inner/getSignInfoVo/{hoscode}")
    public SignInfoVo getSignInfoVo(
            @ApiParam(name = "hoscode", value = "医院code", required = true)
            @PathVariable("hoscode") String hoscode) {
        return hospitalSetService.getSignInfoVo(hoscode);
    }


}
