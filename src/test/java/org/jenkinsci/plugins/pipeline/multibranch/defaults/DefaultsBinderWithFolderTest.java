/*
 * The MIT License
 *
 * Copyright (c) 2016 Saponenko Denis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jenkinsci.plugins.pipeline.multibranch.defaults;

import com.cloudbees.hudson.plugins.folder.Folder;
import jenkins.branch.BranchProperty;
import jenkins.branch.BranchSource;
import jenkins.branch.DefaultBranchPropertyStrategy;
import jenkins.plugins.git.GitSCMSource;
import jenkins.plugins.git.GitSampleRepoRule;
import org.jenkinsci.lib.configprovider.model.Config;
import org.jenkinsci.plugins.configfiles.ConfigFileStore;
import org.jenkinsci.plugins.configfiles.GlobalConfigFiles;
import org.jenkinsci.plugins.configfiles.folder.FolderConfigFileAction;
import org.jenkinsci.plugins.configfiles.folder.FolderConfigFileProperty;
import org.jenkinsci.plugins.configfiles.groovy.GroovyScript;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.jenkinsci.plugins.workflow.test.steps.SemaphoreStep;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.WithPlugin;

import static net.sf.ezmorph.test.ArrayAssertions.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DefaultsBinderWithFolderTest {
    @ClassRule
    public static BuildWatcher buildWatcher = new BuildWatcher();
    @Rule
    public JenkinsRule r = new JenkinsRule();
    @Rule
    public GitSampleRepoRule sampleGitRepo = new GitSampleRepoRule();

    @Test
    public void testDefaultJenkinsFileLoadFromFolderStore() throws Exception {
        Folder folder = r.jenkins.createProject(Folder.class, "folder");
        FolderConfigFileAction action = folder.getAction(FolderConfigFileAction.class);
        action.getGroupedConfigs(); // Initialize Folder Config File store

        ConfigFileStore folderStore = folder.getProperties().get(FolderConfigFileProperty.class);
        Config folderConfig = new GroovyScript("Jenkinsfile", "Jenkinsfile", "",
            "semaphore 'wait'; node {checkout scm; echo readFile('file')}");
        folderStore.save(folderConfig);

        GlobalConfigFiles globalConfigFiles = r.jenkins.getExtensionList(GlobalConfigFiles.class).get(GlobalConfigFiles.class);
        ConfigFileStore store = globalConfigFiles.get();
        Config storeConfig = new GroovyScript("Jenkinsfile", "Jenkinsfile", "",
            "semaphore 'wait'; node {checkout scm}");
        store.save(storeConfig);

        sampleGitRepo.init();
        sampleGitRepo.write("file", "initial content");
        sampleGitRepo.git("commit", "--all", "--message=flow");
        WorkflowMultiBranchProject mp = folder.createProject(PipelineMultiBranchDefaultsProject.class, "p");
        mp.getSourcesList().add(new BranchSource(new GitSCMSource(null, sampleGitRepo.toString(), "", "*", "", false),
            new DefaultBranchPropertyStrategy(new BranchProperty[0])));
        WorkflowJob p = PipelineMultiBranchDefaultsProjectTest.scheduleAndFindBranchProject(mp, "master");
        SemaphoreStep.waitForStart("wait/1", null);
        WorkflowRun b1 = p.getLastBuild();
        assertNotNull(b1);
        assertEquals(1, b1.getNumber());
        SemaphoreStep.success("wait/1", null);
        r.assertLogContains("initial content", r.waitForCompletion(b1));
    }

    @Test
    public void testDefaultJenkinsFileLoadFromGlobalStore() throws Exception {
        Folder folder = r.jenkins.createProject(Folder.class, "folder");
        FolderConfigFileAction action = folder.getAction(FolderConfigFileAction.class);
        action.getGroupedConfigs(); // Initialize Folder Config File store

        GlobalConfigFiles globalConfigFiles = r.jenkins.getExtensionList(GlobalConfigFiles.class).get(GlobalConfigFiles.class);
        ConfigFileStore store = globalConfigFiles.get();

        Config config = new GroovyScript("Jenkinsfile", "Jenkinsfile", "",
            "semaphore 'wait'; node {checkout scm; echo readFile('file')}");
        store.save(config);

        sampleGitRepo.init();
        sampleGitRepo.write("file", "initial content");
        sampleGitRepo.git("commit", "--all", "--message=flow");
        WorkflowMultiBranchProject mp = folder.createProject(PipelineMultiBranchDefaultsProject.class, "p");
        mp.getSourcesList().add(new BranchSource(new GitSCMSource(null, sampleGitRepo.toString(), "", "*", "", false),
            new DefaultBranchPropertyStrategy(new BranchProperty[0])));
        WorkflowJob p = PipelineMultiBranchDefaultsProjectTest.scheduleAndFindBranchProject(mp, "master");
        SemaphoreStep.waitForStart("wait/1", null);
        WorkflowRun b1 = p.getLastBuild();
        assertNotNull(b1);
        assertEquals(1, b1.getNumber());
        SemaphoreStep.success("wait/1", null);
        r.assertLogContains("initial content", r.waitForCompletion(b1));
    }

}
