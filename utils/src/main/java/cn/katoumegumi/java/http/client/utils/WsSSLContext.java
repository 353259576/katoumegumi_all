package cn.katoumegumi.java.http.client.utils;

import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class WsSSLContext {
    //private final static String URL = "D:/项目/环境/apache-tomcat-9.0.12 - 副本/conf/www.fishmaimai.com.jks";
    //private final static String PASSWORD = "199645";

    public static SSLContext getSSLContext(String url,String password,String certificateType){
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLSv1.2");
            TrustManager trustManager[] = new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }};

            KeyManager keyManagers[] = null;
            if(certificateType != null){
                KeyManagerFactory keyManagerFactory = keyManagerFactory(url,password,certificateType);
                keyManagers = keyManagerFactory.getKeyManagers();
            }

            sslContext.init(keyManagers,trustManager,new SecureRandom());
        }catch (Exception e) {
            e.printStackTrace();
        }
        return sslContext;
    }


    public static KeyManagerFactory keyManagerFactory(String url,String password,String certificateType){
        FileInputStream fileInputStream = null;
        try {
            KeyStore keyStore = KeyStore.getInstance(certificateType);
            fileInputStream = new FileInputStream(url);
            keyStore.load(fileInputStream,password.toCharArray());
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore,password.toCharArray());
            return keyManagerFactory;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if(fileInputStream != null){
                    fileInputStream.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return null;

    }


    public static SslContext createNettySslContext(boolean server,KeyManagerFactory keyManagerFactory){
        if (server){
            SslContextBuilder sslContextBuilder = SslContextBuilder.forServer(keyManagerFactory);
            try {
                return sslContextBuilder.build();
            }catch (SSLException e){
                e.printStackTrace();
            }
            return null;
        }else {
            try {
                SslProvider provider = OpenSsl.isAlpnSupported() ? SslProvider.OPENSSL : SslProvider.JDK;
                SslContextBuilder sslContextBuilder = SslContextBuilder.forClient();
                sslContextBuilder.sslProvider(provider);
                /*ApplicationProtocolConfig applicationProtocolConfig = new ApplicationProtocolConfig(
                        ApplicationProtocolConfig.Protocol.ALPN,
                        // NO_ADVERTISE is currently the only mode supported by both OpenSsl and JDK providers.
                        ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                        // ACCEPT is currently the only mode supported by both OpenSsl and JDK providers.
                        ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                        ApplicationProtocolNames.HTTP_2,
                        ApplicationProtocolNames.HTTP_1_1);
                sslContextBuilder.applicationProtocolConfig(applicationProtocolConfig);*/
                sslContextBuilder.keyStoreType("TLSv1.2");
                sslContextBuilder.ciphers(Http2SecurityUtil.CIPHERS,SupportedCipherSuiteFilter.INSTANCE);
                sslContextBuilder.trustManager(InsecureTrustManagerFactory.INSTANCE);
                SslContext sslContext = sslContextBuilder.build();
                return sslContext;
                /*SslContext sslContext = SslContextBuilder.forClient()
                        .sslProvider(provider)
                        .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                        .applicationProtocolConfig(new ApplicationProtocolConfig(
                                ApplicationProtocolConfig.Protocol.ALPN,
                                // NO_ADVERTISE is currently the only mode supported by both OpenSsl and JDK providers.
                                ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                                // ACCEPT is currently the only mode supported by both OpenSsl and JDK providers.
                                ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                                ApplicationProtocolNames.HTTP_2,
                                ApplicationProtocolNames.HTTP_1_1))
                        .build();
                        return sslContext;*/
            }catch (SSLException e){
                e.printStackTrace();
            }

            return null;
        }
    }



}
