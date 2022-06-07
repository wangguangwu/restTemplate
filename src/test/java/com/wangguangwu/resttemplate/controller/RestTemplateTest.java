package com.wangguangwu.resttemplate.controller;

import com.wangguangwu.resttemplate.dto.RestDto;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wangguangwu
 */
public class RestTemplateTest {

    RestTemplate restTemplate;

    @Before
    public void init() {
        restTemplate = new RestTemplate();
    }

    @Test
    public void testGet1() {
        String url = "http://localhost:8080/rest/get";
        // getForObject 方法
        // 获取响应体，并将其转换为第二个参数指定的类型
        RestDto response = restTemplate.getForObject(url, RestDto.class);
        Assertions.assertNotNull(response, "响应数据为空");
        System.out.println(response);
    }

    @Test
    public void testGet2() {
        String url = "http://localhost:8080/rest/get";
        // getForEntity 方法
        // 会返回响应结果中的所有信息，如响应状态码、响应头、响应体
        ResponseEntity<RestDto> responseEntity = restTemplate.getForEntity(url, RestDto.class);
        Assertions.assertNotNull(responseEntity, "响应数据为空");
        System.out.println("响应状态码：" + responseEntity.getStatusCode());
        System.out.println("响应头: " + responseEntity.getHeaders());
        System.out.println("响应体:" + responseEntity.getBody());
    }

    @Test
    public void testGet3() {
        String url = "http://localhost:8080/rest/get/{id}/{name}";
        // 填充动态参数
        Map<String, String> urlVariables = new HashMap<>();
        urlVariables.put("id", "1");
        urlVariables.put("name", "HelloWorld");
        // 使用 getForObject 方法
        RestDto response = restTemplate.getForObject(url, RestDto.class, urlVariables);
        Assertions.assertNotNull(response, "响应数据为空");
        System.out.println(response);
        System.out.println("=======================================================");
        //===========================================================================
        ResponseEntity<RestDto> responseEntity = restTemplate.getForEntity(url, RestDto.class, urlVariables);
        RestDto responseBody = responseEntity.getBody();
        Assertions.assertNotNull(responseBody, "响应数据为空");
        System.out.println(responseBody);
    }

