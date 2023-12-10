package cn.lank8s.springboot.controller;

import cn.lank8s.springboot.dto.BaseResponse;
import cn.lank8s.springboot.dto.OauthToken;
import cn.lank8s.springboot.util.JwtUtils;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

@RestController
@RequestMapping("token")
@Slf4j
public class TokenController {

    public ResponseEntity<BaseResponse> genProxyToken(@RequestParam(required = true,value = "reqhost")String reqhost
            , @RequestParam(required = false,value = "account")String account
            , @RequestParam(required = false,value = "scope")String scope
            , @RequestParam(required = false,value = "service")String service){
        Map map = Maps.newHashMap();
        map.put("jti","uid");
//        Date date = JwtUtils.getInstance().expireTimeFromNow(30);
        Date date = new Date();
        String token = JwtUtils.getInstance().genToken(map);

        String issuedAt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date());
        OauthToken oauthToken = new OauthToken();
        oauthToken.setIssued_at(issuedAt);
        oauthToken.setToken(token);
        oauthToken.setExpires_in( date.getTime()/1000);

        return ResponseEntity.ok().body(oauthToken);
    }

    @GetMapping("")
//    @Produces(MediaType.TEXT_PLAIN)
    public ResponseEntity<BaseResponse> token(
            @RequestHeader(required = false,value = "Authorization")String authorization,
            @RequestParam(required = false,value = "reqhost")String reqhost //如果这个有内容说明是需要请求上游的token
            , @RequestParam(required = false,value = "account")String account
            , @RequestParam(required = false,value = "scope")String scope
            , @RequestParam(required = false,value = "service")String service
    ){
//        if(!StringUtils.hasText(authorization) || !authorization.contains("Basic ")){
        if(StringUtils.hasText(authorization)){
            authorization = authorization.replaceAll("Baseic ","");
            //parse authorization
        }
        String time = LocalDateTime.now().toString();
//        String ip = IPUtils.getIpAddr(request);
        String ip = "none";

//        if(StringUtils.isEmpty(account)){
//            return ResponseEntity.badRequest().body(new ErrorResult());
//        }

        String uid = account + "#" + ip + "#" + scope + "#" + service + "#" + time;
        // create token
        //"jti": uid,
        //		"exp": exp,
        Map map = Maps.newHashMap();
        map.put("jti",uid);
        Date date = JwtUtils.getInstance().expireTimeFromNow(30);
//        Date date = new Date();
        String token = JwtUtils.getInstance().genToken(map);

        //return token struct
        //2022-03-09T03:55:27Z
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Z")));
        String issuedAt = sdf.format(new Date());

        //2023-01-12T11:00:05+0800"
        //2023-01-12T03:32:15Z   need

        OauthToken oauthToken = new OauthToken();
        oauthToken.setIssued_at(issuedAt);
        oauthToken.setToken(token);
        oauthToken.setExpires_in( date.getTime()/1000);

        log.info("response:{}",oauthToken);

        return ResponseEntity.ok().body(oauthToken);
    }

    @GetMapping("/hello")
    public ResponseEntity<String> hello(){
        log.info("hello");
        return ResponseEntity.ok("world");
    }

    @GetMapping("/forward")
    public ResponseEntity<String> forward(@RequestHeader(value = "Authorization",required = false)String auth){
        if(!StringUtils.hasText(auth)){
            log.info("need Authorization");
            return ResponseEntity.status(401).header("www-authenticate","Bearer realm=\"https://localhost:8080/token\",service=\"ghcr.io\",scope=\"repository:liangyuanpeng/replacer:pull\"").build();
        }
        log.info("forward");
        return ResponseEntity.status(302).header("location","http://localhost:8080/api/hello").build();
    }

}