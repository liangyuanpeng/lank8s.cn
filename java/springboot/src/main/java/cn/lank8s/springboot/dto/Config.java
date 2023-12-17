package cn.lank8s.springboot.dto;

import lombok.Data;

import java.util.List;

@Data
public class Config {
    private List<RegionInfo> regionInfoList;
}
