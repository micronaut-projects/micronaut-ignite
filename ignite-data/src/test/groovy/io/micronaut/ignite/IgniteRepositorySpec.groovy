package io.micronaut.ignite

import io.micronaut.data.tck.repositories.AuthorRepository
import io.micronaut.data.tck.repositories.BookDtoRepository
import io.micronaut.data.tck.repositories.CityRepository
import io.micronaut.data.tck.repositories.CompanyRepository
import io.micronaut.data.tck.repositories.CountryRegionCityRepository
import io.micronaut.data.tck.repositories.CountryRepository
import io.micronaut.data.tck.repositories.FaceRepository
import io.micronaut.data.tck.repositories.NoseRepository
import io.micronaut.data.tck.repositories.PersonRepository
import io.micronaut.data.tck.repositories.RegionRepository
import io.micronaut.data.tck.repositories.RoleRepository
import io.micronaut.data.tck.repositories.UserRepository
import io.micronaut.data.tck.repositories.UserRoleRepository
import io.micronaut.data.tck.tests.AbstractRepositorySpec

class IgniteRepositorySpec extends AbstractRepositorySpec {

    @Override
    Object getProperty(String propertyName) {
        return super.getProperty(propertyName)
    }

    @Override
    PersonRepository getPersonRepository() {
        return null
    }

    @Override
    io.micronaut.data.tck.repositories.BookRepository getBookRepository() {
        return null
    }

    @Override
    AuthorRepository getAuthorRepository() {
        return null
    }

    @Override
    CompanyRepository getCompanyRepository() {
        return null
    }

    @Override
    BookDtoRepository getBookDtoRepository() {
        return null
    }

    @Override
    CountryRepository getCountryRepository() {
        return null
    }

    @Override
    CityRepository getCityRepository() {
        return null
    }

    @Override
    RegionRepository getRegionRepository() {
        return null
    }

    @Override
    CountryRegionCityRepository getCountryRegionCityRepository() {
        return null
    }

    @Override
    NoseRepository getNoseRepository() {
        return null
    }

    @Override
    FaceRepository getFaceRepository() {
        return null
    }

    @Override
    UserRepository getUserRepository() {
        return null
    }

    @Override
    UserRoleRepository getUserRoleRepository() {
        return null
    }

    @Override
    RoleRepository getRoleRepository() {
        return null
    }

    @Override
    Map<String, String> getProperties() {
        return null
    }
}
