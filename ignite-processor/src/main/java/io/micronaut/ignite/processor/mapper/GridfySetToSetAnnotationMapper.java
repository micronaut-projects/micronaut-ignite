/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.ignite.processor.mapper;

import io.micronaut.aop.Around;
import io.micronaut.context.annotation.Type;
import io.micronaut.core.annotation.AnnotationClassValue;
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
        final AnnotationValueBuilder builder = AnnotationValue.builder(GridifySetToSet.class);

        annotation.classValue("nodeFilter").ifPresent(s -> builder.member("nodeFilter", s));
        annotation.longValue("timeout").ifPresent(s -> builder.member("timeout", s));
        annotation.intValue("threshold").ifPresent(s -> builder.member("threshold", s));
        annotation.intValue("splitSize").ifPresent(s -> builder.member("splitSize", s));
        annotation.classValue("interceptor").ifPresent(s -> builder.member("interceptor", s));
        annotation.stringValue("gridName").ifPresent(s -> builder.member("nodeFilter", s));
        annotation.stringValue("igniteInstanceName").ifPresent(s -> builder.member("nodeFilter", s));

        AnnotationValueBuilder<Type> typeAnnotationValueBuilder =
            AnnotationValue.builder(Type.class).member("value", new AnnotationClassValue<>("io.micronaut.ignite.intercept.GridfySetToSetAdvice"));

        AnnotationValueBuilder<Around> aroundAnnotationValueBuilder =
            AnnotationValue.builder(Around.class);

        return Arrays.asList(builder.build(), typeAnnotationValueBuilder.build(), aroundAnnotationValueBuilder.build());
    }
}
