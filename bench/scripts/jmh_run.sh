mvn clean install
printf "\n\n[TESTS!!!]\n\n"
java -da:com.almworks.sqlite4java -jar target/benchmarks.jar ".*$1.*" | tee current_log.txt
