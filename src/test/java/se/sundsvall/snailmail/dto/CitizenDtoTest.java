package se.sundsvall.snailmail.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CitizenDtoTest {

    @Test
    void testBuilder() {

        var dto = CitizenDto.builder()
                .withPersonId("somePersonId")
                .withGivenName("someGivenName")
                .withLastName("someLastName")
                .withStreet("someStreetName")
                .withCareOf("some C/O")
                .withApartment("someApartmentNumber")
                .withPostalCode("somePostalCode")
                .withCity("someCity")
                .build();

        assertThat(dto).isNotNull();
        assertThat(dto.getPersonId()).isEqualTo("somePersonId");
        assertThat(dto.getGivenName()).isEqualTo("someGivenName");
        assertThat(dto.getLastName()).isEqualTo("someLastName");
        assertThat(dto.getStreet()).isEqualTo("someStreetName");
        assertThat(dto.getCareOf()).isEqualTo("some C/O");
        assertThat(dto.getApartment()).isEqualTo("someApartmentNumber");
        assertThat(dto.getPostalCode()).isEqualTo("somePostalCode");
        assertThat(dto.getCity()).isEqualTo("someCity");
    }
}