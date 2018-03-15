package com.mmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author GEMI
 * @date 2018/3/13/0013 14:07
 */
public interface IFileService {

    String upload(MultipartFile file, String path);
}
