package org.flowable.experimental.processor.bpmn;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.experimental.bpmn.Bpmn;
import org.flowable.experimental.bpmn.BpmnModels;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

/**
 * @author Filip Hrisafov
 */
@SupportedAnnotationTypes({
    "org.flowable.experimental.bpmn.Bpmn",
    "org.flowable.experimental.bpmn.BpmnModels"
})
public class BpmnModelProcessor extends AbstractProcessor {

    protected BpmnModelCreator bpmnModelCreator;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        bpmnModelCreator = new BpmnModelCreator(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        Set<? extends Element> bpmnElements = roundEnv.getElementsAnnotatedWith(Bpmn.class);
        for (Element element : bpmnElements) {
            processElement(element);
        }

        bpmnElements = roundEnv.getElementsAnnotatedWith(BpmnModels.class);
        for (Element element : bpmnElements) {
            processElement(element);
        }

        return false;
    }

    protected void processElement(Element element) {
        BpmnXMLConverter converter = new BpmnXMLConverter();
        for (BpmnPropertyHolder propertyHolder : collectBpmnPropertyHolders(element)) {
            String resource = propertyHolder.getResource();
            BpmnModel bpmnModel = converter.convertToBpmnModel(() -> getResourceStream(resource), true, true);
            bpmnModel.getProcesses().forEach(process -> process.setEnableEagerExecutionTreeFetching(propertyHolder.isEnableEagerExecutionTreeFetching()));

            TypeSpec type = bpmnModelCreator.createType(bpmnModel)
                .toBuilder()
                .addOriginatingElement(element)
                .build();

            String packageName = packageName(element);
            JavaFile javaFile = JavaFile.builder(packageName, type)
                .skipJavaLangImports(true)
                .indent("    ")
                .build();

            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

    }

    private String packageName(Element element) {
        return processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();
    }

    private InputStream getResourceStream(String resource) {
        try {
            // Most build systems will have copied the file to the class output location
            FileObject fileObject = this.processingEnv.getFiler()
                .getResource(StandardLocation.CLASS_OUTPUT, "", resource);
            File file = locateResourceFile(new File(fileObject.toUri()), resource);
            return file.exists() ?
                new FileInputStream(file)
                : fileObject.toUri().toURL().openStream();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    File locateResourceFile(File standardLocation, String resource) throws IOException {
        if (standardLocation.exists()) {
            return standardLocation;
        }
        return new File(locateGradleResourcesFolder(standardLocation), resource);
    }

    private File locateGradleResourcesFolder(File standardAdditionalMetadataLocation)
        throws FileNotFoundException {
        String path = standardAdditionalMetadataLocation.getPath();
        int index = path.lastIndexOf("classes");
        if (index < 0) {
            throw new FileNotFoundException();
        }
        String buildFolderPath = path.substring(0, index);
        File classOutputLocation = standardAdditionalMetadataLocation.getParentFile()
            .getParentFile();
        return new File(buildFolderPath,
            "resources" + '/' + classOutputLocation.getName());
    }

    private Collection<BpmnPropertyHolder> collectBpmnPropertyHolders(Element element) {
        Set<BpmnPropertyHolder> bpmnResources = new HashSet<>();
        for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
            if (annotation.getAnnotationType().toString().equals("org.flowable.experimental.bpmn.Bpmn")) {
                getBpmnPropertyHolder(annotation).ifPresent(bpmnResources::add);
            } else if (annotation.getAnnotationType().toString().equals("org.flowable.experimental.bpmn.BpmnModels")) {
                bpmnResources.addAll(getBpmnPropertyHolders(annotation));
            }
        }

        return bpmnResources;
    }

    private List<BpmnPropertyHolder> getBpmnPropertyHolders(AnnotationMirror annotationMirror) {
        return extractValue(annotationMirror, "value")
            .flatMap(this::getBpmn)
            .map(this::getBpmnPropertyHolder)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }

    private Stream<AnnotationMirror> getBpmn(AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return Stream.empty();
        }

        Object value = annotationValue.getValue();
        if (value instanceof List) {
            return ((List<AnnotationMirror>) value).stream();
        }

        return Stream.of((AnnotationMirror) value);
    }

    private Optional<BpmnPropertyHolder> getBpmnPropertyHolder(AnnotationMirror annotation) {
        BpmnPropertyHolder.Builder builder = new BpmnPropertyHolder.Builder();
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : processingEnv.getElementUtils().getElementValuesWithDefaults(annotation).entrySet()) {
            String annotationName = entry.getKey().getSimpleName().toString();
            AnnotationValue annotationValue = entry.getValue();
            if (annotationName.equals("resource")) {
                builder.resource(getAnnotationValueAsString(annotationValue));
            } else if (annotationName.equals("enableEagerExecutionTreeFetching")) {
                builder.enableEagerExecutionTreeFetching(getAnnotationValueAsBoolean(annotationValue));
            }
        }

        if (builder.hasResource()) {
            return Optional.of(builder.create());
        } else {
            return Optional.empty();
        }
    }

    private String getAnnotationValueAsString(AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }

        return annotationValue.getValue().toString();
    }

    private boolean getAnnotationValueAsBoolean(AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return false;
        }

        Object value = annotationValue.getValue();
        if (value instanceof Boolean) {
            return (boolean) value;
        }
        return false;
    }

    private Stream<AnnotationValue> extractValue(AnnotationMirror annotation, String valueName) {
        return annotation.getElementValues()
            .entrySet()
            .stream()
            .filter(entry -> entry.getKey().getSimpleName().toString().equals(valueName))
            .map(Map.Entry::getValue);
    }

}
