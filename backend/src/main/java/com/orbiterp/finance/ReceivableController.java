package com.orbiterp.finance;

import com.orbiterp.finance.FinanceDtos.AgingReport;
import com.orbiterp.finance.FinanceDtos.ReceivableResponse;
import com.orbiterp.security.Access;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/finance/receivables")
@PreAuthorize(Access.FINANCE)
@RequiredArgsConstructor
public class ReceivableController {

    private final ReceivableService receivableService;

    @GetMapping
    public List<ReceivableResponse> list(@RequestParam(required = false) ReceivableStatus status) {
        return receivableService.list(status);
    }

    @GetMapping("/aging")
    public AgingReport aging(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOf) {
        return receivableService.aging(asOf);
    }
}
