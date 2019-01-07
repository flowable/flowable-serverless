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
package temp;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.FieldExtension;
import org.flowable.bpmn.model.ServiceTask;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.flowable.engine.impl.util.CommandContextUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Custom behavior for http call, as running the default one on Graal isn't possible due to java.util.Timer being created in the background
 *
 * Also need to add --enable-url-protocols=https to native image generation.
 *
 * Also need to enable security to read from https inputstream https://blog.taylorwood.io/2018/10/04/graalvm-https.html
 * (otherwise exception java.lang.UnsatisfiedLinkError: sun.security.ec.ECDSASignature.verifySignedDigest([B[B[B[B)Z [symbol: Java_sun_security_ec_ECDSASignature_verifySignedDigest or Java_sun_security_ec_ECDSASignature_verifySignedDigest___3B_3B_3B_3B])
 * And copy graalvm-ce-1.0.0-rc10/Contents/Home/jre/lib/libsunec.dyli to current directory (can't get it working with the system property yet)
 */
public class CustomHttpActivityBehavior extends AbstractBpmnActivityBehavior {

    private Expression requestMethod;
    private Expression requestUrl;
    private Expression responseVariableName;

    public CustomHttpActivityBehavior(ServiceTask serviceTask) {
        List<FieldExtension> fieldExtensions = serviceTask.getFieldExtensions();
        for (FieldExtension fieldExtension : fieldExtensions) {
            if (fieldExtension.getFieldName().equals("requestMethod")) {
                this.requestMethod = createExpression(fieldExtension);
            } else if (fieldExtension.getFieldName().equals("requestUrl")) {
                this.requestUrl = createExpression(fieldExtension);
            } if (fieldExtension.getFieldName().equals("responseVariableName")) {
                this.responseVariableName = createExpression(fieldExtension);
            }
        }
    }

    @Override
    public void execute(DelegateExecution execution) {
        String method = (String) requestMethod.getValue(execution);
        String urlString = (String) requestUrl.getValue(execution);
        String variable = (String) responseVariableName.getValue(execution);

        try {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(method.toUpperCase());
            con.setRequestProperty("User-Agent", "custom");
            con.setRequestProperty("Content-type", "application/json");
            con.setRequestProperty("Accept", "*/*");

//            BufferedReader in = new BufferedReader(
//                new InputStreamReader(con.getInputStream()));
//            String inputLine;
//            StringBuffer content = new StringBuffer();
//            while ((inputLine = in.readLine()) != null) {
//                content.append(inputLine);
//            }
//            in.close();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(con.getInputStream());
            execution.setVariable(variable, jsonNode);

            con.disconnect();

        } catch (Exception e) {
            throw new FlowableException("Cannot execute http call", e);
        }
        leave(execution);
    }

    protected Expression createExpression(FieldExtension fieldExtension) {
        String expressionString = fieldExtension.getStringValue();
        if (StringUtils.isEmpty(expressionString)) {
            expressionString = fieldExtension.getExpression();
        }
        return CommandContextUtil.getProcessEngineConfiguration().getExpressionManager().createExpression(expressionString);
    }
}
