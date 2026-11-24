package com.orbiterp.common.sequence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hands out gap-free document numbers per tenant, such as SO-00001. The sequence row is locked
 * until the caller's transaction ends, so two orders saved at once never get the same number.
 */
@Service
@RequiredArgsConstructor
public class DocumentNumberService {

    private final DocumentSequenceRepository sequences;

    @Transactional
    public String next(String sequenceName, String prefix) {
        DocumentSequence sequence = sequences.findByName(sequenceName).orElseGet(() -> {
            DocumentSequence created = new DocumentSequence();
            created.setName(sequenceName);
            created.setNextValue(1);
            return sequences.save(created);
        });
        long value = sequence.getNextValue();
        sequence.setNextValue(value + 1);
        return "%s-%05d".formatted(prefix, value);
    }
}
