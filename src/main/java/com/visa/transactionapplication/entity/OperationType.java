package com.visa.transactionapplication.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "operation_types")
@Getter
@Setter
@NoArgsConstructor
public class OperationType {

    @Id
    @Column(name = "operation_type_id")
    private Long operationTypeId;

    @Column(name = "description", nullable = false, length = 50)
    private String description;

    @Column(name = "requires_negative", nullable = false)
    private boolean requiresNegative;
}
