import io.micronaut.context.ApplicationContext
import io.micronaut.ignite.configuration.IgniteThinClientConfiguration
import io.micronaut.inject.qualifiers.Qualifiers
import org.apache.ignite.client.IgniteClient
import org.apache.ignite.client.SslMode
import org.apache.ignite.transactions.TransactionConcurrency
import org.apache.ignite.transactions.TransactionIsolation
import org.testcontainers.containers.GenericContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Retry
import spock.lang.Shared
import spock.lang.Specification

@Testcontainers
@Retry
class IgniteThinClientConfigurationSpec extends Specification {
    final static String IGNITE_VERSION = System.getProperty("igniteVersion")

    @Shared
    GenericContainer ignite = new GenericContainer("apacheignite/ignite:${IGNITE_VERSION}")
        .withExposedPorts(10800)

    void "test ignite thin client instance is created"() {
        given:
        ApplicationContext ctx = ApplicationContext.run([
            "ignite.enabled"                       : true,
            "ignite.thin-clients.default.addresses": ["127.0.0.1:${ignite.getMappedPort(10800)}"],
            "ignite.thin-clients.other.addresses"  : ["127.0.0.1:${ignite.getMappedPort(10800)}"]
        ])

        when:
        IgniteClient defaultInstance = ctx.getBean(IgniteClient.class, Qualifiers.byName("default"))
        IgniteClient otherInstance = ctx.getBean(IgniteClient.class, Qualifiers.byName("other"))

        then:
        defaultInstance != null
        otherInstance != null
    }

    void "test ignite thin client configuration"() {
        given:
        ApplicationContext ctx = ApplicationContext.run(ApplicationContext, [
            "ignite.enabled"                                                       : true,
            "ignite.thin-clients.default.addresses"                                : ["localhost:1080"],
            "ignite.thin-clients.default.ssl-mode"                                 : "REQUIRED",
            "ignite.thin-clients.default.ssl-client-certificate-key-store-password": "password",
            "ignite.thin-clients.default.timeout"                                  : 5000,
            "ignite.thin-clients.default.send-buffer-size"                         : 200
        ])
        when:
        IgniteThinClientConfiguration clientConfiguration = ctx.getBean(IgniteThinClientConfiguration.class)

        then:
        clientConfiguration != null
        clientConfiguration.getConfiguration().sslMode == SslMode.REQUIRED
        clientConfiguration.getConfiguration().sslClientCertificateKeyStorePassword == "password"
        clientConfiguration.getConfiguration().timeout == 5000
        clientConfiguration.getConfiguration().sendBufferSize == 200
    }

    void "test ignite cache disabled"() {
        when:
        ApplicationContext ctx = ApplicationContext.run(ApplicationContext, [
            "ignite.enabled"                       : false,
            "ignite.thin-clients.default.addresses": ["localhost:1080"],
            "ignite.thin-clients.default.client"   : "test",
        ])

        then:
        !ctx.containsBean(IgniteThinClientConfigurationSpec.class)
        !ctx.containsBean(IgniteThinClientConfigurationSpec.class)
    }

    void "test ignite thin client transaction configuration"() {
        given:
        ApplicationContext ctx = ApplicationContext.run(ApplicationContext, [
            "ignite.enabled"                                                              : true,
            "ignite.thin-clients.default.addresses"                                       : ["localhost:1080"],
            "ignite.thin-clients.default.transaction-configuration.default-tx-isolation"  : "REPEATABLE_READ",
            "ignite.thin-clients.default.transaction-configuration.default-tx-concurrency": "PESSIMISTIC",
            "ignite.thin-clients.default.transaction-configuration.default-tx-timeout"    : 5000,
        ])
        when:
        IgniteThinClientConfiguration clientConfiguration = ctx.getBean(IgniteThinClientConfiguration.class)

        then:
        clientConfiguration != null
        clientConfiguration.getTransaction().defaultTxIsolation == TransactionIsolation.REPEATABLE_READ
        clientConfiguration.getTransaction().defaultTxTimeout == 5000
        clientConfiguration.getTransaction().defaultTxConcurrency == TransactionConcurrency.PESSIMISTIC
    }
}
