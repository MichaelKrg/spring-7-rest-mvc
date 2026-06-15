package guru.springframework.spring7restmvc.model;

import java.sql.Timestamp;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonDeserialize(builder = BeerOrderDTO.BeerOrderDTOBuilder.class)
public class BeerOrderDTO {

    // Relation methods
    public void setCustomer(CustomerDTO customer) {
        this.customer = customer;
    }

    public void setBeerOrderShipment(BeerOrderShipmentDTO beerOrderShipment) {
        this.beerOrderShipment = beerOrderShipment;
    }

    // normal attributes
    @JsonProperty("id")
    private UUID id;

    @JsonProperty("version")
    private Long version;

    @JsonProperty("createdDate")
    private Timestamp createdDate;

    @JsonProperty("lastModifiedDate")
    private Timestamp lastModifiedDate;

    @JsonProperty("customerRef")
    private String customerRef;

    // relation attributes
    private CustomerDTO customer;
    private Set<BeerOrderLineDTO> beerOrderLines;
    private BeerOrderShipmentDTO beerOrderShipment;

}
