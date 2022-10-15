package com.oreki.yygh.cmn.client;

import com.oreki.yygh.common.result.Result;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("service-cmn")
public interface DictFeignClient {
    @ApiOperation(value = "根据上级编码和值获取数据字典名称")
    @GetMapping(value = "/admin/cmn/dict/getName/{parentDictCode}/{value}")
    Result getDictName(@PathVariable("parentDictCode") String parentDictCode, @PathVariable("value") String value);

    @ApiOperation(value = "根据值获取数据字典名称")
    @GetMapping(value = "/admin/cmn/dict/getName/{value}")
    Result getDictName(@PathVariable("value") String value);
}
