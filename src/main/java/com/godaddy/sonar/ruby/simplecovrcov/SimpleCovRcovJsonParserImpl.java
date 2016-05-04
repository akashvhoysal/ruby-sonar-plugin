package com.godaddy.sonar.ruby.simplecovrcov;

import java.io.File;
import java.io.IOException;

import java.util.Collection;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.measures.CoverageMeasuresBuilder;

import com.google.common.collect.Maps;
import org.sonar.api.measures.Measure;
import org.sonar.core.measure.db.MeasureDto;
import org.sonar.squid.api.SourceCode;

public class SimpleCovRcovJsonParserImpl implements SimpleCovRcovJsonParser
{
    private static final Logger LOG = LoggerFactory.getLogger(SimpleCovRcovJsonParserImpl.class);

    public Map<String, CoverageMeasuresBuilder> parse(File file) throws IOException
    {
        Map<String, CoverageMeasuresBuilder> coveredFiles = Maps.newHashMap();

        File fileToFindCoverage = file;

        String fileString = FileUtils.readFileToString(fileToFindCoverage, "UTF-8");
        JSONObject resultJsonObject = (JSONObject) JSONValue.parse(fileString);
        JSONObject coverageJsonObj = (JSONObject) ((JSONObject) resultJsonObject.get(resultJsonObject.keySet().iterator().next())).get("coverage");

        for (int j = 0; j < coverageJsonObj.keySet().size(); j++)
        {
            CoverageMeasuresBuilder fileCoverage = CoverageMeasuresBuilder.create();

            String filePath = coverageJsonObj.keySet().toArray()[j].toString();
        	LOG.debug("filePath " + filePath);

            JSONArray coverageArray = (JSONArray) coverageJsonObj.get(coverageJsonObj.keySet().toArray()[j]);

            // for each line in the coverage array
            for (int i = 0; i < coverageArray.size(); i++)
            {
                Long line = (Long) coverageArray.toArray()[i];
                Integer intLine = 0;
                int lineNumber = i + 1;
                if (line != null)
                {
                    intLine = line.intValue();
                    fileCoverage.setHits(lineNumber, intLine);
                }
            }
            LOG.info("FILE COVERAGE = " + fileCoverage.getCoveredLines());
            coveredFiles.put(filePath, fileCoverage);
        }
        return coveredFiles;
    }
}
