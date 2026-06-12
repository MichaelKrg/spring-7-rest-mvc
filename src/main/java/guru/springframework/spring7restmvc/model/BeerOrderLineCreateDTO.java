package guru.springframework.spring7restmvc.model;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class BeerOrderLineCreateDTO {
    @NotNull
    private UUID beerId;

    @Min(value = 1, message = "Quantity ordered must be greater than 0")
    private Integer orderQuantity;
}
