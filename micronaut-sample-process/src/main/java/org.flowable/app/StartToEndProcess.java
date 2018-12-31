package org.flowable.app;

import org.flowable.experimental.bpmn.Bpmn;

/**
 * The annotation processor from Micronaut seems to disable the one from Flowable, if they are in the same project.
 * Hence that the process is moved to a separate module.
 */
@Bpmn(resource = "processes/start-to-end.bpmn20.xml")
public class StartToEndProcess {

}
