package com.godaddy.sonar.ruby.metricfu;

import org.sonar.api.BatchExtension;
import org.sonar.api.batch.fs.InputFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by akash.v on 25/04/16.
 */
public interface MetricfuDuplicationYamlParser extends BatchExtension {

    public void parse(List<InputFile> inputFiles, File resultsFile) throws IOException;

}
