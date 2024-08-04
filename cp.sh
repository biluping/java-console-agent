#!/bin/zsh
echo 开始 copy jar
cp ./console-agent/target/java-console-agent-jar-with-dependencies.jar ./console-boot/src/main/resources
cp ./console-core/target/java-console-core-jar-with-dependencies.jar ./console-boot/src/main/resources