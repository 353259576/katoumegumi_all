package cn.katoumegumi.java.experiment;


import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class HadoopExperiment {

    public static final String HDFS_PATH = "hdfs://192.168.0.111:9000";//9866

    public static void main(String[] args) throws Exception{
        /*Configuration configuration = new Configuration();
        configuration.set("fs.hdfs.impl",org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
        configuration.set("dfs.replication","1");
        FileSystem fileSystem = FileSystem.get(new URI(HDFS_PATH),configuration,"root");
        //fileSystem.mkdirs(new Path("/ws/file/"));
        fileSystem.copyFromLocalFile(new Path("D:\\新建文件夹\\19.jpg"),new Path("/19.jpg"));
        fileSystem.close();*/
        /*FutureTask futureTask = new FutureTask(new Callable() {
            @Override
            public Object call() throws Exception {
                System.out.println(Thread.currentThread().getName());
                return "你好世界";
            }
        });
        new Thread(()->{
            futureTask.run();
        }).start();

        System.out.println(futureTask.get());*/
        /*String basePath = HadoopExperiment.class.getClassLoader().getResource("").getPath();
        Enumeration<URL> enumeration = HadoopExperiment.class.getClassLoader().getResources("com/ws/java/common/");
        Iterator<URL> iterator = enumeration.asIterator();
        while (iterator.hasNext()){
            URL url = iterator.next();
            System.out.println(url.getPath());
            Enumeration<URL> urlEnumeration = HadoopExperiment.class.getClassLoader().getResources(url.getPath().replaceAll(basePath,""));
            Iterator<URL> urlIterator = urlEnumeration.asIterator();
            while (urlIterator.hasNext()){
                System.out.println(urlIterator.next().getPath());
            }
        }*/
    }

}