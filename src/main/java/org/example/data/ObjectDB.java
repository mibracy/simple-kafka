package org.example.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ObjectDB {

    private ArrayList<Object> data = new ArrayList<>();

    // Copy and modify the Iterable<?> for any new types
    public ObjectDB(Iterable<Users> all) {
        if (all != null){
            data.add(all);
        }
    }
}

/* AVRO Schema
{
  "type" : "record",
  "name" : "ObjectDB",
  "namespace" : "org.example.data",
  "fields" : [ {
    "name" : "data",
    "type" : {
      "type" : "array",
      "items" : {
        "type" : "record",
        "name" : "Object",
        "namespace" : "java.lang",
        "fields" : [ ]
      },
      "java-class" : "java.util.ArrayList"
    }
  } ]
}
 */