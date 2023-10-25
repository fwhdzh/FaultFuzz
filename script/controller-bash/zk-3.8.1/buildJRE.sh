java -jar $FAV_HOME/FaultFuzz-inst-0.0.5-SNAPSHOT.jar -forJava $FAV_HOME/java/jdk1.8.0_271 $FAV_HOME/fav-jre-inst

#java -jar FaultFuzz-inst-0.0.5-SNAPSHOT.jar /home/gaoyu/java/jdk1.8.0_271 fav-jre-inst

#place jce.jar and security together

rm $FAV_HOME/fav-jre-inst/jre/lib/jce.jar

#cp /home/gaoyu/java/java-se-8u41-ri/jre/lib/jce.jar /home/gaoyu/FAVD/FAVTrigger/fav-jre-inst/jre/lib
cp $FAV_HOME/java/jdk8u262-b10/jre/lib/jce.jar $FAV_HOME/fav-jre-inst/jre/lib

#rm -r /home/gaoyu/FAVD/FAVTrigger/fav-jre-inst/jre/lib/security/*
rm -r $FAV_HOME/fav-jre-inst/jre/lib/security/policy/*

#cp -r /home/gaoyu/java/java-se-8u41-ri/jre/lib/security/* /home/gaoyu/FAVD/FAVTrigger/fav-jre-inst/jre/lib/security
cp -r $FAV_HOME/java/jdk8u262-b10/jre/lib/security/policy/* $FAV_HOME/fav-jre-inst/jre/lib/security/policy

chmod +x $FAV_HOME/fav-jre-inst/bin/*
