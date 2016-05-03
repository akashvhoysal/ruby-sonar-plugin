package com.godaddy.sonar.ruby.metricfu;

import com.godaddy.sonar.ruby.RubyPlugin;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.duplication.DuplicationBuilder;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.api.scan.filesystem.PathResolver;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by akash.v on 22/04/16.
 */
public class MetricfuDuplicationSensor implements Sensor {

    private static final Logger LOG                             = LoggerFactory
            .getLogger(MetricfuComplexitySensor.class);

    private MetricfuDuplicationYamlParserImpl metricfuDuplicationYamlParserImpl;
    private Settings settings;
    private FileSystem fs;

    private static final Number[]        FILES_DISTRIB_BOTTOM_LIMITS     = { 0, 5, 10, 20, 30, 60, 90 };
    private static final Number[]        FUNCTIONS_DISTRIB_BOTTOM_LIMITS = { 1, 2, 4, 6, 8, 10, 12, 20, 30 };

    private String                       reportPath                      = "tmp/metric_fu/report.yml";
    private PathResolver pathResolver;

    public MetricfuDuplicationSensor(Settings settings, FileSystem fs,
                                    PathResolver pathResolver,
                                    MetricfuDuplicationYamlParserImpl metricfuDuplicationYamlParserImpl) {
        this.settings = settings;
        this.fs = fs;
        this.metricfuDuplicationYamlParserImpl = metricfuDuplicationYamlParserImpl;
        this.pathResolver = pathResolver;
        String reportpath_prop = settings.getString(RubyPlugin.METRICFU_REPORT_PATH_PROPERTY);
        if (null != reportpath_prop) {
            this.reportPath = reportpath_prop;
        }
    }

    @Override
    public void analyse(Project project, SensorContext sensorContext) {
        LOG.info("Analysing Duplications.");
        File report = pathResolver.relativeFile(fs.baseDir(), reportPath);
        LOG.info("Calling analyse for report results: " + report.getPath());
        if (!report.isFile()) {
            LOG.warn("MetricFu report not found at {}", report);
            return;
        }

        List<InputFile> sourceFiles = Lists.newArrayList(fs.inputFiles(fs.predicates().hasLanguage("ruby")));

        try {
            metricfuDuplicationYamlParserImpl.parse(sourceFiles, report);
        } catch (IOException e) {
            LOG.error("Parsing duplications failed:");
            e.printStackTrace();
        }
    }


    @Override
    public boolean shouldExecuteOnProject(Project project) {
        return fs.hasFiles(fs.predicates().hasLanguage("ruby"));
    }
}
