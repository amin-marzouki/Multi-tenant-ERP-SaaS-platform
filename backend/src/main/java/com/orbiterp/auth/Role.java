package com.orbiterp.auth;

import com.orbiterp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/** A tenant-scoped role. Every tenant gets the four MVP roles when it is created. */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Role extends BaseEntity {

    public static final String ADMIN = "ADMIN";
    public static final String SALES_REP = "SALES_REP";
    public static final String WAREHOUSE_CLERK = "WAREHOUSE_CLERK";
    public static final String ACCOUNTANT = "ACCOUNTANT";
    public static final List<String> ALL = List.of(ADMIN, SALES_REP, WAREHOUSE_CLERK, ACCOUNTANT);

    @Column(nullable = false, length = 100)
    private String name;

    public Role(String name) {
        this.name = name;
    }
}
