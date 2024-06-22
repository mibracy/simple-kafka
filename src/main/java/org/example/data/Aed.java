package org.example.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Aed {

   private String name;
   private String date;
}
/* AVRO Schema
{
  "type" : "record",
  "name" : "Aed",
  "namespace" : "org.example.data",
  "fields" : [ {
    "name" : "date",
    "type" : "string"
  }, {
    "name" : "name",
    "type" : "string"
  } ]
}
 */