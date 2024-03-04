package org.gfs.workshop.camunda.sidecar.repository;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.query.Predicates;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.ParameterizedType;

public abstract class HazelcastBusinessDataRepository<T> implements BusinessDataRepository<T> {

    @Autowired
    private HazelcastInstance hazelcastInstance;

    // assign to the map the name of the type class
    private IMap<String, T> retrieveMap() {
        String className = ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0]
                .getTypeName();
        return hazelcastInstance.getMap(className.substring(
                className.lastIndexOf(".") + 1)
        );
    }

    protected T put(String key, T value) {
        retrieveMap()
                .put(key, value);
        return value;
    }

    protected T get(String key) {
        return retrieveMap()
                .get(key);
    }

    protected <R extends Comparable<R>> T getByField(String fieldName, R value) {
        return retrieveMap()
                .values(Predicates.equal(fieldName, value))
                .stream()
                .findFirst().orElse(null);
    }
}
