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

import com.cloudbees.hudson.plugins.folder.computed.FolderComputation;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.jenkinsci.plugins.pipeline.multibranch.defaults.PipelineBranchDefaultsProjectFactory;
import org.jenkinsci.plugins.pipeline.multibranch.defaults.PipelineMultiBranchDefaultsProject;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertTrue;

/**
 * Created by dsaponenko on 09.10.16.
 */
public class PipelineMultiBranchDefaultsProjectTest {
    @Rule
    public JenkinsRule r = new JenkinsRule();

    @Test
    public void newProjectFactory() throws Exception {
        assertTrue(r.jenkins.createProject(PipelineMultiBranchDefaultsProject.class, "test").
            newProjectFactory() instanceof PipelineBranchDefaultsProjectFactory);
    }


    public static
    @NonNull
    WorkflowJob scheduleAndFindBranchProject(@NonNull WorkflowMultiBranchProject mp, @NonNull String name) throws Exception {
        mp.scheduleBuild2(0).getFuture().get();
        return findBranchProject(mp, name);
    }

    public static
    @NonNull
    WorkflowJob findBranchProject(@NonNull WorkflowMultiBranchProject mp, @NonNull String name) throws Exception {
        WorkflowJob p = mp.getItem(name);
        showIndexing(mp);
        if (p == null) {
            fail(name + " project not found");
        }
        return p;
    }

    static void showIndexing(@NonNull WorkflowMultiBranchProject mp) throws Exception {
        FolderComputation<?> indexing = mp.getIndexing();
        System.out.println("---%<--- " + indexing.getUrl());
        indexing.writeWholeLogTo(System.out);
        System.out.println("---%<--- ");
    }
}