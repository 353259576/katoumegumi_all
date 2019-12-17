package cn.katoumegumi.java.http.client.model;

public enum  MediaType {
    ALL_VALUE("*/*"),
    APPLICATION_XML_VALUE("application/xml"),
    APPLICATION_FORM_URLENCODED_VALUE("application/x-www-form-urlencoded"),
    APPLICATION_JSON_VALUE("application/json"),
    APPLICATION_OCTET_STREAM_VALUE("application/octet-stream");


    private String coed;

    private MediaType(String code){
        this.coed = code;
    }

    public String getCoed() {
        return coed;
    }

    public void setCoed(String coed) {
        this.coed = coed;
    }
}
