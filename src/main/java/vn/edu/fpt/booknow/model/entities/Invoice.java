package vn.edu.fpt.booknow.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "Invoice", schema = "dbo", uniqueConstraints = {
        @UniqueConstraint(name = "UQ_Invoice_Number", columnNames = {"invoice_number"})
})
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Size(max = 50)
    @Nationalized
    @Column(name = "invoice_number", length = 50)
    private String invoiceNumber;

    @NotNull
    @ColumnDefault("sysdatetime()")
    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

}