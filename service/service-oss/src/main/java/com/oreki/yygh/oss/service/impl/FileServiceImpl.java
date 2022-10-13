package com.oreki.yygh.oss.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.oreki.yygh.common.exception.YyghException;
import com.oreki.yygh.common.result.ResultCodeEnum;
import com.oreki.yygh.oss.service.FileService;
import com.oreki.yygh.oss.util.ConstantOssPropertiesUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * @author : Fu QiuJie
 * @date : 2022/10/12 22:04
 */
@Service
public class FileServiceImpl implements FileService {

    /**
     * 上传文件
     *
     * @param file 待上传文件
     * @return 上传文件到oss后的url路径
     */
    @Override
    public String upload(MultipartFile file) {
        //获取到配置项值
        String endpoint = ConstantOssPropertiesUtils.ENDPOINT;
        String accessKeyId = ConstantOssPropertiesUtils.ACCESS_KEY_ID;
        String accessKeySecret = ConstantOssPropertiesUtils.SECRET;
        String bucketName = ConstantOssPropertiesUtils.BUCKET;
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        // 上传文件名。
        String fileName = file.getOriginalFilename();
        //生成随机唯一值，使用uuid，添加到文件名称里面
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        fileName = uuid + fileName;
        //按照当前日期，创建文件夹，上传到创建文件夹里面
        //  2021/02/02/01.jpg
        String timeUrl = new DateTime().toString("yyyy/MM/dd");
        fileName = timeUrl + "/" + fileName;
        //使用try-with-resource获取到上传文件流
        try (InputStream inputStream = file.getInputStream()) {
            //调用方法实现上传
            ossClient.putObject(bucketName, fileName, inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 关闭OSSClient。
        ossClient.shutdown();
        //上传之后文件路径
        // https://yygh-atguigu.oss-cn-beijing.aliyuncs.com/01.jpg
        String url = "https://" + bucketName + "." + endpoint + "/" + fileName;
        //返回
        return url;

    }
}
