echo "Running ./tests/$1 ..."

cd ./tests/"$1"
mvn clean && mvn install -DskipTests && mvn org.pitest:pitest-maven:mutationCoverage
cd ..

echo "Done!"
