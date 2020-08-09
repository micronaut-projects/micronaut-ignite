package io.micronaut.ignite

import io.micronaut.data.tck.repositories.AuthorRepository
import io.micronaut.data.tck.repositories.BookDtoRepository
import io.micronaut.data.tck.repositories.BookRepository
import io.micronaut.data.tck.repositories.CityRepository
import io.micronaut.data.tck.repositories.CompanyRepository
import io.micronaut.data.tck.repositories.CountryRegionCityRepository
import io.micronaut.data.tck.repositories.CountryRepository
import io.micronaut.data.tck.repositories.FaceRepository
import io.micronaut.data.tck.repositories.MealRepository
import io.micronaut.data.tck.repositories.NoseRepository
import io.micronaut.data.tck.repositories.PersonRepository
import io.micronaut.data.tck.repositories.RegionRepository
import io.micronaut.data.tck.repositories.RoleRepository
import io.micronaut.data.tck.repositories.UserRepository
import io.micronaut.data.tck.repositories.UserRoleRepository
import io.micronaut.data.tck.tests.AbstractRepositorySpec
import io.micronaut.ignite.repositories.IgniteAuthorRepository
import io.micronaut.ignite.repositories.IgniteBookDtoRepository
import io.micronaut.ignite.repositories.IgniteBookRepository
import io.micronaut.ignite.repositories.IgniteCarRepository
import io.micronaut.ignite.repositories.IgniteCityRepository
import io.micronaut.ignite.repositories.IgniteCompanyRepository
import io.micronaut.ignite.repositories.IgniteCountryRegionCityRepository
import io.micronaut.ignite.repositories.IgniteCountryRepository
import io.micronaut.ignite.repositories.IgniteFaceRepository
import io.micronaut.ignite.repositories.IgniteMealRepository
import io.micronaut.ignite.repositories.IgniteNoseRepository
import io.micronaut.ignite.repositories.IgnitePersonRepository
import io.micronaut.ignite.repositories.IgniteRegionRepository
import io.micronaut.ignite.repositories.IgniteRoleRepository
import io.micronaut.ignite.repositories.IgniteUserRepository
import io.micronaut.ignite.repositories.IgniteUserRoleRepository
import org.testcontainers.containers.GenericContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.AutoCleanup
import spock.lang.Retry
import spock.lang.Shared

@Testcontainers
@Retry
class IgniteRepositorySpec extends AbstractRepositorySpec implements IgniteTestPropertyProvider {
    final static String IGNITE_VERSION = System.getProperty("igniteVersion")

    @Shared
    @AutoCleanup
    GenericContainer ignite = new GenericContainer("apacheignite/ignite:${IGNITE_VERSION}")
        .withExposedPorts(47500, 47100)

    @Shared
    IgnitePersonRepository pr = context.getBean(IgnitePersonRepository)
    @Shared
    IgniteBookRepository br = context.getBean(IgniteBookRepository)
    @Shared
    IgniteAuthorRepository ar = context.getBean(IgniteAuthorRepository)
    @Shared
    IgniteCompanyRepository cr = context.getBean(IgniteCompanyRepository)
    @Shared
    IgniteBookDtoRepository dto = context.getBean(IgniteBookDtoRepository)
    @Shared
    IgniteCountryRepository country = context.getBean(IgniteCountryRepository)
    @Shared
    IgniteCountryRegionCityRepository countrycr = context.getBean(IgniteCountryRegionCityRepository)
    @Shared
    IgniteCityRepository cityr = context.getBean(IgniteCityRepository)
    @Shared
    IgniteRegionRepository regr = context.getBean(IgniteRegionRepository)
    @Shared
    IgniteFaceRepository fr = context.getBean(IgniteFaceRepository)
    @Shared
    IgniteNoseRepository nr = context.getBean(IgniteNoseRepository)
    @Shared
    IgniteCarRepository carRepo = context.getBean(IgniteCarRepository)
    @Shared
    IgniteUserRoleRepository userRoleRepo = context.getBean(IgniteUserRoleRepository)
    @Shared
    IgniteRoleRepository roleRepo = context.getBean(IgniteRoleRepository)
    @Shared
    IgniteUserRepository userRepo = context.getBean(IgniteUserRepository)
    @Shared
    IgniteMealRepository mealRepo = context.getBean(IgniteMealRepository)

    @Override
    NoseRepository getNoseRepository() {
        return nr
    }

    @Override
    FaceRepository getFaceRepository() {
        return fr
    }

    @Override
    IgnitePersonRepository getPersonRepository() {
        return pr
    }

    @Override
    BookRepository getBookRepository() {
        return br
    }

    @Override
    AuthorRepository getAuthorRepository() {
        return ar
    }

    @Override
    CompanyRepository getCompanyRepository() {
        return cr
    }

    @Override
    BookDtoRepository getBookDtoRepository() {
        return dto
    }

    @Override
    CountryRepository getCountryRepository() {
        return country
    }

    @Override
    CityRepository getCityRepository() {
        return cityr
    }

    @Override
    RegionRepository getRegionRepository() {
        return regr
    }

    @Override
    CountryRegionCityRepository getCountryRegionCityRepository() {
        return countrycr
    }

    @Override
    UserRoleRepository getUserRoleRepository() {
        return userRoleRepo
    }

    @Override
    RoleRepository getRoleRepository() {
        return roleRepo
    }

    @Override
    UserRepository getUserRepository() {
        return userRepo
    }

}
