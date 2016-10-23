RUNTIME_LIB="/Users/ahmad/aspectj1.8/lib/aspectjrt.jar"
TARGET_FOLDER=""
JAVA_AGENT="/Users/ahmad/aspectj1.8/lib/aspectjweaver.jar"
ASPECTS_JAR="aspects.jar"
MAIN_CLASS=""

java -javaagent:${JAVA_AGENT}  -cp ${ASPECTS_JAR}:target/classes:target/test-classes:lib/junit-4.10.jar:lib/pitest-1.1.10.jar:lib/pitest-command-line-1.1.10.jar  \
    org.pitest.mutationtest.commandline.MutationCoverageReport \
    --reportDir target/pit-reports \
    --targetClasses pitexample.* \
    --sourceDirs src/main/java,src/test/java

