package org.gfs.workshop.camunda.embedded.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;

import java.util.Objects;

/**
 * business data composed by key and value
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2024-03-07T03:00:20.896007+01:00[Europe/Rome]")
public class PayloadData {

    private String key;

    private PayloadDataValue value;

    public PayloadData key(String key) {
        this.key = key;
        return this;
    }

    /**
     * name of the data property
     *
     * @return key
     */

    @JsonProperty("key")
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public PayloadData value(PayloadDataValue value) {
        this.value = value;
        return this;
    }

    /**
     * Get value
     *
     * @return value
     */
    @Valid

    @JsonProperty("value")
    public PayloadDataValue getValue() {
        return value;
    }

    public void setValue(PayloadDataValue value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PayloadData payloadData = (PayloadData) o;
        return Objects.equals(this.key, payloadData.key) &&
                Objects.equals(this.value, payloadData.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PayloadData {\n");
        sb.append("    key: ").append(toIndentedString(key)).append("\n");
        sb.append("    value: ").append(toIndentedString(value)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

