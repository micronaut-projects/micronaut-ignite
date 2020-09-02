package io.micronaut.ignite

import io.micronaut.context.ApplicationContext
import org.apache.ignite.configuration.DataStorageConfiguration
import org.apache.ignite.configuration.IgniteConfiguration
import spock.lang.Specification

//TODO: figure out data storage configuration
class IgniteDataStorageConfigurationSpec extends Specification {

//    def "test ignite data-storage configuration"() {
//        given:
//        ApplicationContext ctx = ApplicationContext.run([
//            "ignite.enabled"                                          : true,
//            "ignite.data-storage-configuration.enabled": "true",
//            "ignite.data-storage-configuration.system-region-max-size": 7000,
//            "ignite.data-storage-configuration.default-data-region-configuration.name" : "one",
//            "ignite.data-storage-configuration.regions[0].name" : "20MB_Region_Eviction",
//            "ignite.data-storage-configuration.regions[1].name" : "25MB_Region_Swapping",
//        ])
//        when:
//        IgniteConfiguration configuration = ctx.getBean(IgniteConfiguration.class)
//
//        then:
//        configuration != null
//        DataStorageConfiguration dataStorageConfiguration = configuration.getDataStorageConfiguration();
//        dataStorageConfiguration != null;
//        dataStorageConfiguration.getDefaultDataRegionConfiguration().name == "one"
//        dataStorageConfiguration.systemRegionMaxSize == 7000
//        dataStorageConfiguration.getDataRegionConfigurations().any({k -> k.name == "20MB_Region_Eviction"})
//        dataStorageConfiguration.getDataRegionConfigurations().any({k -> k.name == "25MB_Region_Swapping"})
//    }

}
