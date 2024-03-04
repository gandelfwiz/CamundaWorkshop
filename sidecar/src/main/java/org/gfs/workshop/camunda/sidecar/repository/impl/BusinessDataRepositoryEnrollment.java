package org.gfs.workshop.camunda.sidecar.repository.impl;

import lombok.RequiredArgsConstructor;
import org.gfs.workshop.camunda.sidecar.model.entity.EnrollmentDataEntity;
import org.gfs.workshop.camunda.sidecar.repository.HazelcastBusinessDataRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BusinessDataRepositoryEnrollment extends HazelcastBusinessDataRepository<EnrollmentDataEntity> {

    @Override
    public Optional<EnrollmentDataEntity> select(String id) {
        return Optional.ofNullable(getByField("customerId", id));
    }

    @Override
    public void insert(EnrollmentDataEntity data) {
        put(data.getId(), data);
    }
}
