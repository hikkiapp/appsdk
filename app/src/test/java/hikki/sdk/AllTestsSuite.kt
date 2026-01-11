package hikki.sdk

import hikki.sdk.utils.ContextUtilsTest
import hikki.sdk.utils.EncryptorTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    SettingsUtilsTest::class,
    ConfigPayloadTest::class,
    EncryptorTest::class,
    ContextUtilsTest::class,
)
class AllTestsSuite