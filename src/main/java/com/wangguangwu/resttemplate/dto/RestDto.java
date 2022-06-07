package com.wangguangwu.resttemplate.dto;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wangguangwu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestDto {

    private Integer id;
    private String name;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

}
