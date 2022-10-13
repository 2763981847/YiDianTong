package com.oreki.yygh.oss.controller;

import com.oreki.yygh.common.result.Result;
import com.oreki.yygh.oss.service.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * @author : Fu QiuJie
 * @date : 2022/10/12 21:26
 */
@Api(tags = "阿里云oss存储服务接口")
@RestController
@RequestMapping("/api/oss/file")
public class FileApiController {
    @Resource
    private FileService fileService;

    @ApiOperation("文件上传接口")
    @PostMapping("/upload")
    public Result fileUpload(MultipartFile file) {
        String url = fileService.upload(file);
        return Result.ok(url);
    }
}
