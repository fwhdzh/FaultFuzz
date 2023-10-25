package edu.iscas.tcse.HDFSCasesV3;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;

public class HDFSUtil {
	public static FileSystem getDFSFileSystem() {
		try {
			Configuration configuration = new Configuration();
	        System.out.println("Hadoop conf dir is:"+"file://" + System.getProperty("user.dir")+"/etc/hadoop");
	        configuration.addResource(new Path("file://" + System.getProperty("user.dir")+"/etc/hadoop" + "/core-site.xml"));
	        configuration.addResource(new Path("file://" + System.getProperty("user.dir")+"/etc/hadoop" + "/hdfs-site.xml"));
	        configuration.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
	        configuration.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
//	        configuration.set("fs.defaultFS", "hdfs://mycluster");
//	        configuration.set("ha.zookeeper.quorum", "C1hd-zk:11181");
//	        configuration.set("dfs.client.failover.proxy.provider.mycluster",
//	        		"org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
//	        configuration.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
	        FileSystem fileSystem = FileSystem.get(new URI("hdfs://mycluster"), configuration, "root");
	        return fileSystem;
		} catch (IOException | InterruptedException | URISyntaxException e) {
			e.printStackTrace();
		}
        return null;
	}
	public static void writeFileToHDFS(FileSystem fileSystem, String filePath) throws IOException {
        //Create a path
        String fileName = "favExmple.txt";
//        Path hdfsWritePath = new Path("/user/root/fav/" + fileName);
        Path hdfsWritePath = new Path(filePath);
        FSDataOutputStream fsDataOutputStream = fileSystem.create(hdfsWritePath,true);

        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fsDataOutputStream,StandardCharsets.UTF_8));
        bufferedWriter.write("Java API to write data in HDFS");
        bufferedWriter.newLine();
        bufferedWriter.close();
    }
	public static void appendToHDFSFile(FileSystem fileSystem, String filePath) throws IOException {
        //Create a path
        String fileName = "favExmple.txt";
//        Path hdfsWritePath = new Path("/user/root/fav/" + fileName);
        Path hdfsWritePath = new Path(filePath);
        FSDataOutputStream fsDataOutputStream = fileSystem.append(hdfsWritePath);

        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fsDataOutputStream,StandardCharsets.UTF_8));
        bufferedWriter.write("Java API to append data in HDFS file");
        bufferedWriter.newLine();
        bufferedWriter.close();
    }
	public static void readFileFromHDFS(FileSystem fileSystem, String filePath) throws IOException {
        //Create a path
        String fileName = "favExmple.txt";
//        Path hdfsReadPath = new Path("/user/root/fav/" + fileName);
        Path hdfsReadPath = new Path(filePath);
        //Init input stream
        FSDataInputStream inputStream = fileSystem.open(hdfsReadPath);
        //Classical input stream usage
        String out= IOUtils.toString(inputStream, "UTF-8");
        System.out.println(out);

        /*BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8));

        String line = null;
        while ((line=bufferedReader.readLine())!=null){
            System.out.println(line);
        }*/

        inputStream.close();
    }
	public static RemoteIterator<LocatedFileStatus> listFiles(FileSystem fs, String dir, boolean recursive) throws IOException {
        return fs.listFiles(new Path(dir), recursive);
    }
	public static boolean mkdir(FileSystem fs, String dir) throws IOException {
        return fs.mkdirs(new Path(dir));
    }

	public static void putFile(FileSystem fs, String localPath, String hdfsPath) throws IOException {
		fs.copyFromLocalFile(new Path(localPath), new Path(hdfsPath));
    }

	public static void deleteFile(FileSystem fs, String path, boolean recursive) throws IOException {
        fs.delete(new Path(path), recursive);
    }
	public static FSDataOutputStream createFile(FileSystem fs, String path, boolean overwrite) throws IOException {
        return fs.create(new Path(path), overwrite);
//        fs.rename(src, dst)
//        fs.getLength(f)
//        fs.getFileChecksum(f)
//        fs.getUsed(path)
//        fs.listFiles(f, recursive)
//        fs.truncate(f, newLength)
    }
	public static void dowloadFile(FileSystem fs, String hdfsPath, String localPath) throws IOException {
        fs.copyToLocalFile(new Path(hdfsPath), new Path(localPath));
    }
}
