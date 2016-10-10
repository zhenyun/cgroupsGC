
rm *.csv
rm *.class
/export/apps/jdk/JDK-1_8_0_40/bin/javac HTTest.java 
/export/apps/jdk/JDK-1_8_0_40/bin/java -Xmx$1 -Xms$1 -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+AlwaysPreTouch -XX:+PrintGCApplicationStoppedTime -XX:+UseG1GC -Xloggc:gc.log HTTest $2  $3


