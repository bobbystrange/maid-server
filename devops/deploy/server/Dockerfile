FROM tukeof/oraclejdk:14

MAINTAINER "tuke tukeof@gmail.com"

WORKDIR /usr/local/var

EXPOSE 8080

COPY maid-server.jar /usr/local/var

# -Djava.awt.headless=true
# Could not initialize class sun.awt.X11FontManager
CMD java -Xmx${JVM_MAX_MEMORY=1000M} \
    -Xss1M -server -Djava.security.egd=file:/dev/./urandom \
    -Dfile.encoding=UTF-8 -Duser.timezone=GMT+08 \
    -Djava.awt.headless=true \
    -Dspring.profiles.active=${SPRING_PROFILE} \
    -jar maid-server
