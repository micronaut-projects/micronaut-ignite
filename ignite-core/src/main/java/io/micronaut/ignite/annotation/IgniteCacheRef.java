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

/**
 * Used to reference {@link org.apache.ignite.IgniteCache}.
 */
@Documented
@IgniteRef(value = "")
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
public @interface IgniteCacheRef {

    /**
     * Name of the cache instance.
     *
     * @return the cache name.
     */
    @AliasFor(annotation = IgniteRef.class, member = "value")
    String value();

    /**
     * The client to used with cache. uses primary if not specified.
     *
     * @return the client to used with cache
     */
    @AliasFor(annotation = IgniteRef.class, member = "client")
    String client() default "default";

    /**
     * initialize cache using  {@link org.apache.ignite.configuration.NearCacheConfiguration}.
     *
     * @return near cache Id
     */
    String nearCacheId() default "";

    /**
     * initialize cache using {@link org.apache.ignite.configuration.CacheConfiguration}.
     *
     * @return near cache Id
     */
    String cacheConfigurationId() default "";

    /**
     * try create new cache instance if one does not exist.
     *
     * @return new cache instance
     */
    boolean create() default false;

    /**
     * append a unique id to the end of the cache name.
     *
     * @return unique id.
     */
    boolean uniqueId() default false;
}
