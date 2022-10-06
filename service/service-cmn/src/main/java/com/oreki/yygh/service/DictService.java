package com.oreki.yygh.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.oreki.yygh.model.cmn.Dict;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author 27639
 * @description 针对表【dict(组织架构表)】的数据库操作Service
 * @createDate 2022-09-29 21:17:29
 */
public interface DictService extends IService<Dict> {

    List<Dict> getChildren(long id);

    void exportDict(HttpServletResponse response);

    void importDict(MultipartFile multipartFile);

    String getDictName(String parentDictCode, String value);

    List<Dict> listChildrenByDictCode(String dictCode);
}
