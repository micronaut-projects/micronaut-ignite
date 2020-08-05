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
package io.micronaut.ignite.annotation;

import io.micronaut.context.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Documented
@IgniteRef(value = "")
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
public @interface IgniteCacheRef {


    @AliasFor(annotation = IgniteRef.class, member = "value")
    String value();

    @AliasFor(annotation = IgniteRef.class, member = "client")
    String client() default "default";

    String nearCacheId() default "";

    String cacheConfigurationId() default "";

    boolean uniqueId() default false;

    boolean create() default false;
}
