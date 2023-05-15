java -jar Phosphor-0.0.5-SNAPSHOT.jar -forJava /home/gaoyu/java/jdk1.8.0_271 fav-jre-inst

#java -jar Phosphor-0.0.5-SNAPSHOT.jar /home/gaoyu/java/jdk1.8.0_271 fav-jre-inst

#place jce.jar and security together

rm fav-jre-inst/jre/lib/jce.jar

#cp /home/gaoyu/java/java-se-8u41-ri/jre/lib/jce.jar fav-jre-inst/jre/lib
cp /home/gaoyu/java/jdk8u262-b10/jre/lib/jce.jar fav-jre-inst/jre/lib

#rm -r fav-jre-inst/jre/lib/security/*
rm -r fav-jre-inst/jre/lib/security/policy/*

#cp -r /home/gaoyu/java/java-se-8u41-ri/jre/lib/security/* fav-jre-inst/jre/lib/security
cp -r /home/gaoyu/java/jdk8u262-b10/jre/lib/security/policy/* fav-jre-inst/jre/lib/security/policy

chmod +x fav-jre-inst/bin/*

#java -jar Phosphor-0.0.5-SNAPSHOT.jar add-input add-output
#java -jar Phosphor-0.0.5-SNAPSHOT.jar input add-output
