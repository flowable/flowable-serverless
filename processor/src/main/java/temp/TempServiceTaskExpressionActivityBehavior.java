package temp;

import org.flowable.bpmn.model.ServiceTask;
import org.flowable.engine.impl.bpmn.behavior.ServiceTaskExpressionActivityBehavior;
import org.flowable.engine.impl.util.CommandContextUtil;

public class TempServiceTaskExpressionActivityBehavior extends ServiceTaskExpressionActivityBehavior {

    public TempServiceTaskExpressionActivityBehavior(ServiceTask serviceTask, String expressionString) {
        super(serviceTask, null, null);

        // TODO: cannot create expressions during generation with @Bpm, quick workaround:
        this.expression = CommandContextUtil.getProcessEngineConfiguration().getExpressionManager().createExpression(expressionString);
    }

}
