package org.gfs.workshop.camunda.sidecar.repository;

import java.util.Optional;

public interface BusinessDataRepository<T> {
    Optional<T> select(String id);

    void insert(T data);
}
