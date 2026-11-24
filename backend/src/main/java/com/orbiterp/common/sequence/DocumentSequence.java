package com.orbiterp.common.sequence;

import com.orbiterp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** The next number of one kind of document (quote, order, invoice...) for one tenant. */
@Entity
@Table(name = "document_sequences")
@Getter
@Setter
public class DocumentSequence extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "next_value", nullable = false)
    private long nextValue;
}
