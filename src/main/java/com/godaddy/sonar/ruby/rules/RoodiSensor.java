package com.godaddy.sonar.ruby.rules;

import com.godaddy.sonar.ruby.RubyPlugin;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.scan.filesystem.PathResolver;
import org.sonar.batch.protocol.input.Metric;

import java.io.File;
import java.io.FileNotFoundException;

import java.util.List;

/**
 * Created by akash.v on 27/04/16.
 */
public class RoodiSensor implements Sensor {

    private static final Logger LOG                             = LoggerFactory
            .getLogger(RoodiSensor.class);
    private MetricfuRoodiYamlParser metricfuRoodiYamlParser;
    private ActiveRules activeRules;
    private ResourcePerspectives resourcePerspectives;
    private Settings settings;
    private FileSystem fs;
    private String                       reportPath                      = "tmp/metric_fu/report.yml";
    private PathResolver pathResolver;
    private RoodiRuleParser roodiRuleParser;

    public RoodiSensor(Settings settings, FileSystem fs, ActiveRules activeRules, ResourcePerspectives resourcePerspectives, PathResolver pathResolver, MetricfuRoodiYamlParser metricfuRoodiYamlParser) {

        this.settings = settings;
        this.fs = fs;
        this.activeRules = activeRules;
        this.resourcePerspectives = resourcePerspectives;
        this.pathResolver = pathResolver;
        String reportpath_prop = settings.getString(RubyPlugin.METRICFU_REPORT_PATH_PROPERTY);
        this.metricfuRoodiYamlParser = metricfuRoodiYamlParser;
        if (null != reportpath_prop) {
            this.reportPath = reportpath_prop;
        }
        roodiRuleParser = new RoodiRuleParser();
        roodiRuleParser.parse();
    }


    @Override
    public void analyse(Project project, SensorContext sensorContext) {
        File report = pathResolver.relativeFile(fs.baseDir(), reportPath);
        LOG.info("Calling analyse for report results: " + report.getPath());
        if (!report.isFile()) {
            LOG.warn("MetricFu report not found at {}", report);
            return;
        }

        List<InputFile> sourceFiles = Lists.newArrayList(fs.inputFiles(fs.predicates().hasLanguage("ruby")));

        for (InputFile inputFile : sourceFiles)
        {
            LOG.debug("analyzing functions for Issues in the file: " + inputFile.file().getName());
            try
            {
                analyzeFile(inputFile, sensorContext, report);
            } catch (Exception e)
            {
                LOG.error("Can not analyze the file " + inputFile.absolutePath() + " for issues", e);
            }
        }
    }

    private void analyzeFile(InputFile inputFile, SensorContext sensorContext, File report) throws FileNotFoundException {
        List<RoodiProblem > issues= metricfuRoodiYamlParser.parse(inputFile.relativePath(), report);
        for (RoodiProblem roodiProblem: issues) {
            if (roodiRuleParser.getKey(roodiProblem.problem) != null) {
            RuleKey ruleKey = RuleKey.of(RubyRuleRepository.REPOSITORY_KEY, roodiRuleParser.getKey(roodiProblem.problem));
            LOG.info("Rule Key: {}", ruleKey);
                Issuable issuable = resourcePerspectives.as(Issuable.class, inputFile);
                if (issuable != null) {
                    issuable.addIssue(issuable.newIssueBuilder()
                            .ruleKey(ruleKey)
                            .line(roodiProblem.getLine())
                            .message(roodiProblem.problem)
                            .build());
                    LOG.info("adding issue.");
                }
            } else {
                LOG.warn("Ruby rule '{}' is unknown in Sonar", roodiProblem.problem);
            }
        }
    }

    @Override
    public boolean shouldExecuteOnProject(Project project) {
        return fs.hasFiles(fs.predicates().hasLanguage("ruby"));
    }
}