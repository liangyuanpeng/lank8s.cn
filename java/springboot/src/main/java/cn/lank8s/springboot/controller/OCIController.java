package cn.lank8s.springboot.controller;

import cn.lank8s.springboot.service.OCIService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2")
@Slf4j
public class OCIController {
    
    @GetMapping("/")
    public ResponseEntity<String> v2(){
        return ResponseEntity.status(302).header("location","https://registry.aliyuncs.com/v2/").build();
    }
    
    @Autowired
    OCIService ociService;

    @RequestMapping("/{*repo}")
    public ResponseEntity<String> req(HttpServletRequest request) {

        log.info("manifests.path:{}|{}|{}",request.getHeader("host"),request.getRequestURI());

        String url = ociService.getLocation(request.getHeader("host"),request.getRequestURI());
        return ResponseEntity.status(302).header("location",url).build();
    }

}
