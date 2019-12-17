package cn.katoumegumi.java.common;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WsImageUtils {


    public static void main(String[] args) {
        try {
            /*BufferedImage bufferedImage = ImageIO.read(new FileInputStream("D:\\新建文件夹\\image-1540526825337.jpg"));
            BufferedImage bufferedImage1 = new BufferedImage(bufferedImage.getWidth(),bufferedImage.getHeight(),BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D graphics2D = bufferedImage1.createGraphics();
            graphics2D.drawImage(bufferedImage,0,0,null);
            File file = new File("D:\\新建文件夹\\image-1540526825337-副本.jpg");
            if(file == null){
                file.createNewFile();
            }
            ImageIO.write(bufferedImage1,"png",file);*/

            //FileInputStream fileInputStream = new FileInputStream("D:\\新建文件夹\\19.jpg");
            //byte[] bytes = changeImageSize(fileInputStream,10);
            /*byte[] bytes = cropImage(fileInputStream,100,100,500,500);
            FileOutputStream fileOutputStream = new FileOutputStream("D:\\新建文件夹\\image-1540526825337-副本.jpg");
            FileChannel fileChannel = fileOutputStream.getChannel();
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            //byteBuffer.flip();
            while (byteBuffer.hasRemaining()){
                fileChannel.write(byteBuffer);
            }*/
            //inciseImage(fileInputStream,1,200,"19","D:\\新建文件夹\\19\\");

            //byteToFile(changeImageSize(fileInputStream,2),"123","gif","D:\\新建文件夹\\");
            /*File file = new File("D:\\新建文件夹\\123");
            File newFile = new File("D:\\新建文件夹\\123.gif");
            file.renameTo(newFile);*/
            long startTime = System.currentTimeMillis();
            BufferedImage bufferedImage = ImageIO.read(new FileInputStream("D:\\新建文件夹\\19.jpg"));
            bufferedImage = createMosaic(bufferedImage,0,0,1920,1080,100);
            byte bytes[] = bufferedImageToByteArray(bufferedImage,"png");
            byteToFile(bytes,"newImageFile","jpg","D:\\新建文件夹\\");
            long endTime = System.currentTimeMillis();
            System.out.println(endTime - startTime);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


  /*  public static BufferedImage createFontBuffImage(Integer width,Integer height){

    }*/


    public static byte[] changeImageSize(InputStream inputStream,Integer size){
        try {
            BufferedImage oldBufferedImage = ImageIO.read(inputStream);
            int oldWidth = oldBufferedImage.getWidth();
            int oldHeight = oldBufferedImage.getHeight();
            int newWidth = 0;
            int newHeight = 0;
            if(size >= 0){
                newWidth = oldWidth * size;
                newHeight = oldHeight * size;
            }else {
                size = -size;
                newWidth = oldWidth / size;
                newHeight = oldHeight / size;
            }
            BufferedImage newBufferedImage = new BufferedImage(newWidth,newHeight,BufferedImage.TYPE_INT_RGB);
            Graphics2D newGraphics2D = newBufferedImage.createGraphics();
            newGraphics2D.setBackground(Color.WHITE);
            newGraphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            newGraphics2D.drawImage(oldBufferedImage,0,0,newWidth,newHeight,null);
            oldBufferedImage.flush();
            OutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(newBufferedImage,"jpg",outputStream);
            newBufferedImage.flush();
            byte[] bytes = ((ByteArrayOutputStream) outputStream).toByteArray();
            outputStream.flush();
            outputStream.close();
            return bytes;

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 切割图片
     * @param inputStream
     * @param x
     * @param y
     * @param width
     * @param height
     * @return
     */
    public static byte[] cropImage(InputStream inputStream,int x,int y,int width,int height){
        BufferedImage bufferedImage = null;
        byte bytes[] = null;
        try {
            bufferedImage = ImageIO.read(inputStream);
            bytes = cropImage(bufferedImage,x,y,width,height);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            bufferedImage.flush();
            try {
                inputStream.close();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                inputStream = null;
            }
        }
        return bytes;
    }


    public static byte[] cropImage(BufferedImage bufferedImage,int x,int y,int width,int height){
        //BufferedImage newBufferedImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        BufferedImage copyBufferedImage = bufferedImage.getSubimage(x,y,width,height);
        /*Graphics2D graphics2D = newBufferedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.drawImage(copyBufferedImage,0,0,width,height,null);
        copyBufferedImage.flush();*/
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] bytes = null;
        try {
            ImageIO.write(copyBufferedImage,"jpg",outputStream);
            bytes = outputStream.toByteArray();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                outputStream.flush();
                outputStream.close();
            }catch (Exception e){
                e.printStackTrace();
                outputStream = null;
            }
            //newBufferedImage.flush();

        }
        return bytes;

    }



    public static void inciseImage(InputStream inputStream,Integer direction,Integer directionValue,String fileName,String path){
        try {
            List list = new ArrayList();
            BufferedImage bufferedImage = ImageIO.read(inputStream);
            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();
            if(direction==1){
                int directionNum = height/directionValue;
                if(directionNum == 0){
                    return;
                }
                int i = 0;
                for(i = 0;i < directionNum; i++){
                    byte bytes[] = cropImage(bufferedImage,0,i*directionValue,width,directionValue);
                    byteToFile(bytes, fileName+"-"+i,"jpg",path);
                }
                int directionSurplus = height%directionValue;
                if(directionSurplus > 0){
                    byte bytes[] = cropImage(bufferedImage,0,height-directionSurplus,width,directionSurplus);
                    byteToFile(bytes, fileName+"-"+(i+1),"jpg",path);
                }
            }else if(direction == 2){
                int directionNum = width/directionValue;
                if(directionNum == 0){
                    return;
                }
                int i = 0;
                for(;i < directionNum; i++){
                    byte bytes[] = cropImage(bufferedImage,i*directionValue,0,directionValue,height);
                    byteToFile(bytes, fileName+"-"+i,"jpg",path);
                }
                int directionSurplus = width%directionValue;
                if(directionSurplus > 0){
                    byte bytes[] = cropImage(bufferedImage,height-directionSurplus,0,directionSurplus,height);
                    byteToFile(bytes, fileName+"-"+(i+1),"jpg",path);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public static BufferedImage filePathToBufferedImage(String filePath) throws Exception{
        File file = new File(filePath);
        if(!file.isFile()){
            throw new FileNotFoundException("文件路径为目录");
        }
        if(!file.exists()){
            throw new FileNotFoundException("文件不存在");
        }
        BufferedImage bufferedImage = ImageIO.read(file);
        return bufferedImage;
    }



    /**
     * 复制
     * @param bufferedImage
     * @param bufferedImageType
     * @return
     */
    public static BufferedImage copyBufferedImage(BufferedImage bufferedImage,Integer bufferedImageType){
        BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth(),bufferedImage.getHeight(),bufferedImageType);
        Graphics2D graphics2D = newBufferedImage.createGraphics();
        graphics2D.drawImage(bufferedImage,null,0,0);
        return newBufferedImage;
    }


    /**
     * 放大缩小
     * @param bufferedImage
     * @param enlargementTimes
     * @return
     */
    public static BufferedImage enlargementBufferedImage(BufferedImage bufferedImage,Integer enlargementTimes,Integer bufferedImageType){
        Integer newWidth = bufferedImage.getWidth() * enlargementTimes;
        Integer newHeight = bufferedImage.getHeight() * enlargementTimes;
        BufferedImage newBufferedImage = new BufferedImage(newWidth,newHeight,bufferedImageType);
        Graphics2D graphics2D = newBufferedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.drawImage(bufferedImage,0,0,newWidth,newHeight,null);
        return newBufferedImage;
    }

    /**
     * 将图片改为固定尺寸
     * @param bufferedImage
     * @param bufferedImageType
     * @param width
     * @param height
     * @return
     */
    public static BufferedImage fixedDimensionBufferedImage(BufferedImage bufferedImage,Integer bufferedImageType,Integer width, Integer height){
        BufferedImage newBufferedImage = new BufferedImage(width,height,bufferedImageType);
        Graphics2D graphics2D = newBufferedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.drawImage(bufferedImage,0,0,width,height,null);
        return newBufferedImage;
    }



    /**
     * 合并两个图层
     * @param backBufferedImage
     * @param frontBufferedImage
     * @param pointX
     * @param pointY
     * @return
     */
    public static BufferedImage mergeBufferedImage(BufferedImage backBufferedImage,BufferedImage frontBufferedImage, Integer pointX,Integer pointY){
        Graphics2D graphics2D = backBufferedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.drawImage(frontBufferedImage,pointX,pointY,frontBufferedImage.getWidth(),frontBufferedImage.getHeight(),null);
        return backBufferedImage;
    }

    /**
     * 在图层上写字
     * @param bufferedImage
     * @param str
     * @param pointX
     * @param pointY
     * @param font
     * @param color
     * @return
     */
    public static BufferedImage writeFontBufferedImage(BufferedImage bufferedImage,String str,Integer pointX,Integer pointY,Font font,Color color){
        Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setColor(color);
        graphics2D.setFont(font);
        graphics2D.drawString(str,pointX,pointY);
        return bufferedImage;
    }



    /**
     * byte数组转成文件
     * @param bytes
     * @param fileName
     * @param fileType
     * @param filePath
     */
    public static void byteToFile(byte bytes[],String fileName,String fileType,String filePath){
        File fileFloder = new File(filePath);
        if(!fileFloder.exists()){
            fileFloder.mkdirs();
        }
        fileFloder = null;
        ///fileName = WsStringUtils.stringTrim(fileName);
        filePath = filePath + fileName+"."+fileType;
        File file = new File(filePath);
        FileOutputStream fileOutputStream = null;
        FileChannel fileChannel = null;
        try {
            if(!file.exists()){
                file.createNewFile();
            }
            fileOutputStream = new FileOutputStream(file);
            fileChannel = fileOutputStream.getChannel();
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            fileChannel.write(byteBuffer);
            byteBuffer.clear();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if(fileChannel != null){
                    fileChannel.close();
                }
                if(fileOutputStream != null){
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }


    }


    public static BufferedImage createMosaic(BufferedImage bufferedImage,Integer pointX,Integer pointY,Integer width,Integer height,Integer level){
        Integer bufferImageWidth = bufferedImage.getWidth();
        Integer bufferImageHeight = bufferedImage.getHeight();
        Integer x = pointX;
        Integer y = pointY;
        Integer chunkWidth = width / level;
        Integer chunkHeight = height / level;
        chunkWidth = chunkWidth==0?1:chunkWidth;
        chunkHeight = chunkHeight==0?1:chunkHeight;

        x -= chunkWidth;
        y -= chunkHeight;
        Random random = new Random();
        Graphics2D graphics2D = bufferedImage.createGraphics();
        /*Rectangle rectangle = new Rectangle();
        rectangle.setRect(pointX,pointY,chunkWidth,chunkHeight);*/
        while (x < pointX+width){
            x += chunkWidth;
            while (y < pointY + height){
                y += chunkHeight;
                Integer randomX = random.nextInt(chunkWidth);
                Integer randomY = random.nextInt(chunkWidth);
                Integer modificationX = 0;
                Integer modificationY = 0;
                if(x + randomX >= bufferImageWidth){
                    modificationX = (x + randomX) - bufferImageWidth+1;
                }
                if(y + randomY >= bufferImageHeight){
                    modificationY = (y + randomY) - bufferImageHeight+1;
                }

                System.out.println("("+(x+randomX-modificationX)+","+(y+randomY-modificationY)+")("+x+","+y+")");

                int rgb = bufferedImage.getRGB(x + randomX - modificationX,y + randomY - modificationY);
                int a = rgb>>>24;
                int r = (rgb<<8)>>>24;
                int g = (rgb<<16)>>>24;
                int b = (rgb<<24)>>>24;
                //System.out.println("a:"+a+" r:"+r+" g:"+g+" b:"+b);
                Color color = new Color(r,g,b);
                graphics2D.setColor(color);
                graphics2D.fillRect(x,y,chunkWidth,chunkHeight);


            }
            y = pointY - chunkHeight;
        }
        return bufferedImage;
    }




    /**
     * bufferedImage转成byte数组
     * @param bufferedImage
     * @param type
     * @return
     */
    public static byte[] bufferedImageToByteArray(BufferedImage bufferedImage,String type){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte bytes[] = null;
        try {
            ImageIO.write(bufferedImage,type,byteArrayOutputStream);
            bytes = byteArrayOutputStream.toByteArray();
            bufferedImage.flush();
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                byteArrayOutputStream.flush();
                byteArrayOutputStream.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return bytes;

    }



}
