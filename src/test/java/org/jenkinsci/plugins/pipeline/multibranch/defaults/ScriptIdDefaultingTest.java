package org.jenkinsci.plugins.pipeline.multibranch.defaults;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Covers the scriptId null-or-empty defaulting in both project factories,
 * previously implemented with Commons Lang 2 StringUtils.isEmpty.
 */
public class ScriptIdDefaultingTest {

    @Test
    public void branchFactoryDefaultsNullScriptId() {
        PipelineBranchDefaultsProjectFactory factory = new PipelineBranchDefaultsProjectFactory();
        factory.setScriptId(null);
        assertEquals(PipelineBranchDefaultsProjectFactory.SCRIPT, factory.getScriptId());
    }

    @Test
    public void branchFactoryDefaultsEmptyScriptId() {
        PipelineBranchDefaultsProjectFactory factory = new PipelineBranchDefaultsProjectFactory();
        factory.setScriptId("");
        assertEquals(PipelineBranchDefaultsProjectFactory.SCRIPT, factory.getScriptId());
    }

    @Test
    public void branchFactoryKeepsCustomScriptId() {
        PipelineBranchDefaultsProjectFactory factory = new PipelineBranchDefaultsProjectFactory();
        factory.setScriptId("my-jenkinsfile");
        assertEquals("my-jenkinsfile", factory.getScriptId());
    }

    @Test
    public void multiBranchFactoryDefaultsNullScriptId() {
        PipelineMultiBranchDefaultsProjectFactory factory = new PipelineMultiBranchDefaultsProjectFactory();
        factory.setScriptId(null);
        assertEquals(PipelineMultiBranchDefaultsProjectFactory.SCRIPT, factory.getScriptId());
    }

    @Test
    public void multiBranchFactoryDefaultsEmptyScriptId() {
        PipelineMultiBranchDefaultsProjectFactory factory = new PipelineMultiBranchDefaultsProjectFactory();
        factory.setScriptId("");
        assertEquals(PipelineMultiBranchDefaultsProjectFactory.SCRIPT, factory.getScriptId());
    }

    @Test
    public void multiBranchFactoryKeepsCustomScriptId() {
        PipelineMultiBranchDefaultsProjectFactory factory = new PipelineMultiBranchDefaultsProjectFactory();
        factory.setScriptId("my-jenkinsfile");
        assertEquals("my-jenkinsfile", factory.getScriptId());
    }
}
