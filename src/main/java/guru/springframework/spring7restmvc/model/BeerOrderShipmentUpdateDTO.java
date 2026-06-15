package guru.springframework.spring7restmvc.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class BeerOrderShipmentUpdateDTO {
    @NotBlank
    private String trackingNumber;
}
