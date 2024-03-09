package org.gfs.workshop.camunda.embedded.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusinessDataEntity {
    private String id;
    @JsonIgnore
    private String data;
}
