cd pitest_source/
mvn install -DskipTests

cd ../

cp pitest_source/pitest/target/pitest-1.1.11-SNAPSHOT.jar lib/
cp pitest_source/pitest-maven/target/pitest-maven-1.1.11-SNAPSHOT.jar lib/

for dir in ./tests/*
do
	cp -r ./lib "$dir"
done
