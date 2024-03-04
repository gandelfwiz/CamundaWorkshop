package org.gfs.workshop.camunda.sidecar.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.hazelcast.config.IndexConfig;
import com.hazelcast.config.IndexType;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import lombok.RequiredArgsConstructor;
import org.gfs.workshop.camunda.sidecar.model.entity.EnrollmentDataEntity;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Profile("OUTGOING")
public class HazelcastDataLoader implements CommandLineRunner {
    private final HazelcastInstance hazelcastInstance;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        IMap<String, EnrollmentDataEntity> map =
                hazelcastInstance.getMap("EnrollmentDataEntity");
        map.destroy();
        IndexConfig config = new IndexConfig(IndexType.HASH, "customerId");
        map.addIndex(config);
        new ObjectMapper(new YAMLFactory())
                .readValue(getClass().getClassLoader().getResourceAsStream("data.yaml"),
                        new TypeReference<Map<String, List<EnrollmentDataEntity>>>() {
                        })
                .get("enrollments")
                .forEach(record -> {
                    map.computeIfAbsent(record.getId(), (key) -> record);
                });
    }
}
