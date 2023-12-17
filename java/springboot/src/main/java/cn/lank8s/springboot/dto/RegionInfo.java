package cn.lank8s.springboot.dto;

import lombok.Data;

import java.util.List;

@Data
public class RegionInfo {
    private String host;
    private List<String> regions;
    private String repo;
}
