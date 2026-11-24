package com.orbiterp.common.events;

import java.math.BigDecimal;

public record OrderLineQuantity(Long itemId, BigDecimal quantity) {
}
