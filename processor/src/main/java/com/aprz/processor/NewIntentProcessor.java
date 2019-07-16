package com.aprz.processor;

import com.aprz.annotataion.NewIntent;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author by liyunlei
 * <p>
 * write on 2019/7/16
 * <p>
 * Class desc:
 */
@AutoService(Processor.class)
public class NewIntentProcessor extends AbstractProcessor {

    private static final String METHOD_PREFIX = "start";
    private static final ClassName classIntent = ClassName.get("android.content", "Intent");
    private static final ClassName classContext = ClassName.get("android.content", "Context");

    private Filer filer;
    private Messager messager;
    private Elements elements;
    private Map<String, String> activitiesWithPackage;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
        elements = processingEnvironment.getElementUtils();
        activitiesWithPackage = new HashMap<>();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        try {
            /**
             * 1- Find all annotated element
             */
            for (Element element : roundEnvironment.getElementsAnnotatedWith(NewIntent.class)) {

                if (element.getKind() != ElementKind.CLASS) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Can be applied to class.");
                    return true;
                }

                TypeElement typeElement = (TypeElement) element;
                activitiesWithPackage.put(
                        typeElement.getSimpleName().toString(),
                        elements.getPackageOf(typeElement).getQualifiedName().toString());
            }


            /**
             * 2- Generate a class
             */
            TypeSpec.Builder navigatorClass = TypeSpec
                    .classBuilder("Navigator")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

            for (Map.Entry<String, String> element : activitiesWithPackage.entrySet()) {
                String activityName = element.getKey();
                String packageName = element.getValue();
                ClassName activityClass = ClassName.get(packageName, activityName);
                MethodSpec intentMethod = MethodSpec
                        .methodBuilder(METHOD_PREFIX + activityName)
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(classIntent)
                        .addParameter(classContext, "context")
                        .addStatement("return new $T($L, $L)", classIntent, "context", activityClass + ".class")
                        .build();
                navigatorClass.addMethod(intentMethod);
            }


            /**
             * 3- Write generated class to a file
             */
            JavaFile.builder("com.annotationsample", navigatorClass.build()).build().writeTo(filer);


        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of(NewIntent.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

}
