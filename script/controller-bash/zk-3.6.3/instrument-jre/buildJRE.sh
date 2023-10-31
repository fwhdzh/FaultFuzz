

sutConfigurationFolder=/SUT-configuration

java -jar $sutConfigurationFolder/FaultFuzz-inst-0.0.5-SNAPSHOT.jar -forJava $sutConfigurationFolder/java/jdk1.8.0_271 $sutConfigurationFolder/FaultFuzz-JRE-inst


rm $sutConfigurationFolder/FaultFuzz-JRE-inst/jre/lib/jce.jar

cp $sutConfigurationFolder/java/jdk8u262-b10/jre/lib/jce.jar $sutConfigurationFolder/FaultFuzz-JRE-inst/jre/lib

rm -r $sutConfigurationFolder/FaultFuzz-JRE-inst/jre/lib/security/policy/*

cp -r $sutConfigurationFolder/java/jdk8u262-b10/jre/lib/security/policy/* $sutConfigurationFolder/FaultFuzz-JRE-inst/jre/lib/security/policy

chmod +x $sutConfigurationFolder/FaultFuzz-JRE-inst/bin/*

