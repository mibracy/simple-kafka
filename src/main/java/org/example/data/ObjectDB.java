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
    public ObjectDB(Iterable<H2User> all) {
        if (all != null){
            data.add(all);
        }
    }
}
