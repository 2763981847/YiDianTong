package com.oreki.yygh.controller;

import com.oreki.yygh.common.result.Result;
import com.oreki.yygh.model.cmn.Dict;
import com.oreki.yygh.service.DictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Api(value = "数据字典controller")
@RequestMapping("admin/cmn/dict")
public class DictController {
    @Resource
    private DictService dictService;

    @ApiOperation("根据数据id查询其子数据列表")
    @GetMapping("getChildren/{id}")
    public Result getChildren(@PathVariable long id) {
        List<Dict> children = dictService.getChildren(id);
        return Result.ok(children);
    }

}
