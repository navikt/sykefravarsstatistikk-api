FROM gcr.io/distroless/java21
ENV APP_NAME="sykefravarsstatistikk-api"
ENV LANG="nb_NO.UTF-8"
ENV LANGUAGE="nb_NO:nb"
ENV LC_ALL="nb_NO.UTF-8"
ENV TZ="Europe/Oslo"
ENV JDK_JAVA_OPTIONS="-Dfile.encoding=UTF-8 -Dhttp.proxyHost=webproxy.nais -Dhttps.proxyHost=webproxy.nais -Dhttp.proxyPort=8088 -Dhttps.proxyPort=8088 -Dhttp.nonProxyHosts=localhost|127.0.0.1|10.254.0.1|*.local|*.adeo.no|*.nav.no|*.aetat.no|*.devillo.no|*.oera.no|*.nais.io|*.aivencloud.com|*.intern.dev.nav.no"
COPY /target/sykefravarsstatistikk-api-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]