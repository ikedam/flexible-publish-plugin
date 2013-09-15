/*
 * The MIT License
 * 
 * Copyright (c) 2013 IKEDA Yasuyuki
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

package org.jenkins_ci.plugins.flexible_publish.run_condition;

import hudson.Extension;
import hudson.matrix.MatrixRun;
import hudson.matrix.MatrixBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;

import org.apache.commons.lang.StringUtils;
import org.jenkins_ci.plugins.run_condition.RunCondition;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 *
 */
public class MatrixCombinationBuildedCondition extends RunCondition {
    private String combinationFilter;
    /**
     * @return the combinationFilter
     */
    public String getCombinationFilter() {
        return combinationFilter;
    }
    
    @DataBoundConstructor
    public MatrixCombinationBuildedCondition(String combinationFilter) {
        this.combinationFilter = StringUtils.trim(combinationFilter);
    }
    
    protected boolean isCombinationBuilded(AbstractBuild<?, ?> build, BuildListener listener) {
        if (!(build instanceof MatrixBuild)) {
            listener.getLogger().println(String.format(
                    "%s is applicable only for multi-configuration projects",
                    getDescriptor().getDisplayName()
            ));
            return false;
        }
        MatrixBuild matrixBuild = (MatrixBuild)build;
        for (MatrixRun run: matrixBuild.getExactRuns()) {
            if(run.getParent().getCombination().evalGroovyExpression(
                    matrixBuild.getParent().getAxes(),
                    getCombinationFilter()
            )) {
                listener.getLogger().println(String.format(
                        "%s is satisfied with %s",
                        getCombinationFilter(),
                        run.getFullDisplayName()
                ));
                return true;
            }
        }
        return false;
    }
    
    /**
     * @param build
     * @param listener
     * @return
     * @throws Exception
     * @see org.jenkins_ci.plugins.run_condition.RunCondition#runPrebuild(hudson.model.AbstractBuild, hudson.model.BuildListener)
     */
    @Override
    public boolean runPrebuild(AbstractBuild<?, ?> build, BuildListener listener) throws Exception {
        return isCombinationBuilded(build, listener);
    }
    
    /**
     * @param build
     * @param listener
     * @return
     * @throws Exception
     * @see org.jenkins_ci.plugins.run_condition.RunCondition#runPerform(hudson.model.AbstractBuild, hudson.model.BuildListener)
     */
    @Override
    public boolean runPerform(AbstractBuild<?, ?> build, BuildListener listener) throws Exception {
        return isCombinationBuilded(build, listener);
    }
    
    @Extension
    public static class DescriptorImpl extends RunConditionDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.MatrixCombinationBuildedCondition_DisplayName();
        }
    }
}
