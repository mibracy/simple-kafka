package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.data.H2User;
import org.example.data.KafkaPayload;
import org.example.data.ObjectDB;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class FileExportService {

    @Value("${export.path:../temp/}")
    private String exportPath;
    private final List<String> types = Arrays.asList("json", "xml");
    private final ProducerService producer;

    @Autowired
    public FileExportService(ProducerService producer) {
        this.producer = producer;
    }

    public void export(String name, ObjectDB odb) {
        AtomicReference<String> converted = new AtomicReference<>("");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");

        // Converts Object into desired format
        types.forEach(type -> {
            if ("json".equals(type)){
                ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                try {
                    converted.set(ow.writeValueAsString(odb));
                } catch (JsonProcessingException e) {
                    converted.set("");
                    log.error(String.valueOf(e));
                }
            } else if ("xml".equals(type)) {
                try {
                    converted.set(convertObjectToxStreamXML(odb, name.replace("data/","")));
                } catch (UnsupportedEncodingException e) {
                    converted.set("");
                    log.error(String.valueOf(e));
                }
            }

            // This will place files in the CATALINA_HOME temp directory
            String fileName =  exportPath + name + "_" + sdf.format(new Date()) + "." + type;

            try {
                if (StringUtils.isNotBlank(converted.get())) {
                    writeUsingOutputStream(converted.get(), fileName);
                }
            } catch (IOException e) {
                log.error(e.toString());
            }

            // send Event to Kafka Topic
            producer.sendEvent(new KafkaPayload(type + "_events", fileName, "size=" + odb.getData().size()));
        });
    }

    /**
     * Use Streams to create file on system
     */
    private static void writeUsingOutputStream(String data, String fn) throws IOException {
        OutputStream os = null;
        try {
            os = Files.newOutputStream(new File(fn).toPath());
            os.write(data.getBytes(), 0, data.length());
        } finally{
            assert os != null;
            os.close();
        }
    }

    private static String convertObjectToxStreamXML(Object data, String rootName) throws UnsupportedEncodingException {
        XStream xStream = new XStream(new StaxDriver());

        // Add any data classes with custom @XStreamAlias("")
        xStream.processAnnotations(H2User.class);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
        HierarchicalStreamWriter xmlWriter = new PrettyPrintWriter(writer);
        xStream.marshal(data, xmlWriter);

        return stream.toString("UTF-8")
                .replace("<singleton-set>", "")
                .replace("</singleton-set>", "")
                .replace("<list>", "<" + rootName + ">")
                .replace("</list>", "</" + rootName + ">").trim();
    }

}
