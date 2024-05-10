package org.jenkinsci.plugins.pipeline.multibranch.defaults;

import hudson.Extension;
import hudson.model.TaskListener;
import java.io.IOException;
import javax.annotation.Nonnull;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceCriteria;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;
import org.jenkinsci.plugins.workflow.multibranch.AbstractWorkflowBranchProjectFactory;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/** Recognizes and builds by default {@code Jenkinsfile}. */
public class PipelineBranchScriptProjectFactory extends AbstractWorkflowBranchProjectFactory {
    private String pipelineScript;
    private boolean useSandbox = false;

    @DataBoundConstructor
    public PipelineBranchScriptProjectFactory() {}

    public String getPipelineScript() {
        return pipelineScript;
    }

    @DataBoundSetter
    public void setPipelineScript(String pipelineScript) {
        this.pipelineScript = pipelineScript;
    }

    /**
     * Set whether or not a Jenkinsfile should run within a Groovy sandbox.
     *
     * @param useSandbox Set true to enable Groovy sandbox or false to run in Jenkins master runtime.
     */
    @DataBoundSetter
    public void setUseSandbox(boolean useSandbox) {
        this.useSandbox = useSandbox;
    }

    /**
     * Get the current setting for whether or not to use a groovy sandbox.
     *
     * @return true if using a groovy sandbox is desired.
     */
    public boolean getUseSandbox() {
        return this.useSandbox;
    }

    @Override
    protected FlowDefinition createDefinition() {
        return new CpsFlowDefinition(pipelineScript, useSandbox);
    }

    @Override
    protected SCMSourceCriteria getSCMSourceCriteria(SCMSource source) {
        return new SCMSourceCriteria() {
            @Override
            public boolean isHead(Probe probe, TaskListener listener) throws IOException {
                return true;
            }

            @Override
            public int hashCode() {
                return getClass().hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                return getClass().isInstance(obj);
            }
        };
    }

    @Extension
    public static class DescriptorImpl extends AbstractWorkflowBranchProjectFactoryDescriptor {
        @Nonnull
        @Override
        public String getDisplayName() {
            return "by Pipeline script";
        }
    }
}
