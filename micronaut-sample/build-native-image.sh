./mvnw package
java -cp target/micronaut-sample-app-1.0-SNAPSHOT.jar io.micronaut.graal.reflect.GraalClassLoadingAnalyzer
native-image --no-server \
             --allow-incomplete-classpath \
             --delay-class-initialization-to-runtime=sun.font.SunLayoutEngine \
             --delay-class-initialization-to-runtime=io.netty.handler.ssl.util.BouncyCastleSelfSignedCertGenerator \
             --delay-class-initialization-to-runtime=io.netty.handler.ssl.JdkNpnApplicationProtocolNegotiator \
             --delay-class-initialization-to-runtime=io.netty.handler.ssl.ReferenceCountedOpenSslEngine \
             --class-path target/micronaut-sample-app-1.0-SNAPSHOT.jar \
             -H:ReflectionConfigurationFiles=target/reflect.json \
             -H:EnableURLProtocols=http \
             -H:IncludeResources="logback.xml|application.yml|META-INF/services/*.*" \
             -H:Name=micronaut-sample-app \
             -H:Class=org.flowable.app.Application \
             -H:+ReportUnsupportedElementsAtRuntime \
             -H:+AllowVMInspection \
             -H:-UseServiceLoaderFeature \
             --rerun-class-initialization-at-runtime='sun.security.jca.JCAUtil$CachedSecureRandomHolder,javax.net.ssl.SSLContext' \
             --delay-class-initialization-to-runtime=io.netty.handler.codec.http.HttpObjectEncoder,io.netty.handler.codec.http.websocketx.WebSocket00FrameEncoder,io.netty.handler.ssl.util.ThreadLocalInsecureRandom,com.sun.jndi.dns.DnsClient
