package io.micronaut.ignite.mapper;

import io.micronaut.aop.Around;
import io.micronaut.context.annotation.Type;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.AnnotationValueBuilder;
import io.micronaut.inject.annotation.TypedAnnotationMapper;
import io.micronaut.inject.visitor.VisitorContext;
import org.apache.ignite.compute.gridify.Gridify;

import java.util.Arrays;
import java.util.List;

public class GridifyAnnotationMapper implements TypedAnnotationMapper<Gridify> {
    @Override
    public Class<Gridify> annotationType() {
        return Gridify.class;
    }

    @Override
    public List<AnnotationValue<?>> map(AnnotationValue<Gridify> annotation, VisitorContext visitorContext) {
        final AnnotationValueBuilder<Gridify> builder = AnnotationValue.builder(Gridify.class);

        annotation.stringValue("igniteInstanceName").ifPresent(s -> builder.member("igniteInstanceName", s));
        annotation.classValue("taskClass").ifPresent(s -> builder.member("taskClass", s));
        annotation.intValue("timeout").ifPresent(s -> builder.member("timeout", s));
        annotation.classValue("interceptor").ifPresent(s -> builder.member("interceptor", s));
        annotation.stringValue("igniteInstanceName").ifPresent(s -> builder.member("igniteInstanceName", s));
        annotation.stringValue("gridName").ifPresent(s -> builder.member("gridName", s));

        AnnotationValueBuilder<Type> typeAnnotationValueBuilder =
            AnnotationValue.builder(Type.class).member("value","io.micronaut.ignite.aop.GridfyInterceptor");

        AnnotationValueBuilder<Around> aroundAnnotationValueBuilder =
            AnnotationValue.builder(Around.class);

        return Arrays.asList(builder.build(), typeAnnotationValueBuilder.build(), aroundAnnotationValueBuilder.build());
    }
}
