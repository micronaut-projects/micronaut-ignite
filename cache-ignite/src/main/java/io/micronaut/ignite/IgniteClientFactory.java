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
package io.micronaut.ignite;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import org.apache.ignite.configuration.ClientConfiguration;

import javax.inject.Singleton;

/**
 * Abstract version of the a factory class for creating Apache Ignite Thin clients.
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@Factory
public class IgniteClientFactory {

    @Bean
    @Singleton
    public ClientConfiguration clientConfiguration() {
        System.out.println("==I AM RUN ==");
        ClientConfiguration cfg = new ClientConfiguration().setAddresses("127.0.0.1:10800");
        return cfg;
    }
}
