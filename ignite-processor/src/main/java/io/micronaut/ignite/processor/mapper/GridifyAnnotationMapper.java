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
import org.apache.ignite.compute.gridify.Gridify;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GridifyAnnotationMapper implements TypedAnnotationMapper<Gridify> {

    @Override
    public Class<Gridify> annotationType() {
        return Gridify.class;
    }

    @Override
    public List<AnnotationValue<?>> map(AnnotationValue<Gridify> annotation, VisitorContext visitorContext) {
        final AnnotationValueBuilder builder = AnnotationValue.builder(Gridify.class);

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

        return Arrays.asList(typeAnnotationValueBuilder.build(), aroundAnnotationValueBuilder.build(), builder.build());
    }
}
