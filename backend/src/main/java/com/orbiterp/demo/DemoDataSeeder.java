package com.orbiterp.demo;

import com.orbiterp.auth.AuthDtos.CreateUserRequest;
import com.orbiterp.auth.AuthDtos.SignupRequest;
import com.orbiterp.auth.Role;
import com.orbiterp.auth.SignupService;
import com.orbiterp.auth.UserService;
import com.orbiterp.inventory.InventoryDtos.ItemRequest;
import com.orbiterp.inventory.InventoryDtos.ItemResponse;
import com.orbiterp.inventory.InventoryDtos.StockMoveRequest;
import com.orbiterp.inventory.ItemService;
import com.orbiterp.inventory.StockMoveType;
import com.orbiterp.inventory.StockService;
import com.orbiterp.sales.CustomerService;
import com.orbiterp.sales.SalesDtos.CustomerRequest;
import com.orbiterp.tenant.TenantContext;
import com.orbiterp.tenant.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Creates two demo workspaces, "acme" and "globex", for local development and the manual test
 * script in backend/README.md. Only runs when orbiterp.demo-data.enabled is true (DEMO_DATA_ENABLED).
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "orbiterp.demo-data.enabled", havingValue = "true")
@RequiredArgsConstructor
public class DemoDataSeeder implements ApplicationRunner {

    /** Test password of every demo account. Never enable demo data in production. */
    static final String DEMO_PASSWORD = "demo1234";

    private final TenantService tenants;
    private final SignupService signup;
    private final UserService users;
    private final ItemService items;
    private final StockService stock;
    private final CustomerService customers;

    @Override
    public void run(ApplicationArguments args) {
        seed(new TenantSeed("acme", "Acme Trading",
                List.of(new ItemSeed("LAP-14", "Laptop 14 inch", "1200.00", "25"),
                        new ItemSeed("MON-27", "Monitor 27 inch", "320.00", "40"),
                        new ItemSeed("KEY-01", "Mechanical keyboard", "85.00", "100")),
                List.of("Northwind Retail", "Contoso Ltd")));
        seed(new TenantSeed("globex", "Globex Distribution",
                List.of(new ItemSeed("PAL-01", "Wooden pallet", "15.00", "500"),
                        new ItemSeed("BOX-L", "Large shipping box", "2.50", "2000")),
                List.of("Initech", "Umbrella Supplies")));
    }

    private void seed(TenantSeed seed) {
        if (tenants.findByCode(seed.code()).isPresent()) {
            log.info("Demo workspace '{}' already exists", seed.code());
            return;
        }
        String domain = "@" + seed.code() + ".test";
        signup.signup(new SignupRequest(seed.code(), seed.name(), seed.name() + " Admin", "admin",
                "admin" + domain, DEMO_PASSWORD));
        Long tenantId = tenants.findByCode(seed.code()).orElseThrow().getId();

        TenantContext.runAs(tenantId, () -> {
            users.create(new CreateUserRequest("sales", "sales" + domain, "Sales Rep", DEMO_PASSWORD, Set.of(Role.SALES_REP)));
            users.create(new CreateUserRequest("warehouse", "warehouse" + domain, "Warehouse Clerk", DEMO_PASSWORD, Set.of(Role.WAREHOUSE_CLERK)));
            users.create(new CreateUserRequest("accountant", "accountant" + domain, "Accountant", DEMO_PASSWORD, Set.of(Role.ACCOUNTANT)));
            for (ItemSeed item : seed.items()) {
                ItemResponse created = items.create(new ItemRequest(item.sku(), item.name(), null, "pcs",
                        new BigDecimal(item.price()), true));
                stock.recordManualMove(new StockMoveRequest(StockMoveType.RECEIPT, created.id(), null,
                        new BigDecimal(item.openingStock()), "OPENING", "Opening stock"));
            }
            seed.customers().forEach(name -> customers.create(new CustomerRequest(name, null, null, null)));
        });
        log.info("Created demo workspace '{}' with users admin, sales, warehouse and accountant", seed.code());
    }

    private record TenantSeed(String code, String name, List<ItemSeed> items, List<String> customers) {
    }

    private record ItemSeed(String sku, String name, String price, String openingStock) {
    }
}
