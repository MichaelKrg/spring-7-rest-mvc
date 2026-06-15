package guru.springframework.spring7restmvc.model;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class BeerOrderLineUpdateDTO {
    // for the update we can give the id (to update an existing line)
    // or leave it empty (add new line)
    private UUID id;

    private UUID beerId;

    @Min(value = 1, message = "Quantity ordered must be greater than 0")
    private Integer orderQuantity;
    private Integer quantityAllocated;
}
