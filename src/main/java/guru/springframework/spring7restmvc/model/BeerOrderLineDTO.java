package guru.springframework.spring7restmvc.model;

import java.sql.Timestamp;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import jakarta.validation.constraints.Min;

@Data
@Builder
@JsonDeserialize(builder = BeerOrderLineDTO.BeerOrderLineDTOBuilder.class)
public class BeerOrderLineDTO {
    @JsonProperty("id")
    private UUID id;

    @JsonProperty("version")
    private Long version;

    @JsonProperty("createdDate")
    private Timestamp createdDate;

    @JsonProperty("lastModifiedDate")
    private Timestamp lastModifiedDate;

    @JsonProperty("orderQuantity")
    @Min(value = 1, message = "Quantity ordered must be greater than 0")
    private Integer orderQuantity;

    @JsonProperty("quantityAllocated")
    private Integer quantityAllocated;

    @JsonProperty("status")
    private BeerOrderLineStatus status;

    // relation attributes
    private BeerDTO beer;
}
