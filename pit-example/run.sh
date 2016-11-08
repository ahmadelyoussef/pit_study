RUNTIME_LIB="/Users/Zakotm/Documents/JavaPractice/Mutation/AspectJ/lib/aspectjrt.jar"
TARGET_FOLDER=""
JAVA_AGENT="/Users/Zakotm/Documents/JavaPractice/Mutation/AspectJ/lib/aspectjweaver.jar"
ASPECTS_JAR=""
MAIN_CLASS=""

java -javaagent:${JAVA_AGENT}  -cp ${ASPECTS_JAR}:${TARGET_FOLDER} ${MAIN_CLASS}
