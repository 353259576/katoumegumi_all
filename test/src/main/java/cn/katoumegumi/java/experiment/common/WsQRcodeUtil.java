package cn.katoumegumi.java.experiment.common;

import cn.katoumegumi.java.common.WsImageUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Hashtable;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class WsQRcodeUtil {

    public static void main(String[] args) {
        Executor executor = new ThreadPoolExecutor(4, 4,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), Executors.defaultThreadFactory());
        CountDownLatch countDownLatch = new CountDownLatch(1);

        try {
            Lock lock = new ReentrantLock();
            File file = new File("F:/imageHandle/1.zip");
            //135二维码 40*40logo 133 335 500*300
            Font font = Font.createFont(Font.TRUETYPE_FONT, new File("F:/imageHandle/FZKTJW.TTF"));
            //Font font = new Font(null,Font.PLAIN,24);
            final Font newfont = font.deriveFont(Font.PLAIN, 20);
            Color color = Color.black;
            BufferedImage oldLogoBufferedImage = WsImageUtils.filePathToBufferedImage("F:/imageHandle/logo.png");
            BufferedImage logoBufferedImage = WsImageUtils.fixedDimensionBufferedImage(oldLogoBufferedImage, BufferedImage.TYPE_4BYTE_ABGR, 40, 40);
            oldLogoBufferedImage.flush();
            BufferedImage backgroundBufferedImage = WsImageUtils.filePathToBufferedImage("F:/imageHandle/background.png");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
            zipOutputStream.setLevel(9);
            zipOutputStream.setMethod(ZipOutputStream.DEFLATED);
            long createTime = System.currentTimeMillis();
            for (int i = 0; i < 1; i++) {
                int k = i;
                executor.execute(() -> {
                    try {
                        String name = "你好世界，你好世界你好世";
                        String str = "http://www.fishmaimai.com/html/download.html";
                        //long startTime = System.currentTimeMillis();
                        BufferedImage bufferedImage = createQrcodeBufferedImage(str, 135);
                        //long endTime = System.currentTimeMillis();
                        //log.info("创建二维码花费时间为：{}",endTime - startTime);
                        int width = bufferedImage.getWidth();
                        int height = bufferedImage.getHeight();
                        //startTime = System.currentTimeMillis();
                        BufferedImage qrBufferedImage = WsImageUtils.mergeBufferedImage(bufferedImage, logoBufferedImage, (width - 40) / 2, (height - 40) / 2);
                        //endTime = System.currentTimeMillis();
                        //log.info("二维码合并图层花费时间为：{}",endTime - startTime);
                        bufferedImage.flush();
                        //startTime = System.currentTimeMillis();
                        BufferedImage newBackgroundBufferedImage = WsImageUtils.copyBufferedImage(backgroundBufferedImage, BufferedImage.TYPE_4BYTE_ABGR);
                        //endTime = System.currentTimeMillis();
                        //log.info("复制背景花费时间为：{}",endTime - startTime);
                        //startTime = System.currentTimeMillis();
                        BufferedImage newBufferedImage = WsImageUtils.mergeBufferedImage(newBackgroundBufferedImage, qrBufferedImage, 335, 133);
                        //endTime = System.currentTimeMillis();
                        //log.info("二维码与背景合成花费时间为：{}",endTime - startTime);
                        newBackgroundBufferedImage.flush();
                        //startTime = System.currentTimeMillis();
                        int length = name.length();
                        int nowIndex = 0;
                        int lineFontSize = 22;
                        int lineNum = 0;
                        String s = null;
                        StringBuffer stringBuffer = new StringBuffer();
                        StringBuffer filePath = new StringBuffer();
                        char cs[] = name.toCharArray();
                        for (int j = 0; j < cs.length; j++) {
                            char c = cs[j];
                            if (c < 56 && c > 49) {
                                nowIndex++;
                            } else if (c > 96 && c < 123) {
                                nowIndex++;
                            } else {
                                nowIndex += 2;
                            }
                            if (!(c == ' ' || c == '/' || c == '\\' || c == '?' || c == ':' || c == '*' || c == '\"' || c == '>' || c == '<' || c == '|')) {
                                filePath.append(c);
                            }
                            stringBuffer.append(c);
                            if (nowIndex / 2 / lineFontSize > lineNum) {
                                lineNum++;
                                newBackgroundBufferedImage = WsImageUtils.writeFontBufferedImage(newBufferedImage, stringBuffer.toString(), 25, 50 + 25 * (lineNum - 1), newfont, color);
                                stringBuffer = new StringBuffer();
                            }
                        }
                        if (stringBuffer.length() > 0) {
                            if (lineNum > 0) {
                                if (nowIndex / 2 / lineFontSize > lineNum) {
                                    lineNum++;
                                }
                            }
                            newBackgroundBufferedImage = WsImageUtils.writeFontBufferedImage(newBufferedImage, stringBuffer.toString(), 25, 50 + 25 * (lineNum), newfont, color);
                        }

                        //endTime = System.currentTimeMillis();
                        //log.info("标价牌填写商品名称花费时间为：{}",endTime - startTime);
                        //startTime = System.currentTimeMillis();
                        byte bytes[] = WsImageUtils.bufferedImageToByteArray(newBackgroundBufferedImage, "png");
                        lock.lock();
                        try {
                            ZipEntry zipEntry = new ZipEntry(k + "你好.png");
                            zipOutputStream.putNextEntry(zipEntry);
                            zipOutputStream.write(bytes, 0, bytes.length);
                            zipOutputStream.closeEntry();
                        } finally {
                            lock.unlock();
                        }
                        WsImageUtils.byteToFile(bytes, k + "", "png", "F:/imageHandle/QRcode/");
                        //endTime = System.currentTimeMillis();
                        //log.info("图片保存花费时间为：{}",endTime - startTime);
                        try {

                            logoBufferedImage.flush();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        countDownLatch.countDown();
                    }
                });

            }
            countDownLatch.await();
            zipOutputStream.finish();
            zipOutputStream.close();
            long endTime = System.currentTimeMillis();
            System.out.println(endTime - createTime);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static byte[] createQrcode(String str, int codeSize) {
        try {
            BufferedImage bufferedImage = createQrcodeBufferedImage(str, codeSize);
            bufferedImage = setLogo(bufferedImage, ImageIO.read(new File("D:/2.png")), 4);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
            byte bytes[] = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();
            return bytes;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }


    public static BufferedImage createQrcodeBufferedImage(String str, int codeSize) {
        try {
            Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 1);
            hints.put(EncodeHintType.QR_VERSION, 10);
            BitMatrix bitMatrix = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, codeSize, codeSize, hints);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    /*if(bitMatrix.get(x,y)){
                        pixels[y*width + x] = 0xff000000;
                    }else {
                        pixels[y*width + x] = 0xffffffff;
                    }*/
                    //bufferedImage.setRGB(x,y,bitMatrix.get(x,y) ? 0xFF000000 : 0xFFFFFFFF);
                    if (bitMatrix.get(x, y)) {
                        bufferedImage.setRGB(x, y, 0xFF231F20);
                    } else {
                        //bufferedImage.setRGB(x,y,0xffffffff);
                    }
                    //bufferedImage.setRGB(x,y,bitMatrix.get(x,y) ? 0xFF000000 : 0xFF800080);
                }
            }
            return bufferedImage;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }


    /**
     * 缩小图片
     *
     * @param oldBufferedImage
     * @param shrink
     * @return
     */
    public static BufferedImage shrinkImage(BufferedImage oldBufferedImage, int shrink) {
        int hight = oldBufferedImage.getHeight();
        int width = oldBufferedImage.getWidth();
        int sHight = hight / shrink;
        int sWidth = width / shrink;
        BufferedImage bufferedImage = new BufferedImage(sWidth, sHight, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.drawImage(oldBufferedImage, 0, 0, sWidth, sHight, null);
        oldBufferedImage.flush();
        return bufferedImage;
    }

    /**
     * 放大图片
     *
     * @param oldBufferedImage
     * @param magnify
     * @return
     */
    public static BufferedImage magnifyImage(BufferedImage oldBufferedImage, int magnify) {
        int hight = oldBufferedImage.getHeight();
        int width = oldBufferedImage.getWidth();
        int mHight = hight * magnify;
        int mWidth = width * magnify;
        BufferedImage bufferedImage = new BufferedImage(mWidth, mHight, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.drawImage(oldBufferedImage, 0, 0, mWidth, mHight, null);
        oldBufferedImage.flush();
        return bufferedImage;
    }


    /**
     * 设置图片背景
     *
     * @param frontBufferedImage
     * @param backBuffedImage
     * @return
     */
    public static BufferedImage setBackground(BufferedImage frontBufferedImage, BufferedImage backBuffedImage) {
        int width = frontBufferedImage.getWidth();
        int hight = frontBufferedImage.getHeight();
        BufferedImage bufferedImage = new BufferedImage(width, hight, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.drawImage(backBuffedImage, 0, 0, width, hight, null);
        graphics2D.drawImage(frontBufferedImage, 0, 0, width, hight, null);
        backBuffedImage.flush();
        frontBufferedImage.flush();
        return bufferedImage;
    }

    /**
     * 图片中央设置标志
     *
     * @param backBufferedImage
     * @param logoBufferedImage
     * @param shrink
     * @return
     */
    public static BufferedImage setLogo(BufferedImage backBufferedImage, BufferedImage logoBufferedImage, int shrink) {
        int width = backBufferedImage.getWidth();
        int hight = backBufferedImage.getHeight();
        int logoWidth = width / shrink;
        int logoHight = hight / shrink;
        BufferedImage bufferedImage = new BufferedImage(width, hight, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.drawImage(backBufferedImage, 0, 0, width, hight, null);
        graphics2D.drawImage(logoBufferedImage, (width - logoWidth) / 2, (hight - logoHight) / 2, logoWidth, logoHight, null);
        backBufferedImage.flush();
        logoBufferedImage.flush();
        return bufferedImage;
    }


}
