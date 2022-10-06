package com.oreki.yygh.contoller;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oreki.yygh.common.result.Result;
import com.oreki.yygh.model.hosp.HospitalSet;
import com.oreki.yygh.service.HospitalSetService;
import com.oreki.yygh.vo.hosp.HospitalQueryVo;
import com.oreki.yygh.vo.hosp.HospitalSetQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/admin/hosp/hospitalSet")
@Api(tags = "医院设置接口")
public class HospitalSetController {
    @Resource
    HospitalSetService hospitalSetService;

    @ApiOperation("查询所有医院设置")
    @GetMapping
    public Result getAllHospSet() {
        List<HospitalSet> list = hospitalSetService.list();
        return Result.ok(list);
    }

    @ApiOperation("根据id查找医院设置")
    @GetMapping("/{id}")
    public Result getHospSetById(@PathVariable long id) {
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        return hospitalSet == null ? Result.fail() : Result.ok(hospitalSet);
    }

    @ApiOperation("分页查询医院设置")
    @PostMapping("page/{current}/{limit}")
    public Result getHospSetPage(@PathVariable long current, @PathVariable long limit, @RequestBody(required = false) HospitalSetQueryVo hospitalSetQueryVo) {
        Page page = hospitalSetService.findPageHospSet(current, limit, hospitalSetQueryVo);
        return Result.ok(page);
    }

    @ApiOperation("逻辑删除医院设置")
    @DeleteMapping("/{id}")
    public Result deleteHospSetById(@PathVariable long id) {
        boolean flag = hospitalSetService.removeById(id);
        return Result.judge(flag);
    }

    @ApiOperation("批量删除医院设置")
    @DeleteMapping
    public Result batchRemoveHospSet(@RequestBody List<Long> ids) {
        boolean flag = hospitalSetService.removeByIds(ids);
        return Result.judge(flag);
    }

    @ApiOperation("新增医院设置")
    @PostMapping
    public Result saveHospSet(@RequestBody HospitalSet hospitalSet) {
        boolean flag = hospitalSetService.saveHospSet(hospitalSet);
        return Result.judge(flag);
    }


    @ApiOperation("修改医院设置")
    @PutMapping
    public Result updateHospSet(@RequestBody HospitalSet hospitalSet) {
        boolean flag = hospitalSetService.updateById(hospitalSet);
        return Result.judge(flag);
    }

    @ApiOperation("锁定或解锁医院设置")
    @PutMapping("/changeHospSetStatus/{id}/{status}")
    public Result changeHospSetStatus(@PathVariable long id, @PathVariable int status) {
        boolean flag = hospitalSetService.changeHospSetStatus(id, status);
        return Result.judge(flag);
    }


    @ApiOperation("发送签名密钥")
    @GetMapping("/sendKey/{id}")
    public Result sendKey(@PathVariable long id) {
        //todo 要调用发送短信功能
        return Result.ok();
    }
}
