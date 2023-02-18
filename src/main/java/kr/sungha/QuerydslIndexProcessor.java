package kr.sungha;

import com.google.auto.service.AutoService;
import com.google.common.base.CaseFormat;
import com.squareup.javapoet.*;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.persistence.Entity;
import java.io.IOException;
import java.util.Set;

/**
 * Generate index(Q.java) for all QueryDSL Q- classes
 */
@AutoService(Processor.class)
public class QuerydslIndexProcessor extends AbstractProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(Entity.class.getName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment round) {
        Set<? extends Element> elements = round.getElementsAnnotatedWith(Entity.class);

        if (!elements.isEmpty()) {
            TypeSpec.Builder index = TypeSpec.interfaceBuilder("Q").addModifiers(Modifier.PUBLIC);

            for (Element element : elements) {
                String fqcn = element.toString();
                String className = element.getSimpleName().toString();
                String packageName = fqcn.replace("." + className, "");

                TypeName type = ClassName.get(packageName, "Q" + className);

                String name = CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, className);
                String value = "Q" + className + "." + CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, className);

                FieldSpec field = FieldSpec.builder(type, name, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL).initializer(value).build();
                index.addField(field);
            }

            try {
                JavaFile.builder("querydsl", index.build()).build().writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return false;
    }

}
