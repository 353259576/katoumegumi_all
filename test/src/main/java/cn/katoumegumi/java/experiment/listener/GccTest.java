package cn.katoumegumi.java.experiment.listener;

/*import org.eclipse.jetty.util.UrlEncoded;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;
import org.opencv.video.Video;
import org.opencv.videoio.Videoio;*/

public class GccTest {


    static {
        //System.loadLibrary("jni/SayHello");
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }


    public static void main(String[] args) throws Exception {
        /*String str = "D:/image/19.jpg";
        String str1 = "D:/image/24.jpg";
        String newStr = new String(str.getBytes("GBK"),"GBK");
        String newStr1 = new String(str1.getBytes("GBK"),"GBK");
        Mat mat = Imgcodecs.imread(str);
        Mat mat1 = Imgcodecs.imread(str1);
        System.out.println(mat.size());
        System.out.println(mat.type());
        //Imgproc.medianBlur(mat,mat,9);
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".png",mat,matOfByte);
        //byte[] bytes = matOfByte.toArray();
        //System.out.println(bytes.length);
        Mat mat2 = new Mat(mat.size(), mat1.type());

        for(int i = 0; i < mat.rows(); i++){
            for (int j = 0; j < mat.cols(); j++){
                System.out.println(mat2.put(i,j,mat.get(i,j)));
            }
        }
        Imgcodecs.imwrite("D:\\image\\191.png",matOfByte);
        mat.release();
        mat1.release();
        mat2.release();
        matOfByte.release();*/
        //new GccTest().sayHello();
        /*Mat mat = new Mat(1080,1920,CvType.CV_32FC4,new Scalar(255));
        for(int i = 0; i < 1920; i++){
            for(int j = 0; j < 1080; j++){
                mat.put(j,i,255d,152d,785d,255d);
            }
        }*/
        /*FileInputStream fileInputStream = new FileInputStream("D:\\image\\19.jpg");
        FileChannel fileChannel = fileInputStream.getChannel();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        WritableByteChannel writableByteChannel = Channels.newChannel(byteArrayOutputStream);
        fileChannel.transferTo(0,fileChannel.size(),writableByteChannel);
        byte bytes[] = byteArrayOutputStream.toByteArray();
        writableByteChannel.close();
        fileChannel.close();
        MatOfByte matOfByte = new MatOfByte();
        matOfByte.fromArray(bytes);
        System.out.println(matOfByte.size());
        System.out.println(matOfByte.type());*/
        //Mat mat = Imgcodecs.imread("D:\\image\\19.jpg");

        //Imgproc.medianBlur(mat,mat,8);
        //FileInputStream fileInputStream = new FileInputStream("D:\\image\\19.jpg");
        //FileChannel fileChannel = fileInputStream.getChannel();
        //ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        //WritableByteChannel writableByteChannel = Channels.newChannel(byteArrayOutputStream);
        //fileChannel.transferTo(0,fileChannel.size(),writableByteChannel);
        //byte bytes[] = byteArrayOutputStream.toByteArray();
        //writableByteChannel.close();
        //fileChannel.close();
        //MatOfByte matOfByte = new MatOfByte();
        //matOfByte.fromArray(bytes);
        //Imgcodecs.imencode(".webp",mat,matOfByte);
        //mat.release();
        //Mat mat = Imgcodecs.imdecode(matOfByte,Imgcodecs.IMREAD_ANYCOLOR);
        //Imgproc.putText(mat,"hello world",new Point(500,500),Imgproc.FONT_HERSHEY_SIMPLEX,5,new Scalar(0,0,0),5);
        //byte bytes[] = matOfByte.toArray();
        //matOfByte.create(1080,1920,CvType.CV_8UC3);
        //matOfByte.fromArray(bytes);
        //matOfByte.create(new Size(new Point(1920,1080)),CvType.CV_8UC3);
        //Mat mat1 = new Mat(1920,1080,CvType.CV_8UC3);
        //matOfByte.convertTo(mat1,CvType.CV_8UC3);
        //byte bytes[] = matOfByte.toArray();
        //FileOutputStream fileOutputStream = new FileOutputStream("D:\\image\\191.jpg");
        //fileOutputStream.write(bytes);
        //fileOutputStream.close();
        //mat.release();
        //matOfByte.release();
        //Imgproc.cvtColor(mat,mat1,Imgproc.COLOR_BGR2RGBA);
        //Imgcodecs.imwrite("D:\\image\\191.png",mat);
    }

    public native void sayHello();

}
