package com.oreki.yygh.contoller;

import com.oreki.yygh.common.exception.YyghException;
import com.oreki.yygh.common.result.Result;
import com.oreki.yygh.common.result.ResultCodeEnum;
import com.oreki.yygh.model.hosp.Schedule;
import com.oreki.yygh.service.ScheduleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author : Fu QiuJie
 * @date : 2022/10/6 20:42
 */
@Api(tags = "排班接口")
@RestController
@RequestMapping("/admin/hosp/schedule")
public class ScheduleController {
    @Resource
    private ScheduleService scheduleService;

    @ApiOperation("根据医院编号和科室编号查看排班")
    @GetMapping("/{page}/{limit}/{hoscode}/{depcode}")
    public Result listSchedules(@PathVariable int page, @PathVariable int limit, @PathVariable String hoscode, @PathVariable String depcode) {
        Map<String, Object> map = scheduleService.listSchedules(page, limit, hoscode, depcode);
        return Result.ok(map);
    }

    @ApiOperation("根据医院编号和科室编号和日期查看排班详细信息")
    @GetMapping("/{hoscode}/{depcode}/{workDate}")
    public Result listScheduleDetails(@PathVariable String hoscode, @PathVariable String depcode, @PathVariable String workDate) {
        List<Schedule> list = scheduleService.listScheduleDetails(hoscode, depcode, new DateTime(workDate).toDate());
        return Result.ok(list);
    }
}
