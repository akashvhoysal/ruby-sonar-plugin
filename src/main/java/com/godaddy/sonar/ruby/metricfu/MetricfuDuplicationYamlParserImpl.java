package com.godaddy.sonar.ruby.metricfu;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by akash.v on 22/04/16.
 */
public class MetricfuDuplicationYamlParserImpl implements MetricfuDuplicationYamlParser {

    private static final Logger LOG = LoggerFactory
            .getLogger(MetricfuComplexityYamlParser.class);

    @SuppressWarnings("unchecked")
    @Override
    public void parse(List<InputFile> inputFiles, File resultsFile) throws IOException
    {
        InputStream resultsStream = new FileInputStream(resultsFile);

        LOG.debug("MetricfuDuplicationYamlParserImpl: Start start parse of metrics_fu YAML");

        Yaml yaml = new Yaml();
        Map<String, Map<String, ArrayList>> metricfuResult = new HashMap();
        try {
            metricfuResult = (Map<String, Map<String, ArrayList>>) yaml.load(resultsStream);

            ArrayList<Map<String, Object>> flayResult =  metricfuResult.get(":flay").get(":matches");

            analyzeFlay(inputFiles, flayResult);
            
        } catch (Exception e) {
            LOG.error(Throwables.getStackTraceAsString(e));
            throw new IOException("Failure parsing YAML results", e);
        }

    }

    private void analyzeFlay(List<InputFile> inputFiles, ArrayList<Map<String, Object>> flayResult) {
        Map<String, InputFile> indexedInputFiles = index(inputFiles);
        for(Map<String, Object> duplicate : flayResult){
            if(((String)duplicate.get(":reason")).contains("IDENTICAL")){
                ArrayList<Map<String, String>> matches = (ArrayList<Map<String, String>>) duplicate.get(":matches");
//                DuplicationBuilder duplicationBuilder = new DefaultDuplicationBuilder(indexedInputFiles.get(matches.get(0).get(":name")));
                LOG.info("Identical Code Found: {}", matches    );
                for(Map<String, String> match : matches ){
//                    duplicationBuilder.isDuplicatedBy(indexedInputFiles.get(match.get(":name")), Integer.parseInt(match.get(":line")), 1);

                }
            }
        }

    }

    private Map<String, InputFile> index(List<InputFile> inputFiles) {
        Map<String, InputFile> indexedFiles= Maps.newConcurrentMap();

        for(InputFile inputFile : inputFiles){
            indexedFiles.put(inputFile.relativePath(), inputFile);
        }

        return indexedFiles;
    }
}
