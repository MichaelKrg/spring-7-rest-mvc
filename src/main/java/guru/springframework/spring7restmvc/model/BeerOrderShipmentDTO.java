package guru.springframework.spring7restmvc.model;

import java.sql.Timestamp;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonDeserialize(builder = BeerOrderShipmentDTO.BeerOrderShipmentDTOBuilder.class)
public class BeerOrderShipmentDTO {
    @JsonProperty("id")
    private UUID id;

    @JsonProperty("version")
    private Long version;

    @JsonProperty("createdDate")
    private Timestamp createdDate;

    @JsonProperty("lastModifiedDate")
    private Timestamp lastModifiedDate;

    @JsonProperty("trackingNumber")
    @NotBlank
    private String trackingNumber;
}
