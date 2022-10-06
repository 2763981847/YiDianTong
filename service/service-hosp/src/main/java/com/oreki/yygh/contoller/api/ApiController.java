package com.oreki.yygh.contoller.api;

import com.oreki.yygh.common.exception.YyghException;
import com.oreki.yygh.common.helper.HttpRequestHelper;
import com.oreki.yygh.common.result.Result;
import com.oreki.yygh.common.result.ResultCodeEnum;
import com.oreki.yygh.common.utils.MD5;
import com.oreki.yygh.model.hosp.Department;
import com.oreki.yygh.model.hosp.Hospital;
import com.oreki.yygh.model.hosp.Schedule;
import com.oreki.yygh.service.DepartmentService;
import com.oreki.yygh.service.HospitalService;
import com.oreki.yygh.service.HospitalSetService;
import com.oreki.yygh.service.ScheduleService;
import com.oreki.yygh.vo.hosp.DepartmentQueryVo;
import com.oreki.yygh.vo.hosp.ScheduleQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Api("医院接口")
@RestController
@RequestMapping("/api/hosp")
public class ApiController {
    @Resource
    private HospitalService hospitalService;

    @Resource
    private HospitalSetService hospitalSetService;

    @Resource
    private DepartmentService departmentService;

    @Resource
    private ScheduleService scheduleService;

    @PostMapping("saveHospital")
    @ApiOperation("保存医院")
    public Result saveHosp(HttpServletRequest request) {
        //获取传递过来的医院信息
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //校验签名密钥是否正确
        boolean res = checkSignKey(paramMap);
        //如果不一致则抛出异常
        if (!res) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
        //一致则保存
        String logoData = ((String) paramMap.get("logoData")).replace(" ", "+"); //传输过程中将‘+’转化为了‘ ’要转化回来
        paramMap.put("logoData", logoData);
        hospitalService.save(paramMap);
        return Result.ok();
    }

    @PostMapping("hospital/show")
    @ApiOperation("查询医院信息")
    public Result getHospital(HttpServletRequest request) {
        //获取传递过来的医院信息
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //校验签名密钥是否正确
        boolean res = checkSignKey(paramMap);
        //如果不一致则抛出异常
        if (!res) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
        //一致则查询
        String hoscode = (String) paramMap.get("hoscode");
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        return Result.ok(hospital);
    }


    @ApiOperation(value = "上传科室接口")
    @PostMapping("saveDepartment")
    public Result saveDepartment(HttpServletRequest request) {
        //获取传递过来的医院信息
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //校验签名密钥是否正确
        boolean res = checkSignKey(paramMap);
        //如果不一致则抛出异常
        if (!res) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
        //一致则上传科室
        departmentService.saveDepartment(paramMap);
        return Result.ok();
    }

    @PostMapping("department/list")
    @ApiOperation("查询科室信息")
    public Result getDepartment(HttpServletRequest request) {
        //获取传递过来的医院信息
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //校验签名密钥是否正确
        boolean res = checkSignKey(paramMap);
        //如果不一致则抛出异常
        if (!res) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
        //一致则查询
        String hoscode = (String) paramMap.get("hoscode");
        int page = Integer.parseInt((String) paramMap.get("page"));
        int limit = Integer.parseInt((String) paramMap.get("limit"));
        DepartmentQueryVo departmentQueryVo = new DepartmentQueryVo();
        departmentQueryVo.setHoscode(hoscode);
        Page<Department> pageModel = departmentService.getDepartment(page, limit, departmentQueryVo);
        return Result.ok(pageModel);
    }

    @ApiOperation(value = "删除科室")
    @PostMapping("department/remove")
    public Result removeDepartment(HttpServletRequest request) {
        //获取传递过来的医院信息
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //校验签名密钥是否正确
        boolean res = checkSignKey(paramMap);
        //如果不一致则抛出异常
        if (!res) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
        //一致则删除
        String hoscode = (String) paramMap.get("hoscode");
        String depcode = (String) paramMap.get("depcode");
        departmentService.removeDepartment(hoscode, depcode);
        return Result.ok();
    }

    @ApiOperation(value = "上传排班")
    @PostMapping("saveSchedule")
    public Result saveSchedule(HttpServletRequest request) {
        //获取传递过来的医院信息
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //校验签名密钥是否正确
        boolean res = checkSignKey(paramMap);
        //如果不一致则抛出异常
        if (!res) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
        //一致则保存
        scheduleService.saveSchedule(paramMap);
        return Result.ok();
    }

    @ApiOperation(value = "获取排班分页列表")
    @PostMapping("schedule/list")
    public Result getSchedule(HttpServletRequest request) {
        //获取传递过来的医院信息
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //校验签名密钥是否正确
        boolean res = checkSignKey(paramMap);
        //如果不一致则抛出异常
        if (!res) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
        //一致则查询
        String hoscode = (String) paramMap.get("hoscode");
        int page = Integer.parseInt((String) paramMap.get("page"));
        int limit = Integer.parseInt((String) paramMap.get("limit"));
        ScheduleQueryVo scheduleQueryVo = new ScheduleQueryVo();
        scheduleQueryVo.setHoscode(hoscode);
        Page<Schedule> pageModel = scheduleService.getSchedule(page, limit, scheduleQueryVo);
        return Result.ok(pageModel);
    }

    @ApiOperation(value = "删除科室")
    @PostMapping("schedule/remove")
   public Result removeSchedule(HttpServletRequest request){
        //获取传递过来的医院信息
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //校验签名密钥是否正确
        boolean res = checkSignKey(paramMap);
        //如果不一致则抛出异常
        if (!res) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
        //一致则删除
        String hoscode = (String) paramMap.get("hoscode");
        String hosScheduleId = (String) paramMap.get("hosScheduleId");
        scheduleService.removeSchedule(hoscode, hosScheduleId);
        return Result.ok();
    }


    private boolean checkSignKey(Map<String, Object> paramMap) {
        String Md5sign = (String) paramMap.get("sign");
        String hoscode = (String) paramMap.get("hoscode");
        String signKey = hospitalSetService.getSignByHoscode(hoscode);
        String Md5SignKey = MD5.encrypt(signKey);
        return Md5sign.equals(Md5SignKey);
    }
}
