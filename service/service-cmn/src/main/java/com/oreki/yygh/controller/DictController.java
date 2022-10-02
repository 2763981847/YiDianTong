package com.oreki.yygh.controller;

import com.oreki.yygh.common.result.Result;
import com.oreki.yygh.model.cmn.Dict;
import com.oreki.yygh.service.DictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@Api(value = "数据字典controller")
@RequestMapping("admin/cmn/dict")
@CrossOrigin
public class DictController {
    @Resource
    private DictService dictService;

    @ApiOperation("根据数据id查询其子数据列表")
    @GetMapping("getChildren/{id}")
    public Result getChildren(@PathVariable long id) {
        List<Dict> children = dictService.getChildren(id);
        return Result.ok(children);
    }

    @ApiOperation("数据字典导出")
    @GetMapping("exportDict")
    public void exportDict(HttpServletResponse response) {
        dictService.exportDict(response);
    }

    @ApiOperation("数据字典导入")
    @PostMapping("importDict")
    public void importDict(MultipartFile file) {
        dictService.importDict(file);
    }
}
