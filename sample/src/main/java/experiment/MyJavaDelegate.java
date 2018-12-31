package experiment;

import java.util.concurrent.atomic.AtomicInteger;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

public class MyJavaDelegate implements JavaDelegate {

    public static final AtomicInteger COUNTER = new AtomicInteger(0);

    @Override
    public void execute(DelegateExecution execution) {
        COUNTER.incrementAndGet();
    }

}
