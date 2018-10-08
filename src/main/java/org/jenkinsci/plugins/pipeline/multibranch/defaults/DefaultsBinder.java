/*
 * The MIT License
 *
 * Copyright (c) 2016 Saponenko Denis
 * Copyright (c) 2018 Sam Gleske - https://github.com/samrocketman
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

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Descriptor;
import hudson.model.DescriptorVisibilityFilter;
import hudson.model.Queue;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import org.jenkinsci.lib.configprovider.model.Config;
import org.jenkinsci.plugins.configfiles.ConfigFiles;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;
import org.jenkinsci.plugins.workflow.flow.FlowDefinitionDescriptor;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.flow.FlowExecutionOwner;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.util.List;

/**
 * Checks out the local default version of {@link WorkflowBranchProjectFactory#scriptPath} in order if exist:
 * 1. From module checkout
 * 2. From task workspace directory
 * 3. From the Job's ConfigFileProvider
 * 4. From global jenkins managed files
 */
class DefaultsBinder extends FlowDefinition {

    private String scriptId;
    private boolean useSandbox;

    public DefaultsBinder(String scriptId, boolean useSandbox) {
        this.scriptId = scriptId;
        this.useSandbox = useSandbox;
    }

    @Override
    public FlowExecution create(FlowExecutionOwner handle, TaskListener listener, List<? extends Action> actions) throws Exception {
        Jenkins jenkins = Jenkins.getInstance();
        if (jenkins == null) {
            throw new IllegalStateException("inappropriate context");
        }
        Queue.Executable exec = handle.getExecutable();
        if (!(exec instanceof WorkflowRun)) {
            throw new IllegalStateException("inappropriate context");
        }
        Config config = ConfigFiles.getByIdOrNull((Run<?, ?>) exec, scriptId);
        if (config != null) {
            if (useSandbox) {
                listener.getLogger().println("Running with default Jenkinsfile ID: " + scriptId + "; within Groovy sandbox.");
            } else {
                listener.getLogger().println("Running with default Jenkinsfile ID: " + scriptId);
            }
            return new CpsFlowDefinition(config.content, useSandbox).create(handle, listener, actions);
        }
        throw new IllegalArgumentException("Default " + PipelineBranchDefaultsProjectFactory.SCRIPT + " not found. Check configuration.");
    }

    @Extension
    public static class DescriptorImpl extends FlowDefinitionDescriptor {

        @Override
        public String getDisplayName() {
            return "Pipeline script from default " + PipelineBranchDefaultsProjectFactory.SCRIPT;
        }

    }

    /**
     * Want to display this in the r/o configuration for a branch project, but not offer it on standalone jobs or in any other context.
     */
    @Extension
    public static class HideMeElsewhere extends DescriptorVisibilityFilter {

        @Override
        public boolean filter(Object context, Descriptor descriptor) {
            if (descriptor instanceof DescriptorImpl) {
                return context instanceof WorkflowJob && ((WorkflowJob) context).getParent() instanceof PipelineMultiBranchDefaultsProject;
            }
            return true;
        }

    }
}
