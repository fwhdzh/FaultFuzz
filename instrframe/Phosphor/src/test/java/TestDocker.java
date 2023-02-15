import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class TestDocker {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        run("/home/gaoyu/FAVD/FAVTrigger/gy.sh");
    }
    
    public static ArrayList<String> run(String cmd) {
        System.out.println("---------------------------------------------------------");
        System.out.println("Run command: "+cmd);
        String[] cmds = cmd.split(" ");
        ArrayList<String> outputs = new ArrayList<String>();
        ProcessBuilder processBuilder = new ProcessBuilder(cmds);
        processBuilder.redirectErrorStream(true);
        //plus redirect output stream
        try {
            Process process = processBuilder.start();
            readProcessOutput(process, outputs);
            process.waitFor();
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        System.out.println("*********************************************************");
        return outputs;
    }

    private static void readProcessOutput(final Process process, ArrayList<String> outputs) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            outputs.add(line);
            System.out.println(line);
        }

        // Sleep for a while to let the remaining messages go through.
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ie) {
            // ignore.
        }
    }
}
