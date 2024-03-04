package org.gfs.workshop.camunda.sidecar.repository.impl;

import lombok.RequiredArgsConstructor;
import org.gfs.workshop.camunda.sidecar.model.entity.BusinessDataEntity;
import org.gfs.workshop.camunda.sidecar.repository.HazelcastBusinessDataRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BusinessDataRepositoryBusiness extends HazelcastBusinessDataRepository<BusinessDataEntity> {

    @Override
    public void insert(BusinessDataEntity entity) {
        super.put(entity.getId(), entity);
    }

    @Override
    public Optional<BusinessDataEntity> select(String id) {
        return Optional.ofNullable(get(id));
    }
}
