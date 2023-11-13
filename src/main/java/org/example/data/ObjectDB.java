package org.example.data;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

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
