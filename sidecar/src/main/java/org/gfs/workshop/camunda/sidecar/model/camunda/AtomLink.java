package org.gfs.workshop.camunda.sidecar.model.camunda;

import lombok.Data;

/**
 * AtomLink
 */
@Data
public class AtomLink {
    private String rel = null;
    private String href = null;
    private String method = null;
}
