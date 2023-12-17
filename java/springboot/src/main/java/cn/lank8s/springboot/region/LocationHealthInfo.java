package cn.lank8s.springboot.region;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class LocationHealthInfo {
    private String url;
    private boolean health;
}
