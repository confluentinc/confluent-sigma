package io.confluent.sigmarules.fieldmapping;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import io.confluent.sigmarules.models.SigmaFields;

public class FieldMapper {
    private SigmaFields sigmaFields = new SigmaFields();

    public FieldMapper(String filename) throws IOException {
        this.loadSigmaFields(filename);
    }

    public void loadSigmaFields(String filename) throws IOException {
        String fields = Files.readString(Path.of(filename));

        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        this.sigmaFields = yamlMapper.readValue(fields, SigmaFields.class);
    }

    public SigmaFields getSigmaFields() {
        return this.sigmaFields;
    }

    public static void main(String[] args) throws IOException {
        String fieldFile = "/Users/mpeacock/Development/KafkaSigma/sigma/tools/config/splunk-zeek.yml";

        FieldMapper fieldMapper = new FieldMapper(fieldFile);
        List<String> useragent = fieldMapper.getSigmaFields().getSigmaField("c-useragent");
        System.out.println("c-useragent: " + useragent.toString());

        List<String> dest = fieldMapper.getSigmaFields().getSigmaField("dest_domain");
        System.out.println("dest_domain: " + dest.toString());

    }
}