    @Test
    public void testGetList() {
        String url = "http://localhost:8080/rest/getList";
        // 返回值为泛型的时候，需要使用到 exchange 方法
        // exchange 方法中有个参数是 ParameterizedTypeReference 类型，通过这个参数类指定泛型类型
        ResponseEntity<List<RestDto>> responseEntity = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<RestDto>>() {
        });
        List<RestDto> restDtoList = responseEntity.getBody();
        Assertions.assertNotNull(restDtoList, "响应数据为空");
        System.out.println(restDtoList);
    }

    @Test
    public void testDownload1() {
        String url = "http://localhost:8080/rest/download?filename={filename}";
        // 文件比较小的情况，在内存中操作，直接返回字节数组
        ResponseEntity<byte[]> responseEntity = restTemplate.getForEntity(url, byte[].class, "test.txt");
        // 获取文件内容
        byte[] responseBody = responseEntity.getBody();
        Assertions.assertNotNull(responseBody, "响应数据为空");
        String content = new String(responseBody, StandardCharsets.UTF_8);
        System.out.println("文件数据:" + content);
    }

    @Test
    public void testDownload2() {
        String url = "http://localhost:8080/rest/download?filename={filename}";
        /*
         * 文件比较大时，不能返回字节数组，会把内存撑爆，从而导致 OOM
         *
         * 此时需要使用 execute 方法
         * execute方法中有个 ResponseExtractor 类型的参数，
         * restTemplate拿到结果之后，会回调{@link ResponseExtractor#extractData}这个方法，
         * 在这个方法中可以拿到响应流，然后进行处理，这个过程就是变读边处理，不会导致内存溢出
         */
        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("filename", "test.txt");
        String result = restTemplate.execute(url,
                HttpMethod.GET,
                null,
                response -> {
                    System.out.println("状态：" + response.getStatusCode());
                    System.out.println("头：" + response.getHeaders());
                    //获取响应体流
                    InputStream body = response.getBody();
                    //处理响应体流
                    return IOUtils.toString(body, "utf-8");
                }, uriVariables);
        Assertions.assertNotNull(result, "响应数据为空");
        System.out.println(result);
    }

    @Test
    public void testHeader() {
        String url = "http://localhost:8080/rest/header";
        // 1. 请求头放在 HttpHeaders 对象中
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("header1", "Hello");
        headers.add("header2", "World");
        headers.add("header3", "Hello World");
        // 2. RequestEntity：请求实体，请求的所有信息都可以放在 RequestEntity 中，如请求行、请求头、请求体
        RequestEntity<String> requestEntity = new RequestEntity<>(null, headers, HttpMethod.GET, URI.create(url));
        // 访问接口
        ResponseEntity<Map<String, List<String>>> responseEntity = restTemplate.exchange(requestEntity,
                new ParameterizedTypeReference<Map<String, List<String>>>() {
                });
        Map<String, List<String>> result = responseEntity.getBody();
        Assertions.assertNotNull(result, "响应数据为空");
        System.out.println(result);
    }

    @Test
    public void testGetAll() {
        String url = "http://localhost:8080/rest/getAll/{path1}/{path2}";
        // 1. 请求头
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("header1", "Hello");
        headers.add("header2", "World");
        headers.add("header3", "Hello World");
        // 2. url 的两个参数
        Map<String, String> urlVariables = new HashMap<>();
        urlVariables.put("path1", "hello");
        urlVariables.put("path2", "world");
        // 3. HttpEntity：HTTP 实体，内部包含了请求头和请求体
        HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
        // 4. 使用 exchange 发送请求
        ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(
                // url
                url,
                // 请求方式
                HttpMethod.GET,
                // 请求实体（请求头，请求体）
                requestEntity,
                // 返回的结果类型
                new ParameterizedTypeReference<Map<String, Object>>() {
                },
                // url 中的参数
                urlVariables
        );
        Map<String, Object> result = responseEntity.getBody();
        Assertions.assertNotNull(result, "响应数据为空");
        System.out.println(result);
    }

    @Test
    public void testPost1() {
        String url = "http://localhost:8080/rest/post1";
        // 1. 表单信息，需要放在 MultiValueMap 中
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        // 调用 add 方法填充表单数据
        body.add("id", 1);
        body.add("name", "Hello World");
        // 2. 发送请求（url、请求体、返回值需要转换的类型）
        RestDto response = restTemplate.postForObject(url, body, RestDto.class);
        Assertions.assertNotNull(response, "响应数据为空");
        System.out.println(response);
    }

    @Test
    public void testPost2() {
        String url = "http://localhost:8080/rest/post1";
        // 1. 填充表单信息
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("id", 1);
        body.add("name", "Hello World");
        // 2. 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        // 3. 构建请求实体
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(body, headers);
        // 4. 发送请求（url、请求实体、返回值需要转换的类型）
        RestDto response = restTemplate.postForObject(url, httpEntity, RestDto.class);
        Assertions.assertNotNull(response, "响应数据为空");
        System.out.println(response);
    }

    @Test
    public void testUpload1() {
        String url = "http://localhost:8080/rest/upload";
        // 1. 表单信息，填充在 MultiValueMap
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        // 2. 文件对应的类型，需要是org.springframework.core.io.Resource类型的，常见的有[InputStreamResource,ByteArrayResource]
        body.add("file", new FileSystemResource("/Users/wangguangwu/Desktop/excel/test.txt"));
        // 3. 请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        // 4. 构建请求实体
        RequestEntity<MultiValueMap<String, Object>> requestEntity = new RequestEntity<>(body, headers, HttpMethod.POST, URI.create(url));
        // 5. 发送请求（请求实体，返回值需要转换的类型）
        ResponseEntity<Map<String, String>> responseEntity = restTemplate.exchange(
                requestEntity,
                new ParameterizedTypeReference<Map<String, String>>() {
                }
        );
        Map<String, String> result = responseEntity.getBody();
        Assertions.assertNotNull(result, "响应数据为空");
        System.out.println(result);
    }

    @Test
    public void testUpload2() {
        String url = "http://localhost:8080/rest/upload";
        // 1. 表单信息，填充在 MultiValueMap
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        /*
         * 2. 通过流的方式上传文件
         * 只能用来读取相对路径的文件
         * 流的方式需要用到 InputStreamAsResource，需要重写 2 个方法
         * getFileName: 文件名称
         * contentLength: 长度
         */
        InputStream inputStream = this.getClass().getResourceAsStream("/static/hello.txt");
        Assert.notNull(inputStream, "读取出的数据为空");
        InputStreamResource inputStreamResource = new InputStreamResource(inputStream) {
            @Override
            public String getFilename() {
                return "hello.txt";
            }

            @Override
            public long contentLength() throws IOException {
                return inputStream.available();
            }
        };
        body.add("file", inputStreamResource);
        // 3. 请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        // 4. 请求实体
        RequestEntity<MultiValueMap<String, Object>> requestEntity = new RequestEntity<>(body, headers, HttpMethod.POST, URI.create(url));
        // 5. 发送请求
        ResponseEntity<Map<String, String>> responseEntity = restTemplate.exchange(
                requestEntity,
                new ParameterizedTypeReference<Map<String, String>>() {
                }
        );
        Map<String, String> result = responseEntity.getBody();
        Assertions.assertNotNull(result, "响应数据为空");
        System.out.println(result);
    }

    @Test
    public void testComplexForm() {
        String url = "http://localhost:8080/rest/complexForm";
        // 1. 表单信息，填充在 MultiValueMap 中
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("name", "hello");
        body.add("headImage", new FileSystemResource("/Users/wangguangwu/workSpace/wangguangwu/restTemplate/src/main/resources/static/1.jpg"));
        body.add("idImageList", new FileSystemResource("./src/main/resources/static/2.jpg"));
        body.add("idImageList", new FileSystemResource("./src/main/resources/static/3.jpg"));
        // 2. 请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        // 3. 请求实体
        RequestEntity<MultiValueMap<String, Object>> requestEntity = new RequestEntity<>(body, headers, HttpMethod.POST, URI.create(url));
        // 4. 发送请求（请求实体、返回值需要转换的类型
        ResponseEntity<Map<String, String>> responseEntity = restTemplate.exchange(
                requestEntity,
                new ParameterizedTypeReference<Map<String, String>>() {
                }
        );
        Map<String, String> result = responseEntity.getBody();
        Assertions.assertNotNull(result, "响应数据为空");
        System.out.println(result);
    }

    @Test
    public void testPostRequestBody() {
        String url = "http://localhost:8080/rest/postRequestBody";
        RestDto response = restTemplate.postForObject(url, new RestDto(1, "Hello World"), RestDto.class);
        Assertions.assertNotNull(response, "响应数据为空");
        System.out.println(response);
    }

    @Test
    public void testPostForList() {
        String url = "http://localhost:8080/rest/postForList";
        // 1. 设置请求体为一个 json 格式的字符串
        String body = "[{\"id\":1,\"name\":\"Hello\"},{\"id\":2,\"name\":\"World\"}]";
        // 2. 请求体为 json 字符串时，需要指定 Content-Type=application/json
        // 如果 body 是普通的 java 类，不需要指定，restTemplate 默认指定 Content-Type=application/json
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // 3. 构建请求实体（请求行、请求头、请求体）
        RequestEntity<String> requestEntity = new RequestEntity<>(body, headers, HttpMethod.POST, URI.create(url));
        // 4. 发送请求
        ResponseEntity<List<RestDto>> responseEntity = restTemplate.exchange(
                requestEntity,
                new ParameterizedTypeReference<List<RestDto>>() {
                }
        );
        List<RestDto> result = responseEntity.getBody();
        Assertions.assertNotNull(result, "响应数据为空");
        System.out.println(result);
    }


}
