package com.oreki.yygh.oss.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author : Fu QiuJie
 * @date : 2022/10/12 22:04
 */
public interface FileService {
    String upload(MultipartFile file);
}
