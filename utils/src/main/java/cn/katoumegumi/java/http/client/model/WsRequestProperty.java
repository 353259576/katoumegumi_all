package cn.katoumegumi.java.http.client.model;

/**
 * @author ws
 */
public class WsRequestProperty {
    private String key;
    private String value;

    public static WsRequestProperty createWsRequestProPerty(String key, String value) {
        WsRequestProperty wsRequestProperty = new WsRequestProperty();
        wsRequestProperty.setKey(key);
        wsRequestProperty.setValue(value);
        return wsRequestProperty;
    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
