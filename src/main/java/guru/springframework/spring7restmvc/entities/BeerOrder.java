package guru.springframework.spring7restmvc.entities;

import java.sql.Timestamp;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Version;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Builder
public class BeerOrder {

    public BeerOrder(UUID id, Long version, Timestamp createdDate, Timestamp lastModifiedDate, String customerRef,
                     Customer customer, Set<BeerOrderLine> beerOrderLines, BeerOrderShipment beerOrderShipment) {
        this.id = id;
        this.version = version;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
        this.customerRef = customerRef;
        // builder calls this constructor, which unlike a generated constructor with AllArgsConstructor
        // calls setCustomer so that the backward reference is properly set up
        this.setCustomer(customer);
        this.setBeerOrderLines(beerOrderLines);
        this.setBeerOrderShipment(beerOrderShipment);
    }

    // Helper methods for the relations to ensure that
    // the backward relation is set correctly
    public void setCustomer(Customer customer) {
        this.customer = customer;
        if (!customer.getBeerOrders().contains(this)) {
            customer.getBeerOrders().add(this);
        }
    }

    public void setBeerOrderShipment(BeerOrderShipment beerOrderShipment) {
        this.beerOrderShipment = beerOrderShipment;
        if (beerOrderShipment != null && beerOrderShipment.getBeerOrder() != this) {
            beerOrderShipment.setBeerOrder(this);
        }
    }

    public void setBeerOrderLines(Set<BeerOrderLine> orderLines) {
        if(orderLines != null) {
            // make sure backward mapping is correctly set
            orderLines.forEach(orderLine -> orderLine.setBeerOrder(this));
        }
        this.beerOrderLines = orderLines;
    }

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

    private String customerRef;

    @ManyToOne
    private Customer customer;

    @OneToMany(mappedBy = "beerOrder",
            cascade= CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private Set<BeerOrderLine> beerOrderLines;

    @OneToOne(mappedBy = "beerOrder",
            cascade= CascadeType.PERSIST)
    private BeerOrderShipment beerOrderShipment;
}
