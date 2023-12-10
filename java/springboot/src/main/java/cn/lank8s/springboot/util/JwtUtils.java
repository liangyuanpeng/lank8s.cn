/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.lank8s.springboot.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

import java.time.*;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class JwtUtils {

    public static  JwtUtils instance = new JwtUtils();

    public static  JwtUtils getInstance(){
        return instance;
    }


    //默认secret
    private String secret = "56e170f784304367a4112640e820c59alank8scnlank8scn";

    // 默认过期时间30分钟
    private Integer expire = 30;

    public String genToken(Integer expir, Map<String, Object> claims, Map<String, Object> headers) {
        return Jwts.builder()
                .addClaims(claims)
                .setHeader(headers)
                .setExpiration(expireTimeFromNow(expir))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public String genToken(Integer expir, Map<String, Object> claims) {
        if (expir == null) {
            expir = expire;
        }
        return genToken(expir, claims, Collections.emptyMap());
    }

    public String genToken(Map<String, Object> claims) {
        return genToken(expire, claims, Collections.emptyMap());
    }

    public String genToken() {
        return genToken(expire, Collections.emptyMap(), Collections.emptyMap());
    }

    public String genToken(Integer expire) {
        return genToken(expire, Collections.emptyMap(), Collections.emptyMap());
    }

    /**
     * 获取token中注册信息
     *
     * @param token
     * @return
     */
    public Claims getTokenClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secret).parseClaimsJws(token).getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取token中注册信息
     *
     * @param token
     * @return
     */
    public Object getTokenClaim(String token, String claim) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secret).parseClaimsJws(token).getBody();
            return claims.get(claim);
        } catch (Exception e) {
            log.error("JwtUtils getTokenClaim error",e);
            return null;
        }
    }

    public Optional<String> getSubFromToken(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return Optional.ofNullable(claimsJws.getBody().getSubject());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Object getClaimObjFromToken(String claim, String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return claimsJws.getBody().get(claim);
        } catch (Exception e) {
            log.error("parse.token.fail:{}|{}", claim, token);
        }
        return null;
    }

    public String getClaimFromToken(String claim, String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            Object optional = Optional.ofNullable(claimsJws.getBody().get(claim)).orElseGet(() -> Optional.empty());
            if (optional != null) {
                return optional.toString();
            }
        } catch (Exception e) {
            log.error("parse.token.fail:{}|{}", claim, token);
        }
        return null;
    }

    public Date expireTimeFromNow(Integer durationMin) {
        LocalDateTime localDateTime = LocalDateTime.now().plusMinutes(durationMin);
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDateTime.atZone(zone).toInstant();
        Date date = Date.from(instant);
        return date;
    }

}
