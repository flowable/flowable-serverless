FROM oracle/graalvm-ce:1.0.0-rc8
EXPOSE 8080
COPY target/my-app-*.jar my-app.jar
ADD . target
RUN java -cp my-app.jar io.micronaut.graal.reflect.GraalClassLoadingAnalyzer 
RUN native-image --no-server \
             --class-path my-app.jar \
             -H:ReflectionConfigurationFiles=target/reflect.json \
             -H:EnableURLProtocols=http \
             -H:IncludeResources="logback.xml|application.yml|META-INF/services/*.*" \
             -H:Name=my-app \
             -H:Class=my.app.Application \
             -H:+ReportUnsupportedElementsAtRuntime \
             -H:+AllowVMInspection \
             -H:-UseServiceLoaderFeature \
             --rerun-class-initialization-at-runtime='sun.security.jca.JCAUtil$CachedSecureRandomHolder,javax.net.ssl.SSLContext' \
             --delay-class-initialization-to-runtime=io.netty.handler.codec.http.HttpObjectEncoder,io.netty.handler.codec.http.websocketx.WebSocket00FrameEncoder,io.netty.handler.ssl.util.ThreadLocalInsecureRandom,com.sun.jndi.dns.DnsClient
ENTRYPOINT ["./my-app"]