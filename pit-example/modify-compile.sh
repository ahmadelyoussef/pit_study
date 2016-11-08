cd pitest_source/pitest/
mvn install -DskipTests
cd ../../ 
#cd pitest_source/pitest-command-line
#mvn install -DskipTests
#cd ../../
#cp pitest_source/pitest-command-line/target/pitest-command-line-1.1.11-SNAPSHOT.jar lib/
cp pitest_source/pitest/target/pitest-1.1.11-SNAPSHOT.jar lib/
