package com.ws.java.common;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


public class Encryption {

    public static final String RSA_ALGORITHM = "RSA";
    public static final String DES_ALGORITHM = "DES";
    public static final String DES_INTERFACE = "DES/CBC/PKCS5Padding";
    public static final String CHARSET = "UTF-8";
    public static final String PUBLIC_KEY = "publicKey";
    public static final String PRIVATE_KEY = "privateKey";
    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};


    public static void main(String[] args) {
        /*Map map = createKeys(4096);
        System.out.println(map.get("privateKey"));
        System.out.println(map.get("publicKey"));*/
        /*try {
            byte[] compressBytes = Base64.getEncoder().encode("你好世界".getBytes("UTF-8"));
            Inflater decompression =  new Inflater();
            decompression.setInput(compressBytes, 0, compressBytes.length);
            byte [] decompressBytes = new byte [1024];
            int decompressLength = decompression.inflate(decompressBytes);
            decompression.end();
            String jsonString = new String(Arrays.copyOfRange(decompressBytes, 0, decompressLength));
            JSONObject jsonObject= new JSONObject(jsonString);
            String sigTLS = jsonObject.getString("TLS.sig");
            System.out.println(sigTLS);
        }catch (Exception e){
            e.printStackTrace();
        }*/

        //String privateKey = "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgy1T7YaaJMd2aj46JUJIMMLH7jeHSbyyXcdfN4I5LIPahRANCAARsdKt2Z3HotSIg6KFEU5ofMueU+0Ii0uin4veILy6aADpTLPGyvpOy0KOqUTfGxdsGFQs7ZOI02uvPn1m/amt0";
        String publicKey = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEbHSrdmdx6LUiIOihRFOaHzLnlPtCItLop+L3iC8umgA6Uyzxsr6TstCjqlE3xsXbBhULO2TiNNrrz59Zv2prdA==";

        String privateKey = "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgy1T7YaaJMd2aj46JUJIMMLH7jeHSbyyXcdfN4I5LIPahRANCAARsdKt2Z3HotSIg6KFEU5ofMueU+0Ii0uin4veILy6aADpTLPGyvpOy0KOqUTfGxdsGFQs7ZOI02uvPn1m/amt0";
        //String publicKey = "-----BEGIN PUBLIC KEY-----\r\nMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEbHSrdmdx6LUiIOihRFOaHzLnlPtCItLop+L3iC8umgA6Uyzxsr6TstCjqlE3xsXbBhULO2TiNNrrz59Zv2prdA==\r\n-----END PUBLIC KEY-----";

        /*Map<String,String> map = createSecp256k1Key(256);
        System.out.println(JSON.toJSONString(map));*/
        /*PublicKey privateKey = getECPublicKey("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE5E5AQsxbq0UIRxukyoiSAgfRD/x8YIWXi+YcrTODcPj6E2ksyUomLL0ZsN0em83OkHa+8663yr2/q3y/cQgN3g==");
        System.out.println(Base64.getEncoder().encodeToString(privateKey.getEncoded()).equals("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE5E5AQsxbq0UIRxukyoiSAgfRD/x8YIWXi+YcrTODcPj6E2ksyUomLL0ZsN0em83OkHa+8663yr2/q3y/cQgN3g=="));*/

/*        String serialString =
                "TLS.appid_at_3rd:" + 0 + "\n" +
                        "TLS.account_type:" + 0 + "\n" +
                        "TLS.identifier:" + "admin" + "\n" +
                        "TLS.sdk_appid:" + "" + "\n" +
                        "TLS.time:" + "10000000000" + "\n" +
                        "TLS.expire_after:" + "1800" + "\n";


        String sign = signEC(serialString,privateKey);
        System.out.println(sign);
        System.out.println(verifyEC(serialString,publicKey,sign));*/


    }





    //*****************************************************************************************************
    public static String desEncoder(String str,String password){
        try {
            password = md5Encoder(password).substring(0,8);
            //SecureRandom secureRandom = new SecureRandom();
            IvParameterSpec ivParameterSpec = new IvParameterSpec(password.getBytes("UTF-8"));
            DESKeySpec desKeySpec = new DESKeySpec(password.getBytes());
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(DES_ALGORITHM);
            SecretKey secretKey = secretKeyFactory.generateSecret(desKeySpec);
            Cipher cipher = Cipher.getInstance(DES_INTERFACE);
            cipher.init(Cipher.ENCRYPT_MODE,secretKey,ivParameterSpec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(str.getBytes("UTF-8")));
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static String desDecoder(String str,String password){
        try {
            password = md5Encoder(password).substring(0,8);
            //SecureRandom secureRandom = new SecureRandom();
            IvParameterSpec ivParameterSpec = new IvParameterSpec(password.getBytes());
            DESKeySpec desKeySpec = new DESKeySpec(password.getBytes());
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(DES_ALGORITHM);
            SecretKey secretKey = secretKeyFactory.generateSecret(desKeySpec);
            Cipher cipher = Cipher.getInstance(DES_INTERFACE);
            cipher.init(Cipher.DECRYPT_MODE,secretKey,ivParameterSpec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(str.getBytes(CHARSET))));
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;

    }







    //*****************************************************************************************************

    /**
     * sha1加密
     * @param str
     * @return
     */
    public static String sha1Encoder(String str){
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
            messageDigest.update(str.getBytes("UTF-8"));
            byte bytes[] = messageDigest.digest();
            return byteHexToString(bytes);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * hmacsha1加密
     * @param str
     * @param key
     * @return
     */
    public static String hmacSha1Encoder(String str,String key){
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(),"HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(secretKeySpec);
            byte bytes[] = mac.doFinal(str.getBytes());
            return byteHexToString(bytes);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }





    //***********************************************************************************************************

    public static Map<String,String> createSecp256k1Key(int keySize){
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
            keyPairGenerator.initialize(keySize);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();
            String publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            String privateKeyString = Base64.getEncoder().encodeToString(privateKey.getEncoded());
            Map<String,String> map = new HashMap<>();
            map.put(PUBLIC_KEY,publicKeyString);
            map.put(PRIVATE_KEY,privateKeyString);
            return map;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


    public static PrivateKey getECPrivateKey(String privateKeyString){
        try {
            byte[] privateKeyStringByte = Base64.getDecoder().decode(privateKeyString);
            // 取得私钥
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(privateKeyStringByte);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            ECPrivateKey priKey = (ECPrivateKey) keyFactory.generatePrivate(pkcs8KeySpec);
            return priKey;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static PublicKey getECPublicKey(String publicKeyString){
        try {
            byte[] privateKeyStringByte = Base64.getDecoder().decode(publicKeyString);
            // 取得公钥
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(privateKeyStringByte);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            ECPublicKey ecPublicKey = (ECPublicKey) keyFactory.generatePublic(x509EncodedKeySpec);
            return ecPublicKey;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }



    public static byte[] signECByte(String str,String privateKeyString){
        try {
            //KeyStore keyStore = KeyStore.getInstance(PRIVATE_KEY);
            PrivateKey privateKey = getECPrivateKey(privateKeyString);
            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initSign(privateKey);
            signature.update(str.getBytes("UTF-8"));
            byte bytes[] = signature.sign();
            //return Base64.getEncoder().encodeToString(bytes);
            return bytes;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static String signEC(String str,String privateKeyString){
        return Base64.getEncoder().encodeToString(signECByte(str,privateKeyString));
    }


    public static boolean verifyEC(String str,String publicKeyString,String sign){
        try {

            //ecurity.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

            //另一种方式获取publicKey
            /*Reader reader = new CharArrayReader(publicKeyString.toCharArray());
            PEMParser parser = new PEMParser(reader);
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            Object obj = parser.readObject();
            parser.close();
            SubjectPublicKeyInfo subjectPublicKeyInfo = (SubjectPublicKeyInfo)obj;
            PublicKey publicKey  = converter.getPublicKey(subjectPublicKeyInfo);*/
            //byte[] signatureBytes = Base64.getEncoder().encode(str.getBytes("UTF-8"));



            PublicKey publicKey = getECPublicKey(publicKeyString);
            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initVerify(publicKey);
            signature.update(str.getBytes("UTF-8"));
            boolean k = signature.verify(Base64.getDecoder().decode(sign));
            //boolean k = signature.verify(Base64Url.base64DecodeUrl(sign.getBytes("UTF-8")));
            return k;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    //**************************************************************************************

    /**
     * md5加密
     * @param str
     * @return
     */
    public static String md5Encoder(String str){
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(str.getBytes("UTF-8"));
            byte bytes[] = messageDigest.digest();
            return byteHexToString(bytes);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;

    }


    private static String byteHexToString(byte bytes[]){
        StringBuffer stringBuffer = new StringBuffer();
        for(int i = 0; i < bytes.length; i++){
            stringBuffer.append(HEX_DIGITS[(bytes[i] >> 4) & 0x0f]);
            stringBuffer.append(HEX_DIGITS[bytes[i] & 0x0f]);
        }
        return stringBuffer.toString();
    }





    //******************************************************************************************************
    /**
     * 获取公钥私钥
     * @param keySize
     * @return
     */
    public static Map<String,String> createKeys(int keySize){
        KeyPairGenerator keyPairGenerator = null;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        if(keyPairGenerator != null){
            keyPairGenerator.initialize(keySize);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            Key publicKey = keyPair.getPublic();
            Key privateKey = keyPair.getPrivate();
            String publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            String privateKeyString = Base64.getEncoder().encodeToString(privateKey.getEncoded());
            Map<String,String> map = new HashMap<>();
            map.put(PUBLIC_KEY,publicKeyString);
            map.put(PRIVATE_KEY,privateKeyString);
            return map;
        }else {
            return null;
        }

    }


    /**
     * 公钥翻译为公钥
     * @param publicKey
     * @return
     */
    public static RSAPublicKey getPublicKey(String publicKey){
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey.getBytes()));
            RSAPublicKey rsaPublicKey = (RSAPublicKey) keyFactory.generatePublic(x509EncodedKeySpec);
            return rsaPublicKey;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 私钥翻译为私钥
     * @param privateKey
     * @return
     */
    public static RSAPrivateKey getPrivateKey(String privateKey){
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey.getBytes()));
            RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyFactory.generatePrivate(pkcs8EncodedKeySpec);
            return rsaPrivateKey;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 公钥加密
     * @param data
     * @param rsaPublicKey
     * @return
     */
    public static String publicKeyEncoder(String data,RSAPublicKey rsaPublicKey){
        try {
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE,rsaPublicKey);
            return new String(Base64.getEncoder().encode(rsaSplitCode(cipher,Cipher.ENCRYPT_MODE,data.getBytes(CHARSET),rsaPublicKey.getModulus().bitLength())));
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 公钥解密
     * @param data
     * @param rsaPublicKey
     * @return
     */
    public static String publicKeyDecoder(String data,RSAPublicKey rsaPublicKey){
        try {
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE,rsaPublicKey);
            return new String(rsaSplitCode(cipher,Cipher.DECRYPT_MODE,Base64.getDecoder().decode(data),rsaPublicKey.getModulus().bitLength()),CHARSET);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 私钥加密
     * @param data
     * @param rsaPrivateKey
     * @return
     */
    public static String privateKeyEncoder(String data,RSAPrivateKey rsaPrivateKey){
        try {
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE,rsaPrivateKey);
            return new String(Base64.getEncoder().encode(rsaSplitCode(cipher,Cipher.ENCRYPT_MODE,data.getBytes(CHARSET),rsaPrivateKey.getModulus().bitLength())));
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 私钥解密
     * @param data
     * @param rsaPrivateKey
     * @return
     */
    public static String privateKeyDecoder(String data,RSAPrivateKey rsaPrivateKey){
        try {
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE,rsaPrivateKey);
            return new String(rsaSplitCode(cipher,Cipher.DECRYPT_MODE,Base64.getDecoder().decode(data),rsaPrivateKey.getModulus().bitLength()),CHARSET);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 开始加密解密
     * @param cipher
     * @param opmode
     * @param dataBytes
     * @param keySize
     * @return
     */
    private static byte[] rsaSplitCode(Cipher cipher,int opmode,byte[] dataBytes,int keySize){
        int maxBlock = 0;
        if(opmode==Cipher.DECRYPT_MODE){
            maxBlock = keySize / 8;
        }else {
            maxBlock = keySize / 8 -11;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] buff;
        int i = 0;
        try {
            while (dataBytes.length > offSet){
                if(dataBytes.length-offSet > maxBlock){
                    buff = cipher.doFinal(dataBytes,offSet,maxBlock);
                }else {
                    buff = cipher.doFinal(dataBytes, offSet,dataBytes.length-offSet);
                }
                byteArrayOutputStream.write(buff,0,buff.length);
                i++;
                offSet = i*maxBlock;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        byte[] resultDatas = byteArrayOutputStream.toByteArray();
        try {
            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return resultDatas;
    }




}