#!/usr/bin/env sh

rm flowablespringfu

mvn clean package -Pgraal
unzip target/flowable-function-spring-fu-sample*.jar -d target/flowable-function-spring-fu-sample

native-image -H:IncludeResources='META-INF/.*.json|META-INF/spring.factories|org/springframework/boot/logging/.*'\
             --allow-incomplete-classpath\
             --delay-class-initialization-to-runtime=io.netty.handler.codec.http.HttpObjectEncoder,org.springframework.core.io.VfsUtils,org.springframework.format.support.DefaultFormattingConversionService\
             -H:ReflectionConfigurationFiles=graal/app.json,graal/boot.json,graal/framework.json,graal/log4j.json,graal/netty.json,graal/custom-reflect.json\
             -Dio.netty.noUnsafe=true -H:+ReportUnsupportedElementsAtRuntime\
             -Dfile.encoding=UTF-8\
             -cp ".:$(echo target/flowable-function-spring-fu-sample/BOOT-INF/lib/*.jar | tr ' ' ':')":target/flowable-function-spring-fu-sample/BOOT-INF/classes org.flowable.sample.SpringFuApplication
mv org.flowable.sample.springfuapplication flowablespringfu
