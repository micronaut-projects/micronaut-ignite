package io.micronaut.ignite.mapper;

import io.micronaut.aop.Around;
import io.micronaut.context.annotation.Type;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.AnnotationValueBuilder;
import io.micronaut.inject.annotation.TypedAnnotationMapper;
import io.micronaut.inject.visitor.VisitorContext;
import org.apache.ignite.compute.gridify.GridifySetToSet;

import java.util.Arrays;
import java.util.List;

public class GridfySetToSetAnnotationMapper implements TypedAnnotationMapper<GridifySetToSet> {
    @Override
    public Class<GridifySetToSet> annotationType() {
        return GridifySetToSet.class;
    }

    @Override
    public List<AnnotationValue<?>> map(AnnotationValue<GridifySetToSet> annotation, VisitorContext visitorContext) {
        final AnnotationValueBuilder<GridifySetToSet> builder = AnnotationValue.builder(GridifySetToSet.class);

        annotation.classValue("nodeFilter").ifPresent(s -> builder.member("nodeFilter", s));
        annotation.longValue("timeout").ifPresent(s -> builder.member("timeout", s));
        annotation.intValue("threshold").ifPresent(s -> builder.member("threshold", s));
        annotation.intValue("splitSize").ifPresent(s -> builder.member("splitSize", s));
        annotation.classValue("interceptor").ifPresent(s -> builder.member("interceptor", s));
        annotation.stringValue("gridName").ifPresent(s -> builder.member("nodeFilter", s));
        annotation.stringValue("igniteInstanceName").ifPresent(s -> builder.member("nodeFilter", s));

        AnnotationValueBuilder<Type> typeAnnotationValueBuilder =
            AnnotationValue.builder(Type.class).member("value", "io.micronaut.ignite.intercept.GridfySetToSetAdvice");

        AnnotationValueBuilder<Around> aroundAnnotationValueBuilder =
            AnnotationValue.builder(Around.class);

        return Arrays.asList(builder.build(), typeAnnotationValueBuilder.build(), aroundAnnotationValueBuilder.build());
    }
}
