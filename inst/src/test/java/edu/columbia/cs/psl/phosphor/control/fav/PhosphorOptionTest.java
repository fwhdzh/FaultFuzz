package edu.columbia.cs.psl.phosphor.control.fav;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.columbia.cs.psl.phosphor.PhosphorOption;

public class PhosphorOptionTest {
    @Test
    public void testConfigure() {
        // String args[] = {"java -jar phosphor.jar -Xbootclasspath/a:/home/gaoyu/Phosphor-0.0.5-SNAPSHOT.jar -javaagent:/home/gaoyu/Phosphor-0.0.5-SNAPSHOT.jar=useFav=true,forZk=true,useMsgid=false,jdkMsg=false,jdkFile=true,recordPhase=true,recordPath=/home/gaoyu/zk363-fav-rst/,dataPaths=/home/gaoyu/evaluation/zk-3.6.3/zkData/version-2,cacheDir=/home/gaoyu/CacheFolder,favHome=/home/gaoyu,currentFault=$FAV_HOME/zk363curCrash,controllerSocket=172.30.0.1:12090,strictCheck=false,mapSize=10000,wordSize=64,covPath=/home/gaoyu/fuzzcov,covIncludes=org/apache/zookeeper,aflAllow=/home/gaoyu/evaluation/zk-3.6.3/allowlist,aflDeny=/home/gaoyu/evaluation/zk-3.6.3/denylist,aflPort=12081,saveResultInternal=100000"};
        String args[] = {"-saveResultInternal", "100000", "-useFav", "true", "-execMode", "Replay"};
        PhosphorOption.configure(true, args);
        assertEquals(Configuration.SAVE_RESULT_INTERNAL, 100000);
        assertTrue(Configuration.USE_FAV);
        assertEquals(Configuration.EXEC_MODE, Configuration.EXEC_MODE_SET.Replay);
    }
}
