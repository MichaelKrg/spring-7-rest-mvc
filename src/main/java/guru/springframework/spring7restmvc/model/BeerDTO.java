package guru.springframework.spring7restmvc.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Created by jt, Spring Framework Guru.
 */
@JsonDeserialize(builder = BeerDTO.BeerDTOBuilder.class)
@Builder
@Data
public class BeerDTO {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("version")
    private Integer version;

    @JsonProperty("beerName")
    @NotNull
    @NotBlank
    private String beerName;

    @JsonProperty("beerStyle")
    @NotNull
    private BeerStyle beerStyle;

    @JsonProperty("upc")
    @NotNull
    @NotBlank
    private String upc;

    @JsonProperty("quantityOnHand")
    private Integer quantityOnHand;

    @JsonProperty("price")
    @NotNull
    @Digits(integer = 4, fraction = 2)
    @Positive
    private BigDecimal price;

    private LocalDateTime createdDate;
    private LocalDateTime updateDate;
}
