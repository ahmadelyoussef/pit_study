RUNTIME_LIB="/Users/alifakhrzadegan/aspectj1.8/lib/aspectjrt.jar"
TARGET_FOLDER=""
JAVA_AGENT="/Users/alifakhrzadegan/aspectj1.8/lib/aspectjweaver.jar"
ASPECTS_JAR=""
MAIN_CLASS=""

java -javaagent:${JAVA_AGENT}  -cp ${ASPECTS_JAR}:${TARGET_FOLDER} ${MAIN_CLASS}

