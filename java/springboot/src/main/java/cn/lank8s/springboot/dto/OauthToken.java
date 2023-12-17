package cn.lank8s.springboot.dto;

import lombok.Data;

@Data
public class OauthToken extends BaseResponse {
    long expires_in;
    String token;
    String issued_at;

    public long getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(long expires_in) {
        this.expires_in = expires_in;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getIssued_at() {
        return issued_at;
    }

    public void setIssued_at(String issued_at) {
        this.issued_at = issued_at;
    }
}
