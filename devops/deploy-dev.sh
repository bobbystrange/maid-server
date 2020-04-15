#!/usr/bin/env bash

cd /Users/tuke/repository/maid/maid-server
./gradlew :maid-cassandra:bootJar

cd /Users/tuke/repository/maid/maid-server
java -DSPRING_MAIL_HOST=smtp.sina.com.cn \
    -DSPRING_MAIL_USERNAME=tukeof@sina.com \
    -DSPRING_MAIL_PASSWORD=588fd5b9a274d841 \
    -jar ./maid-cassandra/build/libs/maid-cassandra-0.1.0.jar

# #### #### #### ####    #### #### #### ####    #### #### #### ####

# multi api instance

cd /Users/tuke/repository/maid/maid-server
java -DSPRING_MAIL_HOST=smtp.sina.com.cn \
    -DSPRING_MAIL_USERNAME=tukeof@sina.com \
    -DSPRING_MAIL_PASSWORD=588fd5b9a274d841 \
    -Dserver.port=8011 \
    -jar ./maid-cassandra/build/libs/maid-cassandra-0.1.0.jar

cd /Users/tuke/repository/maid/maid-server
java -DSPRING_MAIL_HOST=smtp.sina.com.cn \
    -DSPRING_MAIL_USERNAME=tukeof@sina.com \
    -DSPRING_MAIL_PASSWORD=588fd5b9a274d841 \
    -Dserver.port=8012 \
    -jar ./maid-cassandra/build/libs/maid-cassandra-0.1.0.jar

cd /Users/tuke/repository/maid/maid-server
java -DSPRING_MAIL_HOST=smtp.sina.com.cn \
    -DSPRING_MAIL_USERNAME=tukeof@sina.com \
    -DSPRING_MAIL_PASSWORD=588fd5b9a274d841 \
    -Dserver.port=8013 \
    -jar ./maid-cassandra/build/libs/maid-cassandra-0.1.0.jar
