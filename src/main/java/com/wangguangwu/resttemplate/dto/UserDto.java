package com.wangguangwu.resttemplate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author wangguangwu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private String name;

    private MultipartFile headImage;

    private List<MultipartFile> idImageList;

}
