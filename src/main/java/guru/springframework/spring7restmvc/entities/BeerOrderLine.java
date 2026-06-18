package guru.springframework.spring7restmvc.entities;

import java.sql.Timestamp;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import guru.springframework.spring6restmvcapi.model.BeerOrderLineStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeerOrderLine {

    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 36, columnDefinition = "varchar(36)", updatable = false, nullable = false)
    private UUID id;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp createdDate;

    @UpdateTimestamp
    private Timestamp lastModifiedDate;

    public boolean isNew() {
        return this.id == null;
    }

    @ManyToOne
    private Beer beer;

    @ManyToOne
    private BeerOrder beerOrder;

    @Builder.Default
    @Min(value = 1, message = "Quantity ordered must be more than 0")
    private Integer orderQuantity = 0;
    
    @Builder.Default
    private Integer quantityAllocated = 0;

    @Builder.Default
    private BeerOrderLineStatus status = BeerOrderLineStatus.NEW;

}
