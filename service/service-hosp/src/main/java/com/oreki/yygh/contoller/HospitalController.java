package com.oreki.yygh.contoller;

import com.oreki.yygh.common.result.Result;
import com.oreki.yygh.model.hosp.Hospital;
import com.oreki.yygh.service.HospitalService;
import com.oreki.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@Api(tags = "医院管理接口")
@RequestMapping("/admin/hosp/hospital")
public class HospitalController {
    @Resource
    private HospitalService hospitalService;

    @ApiOperation(value = "获取分页列表")
    @GetMapping("{page}/{limit}")
    public Result listHospitals(@PathVariable int page, @PathVariable int limit, HospitalQueryVo hospitalQueryVo) {
        Page<Hospital> hospitals = hospitalService.listHospitals(page, limit, hospitalQueryVo);
        return Result.ok(hospitals);
    }

    @ApiOperation("根据id修改医院上线状态")
    @PutMapping("updateStatus/{id}/{status}")
    public Result updateStatus(@PathVariable String id, @PathVariable int status) {
        boolean flag = hospitalService.updateStatus(id, status);
        return Result.judge(flag);
    }

}
