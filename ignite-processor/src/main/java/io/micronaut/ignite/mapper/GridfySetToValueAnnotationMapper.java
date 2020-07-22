package io.micronaut.ignite.mapper;

import io.micronaut.aop.Around;
import io.micronaut.context.annotation.Type;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.AnnotationValueBuilder;
import io.micronaut.inject.annotation.TypedAnnotationMapper;
import io.micronaut.inject.visitor.VisitorContext;
import org.apache.ignite.compute.gridify.GridifySetToValue;

import java.util.Arrays;
import java.util.List;

public class GridfySetToValueAnnotationMapper implements TypedAnnotationMapper<GridifySetToValue> {
    @Override
    public Class<GridifySetToValue> annotationType() {
        return GridifySetToValue.class;
    }

    @Override
    public List<AnnotationValue<?>> map(AnnotationValue<GridifySetToValue> annotation, VisitorContext visitorContext) {
        final AnnotationValueBuilder<GridifySetToValue> builder = AnnotationValue.builder(GridifySetToValue.class);

        annotation.classValue("nodeFilter").ifPresent(s -> builder.member("nodeFilter", s));
        annotation.longValue("timeout").ifPresent(s -> builder.member("timeout", s));
        annotation.intValue("threshold").ifPresent(s -> builder.member("threshold", s));
        annotation.intValue("splitSize").ifPresent(s -> builder.member("splitSize", s));
        annotation.classValue("interceptor").ifPresent(s -> builder.member("interceptor", s));
        annotation.stringValue("gridName").ifPresent(s -> builder.member("gridName", s));
        annotation.stringValue("igniteInstanceName").ifPresent(s -> builder.member("igniteInstanceName", s));

        AnnotationValueBuilder<Type> typeAnnotationValueBuilder =
            AnnotationValue.builder(Type.class).member("value", "io.micronaut.ignite.intercept.GridfySetToValueAdvice");

        AnnotationValueBuilder<Around> aroundAnnotationValueBuilder =
            AnnotationValue.builder(Around.class);

        return Arrays.asList(typeAnnotationValueBuilder.build(), aroundAnnotationValueBuilder.build(), builder.build());
    }
}
