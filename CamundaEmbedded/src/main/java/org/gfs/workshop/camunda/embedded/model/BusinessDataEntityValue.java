package org.gfs.workshop.camunda.embedded.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Data
public class BusinessDataEntityValue extends BusinessDataEntity {
    @JsonProperty("data")
    private List<PayloadData> dataList;

    public static BusinessDataEntityValue from(BusinessDataEntity businessDataEntity) {
        if (Objects.isNull(businessDataEntity)) return null;
        try {
            BusinessDataEntityValue result = new BusinessDataEntityValue();
            result.setDataList(new ObjectMapper().readValue(businessDataEntity.getData(),
                    new TypeReference<>() {
                    }));
            result.setId(businessDataEntity.getId());
            return result;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
