OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

START_TIME=`date +%s`

source ${OWN_DIR}/fav-env.sh
echo $FAV_OPTS

SOURCE_JAVA=/data/fengwenhan/util/jdk1.8.0_371
INSTRUMENTED_JAVA=/data/fengwenhan/data/fav-jre-inst

java -jar ${OWN_DIR}/FaultFuzz-inst-0.0.5-SNAPSHOT.jar -forJava ${SOURCE_JAVA} ${INSTRUMENTED_JAVA}

# I don't know why the following codes are needed.
# I comment them out. If something bad heppens, please uncomment them.

# #place jce.jar and security together

# rm $FAV_HOME/fav-jre-inst/jre/lib/jce.jar

# #cp /home/gaoyu/java/java-se-8u41-ri/jre/lib/jce.jar /home/gaoyu/FAVD/FAVTrigger/fav-jre-inst/jre/lib
# cp $FAV_HOME/java/jdk8u262-b10/jre/lib/jce.jar $FAV_HOME/fav-jre-inst/jre/lib

# #rm -r /home/gaoyu/FAVD/FAVTrigger/fav-jre-inst/jre/lib/security/*
# rm -r $FAV_HOME/fav-jre-inst/jre/lib/security/policy/*

# #cp -r /home/gaoyu/java/java-se-8u41-ri/jre/lib/security/* /home/gaoyu/FAVD/FAVTrigger/fav-jre-inst/jre/lib/security
# cp -r $FAV_HOME/java/jdk8u262-b10/jre/lib/security/policy/* $FAV_HOME/fav-jre-inst/jre/lib/security/policy

# chmod +x $FAV_HOME/fav-jre-inst/bin/*
