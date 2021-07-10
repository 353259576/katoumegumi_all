package cn.katoumegumi.java.common;

import java.net.*;
import java.util.*;

/**
 * 系统基本信息
 * @author 星梦苍天
 */
public class OSMessageUtils {

    public static enum OSType{
        /**
         * 系统类型
         */
        WINDOWS("windows"),
        LINUX("linux"),
        MAC_OS("mac")
        ;
        public final String baseName;

        private OSType(String baseName){
            this.baseName = baseName;
        }

        public String getBaseName() {
            return baseName;
        }
    }


    /**
     * 获取系统名称
     * @return
     */
    public static String getOSName(){
        return System.getProperty("os.name").toLowerCase(Locale.ROOT);
    }

    /**
     * 获取系统类型
     * @return
     */
    public static OSType getOSType(){
        String osName = getOSName();
        OSType[] osTypes = OSType.values();
        for(OSType osType:osTypes){
            if(osName.indexOf(osType.getBaseName()) > -1){
                return osType;
            }
        }
        return null;
    }

    /**
     * 获取系统版本
     * @return
     */
    public static String getOSVersion(){
        return System.getProperty("os.version").toLowerCase(Locale.ROOT);
    }

    /**
     * 获取本机网卡MAC地址
     * @return
     */
    public static String getLocalMac() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            try {
                byte[] bytes = NetworkInterface.getByInetAddress(inetAddress).getHardwareAddress();
                List<String> stringList = new ArrayList<>(bytes.length);
                for(byte b:bytes){
                    int num = b&0xff;
                    stringList.add(Integer.toHexString(num));
                }
                if(WsListUtils.isEmpty(stringList)){
                    return null;
                }
                return String.join("-",stringList).toUpperCase(Locale.ROOT);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取本机ipv4地址
     * @return
     */
    public static String getLocalIpv4(){
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取本机ipv6地址
     * @return
     */
    public static String getLocalIpv6(){
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            try {
                NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress);
                List<InterfaceAddress> addressList = networkInterface.getInterfaceAddresses();
                for (InterfaceAddress address:addressList){
                    byte[] bytes = address.getAddress().getAddress();
                    if(bytes.length == 16){
                        return address.getAddress().getHostAddress();
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }



}
