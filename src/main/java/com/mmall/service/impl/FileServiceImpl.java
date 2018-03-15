package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @author GEMI
 * @date 2018/3/13/0013 14:07
 */
@Service("iFileService")
public class FileServiceImpl implements IFileService {

    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    public String upload(MultipartFile file, String path) {
        //得到上传时的文件名
        String fileName = file.getOriginalFilename();
        //获得文件名的后缀
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
        //修改文件名，防止文件名重复
        String uploadFimeName = UUID.randomUUID().toString() + "." + fileExtension;
        //打印日志信息
        logger.info("上传文件名{}，上传文件路径{}，新文件名", fileName, path, uploadFimeName);

        File fileDir = new File(path);
        if (!fileDir.exists()) {
            //设置写的访问权限为true，允许进行创建这个文件夹
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        File targetFile=new File(path,uploadFimeName);

        try {
            file.transferTo(targetFile);//将文件进行上传
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));//文件已经上传到ftp服务器
            targetFile.delete();//上传成功后删除文件
        } catch (IOException e) {
            logger.error("上传文件异常", e);
            return null;
        }
        return targetFile.getName();
    }
}
