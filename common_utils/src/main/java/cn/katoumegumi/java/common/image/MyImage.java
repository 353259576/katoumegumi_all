package cn.katoumegumi.java.common.image;

import cn.katoumegumi.java.common.WsImageUtils;

import java.awt.image.BufferedImage;

public class MyImage {

    private final BufferedImage baseBufferedImage;

    public MyImage(BufferedImage bufferedImage) {
        this.baseBufferedImage = bufferedImage;
    }
    public MyImage(int width,int height,int type) {
        this.baseBufferedImage = new BufferedImage(width,height,type);
    }

    public MyImage(BufferedImage bufferedImage,int width,int height,int type) {
        if(width == bufferedImage.getWidth() && height == bufferedImage.getHeight() && type == bufferedImage.getType()){
            this.baseBufferedImage = bufferedImage;
        }else {
            this.baseBufferedImage = WsImageUtils.fixedDimensionBufferedImage(bufferedImage,type,width,height);
        }
    }
}
