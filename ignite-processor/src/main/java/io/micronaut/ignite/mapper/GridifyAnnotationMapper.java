package io.micronaut.ignite.mapper;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.aop.Around;
import io.micronaut.context.annotation.Type;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.AnnotationValueBuilder;
import io.micronaut.inject.annotation.NamedAnnotationMapper;
import io.micronaut.inject.annotation.TypedAnnotationMapper;
import io.micronaut.inject.visitor.VisitorContext;
import org.apache.ignite.compute.gridify.Gridify;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

public class GridifyAnnotationMapper implements NamedAnnotationMapper {

    @NonNull
    @Override
    public String getName() {
        return "org.apache.ignite.compute.gridify.Gridify";
    }

    @Override
    public List<AnnotationValue<?>> map(AnnotationValue<Annotation> annotation, VisitorContext visitorContext) {
        final AnnotationValueBuilder<Gridify> builder = AnnotationValue.builder(Gridify.class);

        annotation.stringValue("taskName").ifPresent(s -> builder.member("taskName", s));
        annotation.classValue("taskClass").ifPresent(s -> builder.member("taskClass", s));
        annotation.intValue("timeout").ifPresent(s -> builder.member("timeout", s));
        annotation.classValue("interceptor").ifPresent(s -> builder.member("interceptor", s));
        annotation.stringValue("gridName").ifPresent(s -> builder.member("gridName", s));
        annotation.stringValue("igniteInstanceName").ifPresent(s -> builder.member("igniteInstanceName", s));


        AnnotationValueBuilder<Type> typeAnnotationValueBuilder =
            AnnotationValue.builder(Type.class).member("value", new AnnotationClassValue<>("io.micronaut.ignite.intercept.GridifyAdvice"));

        AnnotationValueBuilder<Around> aroundAnnotationValueBuilder =
            AnnotationValue.builder(Around.class);

        return Arrays.asList(builder.build(), typeAnnotationValueBuilder.build(), aroundAnnotationValueBuilder.build());
    }
}
