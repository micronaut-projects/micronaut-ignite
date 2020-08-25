package io.micronaut.ignite

import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.ignite.annotation.IgniteRef
import io.micronaut.test.annotation.MicronautTest
import org.apache.ignite.IgniteCache
import org.testcontainers.containers.GenericContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.AutoCleanup
import spock.lang.Retry
import spock.lang.Shared;
import spock.lang.Specification

import javax.inject.Inject;

@MicronautTest
@Property( name  = "ignite.enabled", value= "true")
@Property( name  = "ignite.communication-spi.local-port", value= "10800.")
@Testcontainers
@Retry
class IgniteRefSpec extends Specification {
    final static String IGNITE_VERSION = System.getProperty("igniteVersion")

    @Shared
    @AutoCleanup
    GenericContainer ignite = new GenericContainer("apacheignite/ignite:${IGNITE_VERSION}")
        .withExposedPorts(47500, 47100)

    @Inject
    @Client("/ignite")
    HttpClient client;

    @Inject
    @IgniteRef("t1")
    IgniteCache<String,String> cache;

    def "Ignite ref cache sample controller"() {
        given:
        HttpResponse<String> response = client.toBlocking().exchange("/k1/masdifnawek", String.class);

        expect:
        response.body() == "masdifnawek"
        cache.get('k1') == "masdifnawek"
    }

    def "Ignite ref cache spec"() {
        given:
        cache.put("res","9malkmdim")

        expect:
        cache.get("res") == "9malkmdim"
    }
}
