package com.oreki.yygh.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oreki.yygh.listener.DictListener;
import com.oreki.yygh.model.cmn.Dict;
import com.oreki.yygh.service.DictService;
import com.oreki.yygh.mapper.DictMapper;
import com.oreki.yygh.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author 27639
 * @description 针对表【dict(组织架构表)】的数据库操作Service实现
 * @createDate 2022-09-29 21:17:29
 */
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {
    /**
     * 根据id获取子数据列表
     *
     * @param id 数据id
     * @return 子数据列表
     */
    @Override
    @Cacheable(value = "dict", keyGenerator = "keyGenerator")
    public List<Dict> getChildren(long id) {
        LambdaQueryWrapper<Dict> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dict::getParentId, id);
        List<Dict> children = this.list(queryWrapper);
        //判断子数据是否还有子数据
        children.forEach(child -> child.setHasChildren(hasChild(child.getId())));
        return children;
    }

    /**
     * 数据字典导出到本地功能
     *
     * @param response 响应信息
     */
    @Override
    public void exportDict(HttpServletResponse response) {
        //下载前设置
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = "dict";
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        //从数据库中查询到数据字典数据
        List<Dict> dicts = this.list();
        List<DictEeVo> list = new LinkedList<>();
        for (Dict dict : dicts) {
            DictEeVo dictEeVo = new DictEeVo();
            BeanUtils.copyProperties(dict, dictEeVo, DictEeVo.class);
            list.add(dictEeVo);
        }
        try {
            EasyExcel.write(response.getOutputStream(), DictEeVo.class).sheet("dict").doWrite(list);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @CacheEvict(value = "dict", allEntries = true)
    public void importDict(MultipartFile multipartFile) {
        try {
            EasyExcel.read(multipartFile.getInputStream(), DictEeVo.class, new DictListener(this)).sheet().doRead();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 判断该数据是否还有子数据
     *
     * @param id 数据id
     * @return 是否有子数据
     */
    public boolean hasChild(long id) {
        LambdaQueryWrapper<Dict> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dict::getParentId, id);
        int count = this.count(queryWrapper);
        return count > 0;
    }
}
