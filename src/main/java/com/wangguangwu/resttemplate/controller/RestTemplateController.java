package com.wangguangwu.resttemplate.controller;

import com.wangguangwu.resttemplate.dto.RestDto;
import com.wangguangwu.resttemplate.dto.UserDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.*;

/**
 * @author wangguangwu
 */
@RestController
@RequestMapping("rest")
@SuppressWarnings("all")
public class RestTemplateController {

    @Value("${file.Path}")
    String filePath;


    @GetMapping("/get")
    public RestDto get() {
        return new RestDto(1, "Hello World");
    }

    @GetMapping("/get/{id}/{name}")
    public RestDto get(@PathVariable("id") Integer id, @PathVariable("name") String name) {
        return new RestDto(id, name);
    }

    @GetMapping("/getList")
    public List<RestDto> getList() {
        return Arrays.asList(
                new RestDto(1, "Hello World"),
                new RestDto(2, "Hello China")
        );
    }

    /**
     * 下载绝对路径下的文件
     */
    @GetMapping("/download")
    public HttpEntity<InputStreamResource> download(@RequestParam("filename") String filename) {
        // 获取文件流
        InputStream inputStream = getInputStream(filename);
        Assert.notNull(inputStream, "文件为空");
        // 将文件流封装为 InputStreamResource 对象
        InputStreamResource inputStreamResource = new InputStreamResource(inputStream);
        // 设置 header
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + filename);
        try {
            headers.setContentLength(inputStream.available());
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 构建 http 报文
        return new HttpEntity<>(inputStreamResource, headers);
    }

    /**
     * 解析请求头
     */
    @GetMapping("/header")
    public Map<String, List<String>> header(HttpServletRequest request) {
        Map<String, List<String>> header = new LinkedHashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            Enumeration<String> values = request.getHeaders(name);
            List<String> list = new ArrayList<>();
            while (values.hasMoreElements()) {
                list.add(values.nextElement());
            }
            header.put(name, list);
        }
        return header;
    }

    @GetMapping("/getAll/{path1}/{path2}")
    public Map<String, Object> getAll(@PathVariable("path1") String path1,
                                      @PathVariable("path2") String path2,
                                      HttpServletRequest request) {
        // 参数
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("path1", path1);
        result.put("path2", path2);
        // 请求头
        Map<String, List<String>> header = new LinkedHashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            Enumeration<String> values = request.getHeaders(name);
            List<String> list = new ArrayList<>();
            while (values.hasMoreElements()) {
                list.add(values.nextElement());
            }
            header.put(name, list);
        }
        result.put("header", header);
        return result;
    }


    //===================================私有方法========================================

    /**
     * 判断文件是绝对路径下获取还是相对路径
     *
     * @param filename filename
     * @return inputStream
     */
    private InputStream getInputStream(String filename) {
        filename = filename.startsWith(File.separator) ? filename : File.separator + filename;
        File file1 = new File(filePath + filename);
        File file2 = new File(filename);
        Assert.isTrue(file1.exists() || file2.exists(), "文件不存在");
        // 判断是绝对路径文件还是相对路径文件
        try {
            return file1.exists() ?
                    new BufferedInputStream(new FileInputStream(file1)) :
                    this.getClass().getResourceAsStream(filename);
        } catch (FileNotFoundException e) {
            // Ignore
            e.printStackTrace();
        }
        return null;
    }

    @PostMapping("/post1")
    public RestDto post1(RestDto restDto) {
        return restDto;
    }

    @PostMapping("/upload")
    public Map<String, String> upload(@RequestParam("file") MultipartFile file) {
        Map<String, String> fileMetaData = new LinkedHashMap<>();
        fileMetaData.put("文件名", file.getOriginalFilename());
        fileMetaData.put("文件类型", file.getContentType());
        fileMetaData.put("文件大小(byte)", String.valueOf(file.getSize()));
        return fileMetaData;
    }

    /**
     * 复杂的表单：包含了普通元素、多文件
     */
    @PostMapping("/complexForm")
    public Map<String, String> complexForm(UserDto userDto) {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("name", userDto.getName());
        result.put("headImage", userDto.getHeadImage().getOriginalFilename());
        result.put("idImageList", Arrays.toString(userDto.getIdImageList().stream().
                map(MultipartFile::getOriginalFilename).toArray()));
        return result;
    }

    @PostMapping("/postRequestBody")
    public RestDto postRequestBody(@RequestBody RestDto restDto) {
        return restDto;
    }

    @PostMapping("/postForList")
    public List<RestDto> postForList(@RequestBody List<RestDto> list) {
        return list;
    }

}
