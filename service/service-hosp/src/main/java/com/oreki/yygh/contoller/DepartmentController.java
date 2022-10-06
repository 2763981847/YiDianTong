package com.oreki.yygh.contoller;

import com.oreki.yygh.common.result.Result;
import com.oreki.yygh.service.DepartmentService;
import com.oreki.yygh.vo.hosp.DepartmentVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author : Fu QiuJie
 * @date : 2022/10/6 17:42
 */
@Api(tags = "科室接口")
@RestController
@RequestMapping("/admin/hosp/department")
public class DepartmentController {
    @Resource
    private DepartmentService departmentService;

    @ApiOperation("根据医院编号查询所有科室列表")
    @GetMapping("/listDepartmentsTree/{hoscode}")
    public Result listDepartmentsTree(@PathVariable String hoscode) {
        List<DepartmentVo> list = departmentService.listDepartmentsTree(hoscode);
        return Result.ok(list);
    }
}
