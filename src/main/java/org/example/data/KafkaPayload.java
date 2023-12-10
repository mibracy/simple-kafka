package org.example.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KafkaPayload {

   @Pattern(regexp = "^[0-9a-zA-Z]{5}-[0-9a-zA-Z]{4}-[0-9a-zA-Z]{4}$")
   @NotBlank
   private String topic;

   @NotBlank
   private String key;

   @NotBlank
   private String value;

}
