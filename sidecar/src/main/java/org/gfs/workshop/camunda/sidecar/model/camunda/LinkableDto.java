package org.gfs.workshop.camunda.sidecar.model.camunda;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;

/**
 * LinkableDto
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class LinkableDto extends ArrayList<AtomLink> {

}
