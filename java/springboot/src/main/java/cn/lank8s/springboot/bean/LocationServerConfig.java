package cn.lank8s.springboot.bean;


import com.google.common.collect.Maps;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "kafeidou.forward.servers")
@Configuration
public class LocationServerConfig {
    Map<String, List<String>> servers = Maps.newLinkedHashMap();
}
