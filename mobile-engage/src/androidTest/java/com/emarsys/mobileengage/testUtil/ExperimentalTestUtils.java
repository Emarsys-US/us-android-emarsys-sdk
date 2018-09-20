package com.emarsys.mobileengage.testUtil;

import com.emarsys.core.experimental.ExperimentalFeatures;
import com.emarsys.testUtil.ReflectionTestUtils;

public class ExperimentalTestUtils {

    private ExperimentalTestUtils() {
    }

    public static void resetExperimentalFeatures() {
        ReflectionTestUtils.invokeStaticMethod(ExperimentalFeatures.class, "reset");
    }
}
