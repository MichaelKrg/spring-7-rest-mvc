package guru.springframework.spring7restmvc.model;

import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class BeerOrderCreateDTO {
    private String customerRef;

    @NotNull
    private UUID customerId;

    private Set<BeerOrderLineCreateDTO> beerOrderLines;
}
