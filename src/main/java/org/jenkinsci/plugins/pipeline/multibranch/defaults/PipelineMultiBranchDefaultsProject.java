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

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.ItemGroup;
import hudson.model.TopLevelItem;
import jenkins.branch.BranchProjectFactory;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;

/**
 * Representation of a set of workflows keyed off of source branches.
 */
@SuppressWarnings({"unchecked", "rawtypes"}) // core’s fault
public class PipelineMultiBranchDefaultsProject extends WorkflowMultiBranchProject {

    public PipelineMultiBranchDefaultsProject(ItemGroup parent, String name) {
        super(parent, name);
    }

    @NonNull
    protected BranchProjectFactory<WorkflowJob, WorkflowRun> newProjectFactory() {
        return new PipelineBranchDefaultsProjectFactory();
    }

    @Extension
    public static class DescriptorImpl extends WorkflowMultiBranchProject.DescriptorImpl {

        @NonNull
        @Override
        public String getDisplayName() {
            return DefaultsMessages.PipelineMultiBranchDefaultsProject_DisplayName();
        }

        public String getDescription() {
            return DefaultsMessages.PipelineMultiBranchDefaultsProject_Description();
        }

        public String getIconFilePathPattern() {
            return "plugin/pipeline-multibranch-defaults/images/:size/pipelinemultibranchdefaultsproject.png";
        }

        @Override
        public TopLevelItem newInstance(ItemGroup parent, String name) {
            return new PipelineMultiBranchDefaultsProject(parent, name);
        }
    }
}
