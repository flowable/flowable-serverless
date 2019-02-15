/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.sample;

import static org.springframework.fu.jafu.Jafu.webApplication;
import static org.springframework.fu.jafu.web.WebFluxServerDsl.server;

import org.flowable.engine.ProcessEngine;
import org.flowable.serverless.ServerlessUtil;
import org.springframework.fu.jafu.JafuApplication;

/**
 * Build with mvn clean package
 *
 * Try it out with
 *
 * curl localhost:8080/process
 */
public class SpringFuApplication {

    public static ProcessEngine processEngine =
        ServerlessUtil.initializeProcessEngineForBpmnModel(commandContext -> org.flowable.sample.SimpleServiceTask.createSimpleServiceTaskBpmnModel());

    public static JafuApplication app = webApplication(application -> {

        application.beans(b -> {
            b.bean(ProcessHandler.class);
            b.bean(ProcessService.class);
        });

        application.enable(server(server -> {

            server.port(8080);

            server.router(r -> {
                ProcessHandler handler = server.ref(ProcessHandler.class);
                r.GET("/process", handler::startProcess);
            });

            server.codecs(codec -> { codec.string(); codec.jackson(); });
        }));
    });

    public static void main(String[] args) {
        app.run(args);
    }

}