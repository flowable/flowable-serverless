package org.flowable.experimental.bpmn;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Filip Hrisafov
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface BpmnModels {

    Bpmn[] value();

}
