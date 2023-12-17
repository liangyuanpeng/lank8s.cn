package cn.lank8s.springboot.dto;

import lombok.Data;

@Data
public class ApiError {
    String code;
    String message;
}
