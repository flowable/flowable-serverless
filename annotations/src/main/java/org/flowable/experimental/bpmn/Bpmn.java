package org.flowable.experimental.bpmn;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Filip Hrisafov
 */
@Repeatable(BpmnModels.class)
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface Bpmn {

    String resource();

    boolean enableEagerExecutionTreeFetching() default true;

}
