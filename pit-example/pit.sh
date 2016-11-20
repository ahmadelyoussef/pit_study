java -cp target/classes:target/test-classes:lib/junit-4.10.jar:lib/pitest-1.1.11-SNAPSHOT.jar:lib/pitest-command-line-1.1.11-SNAPSHOT.jar \
    org.pitest.mutationtest.commandline.MutationCoverageReport \
    --reportDir target/pit-reports \
    --targetClasses com.squareup.javapoet.* \
    --targetTests com.squareup.javapoet.* \
    --sourceDirs src/main/java,src/test/java
