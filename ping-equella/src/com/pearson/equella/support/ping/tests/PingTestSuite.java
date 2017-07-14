package com.pearson.equella.support.ping.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * When running these tests, you should receive the following emails:
 * MULTI TO LIST:
 * > testEmailSettingsE2E
 * > attachment-testRun1FilterByCollection
 * SINGLE TO LIST:
 * > testEmailSettingsE2EonlyChangedAtts
 * > testCompareMissingAttachmentsCompareShouldBeDifferentFirst
 * > testCompareMissingAttachmentsCompareShouldBeDifferentSecond
 * > testCompareMissingAttachmentsCompareShouldBeEqualWithNormalEmail
 * > testEmailSettingsFailFastWithOnlyMissingAttsEmailSpecified
 * > testEmailSettingsFailFastWithNormalEmailSpecified
 * 
 * Pass in the following jvm args:
 * -DJUNIT_EMAIL_USERNAME= 
 * -DJUNIT_EMAIL_PASSWORD= 
 * -DJUNIT_EMAIL_SINGLE_TO_LIST=
 * -DJUNIT_EMAIL_MULTI_TO_LIST=
 * 
 *
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({ CompareTests.class, ConfigTests.class,
	DirectPingTests.class, EmailTests.class })
public class PingTestSuite {
	//AttachmentPingTests.class, 
}
