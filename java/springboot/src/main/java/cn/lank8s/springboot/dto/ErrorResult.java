package cn.lank8s.springboot.dto;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class ErrorResult extends BaseResponse {
    List<ApiError> errors = Collections.emptyList();
}
